"""Prepare a shader-only release candidate against a pinned GitHub source archive.

Python 3.11+. Does not modify the live instance, publish, or deploy.
The output must be a NEW directory outside the instance. Archive entries are
screened before extraction; live input copying is restricted to named files.
"""
from pathlib import Path, PurePosixPath
import argparse
import difflib
import hashlib
import json
import os
import re
import shutil
import stat
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
BASE = '72526d32176ea3bcae70d2960ad215cab6f39821'
REPO = 'TheFafalStudios/Apocalypse-Industries'
SHADER = 'shaderpacks/ApocalypseAtmosphere_NetherDH_v1.2.zip'
ADDON = 'mods/apocalypse-submarine-lava-visuals-1.1.2.jar'
COPY = [SHADER, SHADER + '.txt', ADDON,
        'mods/create_submarine-3.4.4.pw.toml',
        'mods/create-aeronautics-bundled-1.21.1-1.3.2.pw.toml',
        'mods/DistantHorizons-3.3.3-1.21.1-fabric-neoforge.pw.toml',
        'mods/aeronautics-iris-fix.pw.toml']
REMOVE = [
    'shaderpacks/ComplementaryUnbound_r5.8.1_APOCALYPSE_ATMOSPHERE_v1.1_PARSEFIX_TESTED.zip',
    'mods/EuphoriaPatcher-1.9.3-r5.8.1-neoforge.jar',
    'mods/create_submarine-2.2.4.pw.toml',
    'mods/create-aeronautics-bundled-1.21.1-1.3.0.pw.toml',
    'mods/DistantHorizons-3.2.0-b-1.21.1-fabric-neoforge.pw.toml']
PROTECTED = {'saves', 'world', 'worlds', 'dimensions', 'schematics',
             'sable-schematics', 'blueprints', 'backups',
             'distant_horizons_server_data', '.git', '.local', 'logs',
             'crash-reports', '__pycache__'}

def sha(data):
    return hashlib.sha256(data).hexdigest()

def blocked(rel):
    p = PurePosixPath(rel)
    low = rel.lower()
    return (p.is_absolute() or '..' in p.parts or '\\' in rel or ':' in rel
            or any(x.lower() in PROTECTED for x in p.parts)
            or any(x.lower().startswith(('dim-', 'dim_')) for x in p.parts)
            or low.endswith(('.db', '.sqlite', '.sqlite3', '.bak', '.old', '.log', '.pyc'))
            or '.bak.' in low or 'apocalypse-performance' in low
            or low.startswith(('config/spark/activity',
                               'kubejs/config/web_server', 'config/jei/world/'))
            or 'euphoriapatches_errorshader' in low)

def checked_file(path):
    # Reject reparse points anywhere between the named input and instance root.
    for part in [path, *path.parents]:
        if part == ROOT.parent:
            break
        if part.is_symlink() or getattr(part.lstat(), 'st_file_attributes', 0) & 1024:
            raise ValueError('Linked input: ' + str(part))
    if not path.is_file():
        raise ValueError('Missing input: ' + str(path))
    return path.read_bytes()

def write(root, rel, data):
    assert not blocked(rel), rel
    p = root / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_bytes(data if isinstance(data, bytes) else data.encode('utf-8'))

def meta(text):
    return tomllib.loads(text.decode('utf-8-sig'))

def prepare(archive, output, version):
    assert re.fullmatch(r'v\d+\.\d+\.\d+', version), 'Explicit release version required'
    assert not output.exists() and ROOT not in output.parents and output != ROOT
    output.mkdir(parents=True)
    candidate = output / 'candidate'
    baseline = {}
    excluded = []
    with zipfile.ZipFile(archive) as z:
        top = z.namelist()[0].split('/')[0]
        assert top == 'Apocalypse-Industries-' + BASE, 'Archive is not the pinned baseline'
        for item in z.infolist():
            if item.is_dir():
                continue
            assert item.filename.startswith(top + '/')
            rel = item.filename[len(top) + 1:]
            if blocked(rel):
                excluded.append(rel)
                continue
            assert not stat.S_ISLNK(item.external_attr >> 16), rel
            assert rel not in baseline, rel
            data = z.read(item)
            baseline[rel] = data
            write(candidate, rel, data)
    pack = meta(baseline['pack.toml'])
    idx = meta(baseline['index.toml'])
    assert pack['version'] == 'v1.3.2'
    assert sha(baseline['index.toml']) == pack['index']['hash']
    for entry in idx['files']:
        if not blocked(entry['file']):
            assert sha(baseline[entry['file']]) == entry['hash'], entry['file']
    for rel in REMOVE:
        assert rel in baseline, 'Unexpected baseline: ' + rel
        (candidate / rel).unlink()
    for rel in COPY:
        write(candidate, rel, checked_file(ROOT / rel))

    # Direct custom addon is stored in GitHub but installed only via client metafile.
    addon_meta = 'mods/apocalypse-submarine-lava-visuals.pw.toml'
    write(candidate, addon_meta,
          'name = "Apocalypse Submarine and Vortex Shader Bridge"\n'
          'filename = "apocalypse-submarine-lava-visuals-1.1.2.jar"\nside = "client"\n\n'
          '[download]\nurl = "https://raw.githubusercontent.com/' + REPO + '/main/' + ADDON + '"\n'
          'hash-format = "sha256"\nhash = "' + sha((candidate / ADDON).read_bytes()) + '"\n')
    # Only transfer the deliberate cloud toggle, not WIP's unrelated DH settings.
    dh = baseline['config/DistantHorizons.toml'].decode('utf-8-sig')
    dh, count = re.subn(r'(?m)^(\s*enableCloudRendering\s*=\s*)true\s*$', r'\g<1>false', dh)
    assert count == 1
    write(candidate, 'config/DistantHorizons.toml', dh)
    iris = baseline['config/iris.properties'].decode('utf-8-sig')
    iris, count = re.subn(r'(?m)^shaderPack=.*$', 'shaderPack=' + Path(SHADER).name, iris)
    assert count == 1
    iris, count = re.subn(r'(?m)^enableShaders=.*$', 'enableShaders=true', iris)
    assert count == 1
    write(candidate, 'config/iris.properties', iris)
    # Publish current sources, excluding build outputs before traversal.
    for base, dirs, files in os.walk(ROOT / 'scripts/submarine_lava', followlinks=False):
        dirs[:] = [d for d in dirs if d not in {'__pycache__', 'build', 'out', '.local'}
                   and not (Path(base)/d).is_symlink()
                   and not getattr((Path(base)/d).lstat(), 'st_file_attributes', 0) & 1024]
        for name in sorted(files):
            path = Path(base) / name
            if path.suffix in {'.py', '.java', '.json', '.glsl', '.fsh', '.vsh', '.mcmeta', '.md', '.txt'}:
                write(candidate, path.relative_to(ROOT).as_posix(), checked_file(path))
    write(candidate, 'scripts/prepare_shader_release.py', checked_file(Path(__file__).resolve()))
    write(candidate, 'docs/SHADER_RELEASE.md', checked_file(ROOT/'docs/SHADER_RELEASE.md'))
    write(candidate, 'docs/SHADER_RELEASE_NOTES.md', checked_file(ROOT/'docs/SHADER_RELEASE_NOTES.md'))
    write(candidate, 'VERSION', version + '\n')
    pack_text = baseline['pack.toml'].decode('utf-8-sig')
    pack_text = re.sub(r'(?m)^version\s*=.*$', 'version = "' + version + '"', pack_text)
    entries = {e['file']: dict(e) for e in idx['files'] if e['file'] not in REMOVE and not blocked(e['file'])}
    for rel in COPY:
        if rel != ADDON:
            entries[rel] = {'file': rel, **({'metafile': True} if rel.endswith('.pw.toml') else {})}
    entries[addon_meta] = {'file': addon_meta, 'metafile': True}
    for rel in ['config/iris.properties', SHADER + '.txt']:
        entries[rel]['preserve'] = True
    lines = ['hash-format = "sha256"', '']
    for rel, entry in sorted(entries.items()):
        entry['hash'] = sha((candidate/rel).read_bytes())
        lines += ['[[files]]', 'file = ' + json.dumps(rel), 'hash = "' + entry['hash'] + '"']
        lines += [k + ' = true' for k in ['metafile', 'preserve'] if entry.get(k)]
        lines += ['']
    index = '\n'.join(lines) + '\n'
    write(candidate, 'index.toml', index)
    pack_text = re.sub(r'(?m)^hash\s*=.*$', 'hash = "' + sha(index.encode()) + '"', pack_text)
    write(candidate, 'pack.toml', pack_text)
    # Only release notes are prepended; existing changelog remains intact.
    notes = (ROOT/'docs/SHADER_RELEASE_NOTES.md').read_text(encoding='utf-8')
    write(candidate, 'CHANGELOG.md', '# ' + version + ' — Shader integration\n\n' + notes + '\n\n' + baseline['CHANGELOG.md'].decode('utf-8-sig'))
    (output/'RELEASE_NOTES.md').write_text(notes, encoding='utf-8')

    # Scope/evidence improvement: every byte changed must belong to this allowlist.
    changed = []
    current = {p.relative_to(candidate).as_posix(): p.read_bytes() for p in candidate.rglob('*') if p.is_file()}
    allowed = set(COPY + REMOVE + ['VERSION', 'pack.toml', 'index.toml', 'CHANGELOG.md', addon_meta,
                  'config/iris.properties', 'config/DistantHorizons.toml',
                  'docs/SHADER_RELEASE.md', 'docs/SHADER_RELEASE_NOTES.md', 'scripts/prepare_shader_release.py'])
    for rel in sorted(set(baseline) | set(current)):
        before, after = baseline.get(rel), current.get(rel)
        if before != after:
            assert rel in allowed or rel.startswith('scripts/submarine_lava/'), 'Unscoped change: ' + rel
            changed.append({'path': rel, 'before': sha(before) if before is not None else None,
                            'after': sha(after) if after is not None else None})
    for rel in current:
        assert not blocked(rel), rel
    # Check metafile hashes against named, installed inputs; no broad mod inventory.
    dependencies = []
    for rel in [r for r in COPY if r.endswith('.pw.toml')]:
        m = meta(current[rel])
        jar = checked_file(ROOT/'mods'/m['filename'])
        assert sha(jar) == m['download']['hash'], rel
        dependencies.append({'metafile': rel, 'filename': m['filename'], 'side': m['side'],
                             'sha256': sha(jar), 'url': m['download']['url']})
    with zipfile.ZipFile(candidate/SHADER) as z:
        assert z.testzip() is None
        assert 'License.txt' in z.namelist(), 'Keep upstream shader license'
        shader_license = z.read('License.txt').decode('utf-8-sig')
    (output/'SHADER_UPSTREAM_LICENSE.txt').write_text(shader_license, encoding='utf-8')
    with zipfile.ZipFile(candidate/ADDON) as z:
        m = meta(z.read('META-INF/neoforge.mods.toml'))
        assert m['mods'][0]['version'] == '1.1.2'
        assert any(d['modId']=='create_submarine' and d['versionRange']=='[3.4.4]' for d in m['dependencies']['apocalypse_submarine_lava'])
    # Extract exact old bytes for rollback, never live/world state.
    with zipfile.ZipFile(output/'rollback-changed-pack-files.zip', 'w', zipfile.ZIP_DEFLATED) as z:
        for e in changed:
            if e['before'] is not None:
                z.writestr(e['path'], baseline[e['path']])
    with zipfile.ZipFile(output/'shader-release-overlay.zip', 'w', zipfile.ZIP_DEFLATED) as z:
        for e in changed:
            if e['after'] is not None:
                z.writestr(e['path'], current[e['path']])
    report = {'status': 'PREPARED_NOT_PUBLISHED', 'method': 'pinned-baseline-allowlist-v1',
              'baseline_commit': BASE, 'baseline_pack_version': 'v1.3.2', 'proposed_version': version,
              'baseline_archive_sha256': sha(archive.read_bytes()),
              'shader_sha256': sha(current[SHADER]), 'addon_sha256': sha(current[ADDON]),
              'changes': changed, 'excluded_baseline_paths': excluded, 'dependencies': dependencies,
              'validation': ['baseline index verified', 'all changed bytes allowlisted',
                             'changed upstream downloads match local artifact hashes',
                             'shader ZIP integrity and original license retained'],
              'gates': ['User approval of shared client/server release before publication',
                        'Validate server applicable dependencies and HYBRID pressure policy before deployment',
                        'Fresh-install and existing-install gameplay unverified',
                        'Missing-chunk fallback still assumes lava plane Y32; current WIP sea is Y100',
                        'No controlled evidence of FPS improvement'],
              'exclusions': ['Unrelated WIP changes', 'Performance recorder and captures',
                             'Worlds, schematics, blueprints, generated DH data',
                             'Live WIP/server settings, histories, credentials']}
    # Review the actual shader source delta, not just an opaque ZIP hash.
    import io
    with zipfile.ZipFile(io.BytesIO(baseline[REMOVE[0]])) as old, zipfile.ZipFile(candidate/SHADER) as new:
        old_names, new_names = set(old.namelist()), set(new.namelist())
        shader_changes, patch = [], []
        for name in sorted(old_names | new_names):
            before = old.read(name) if name in old_names else None
            after = new.read(name) if name in new_names else None
            if before == after:
                continue
            shader_changes.append({'entry':name, 'before':sha(before) if before is not None else None,
                                   'after':sha(after) if after is not None else None})
            if name.endswith(('.glsl', '.fsh', '.vsh', '.properties', '.txt')):
                patch.extend(difflib.unified_diff((before or b'').decode('utf-8-sig').splitlines(True),
                                                 (after or b'').decode('utf-8-sig').splitlines(True),
                                                 fromfile='old/'+name, tofile='new/'+name))
    (output/'shader-entry-changes.json').write_text(json.dumps(shader_changes, indent=2)+'\n', encoding='utf-8')
    (output/'shader-source-changes.patch').write_text(''.join(patch), encoding='utf-8')
    report['shader_changed_entries'] = len(shader_changes)
    (output/'manifest.json').write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
    (output/'DELETE_FROM_REPOSITORY.txt').write_text('\n'.join(REMOVE + excluded)+'\n', encoding='utf-8')
    (output/'README.md').write_text(
        '# Prepared shader release candidate\n\nNot published or deployed. Proposed version: '+version+'.\n\n'
        'candidate/ is a complete filtered GitHub snapshot based on commit '+BASE+'. '
        'shader-release-overlay.zip contains only changed/new repository files. '
        'Apply DELETE_FROM_REPOSITORY.txt to that exact repository baseline, then validate. '
        'This deletion list is for the repository only, never an instance/server/world.\n\n'
        'manifest.json records exact before/after hashes, dependency downloads, exclusions and publication gates. '
        'rollback-changed-pack-files.zip holds prior bytes; rollback also removes files with before=null in the manifest.\n\n'
        'The bootstrap continues to track live main. It is not a runnable preview of this unpublished candidate. '
        'See RELEASE_NOTES.md and candidate/docs/SHADER_RELEASE.md.\n', encoding='utf-8')
    print(json.dumps({'output':str(output), 'changed_files':len(changed), 'excluded_baseline_files':len(excluded),
                      'status':report['status']}, indent=2))

if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--baseline-archive', required=True, type=Path)
    p.add_argument('--output', required=True, type=Path)
    p.add_argument('--version', default='v1.3.3')
    a = p.parse_args()
    prepare(a.baseline_archive.resolve(), a.output.resolve(), a.version)
