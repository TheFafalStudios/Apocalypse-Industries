"""Compile the pinned, common-side Deep Seas pressure policy; never start Minecraft."""
from pathlib import Path
import hashlib,json,os,subprocess,zipfile,argparse
ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
LAUNCHER=ROOT.parents[1]
JAVA=LAUNCHER/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
EXPECTED='5f190e51f0c05c4b77eade488f514014cf5409a2f6792625772d9a6647604c51'
def build(out):
    assert hashlib.sha256((ROOT/'mods/create_submarine-3.4.4.jar').read_bytes()).hexdigest()==EXPECTED
    out.mkdir(parents=True,exist_ok=False)
    libs=LAUNCHER/'libraries'
    cp=[libs/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',libs/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar']
    for base,dirs,files in os.walk(libs,followlinks=False):
        dirs[:]=[d for d in dirs if not (Path(base)/d).is_symlink() and not getattr((Path(base)/d).lstat(),'st_file_attributes',0)&1024]
        cp.extend(Path(base)/f for f in files if f.endswith('.jar'))
    cp.extend(sorted((ROOT/'mods').glob('*.jar')))
    cp=list(dict.fromkeys(cp)); deps=out/'deps';deps.mkdir()
    for jar in list(cp):
        with zipfile.ZipFile(jar) as z:
            for name in z.namelist():
                if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
                    dest=deps/(jar.name+'-'+Path(name).name); dest.write_bytes(z.read(name));cp.append(dest)
    classes=out/'classes';classes.mkdir()
    args=['--release','21','-proc:none','-encoding','UTF-8','-cp',os.pathsep.join(map(str,cp)),'-d',str(classes),*map(str,sorted((HERE/'src').rglob('*.java'))+sorted((HERE/'tests').rglob('*.java')))]
    (out/'javac.args').write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
    subprocess.run([str(JAVA/'javac.exe'),'@'+str(out/'javac.args')],check=True)
    (out/'classpath.txt').write_text(os.pathsep.join(map(str,[classes,HERE/'resources',*cp])))
    artifact=out/'apocalypse-submarine-balance-1.0.0.jar'
    metadata='''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_submarine_balance"
version="1.0.0"
displayName="Apocalypse Submarine Pressure Balance"
description="Opaque endgame hulls, fluid-specific ratings, and costly netherite fitting jackets."
[[mixins]]
config="apocalypse-submarine-balance.mixins.json"
[[dependencies.apocalypse_submarine_balance]]
modId="create_submarine"
type="required"
versionRange="[3.4.4]"
side="BOTH"
[[dependencies.apocalypse_submarine_balance]]
modId="sable"
type="required"
versionRange="[2.0.3]"
side="BOTH"
[[dependencies.apocalypse_submarine_balance]]
modId="dndecor"
type="required"
versionRange="*"
side="BOTH"
'''
    with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
        for p in sorted(classes.rglob('*.class')):
            if not p.name.endswith('Regression.class') and p.name!='NativeHarness.class':z.write(p,p.relative_to(classes).as_posix())
        for p in sorted((HERE/'resources').rglob('*')):
            if p.is_file():z.write(p,p.relative_to(HERE/'resources').as_posix())
        for p in sorted((HERE/'src').rglob('*.java')):z.write(p,'sources/'+p.relative_to(HERE/'src').as_posix())
        z.writestr('META-INF/neoforge.mods.toml',metadata)
        z.writestr('apocalypse-submarine-balance.mixins.json',json.dumps({'required':True,'minVersion':'0.8','compatibilityLevel':'JAVA_21','package':'dev.apocalypse.subbalance.mixin','mixins':['WaterRatings','PressureRatings','ActualMaterials'],'injectors':{'defaultRequire':1}}))
    report={'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),'deepSeasSha256':EXPECTED}
    with zipfile.ZipFile(out/'balance-native-harness.jar','w',zipfile.ZIP_DEFLATED) as z:
        z.write(classes/'dev/apocalypse/subbalance/tests/NativeHarness.class','dev/apocalypse/subbalance/tests/NativeHarness.class')
        z.writestr('META-INF/neoforge.mods.toml','''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_sub_balance_tests"
version="1.0.0"
[[dependencies.apocalypse_sub_balance_tests]]
modId="apocalypse_submarine_balance"
type="required"
versionRange="[1.0.0]"
ordering="AFTER"
side="BOTH"
''')
    (out/'build.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=Path);a=p.parse_args();build(a.output.resolve())
