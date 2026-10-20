"""Install only the native-tested Nether portal safety addon update."""
from pathlib import Path
from datetime import datetime
import hashlib,json,shutil,zipfile

ROOT=Path(__file__).resolve().parents[2]
LOCAL=ROOT/'.local/nether-islands-test'
AUDIT=LOCAL/'portal-native-02/island-audit.json'
OLD='apocalypse-nether-islands-0.6.0.jar'
NEW='apocalypse-nether-islands-0.7.0.jar'


def main():
    report=json.loads(AUDIT.read_text())
    assert report['pass'] and report['overworldLowPortalPreserved']
    assert report['submergedExistingPortalsRejected'] and report['lavaAdjacentPortalsRejected']
    assert len(report['portalCases'])==2 and all(c['fallbackY']>=104 for c in report['portalCases'])
    production=LOCAL/'portal-production-build-02'/NEW
    sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
    with zipfile.ZipFile(production) as p,zipfile.ZipFile(AUDIT.parent/'mods'/NEW) as t:
        assert b'apocalypse_island_tests' not in p.read('META-INF/neoforge.mods.toml')
        for name in p.namelist():
            if name.endswith('.class'):assert p.read(name)==t.read(name),name
    old,new=ROOT/'mods'/OLD,ROOT/'mods'/NEW
    baseline=json.loads((ROOT/'docs/NETHER_ISLANDS_RAISED_INSTALLATION.json').read_text())
    expected=next(e['afterSha256'] for e in baseline['files'] if e['path'].endswith(OLD))
    assert sha(old)==expected and not new.exists()
    record=LOCAL/datetime.now().strftime('portals-install-%Y%m%d-%H%M%S')
    (record/'rollback/mods').mkdir(parents=True,exist_ok=False)
    shutil.copy2(old,record/'rollback/mods'/OLD)
    before=sha(old)
    shutil.copy2(production,new);assert sha(new)==sha(production)
    old.unlink()
    result={'scope':'WIP only','validation':report,'nativeAudit':str(AUDIT.relative_to(ROOT)),
        'files':[{'path':'mods/'+OLD,'beforeSha256':before,'afterSha256':None},
                 {'path':'mods/'+NEW,'beforeSha256':None,'afterSha256':sha(new)}],
        'rollbackDirectory':str(record/'rollback'),
        'activation':'User-managed full Minecraft relaunch. Applies to new arrivals, including existing worlds. No save files edited.',
        'rollback':'Remove addon 0.7.0 and restore addon 0.6.0 from rollback.'}
    content=json.dumps(result,indent=2)+'\n'
    (record/'installation.json').write_text(content)
    (ROOT/'docs/NETHER_ISLANDS_PORTALS_INSTALLATION.json').write_text(content)
    print(json.dumps({'validation':report,'rollback':str(record/'rollback')},indent=2))


if __name__=='__main__':main()
