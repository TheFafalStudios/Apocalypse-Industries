"""Install validated Nether biome toggles and underside glowstone placement."""
from pathlib import Path
from datetime import datetime
import hashlib,json,shutil,zipfile
from configure_ecology import NETHER_BIOMES

ROOT=Path(__file__).resolve().parents[2]
LOCAL=ROOT/'.local/nether-islands-test'
STAGE=LOCAL/'ecology-pack-01'
DEST=ROOT/'config/paxi/datapacks/diabolical_islands_wip'
AUDIT=LOCAL/'ecology-native-01/island-audit.json'


def main():
    report=json.loads(AUDIT.read_text())
    assert report['pass'] and report['glowstoneBlocks']>0 and len(report['moddedBiomeLocations'])==5
    sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
    baseline=json.loads((ROOT/'docs/NETHER_ISLANDS_RAISED_INSTALLATION.json').read_text())
    for entry in baseline['files']:
        if entry['afterSha256'] is not None:assert sha(ROOT/entry['path'])==entry['afterSha256']
    addon='apocalypse-nether-islands-0.6.0.jar'
    with zipfile.ZipFile(ROOT/'mods'/addon) as p,zipfile.ZipFile(AUDIT.parent/'mods'/addon) as t:
        for name in p.namelist():
            if name.endswith('.class'):assert p.read(name)==t.read(name)
    changes=[]
    for source in STAGE.rglob('*'):
        if source.is_file():
            target=DEST/source.relative_to(STAGE)
            if not target.exists() or source.read_bytes()!=target.read_bytes():changes.append((source,target))
    assert {t.relative_to(DEST).as_posix() for _,t in changes}=={
        'data/minecraft/worldgen/placed_feature/glowstone.json',
        'data/minecraft/worldgen/placed_feature/glowstone_extra.json'}
    target=ROOT/'config/biomesoplenty/biome_toggles.json'
    source=LOCAL/'ecology-biome-toggles.json'
    before,after=json.loads(target.read_text()),json.loads(source.read_text())
    expected={n+'_enabled' for n in NETHER_BIOMES}
    assert {k for k in before if before[k]!=after[k]}==expected
    assert all(after[k] is True for k in expected)
    changes.append((source,target))
    record=LOCAL/datetime.now().strftime('ecology-install-%Y%m%d-%H%M%S');record.mkdir(exist_ok=False)
    entries=[]
    for source,target in changes:
        before=sha(target) if target.exists() else None
        if before:
            backup=record/'rollback'/target.relative_to(ROOT)
            backup.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(target,backup)
        target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,target)
        assert sha(source)==sha(target)
        entries.append({'path':str(target.relative_to(ROOT)),'beforeSha256':before,'afterSha256':sha(target)})
    result={'scope':'WIP only','validation':report,'files':entries,'rollbackDirectory':str(record/'rollback'),
        'nativeAudit':str(AUDIT.relative_to(ROOT)),
        'activation':'User-managed full relaunch; fresh Nether terrain required. Existing worlds untouched.',
        'rollback':'Restore biome_toggles.json and remove the two new glowstone placed-feature overrides.'}
    content=json.dumps(result,indent=2)+'\n'
    (record/'installation.json').write_text(content)
    (ROOT/'docs/NETHER_ISLANDS_ECOLOGY_INSTALLATION.json').write_text(content)
    print(json.dumps({'filesChanged':len(entries),'glowstoneBlocks':report['glowstoneBlocks'],
        'moddedBiomes':list(report['moddedBiomeLocations']),'rollback':str(record/'rollback')},indent=2))


if __name__=='__main__':main()
