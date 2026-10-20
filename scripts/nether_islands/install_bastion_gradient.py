"""Install visible bastion islands and the tested depth-dependent debris preset."""
from pathlib import Path
from datetime import datetime
import hashlib,json,shutil,zipfile

ROOT=Path(__file__).resolve().parents[2]
LOCAL=ROOT/'.local/nether-islands-test'
STAGE=LOCAL/'bastion-gradient-pack-01'
DEST=ROOT/'config/paxi/datapacks/diabolical_islands_wip'
AUDIT=LOCAL/'bastion-gradient-native-02/island-audit.json'
OLD='apocalypse-nether-islands-0.7.0.jar'
NEW='apocalypse-nether-islands-0.8.0.jar'


def main():
    report=json.loads(AUDIT.read_text())
    assert report['pass'] and report['minimumLavaBlocksAboveDeposit']>=30
    assert report['meanDeepClumpSize']>report['meanShallowClumpSize']+4
    bastion=report['structures']['minecraft:bastion_remnant']
    assert bastion['postStructureCoreColumnsSolid']==bastion['postStructureCoreColumnsChecked']
    assert bastion['postStructureRimColumnsSolid']==bastion['postStructureRimColumnsChecked']
    assert bastion['islandTopY']>=111
    for s in report['structures'].values():assert s['checkedFloorPoints']==s['supportedFloorPoints']
    sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
    production=LOCAL/'bastion-gradient-production-build-02'/NEW
    with zipfile.ZipFile(production) as p,zipfile.ZipFile(AUDIT.parent/'mods'/NEW) as t:
        assert b'apocalypse_island_tests' not in p.read('META-INF/neoforge.mods.toml')
        for name in p.namelist():
            if name.endswith('.class'):assert p.read(name)==t.read(name),name
    old,new=ROOT/'mods'/OLD,ROOT/'mods'/NEW
    baseline=json.loads((ROOT/'docs/NETHER_ISLANDS_PORTALS_INSTALLATION.json').read_text())
    assert sha(old)==next(e['afterSha256'] for e in baseline['files'] if e['path'].endswith(OLD))
    assert not new.exists()
    changes=[]
    for source in STAGE.rglob('*'):
        if source.is_file():
            target=DEST/source.relative_to(STAGE)
            assert target.is_file()
            if source.read_bytes()!=target.read_bytes():changes.append((source,target))
    assert {t.relative_to(DEST).as_posix() for _,t in changes}=={
        'data/apocalypse_islands/worldgen/configured_feature/floating_debris.json',
        'data/apocalypse_islands/worldgen/placed_feature/floating_debris.json'}
    record=LOCAL/datetime.now().strftime('bastion-gradient-install-%Y%m%d-%H%M%S');record.mkdir(exist_ok=False)
    for target in [t for _,t in changes]+[old]:
        backup=record/'rollback'/target.relative_to(ROOT)
        backup.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(target,backup)
    entries=[]
    for source,target in changes+[(production,new)]:
        before=sha(target) if target.exists() else None
        shutil.copy2(source,target);assert sha(source)==sha(target)
        entries.append({'path':str(target.relative_to(ROOT)),'beforeSha256':before,'afterSha256':sha(target)})
    entries.append({'path':str(old.relative_to(ROOT)),'beforeSha256':sha(old),'afterSha256':None});old.unlink()
    evidence={'scope':'WIP only','validation':report,'nativeAudit':str(AUDIT.relative_to(ROOT)),
        'files':entries,'rollbackDirectory':str(record/'rollback'),
        'design':'One whole bastion island below actual template bounds; debris yield grows with depth, symmetric tapered height distribution, nominal 1.125 debris/chunk before rejected attempts.',
        'activation':'Full user-managed Minecraft relaunch; new bastions/deposits in fresh terrain only. No existing saves edited.',
        'rollback':'Remove addon 0.8.0 and restore addon 0.7.0 plus the two recorded ore JSON files.'}
    content=json.dumps(evidence,indent=2)+'\n'
    (record/'installation.json').write_text(content)
    (ROOT/'docs/NETHER_ISLANDS_BASTION_GRADIENT_INSTALLATION.json').write_text(content)
    print(json.dumps({'bastionIsland':{k:v for k,v in bastion.items() if k.startswith('postStructure') or k=='islandTopY'},
        'meanShallowClumpSize':report['meanShallowClumpSize'],'meanDeepClumpSize':report['meanDeepClumpSize'],
        'lavaOverhead':report['minimumLavaBlocksAboveDeposit'],'rollback':str(record/'rollback')},indent=2))


if __name__=='__main__':main()
