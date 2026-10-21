"""Build the common-side ballast-only lava compatibility addon (Deep Seas 3.4.4)."""
from pathlib import Path
import argparse,hashlib,json,os,subprocess,zipfile
ROOT=Path(__file__).resolve().parents[2];HERE=Path(__file__).resolve().parent
LAUNCHER=ROOT.parents[1];JAVA=LAUNCHER/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
JAR='apocalypse-submarine-lava-ballast-1.0.0.jar'
def build(out):
    assert hashlib.sha256((ROOT/'mods/create_submarine-3.4.4.jar').read_bytes()).hexdigest()=='5f190e51f0c05c4b77eade488f514014cf5409a2f6792625772d9a6647604c51'
    if ROOT/'.local' not in out.parents:raise ValueError('Build under .local, outside distribution paths')
    out.mkdir(parents=True,exist_ok=False);libs=LAUNCHER/'libraries'
    cp=[libs/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',libs/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar']
    for base,dirs,files in os.walk(libs,followlinks=False):
        dirs[:]=[d for d in dirs if not (Path(base)/d).is_symlink() and not getattr((Path(base)/d).lstat(),'st_file_attributes',0)&1024]
        cp.extend(Path(base)/f for f in files if f.endswith('.jar'))
    cp.extend(sorted((ROOT/'mods').glob('*.jar')));cp=list(dict.fromkeys(cp));deps=out/'deps';deps.mkdir()
    for jar in list(cp):
        with zipfile.ZipFile(jar) as z:
            for name in z.namelist():
                if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
                    dest=deps/(jar.name+'-'+Path(name).name);dest.write_bytes(z.read(name));cp.append(dest)
    classes=out/'classes';classes.mkdir()
    args=['--release','21','-proc:none','-encoding','UTF-8','-cp',os.pathsep.join(map(str,cp)),'-d',str(classes),*map(str,sorted((HERE/'src').rglob('*.java'))+sorted((HERE/'tests').rglob('*.java')))]
    (out/'javac.args').write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
    subprocess.run([str(JAVA/'javac.exe'),'@'+str(out/'javac.args')],check=True)
    (out/'classpath.txt').write_text(os.pathsep.join(map(str,[classes,*cp])))
    metadata='''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_submarine_lava_machinery"
version="1.0.0"
displayName="Apocalypse Lava Ballast"
description="Lava-aware ballast tanks, vents and floaters. No propulsion changes."
[[mixins]]
config="apocalypse-lava-ballast.mixins.json"
[[dependencies.apocalypse_submarine_lava_machinery]]
modId="create_submarine"
type="required"
versionRange="[3.4.4]"
ordering="AFTER"
side="BOTH"
[[dependencies.apocalypse_submarine_lava_machinery]]
modId="sable"
type="required"
versionRange="[2.0.3]"
ordering="AFTER"
side="BOTH"
'''
    with zipfile.ZipFile(out/JAR,'w',zipfile.ZIP_DEFLATED) as z:
        for path in sorted(classes.rglob('*.class')):
            rel=path.relative_to(classes).as_posix()
            if '/tests/' not in rel:z.write(path,rel)
        for path in sorted((HERE/'src').rglob('*.java')):z.write(path,'sources/'+path.relative_to(HERE/'src').as_posix())
        z.writestr('META-INF/neoforge.mods.toml',metadata)
        z.writestr('assets/apocalypse_submarine_lava_machinery/lang/en_us.json',json.dumps({'apocalypse_submarine_lava_machinery.ballast_fluid':'Ballast fluid'}))
        z.writestr('apocalypse-lava-ballast.mixins.json',json.dumps({'required':True,'minVersion':'0.8','compatibilityLevel':'JAVA_21','package':'dev.apocalypse.lavamachinery.mixin','mixins':['TankAccess','TankLiquids','VentLiquids','FloaterLiquids'],'injectors':{'defaultRequire':1}}))
    with zipfile.ZipFile(out/'ballast-native-harness.jar','w',zipfile.ZIP_DEFLATED) as z:
        for path in sorted((classes/'dev/apocalypse/lavamachinery/tests').rglob('*.class')):z.write(path,path.relative_to(classes).as_posix())
        z.writestr('META-INF/neoforge.mods.toml','''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_ballast_tests"
version="1.0.0"
[[dependencies.apocalypse_ballast_tests]]
modId="apocalypse_submarine_lava_machinery"
type="required"
versionRange="[1.0.0]"
ordering="AFTER"
side="BOTH"
''')
    report={'artifact':JAR,'sha256':hashlib.sha256((out/JAR).read_bytes()).hexdigest()}
    (out/'build.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--output',required=True,type=Path);a=p.parse_args();build(a.output.resolve())
