"""Install only the validated WIP bedrock-floor datapack changes."""
from pathlib import Path
from datetime import datetime
import hashlib
import json
import shutil

ROOT = Path(__file__).resolve().parents[2]
STAGE = ROOT / '.local/nether-islands-test/bedrock-pack-01'
DEST = ROOT / 'config/paxi/datapacks/diabolical_islands_wip'
AUDIT = ROOT / '.local/nether-islands-test/bedrock-native-01/island-audit.json'


def main():
    report = json.loads(AUDIT.read_text())
    assert report['pass'] and report['bottomY'] == -64
    assert report['bedrockFloorBlocksChecked'] == 2304
    assert report['bedrockAboveFloor'] == report['bedrockAtRoof'] == 0
    changed = []
    for src in STAGE.rglob('*'):
        if src.is_file():
            rel = src.relative_to(STAGE)
            target = DEST / rel
            assert target.is_file(), rel
            if src.read_bytes() != target.read_bytes():
                changed.append(rel)
    expected = {
        Path('data/diabolical_islands/worldgen/density_function/terrain/final.json'),
        Path('data/minecraft/worldgen/noise_settings/nether.json'),
    }
    assert set(changed) == expected, changed
    record = ROOT / '.local/nether-islands-test' / datetime.now().strftime('bedrock-install-%Y%m%d-%H%M%S')
    record.mkdir(exist_ok=False)
    sha = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    entries = []
    for rel in sorted(changed):
        target, source = DEST / rel, STAGE / rel
        backup = record / 'rollback' / rel
        backup.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(target, backup)
        before = sha(target)
        shutil.copy2(source, target)
        after = sha(target)
        assert after == sha(source)
        entries.append({'path': str(target.relative_to(ROOT)), 'beforeSha256': before, 'afterSha256': after})
    result = {'scope': 'WIP only', 'rollbackDirectory': str(record / 'rollback'),
              'audit': str(AUDIT.relative_to(ROOT)), 'validation': report, 'files': entries,
              'activation': 'User-managed relaunch; newly generated chunks only. Existing saves untouched.'}
    content = json.dumps(result, indent=2) + '\n'
    (record / 'installation.json').write_text(content)
    (ROOT / 'docs/NETHER_ISLANDS_BEDROCK_INSTALLATION.json').write_text(content)
    print(content)


if __name__ == '__main__':
    main()
