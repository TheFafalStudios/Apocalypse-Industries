"""Install the validated basalt seabed, preserving all other WIP settings."""
from pathlib import Path
from datetime import datetime
import hashlib,json,shutil,zipfile

ROOT=Path(__file__).resolve().parents[2]
LOCAL=ROOT/'.local/nether-islands-test'
STAGE=LOCAL/'seabed-pack-01'
DEST=ROOT/'config/paxi/datapacks/diabolical_islands_wip'
AUDIT=LOCAL/'seabed-native-02/island-audit.json'


def main():
    report=json.loads(AUDIT.read_text())
    assert report['pass'] and report['bedrockBlocksChecked']==16384
    assert report['maximumAdjacentHeightStep']<=1
    sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
    for doc in ['NETHER_ISLANDS_CASED_DEBRIS_INSTALLATION.json','NETHER_ISLANDS_LAYERS_INSTALLATION.json']:
        baseline=json.loads((ROOT/'docs'/doc).read_text())
        for entry in baseline['files']:
            if entry['afterSha256'] is not None:
                assert sha(ROOT/entry['path'])==entry['afterSha256'],entry['path']
    addon='apocalypse-nether-islands-0.5.0.jar'
    old=ROOT/'mods/apocalypse-nether-islands-0.4.0.jar'
    production=LOCAL/'seabed-production-build-01'/addon
    assert old.is_file() and not (ROOT/'mods'/addon).exists()
    with zipfile.ZipFile(production) as p,zipfile.ZipFile(AUDIT.parent/'mods'/addon) as t:
        assert b'apocalypse_island_tests' not in p.read('META-INF/neoforge.mods.toml')
        for name in p.namelist():
            if name.endswith('.class'):assert p.read(name)==t.read(name)
    changed=[]
    for source in STAGE.rglob('*'):
        if source.is_file():
            target=DEST/source.relative_to(STAGE)
            if not target.exists() or source.read_bytes()!=target.read_bytes():changed.append((source,target))
    expected={
        'data/apocalypse_islands/worldgen/noise/seabed.json',
        'data/apocalypse_islands/worldgen/density_function/seabed.json',
        'data/diabolical_islands/worldgen/density_function/terrain/final.json',
        'data/minecraft/worldgen/noise_settings/nether.json'}
    assert {t.relative_to(DEST).as_posix() for _,t in changed}==expected
    record=LOCAL/datetime.now().strftime('seabed-install-%Y%m%d-%H%M%S')
    record.mkdir(exist_ok=False)
    backup=record/'rollback/mods'/old.name
    backup.parent.mkdir(parents=True,exist_ok=True)
    shutil.copy2(old,backup)
    changed.append((production,ROOT/'mods'/addon))
    entries=[]
    for source,target in changed:
        before=sha(target) if target.exists() else None
        if before:
            backup=record/'rollback'/target.relative_to(ROOT)
            backup.parent.mkdir(parents=True,exist_ok=True)
            shutil.copy2(target,backup)
        target.parent.mkdir(parents=True,exist_ok=True)
        shutil.copy2(source,target)
        assert sha(target)==sha(source)
        entries.append({'path':str(target.relative_to(ROOT)),'beforeSha256':before,'afterSha256':sha(target)})
    entries.append({'path':str(old.relative_to(ROOT)),'beforeSha256':sha(old),'afterSha256':None})
    old.unlink()
    result={'scope':'WIP only','validation':report,'nativeAudit':str(AUDIT.relative_to(ROOT)),
        'files':entries,'rollbackDirectory':str(record/'rollback'),
        'activation':'User-managed relaunch; newly generated chunks only. Existing saves untouched.',
        'rollback':'Restore the two modified JSON files and old addon; remove addon 0.5.0 and the two newly added seabed JSON files.'}
    content=json.dumps(result,indent=2)+'\n'
    (record/'installation.json').write_text(content)
    (ROOT/'docs/NETHER_ISLANDS_SEABED_INSTALLATION.json').write_text(content)
    print(content)


if __name__=='__main__':main()
