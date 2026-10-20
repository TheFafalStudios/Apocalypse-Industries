"""Install only the verified WIP test files, preserving rollback copies and hashes."""
from pathlib import Path
from datetime import datetime
import hashlib, json, shutil

ROOT = Path(__file__).resolve().parents[2]
LOCAL = ROOT / '.local/nether-islands-test'
STAGED = LOCAL / 'revision-02c-pack'
TARGET = ROOT / 'config/paxi/datapacks/diabolical_islands_wip'
ADDON = LOCAL / 'addon-build-final/apocalypse-nether-islands-0.2.0.jar'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    report = json.loads((LOCAL / 'revision-02c-full/island-audit.json').read_text())
    assert report['pass'] and report['foundationsEnabled']
    assert all(v['checkedFloorPoints'] == v['supportedFloorPoints'] for v in report['structures'].values())
    # Ensure the production addon is compiled from exactly the tested sources,
    # while deliberately excluding the test-only entrypoint.
    import zipfile
    with zipfile.ZipFile(LOCAL / 'audit-build-08/apocalypse-nether-islands-0.2.0.jar') as tested, zipfile.ZipFile(ADDON) as built:
        for entry in built.namelist():
            if entry.endswith('.class'):
                assert built.read(entry) == tested.read(entry), entry
        assert not any('IslandRuntimeTests' in entry for entry in built.namelist())
    stamp = datetime.now().strftime('%Y%m%d-%H%M%S')
    record = LOCAL / ('install-' + stamp)
    record.mkdir()
    changes = []
    candidates = [(p, TARGET / p.relative_to(STAGED)) for p in sorted(STAGED.rglob('*')) if p.is_file()]
    candidates.append((ADDON, ROOT / 'mods' / ADDON.name))
    for source, target in candidates:
        for path in (source, target):
            assert path.resolve().is_relative_to(ROOT.resolve())
            assert not path.is_symlink()
        before = sha(target) if target.exists() else None
        after = sha(source)
        if before == after:
            continue
        relative = target.relative_to(ROOT)
        if target.exists():
            backup = record / 'rollback' / relative
            backup.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(target, backup)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        assert sha(target) == after
        changes.append({'path': relative.as_posix(), 'before': before, 'after': after})
    result = {'scope': 'WIP testing only', 'record': str(record), 'changes': changes,
              'native_floor_points_passed': sum(s['checkedFloorPoints'] for s in report['structures'].values()),
              'client_gameplay_tested': False, 'existing_worlds_modified': False}
    (record / 'installation.json').write_text(json.dumps(result, indent=2) + '\n')
    (ROOT / 'docs/NETHER_ISLANDS_INSTALLATION.json').write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps(result, indent=2))


if __name__ == '__main__':
    main()
