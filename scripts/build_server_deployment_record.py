"""Prepare immutable deployment records from explicit local before/after snapshots.
No network, deployment, server commands, world backups or marker writes.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import zipfile
from datetime import datetime, timezone

ROOTS = {'config', 'defaultconfigs', 'datapacks', 'kubejs', 'mods', 'libraries', 'versions'}
ROOT_FILES = {'server.jar', 'minecraft_server.1.21.1.jar', 'run.bat', 'run.sh',
              'LaunchServer.bat', 'LaunchServer.sh', 'START_SERVER.bat', 'START_SERVER.sh', 'user_jvm_args.txt'}
BLOCKED = re.compile(r'(?i)^(world.*|saves?|dimensions|DIM-?\d+|.*schematic.*|.*blueprint.*|.*lod.*|distant.*horizon.*|backups?|logs|crash-reports|\.git|\.local|local)$')


def linked(path):
    return path.is_symlink() or (hasattr(path, 'is_junction') and path.is_junction())


def safe_path(root, rel):
    """Reject unsafe paths and ancestors before reading file contents."""
    if '\\' in rel or ':' in rel:
        raise ValueError(f'Not a portable relative path: {rel}')
    if linked(root):
        raise ValueError("Snapshot root must not be a link")
    parts = PurePosixPath(rel).parts
    if not parts or rel != PurePosixPath(rel).as_posix() or rel.startswith('/') or any(x in ('.', '..') for x in parts):
        raise ValueError(f'Unsafe path: {rel}')
    if parts[0] not in ROOTS and rel not in ROOT_FILES:
        raise ValueError(f'Not an eligible pack path: {rel}')
    if any(BLOCKED.fullmatch(x) for x in parts[:-1]):
        raise ValueError(f'Protected storage: {rel}')
    if parts[-1].lower().endswith(('.db', '.sqlite', '.sqlite3', '.mca', '.mcr')):
        raise ValueError(f'Generated save/storage file: {rel}')
    # Also reject the configured custom world directory, including nested names.
    prop = root / 'server.properties'
    if prop.is_file() and not linked(prop):
        for line in prop.read_text(encoding='utf-8-sig').splitlines():
            if line.startswith('level-name='):
                world = line.split('=', 1)[1].strip().replace('\\', '/').strip('/')
                if world and (rel.casefold() == world.casefold() or rel.casefold().startswith(world.casefold() + '/')):
                    raise ValueError(f'Configured world path: {rel}')
    current = root
    if linked(root):
        raise ValueError('Snapshot root must not be a link')
    for part in parts:
        current = current / part
        if linked(current):
            raise ValueError(f'Linked path: {rel}')
    return current


def eligible_files(root):
    """Only traverse known pack roots; prune storage before os.scandir."""
    result = set()
    def walk(rel):
        try:
            p = safe_path(root, rel)
        except ValueError:
            return
        if p.is_dir():
            if BLOCKED.fullmatch(p.name):
                return
            # Validate the directory as an ancestor before inspecting children.
            try:
                safe_path(root, rel + '/__path_check__')
            except ValueError:
                return
            with os.scandir(p) as entries:
                for entry in entries:
                    walk(rel + '/' + entry.name)
        elif p.is_file():
            result.add(rel)
    for rel in sorted(ROOTS | ROOT_FILES):
        walk(rel)
    return result


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else None


def prepare(before, candidate, out, version, client_version, commit, paths,
            notes='', full=False, create_zip=False, mismatch_approved=False,
            baseline_verified=False, baseline_root=None, review_paths=()):
    if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9._-]*', version):
        raise ValueError('Invalid version')
    if not re.fullmatch(r'[0-9a-fA-F]{7,40}', commit):
        raise ValueError('Invalid commit')
    if version != client_version and not mismatch_approved:
        raise ValueError('Client/server versions differ: obtain explicit mismatch approval first')
    if out.exists():
        raise ValueError('Deployment attempt already exists; use a new attempt ID')
    before, candidate = Path(before).absolute(), Path(candidate).absolute()
    if before == candidate or before.is_relative_to(candidate) or candidate.is_relative_to(before):
        raise ValueError('Use separate, non-nested before and candidate snapshots')
    for root in (before, candidate):
        if not root.is_dir() or linked(root):
            raise ValueError('Snapshot roots must be existing directories, not links')
        if out.absolute().is_relative_to(root):
            raise ValueError('Records must be outside the snapshot roots')
    selected = sorted(eligible_files(before) | eligible_files(candidate)) if full else sorted(set(paths))
    if not selected and not full:
        raise ValueError('Hotfix mode requires explicit paths')
    # Complete preflight before creating any record.
    changes, manifest = [], []
    for rel in selected:
        a, b = safe_path(before, rel), safe_path(candidate, rel)
        for p in (a, b):
            if p.exists() and not p.is_file():
                raise ValueError(f'Expected file, got directory: {rel}')
        ah, bh = digest(a), digest(b)
        if ah is None and bh is None:
            raise ValueError(f'File absent in both snapshots: {rel}')
        if bh:
            manifest.append({'path': rel, 'sha256': bh, 'size': b.stat().st_size})
        if ah != bh:
            changes.append({'path': rel, 'action': 'add' if ah is None else 'delete' if bh is None else 'modify',
                            'before_sha256': ah, 'after_sha256': bh})
    reviews = []
    for rel in sorted(set(review_paths)):
        if not rel.startswith(('config/', 'defaultconfigs/')):
            raise ValueError('Review candidates must be quest/config paths')
        if baseline_root is None:
            raise ValueError('Remote review requires a published baseline snapshot')
        a, b = safe_path(Path(baseline_root), rel), safe_path(before, rel)
        ah, bh = digest(a), digest(b)
        if ah != bh:
            reviews.append({'path': rel, 'published_sha256': ah, 'chunkserve_sha256': bh,
                            'decision': 'pending-review', 'difference_summary': 'Required: review locally without exposing secrets',
                            'likely_effect': 'Requires review',
                            'choices': ['promote-to-pack', 'keep-server-specific', 'discard-with-approval']})
    out.mkdir(parents=True)
    (out / 'ROLLBACK').mkdir()
    for change in changes:
        if change['before_sha256']:
            src = safe_path(before, change['path'])
            dest = out / 'ROLLBACK' / change['path']
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(src, dest)
            if digest(dest) != change['before_sha256']:
                raise RuntimeError('Rollback copy mismatch')
    def write_json(name, value):
        (out / name).write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
    write_json('CHANGES.json', {'schema': 1, 'version': version, 'commit': commit, 'changes': changes})
    write_json('SERVER_FILE_MANIFEST.json', {'scope': 'full-eligible-pack' if full else 'targeted', 'files': manifest})
    write_json('REMOTE_CHANGE_CATALOGUE.json', {'status': 'candidates-pending-review' if reviews else 'no-differences-in-supplied-paths' if review_paths else 'not-inspected', 'candidates': reviews})
    write_json('DEPLOYMENT_STATUS.json', {'status': 'prepared-not-deployed', 'created_at': datetime.now(timezone.utc).isoformat(),
        'targets': {t: {'status': 'not-verified'} for t in ('client', 'twin', 'chunkserve')},
        'activation': 'not-performed', 'temporary_mismatch_approved': mismatch_approved})
    (out / 'COMPATIBILITY.toml').write_text(
        f'status = "prepared-unverified"\ntarget_pack_version = "{version}"\nclient_pack_version = {json.dumps(client_version)}\n'
        f'source_commit = "{commit}"\nbaseline_verified = {str(baseline_verified).lower()}\n'
        'applicable_payload_verified = false\nintentional_overrides_reviewed = false\n', encoding='utf-8')
    (out / 'DEPLOYMENT.md').write_text(
        f'# Deployment {version}\n\n{notes}\n\n'
        'This record is prepared, not proof of deployment. CHANGES.json lists only approved candidate paths; confirm remote drift before each write. '
        'Preserved originals are in ROLLBACK; added files have no original. Never restore a directory wholesale.\n\n'
        'After transfer, verify each affected target hash, record client/twin/Chunkserve results separately and confirm activation timing. '
        'Record sanitized config/quest summaries and likely effects in REMOTE_CHANGE_CATALOGUE.json before deciding their disposition. '
        'Advance compatibility markers only after applicable payload and intentional overrides are verified; a targeted update additionally needs a verified baseline. '
        'An uncertain baseline stays unverified. World backups belong outside this record.\n\n'
        'At completion, update deployment status/evidence, regenerate SHA256SUMS.txt, and treat the record as immutable. '
        'Retries and corrections use new attempt IDs. No reload, restart or world operation is performed by this tool.\n', encoding='utf-8')
    if create_zip:
        with zipfile.ZipFile(out / f'Server-Update-{version}.zip', 'w', zipfile.ZIP_DEFLATED) as z:
            for change in changes:
                if change['after_sha256']:
                    z.write(safe_path(candidate, change['path']), change['path'])
        deletions = [c['path'] for c in changes if c['action'] == 'delete']
        (out / 'DELETE_THESE_FILES.txt').write_text('\n'.join(deletions) + '\n' if deletions else '# No deletions\n', encoding='utf-8')
    checksums(out)
    return changes


def checksums(out):
    lines = [f'{digest(p)}  {p.relative_to(out).as_posix()}' for p in sorted(out.rglob('*'))
             if p.is_file() and p.name != 'SHA256SUMS.txt']
    (out / 'SHA256SUMS.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')


def main():
    p = argparse.ArgumentParser(description=__doc__)
    for name in ('before-root', 'candidate-root', 'output', 'version', 'client-version', 'commit', 'changelog'):
        p.add_argument('--' + name, required=True)
    p.add_argument('--path', action='append', default=[])
    p.add_argument('--review-path', action='append', default=[])
    p.add_argument('--baseline-root', type=Path)
    for name in ('full', 'create-zip', 'mismatch-approved', 'baseline-verified'):
        p.add_argument('--' + name, action='store_true')
    a = p.parse_args()
    changes = prepare(Path(a.before_root), Path(a.candidate_root), Path(a.output), a.version,
        a.client_version, a.commit, a.path, Path(a.changelog).read_text(encoding='utf-8-sig'),
        a.full, a.create_zip, a.mismatch_approved, a.baseline_verified, a.baseline_root, a.review_path)
    print(f'Prepared record: {a.output}; {len(changes)} changes; ZIP={a.create_zip}; no targets modified')


if __name__ == '__main__':
    main()