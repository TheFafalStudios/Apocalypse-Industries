"""Install the tested larger, rarer cased deposits into WIP only."""
from pathlib import Path
from datetime import datetime
import hashlib
import json
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[2]
LOCAL = ROOT / '.local/nether-islands-test'
STAGE = LOCAL / 'cased-debris-pack-01'
DEST = ROOT / 'config/paxi/datapacks/diabolical_islands_wip'
AUDIT = LOCAL / 'cased-debris-native-01/island-audit.json'
OLD = 'apocalypse-nether-islands-0.3.0.jar'
NEW = 'apocalypse-nether-islands-0.4.0.jar'


def main():
    report = json.loads(AUDIT.read_text())
    assert report['pass'] and report['chunksScanned'] == 256
    assert report['bedrockFloorBlocksChecked'] == 65536
    assert report['casingBlocksChecked'] > 0
    assert report['previousExpectedDebrisPerChunk'] == report['newExpectedDebrisPerChunk']
    production = LOCAL / 'cased-debris-production-build-01' / NEW
    tested = LOCAL / 'cased-debris-audit-build-01' / NEW
    with zipfile.ZipFile(production) as p, zipfile.ZipFile(tested) as t:
        assert b'apocalypse_island_tests' not in p.read('META-INF/neoforge.mods.toml')
        for name in p.namelist():
            if name.endswith('.class'):
                assert p.read(name) == t.read(name), name
    sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
    baseline = json.loads((ROOT / 'docs/NETHER_ISLANDS_DEBRIS_INSTALLATION.json').read_text())
    for entry in baseline['files']:
        if entry['afterSha256'] is not None:
            assert sha(ROOT / entry['path']) == entry['afterSha256'], entry['path']
    changes = []
    for source in STAGE.rglob('*'):
        if source.is_file():
            target = DEST / source.relative_to(STAGE)
            assert target.is_file()
            if source.read_bytes() != target.read_bytes():
                changes.append((source, target))
    expected = {Path('data/apocalypse_islands/worldgen/configured_feature/floating_debris.json'),
                Path('data/apocalypse_islands/worldgen/placed_feature/floating_debris.json')}
    assert {t.relative_to(DEST) for _, t in changes} == expected
    old, new = ROOT / 'mods' / OLD, ROOT / 'mods' / NEW
    assert old.is_file() and not new.exists()
    record = LOCAL / datetime.now().strftime('cased-debris-install-%Y%m%d-%H%M%S')
    record.mkdir(exist_ok=False)
    for target in [t for _, t in changes] + [old]:
        backup = record / 'rollback' / target.relative_to(ROOT)
        backup.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(target, backup)
    entries = []
    for source, target in changes + [(production, new)]:
        before = sha(target) if target.exists() else None
        shutil.copy2(source, target)
        assert sha(source) == sha(target)
        entries.append({'path': str(target.relative_to(ROOT)), 'beforeSha256': before, 'afterSha256': sha(target)})
    entries.append({'path': str(old.relative_to(ROOT)), 'beforeSha256': sha(old), 'afterSha256': None})
    old.unlink()
    result = {'scope': 'WIP only', 'rollbackDirectory': str(record / 'rollback'),
        'validation': report, 'nativeAudit': str(AUDIT.relative_to(ROOT)), 'files': entries,
        'density': '3–6 / 4 = 9–18 / 12: both average 1.125 debris blocks per chunk before placement skips.',
        'activation': 'User-managed relaunch; newly generated chunks only. Existing saves untouched.',
        'rollback': 'Remove addon 0.4.0, then restore the rollback files at their recorded relative paths.'}
    content = json.dumps(result, indent=2) + '\n'
    (record / 'installation.json').write_text(content)
    (ROOT / 'docs/NETHER_ISLANDS_CASED_DEBRIS_INSTALLATION.json').write_text(content)
    print(content)


if __name__ == '__main__':
    main()
