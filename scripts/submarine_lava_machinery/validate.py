"""Validate the pinned ballast-only artifact and completed isolated runtime evidence."""
from pathlib import Path
import argparse,hashlib,json,zipfile
from build import ROOT,JAR
p=argparse.ArgumentParser();p.add_argument('--build',required=True,type=Path);p.add_argument('--native',required=True,type=Path);a=p.parse_args()
jar=a.build/JAR
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
native=json.loads((a.native/'ballast-native-validation.json').read_text())
assert native['pass'] and native['checks']>=304
assert sha(jar)==sha(a.native/'mods'/JAR)
with zipfile.ZipFile(jar) as z:
    config=json.loads(z.read('apocalypse-lava-ballast.mixins.json'))
    assert config['mixins']==['TankAccess','TankLiquids','VentLiquids','FloaterLiquids']
    for name in z.namelist():
        assert '/tests/' not in name
        if name.endswith('.class'):
            data=z.read(name)
            for forbidden in [b'net/minecraft/client/',b'SubmarinePropellerBlockEntity',b'WaterThrusterBlockEntity',b'BoatBuoyancySystem']:
                assert forbidden not in data,(name,forbidden)
assert sha(ROOT/'mods/create_submarine-3.4.4.jar')=='5f190e51f0c05c4b77eade488f514014cf5409a2f6792625772d9a6647604c51'
result={'pass':True,'sha256':sha(jar),'nativeChecks':native['checks'],'propulsion':'No propeller, thruster or boat targets/references; upstream jar unchanged','distribution':'Common-side classes; no client-only references; test harness excluded','worldTest':None}
if (a.native/'ballast-world-validation.json').exists():
    world=json.loads((a.native/'ballast-world-validation.json').read_text());assert world['pass'] and not world['baseline'];result['worldTest']=world
(a.build/'validation.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
