import pathlib, subprocess, json, zipfile, hashlib, argparse
ROOT=pathlib.Path(__file__).resolve().parents[2];HERE=ROOT/'scripts/weapon_library';LAUNCHER=ROOT.parents[1]
p=argparse.ArgumentParser();p.add_argument('--output',type=pathlib.Path,required=True);a=p.parse_args();OUT=a.output.resolve()
if OUT==ROOT or ROOT in OUT.parents:raise SystemExit('Build output must be outside the live instance')
OUT.mkdir(parents=True,exist_ok=False)
with zipfile.ZipFile(LAUNCHER/'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar') as z:
 names=[n for n in z.namelist() if n.endswith('.jar') and 'mixinextras' in n.lower()];assert len(names)==1;(OUT/'mixinextras.jar').write_bytes(z.read(names[0]))
JAVA=LAUNCHER/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/javac.exe'
CLASSES=OUT/'classes';CLASSES.mkdir(exist_ok=True)
cp=[LAUNCHER/'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',
    LAUNCHER/'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar',
    LAUNCHER/'libraries/net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar',
    ROOT/'mods/polytone-1.21-4.1.0-neoforge.jar',OUT/'mixinextras.jar']
cp += [p for p in (LAUNCHER/'libraries').rglob('*.jar') if '/net/minecraft/' not in p.as_posix() and '/net/neoforged/neoforge/' not in p.as_posix()]
args=['--release','21','-encoding','UTF-8','-proc:none','-classpath',';'.join(map(str,cp)),'-d',str(CLASSES)]+[str(p) for p in (HERE/'src').rglob('*.java')]
argfile=OUT/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8')
subprocess.run([str(JAVA),'@'+str(argfile)],check=True)
out=OUT/'apocalypse-named-trident-rendering-1.3.2.jar'
toml='''modLoader="lowcodefml"
loaderVersion="[1,)"
license="Private pack integration"
[[mods]]
modId="apocalypse_named_tridents"
version="1.3.2"
displayName="Apocalypse Named Trident Rendering"
description="Client rendering bridge for the four weapon-library tridents."
[[mixins]]
config="apocalypse_named_tridents.mixins.json"
[[dependencies.apocalypse_named_tridents]]
modId="neoforge"
type="required"
versionRange="[21.1,)"
side="CLIENT"
[[dependencies.apocalypse_named_tridents]]
modId="polytone"
type="required"
versionRange="[1.21-4.1.0]"
side="CLIENT"
'''
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
 for p in CLASSES.rglob('*.class'):z.write(p,p.relative_to(CLASSES).as_posix())
 z.writestr('META-INF/neoforge.mods.toml',toml)
 z.writestr('apocalypse_named_tridents.mixins.json',json.dumps({'required':True,'minVersion':'0.8','package':'apocalypse.tridents.mixin','compatibilityLevel':'JAVA_21','client':['TridentItemRendererMixin','ThrownTridentRendererMixin','PolytoneNameGuardMixin','SpearModelShaperMixin'],'injectors':{'defaultRequire':1}}))
print(out.name,hashlib.sha256(out.read_bytes()).hexdigest())

