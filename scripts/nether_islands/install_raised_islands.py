"""Install the tested Y100 rounded-island preset, with rollback evidence."""
from pathlib import Path
from datetime import datetime
import hashlib,json,shutil,zipfile

ROOT=Path(__file__).resolve().parents[2]
LOCAL=ROOT/'.local/nether-islands-test'
STAGE=LOCAL/'raised-pack-02'
DEST=ROOT/'config/paxi/datapacks/diabolical_islands_wip'
AUDIT=LOCAL/'raised-native-02/island-audit.json'
NEW='apocalypse-nether-islands-0.6.0.jar'
OLD='apocalypse-nether-islands-0.5.0.jar'


def main():
    report=json.loads(AUDIT.read_text())
    assert report['pass'] and report['minimumLavaBlocksAboveDeposit']>=30
    assert all(n>0 for n in report['solidBlocksByHeightBand'])
    for s in report['structures'].values():assert s['supportedFloorPoints']==s['checkedFloorPoints']
    sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
    baseline=json.loads((ROOT/'docs/NETHER_ISLANDS_SEABED_INSTALLATION.json').read_text())
    for entry in baseline['files']:
        if entry['afterSha256'] is not None:assert sha(ROOT/entry['path'])==entry['afterSha256']
    production=LOCAL/'raised-production-build-01'/NEW
    with zipfile.ZipFile(production) as p,zipfile.ZipFile(AUDIT.parent/'mods'/NEW) as t:
        assert b'apocalypse_island_tests' not in p.read('META-INF/neoforge.mods.toml')
        for name in p.namelist():
            if name.endswith('.class'):assert p.read(name)==t.read(name),name
    changed=[]
    for source in STAGE.rglob('*'):
        if source.is_file():
            target=DEST/source.relative_to(STAGE)
            assert target.is_file()
            if source.read_bytes()!=target.read_bytes():changed.append((source,target))
    paths={t.relative_to(DEST).as_posix() for _,t in changed}
    expected={'data/minecraft/worldgen/noise_settings/nether.json',
              'data/apocalypse_islands/worldgen/placed_feature/floating_debris.json',
              'data/diabolical_islands/worldgen/density_function/islands/final.json'}
    expected.update(f'data/diabolical_islands/worldgen/density_function/islands/layer{i}/depth_{side}.json'
                    for i in range(1,5) for side in ['bottom','top'])
    assert paths==expected,paths
    old,new=ROOT/'mods'/OLD,ROOT/'mods'/NEW
    assert old.is_file() and not new.exists()
    record=LOCAL/datetime.now().strftime('raised-install-%Y%m%d-%H%M%S')
    record.mkdir(exist_ok=False)
    for target in [t for _,t in changed]+[old]:
        backup=record/'rollback'/target.relative_to(ROOT)
        backup.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(target,backup)
    entries=[]
    for source,target in changed+[(production,new)]:
        before=sha(target) if target.exists() else None
        shutil.copy2(source,target);assert sha(source)==sha(target)
        entries.append({'path':str(target.relative_to(ROOT)),'beforeSha256':before,'afterSha256':sha(target)})
    entries.append({'path':str(old.relative_to(ROOT)),'beforeSha256':sha(old),'afterSha256':None});old.unlink()
    result={'scope':'WIP only','validation':report,'files':entries,'rollbackDirectory':str(record/'rollback'),
        'nativeAudit':str(AUDIT.relative_to(ROOT)),
        'design':'Lava sea level 100; naturally rounded floating islands, no enforced lava-contact proportion; 30 full lava blocks above debris casing.',
        'activation':'User-managed full relaunch; fresh test terrain. Existing saves untouched.',
        'rollback':'Remove addon 0.6.0 and restore the recorded rollback files.'}
    content=json.dumps(result,indent=2)+'\n'
    (record/'installation.json').write_text(content)
    (ROOT/'docs/NETHER_ISLANDS_RAISED_INSTALLATION.json').write_text(content)
    print(json.dumps({'changedFiles':len(entries),'rollbackDirectory':str(record/'rollback'),
        'lavaOverhead':report['minimumLavaBlocksAboveDeposit'],
        'structureFloorPoints':sum(s['checkedFloorPoints'] for s in report['structures'].values())},indent=2))


if __name__=='__main__':main()
