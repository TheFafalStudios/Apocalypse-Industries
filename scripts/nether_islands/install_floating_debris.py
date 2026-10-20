"""Install the validated submerged-debris WIP change with rollback evidence."""
from pathlib import Path
from datetime import datetime
import hashlib
import json
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[2]
LOCAL = ROOT / '.local/nether-islands-test'
STAGE = LOCAL / 'debris-pack-02'
DEST = ROOT / 'config/paxi/datapacks/diabolical_islands_wip'
AUDIT = LOCAL / 'debris-native-02/island-audit.json'
NEW = 'apocalypse-nether-islands-0.3.0.jar'
OLD = 'apocalypse-nether-islands-0.2.0.jar'


def main():
    report = json.loads(AUDIT.read_text())
    assert report['pass'] and report['debrisBlocks'] > 0
    assert report['bedrockFloorBlocksChecked'] == 16384
    production = LOCAL / 'debris-production-build-01' / NEW
    tested = LOCAL / 'debris-audit-build-01' / NEW
    with zipfile.ZipFile(production) as p, zipfile.ZipFile(tested) as t:
        assert b'apocalypse_island_tests' not in p.read('META-INF/neoforge.mods.toml')
        for name in p.namelist():
            if name.endswith('.class'):
                assert p.read(name) == t.read(name), name
    old = ROOT / 'mods' / OLD
    new = ROOT / 'mods' / NEW
    assert old.is_file() and not new.exists()
    additions = []
    for src in STAGE.rglob('*'):
        if not src.is_file():
            continue
        rel = src.relative_to(STAGE)
        target = DEST / rel
        if target.exists():
            assert src.read_bytes() == target.read_bytes(), 'Unexpected existing pack difference: ' + str(rel)
        else:
            additions.append((src, target))
    assert len(additions) == 4
    assert all('apocalypse_islands' in str(target) for _, target in additions)
    stamp = datetime.now().strftime('debris-install-%Y%m%d-%H%M%S')
    record = LOCAL / stamp
    (record / 'rollback/mods').mkdir(parents=True, exist_ok=False)
    sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    shutil.copy2(old, record / 'rollback/mods' / OLD)
    old_hash = sha(old)
    changes = []
    for source, target in additions + [(production, new)]:
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        assert sha(source) == sha(target)
        changes.append({'path': str(target.relative_to(ROOT)), 'beforeSha256': None, 'afterSha256': sha(target)})
    old.unlink()
    changes.append({'path': str(old.relative_to(ROOT)), 'beforeSha256': old_hash, 'afterSha256': None})
    result = {'scope': 'WIP only', 'rollbackDirectory': str(record / 'rollback'),
        'validation': report, 'nativeAudit': str(AUDIT.relative_to(ROOT)), 'files': changes,
        'activation': 'User-managed relaunch, newly generated chunks only. Existing saves untouched.',
        'rollback': 'Restore the old addon; remove the new addon and four added datapack JSON files.'}
    content = json.dumps(result, indent=2) + '\n'
    (record / 'installation.json').write_text(content)
    (ROOT / 'docs/NETHER_ISLANDS_DEBRIS_INSTALLATION.json').write_text(content)
    print(content)


if __name__ == '__main__':
    main()
