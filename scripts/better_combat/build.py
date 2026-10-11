"""Build pinned Better Combat scope addon outside the pack; does not install."""
from pathlib import Path
import argparse, hashlib, json, os, subprocess, zipfile
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--output',type=Path,required=True)
p.add_argument('--better-combat',type=Path,required=True)
a=p.parse_args();out=a.output.resolve()
if out.exists() or out==ROOT or ROOT in out.parents:raise SystemExit('Use a new directory outside the pack')
pins={a.better_combat:'bacbc8c86671663e9998b141c5732250c1f4d4fb',ROOT/'mods/spear-backport-neoforge-1.8.0-1.21.1.jar':'944b22a7fa34c1e87e70f4558c9164d950646dd6'}
for f,h in pins.items():assert hashlib.sha1(f.read_bytes()).hexdigest()==h,f
out.mkdir(parents=True);launcher=ROOT.parents[1];lib=launcher/'libraries';server=launcher/'servers/Chunkserve - Apocalypse Industries/libraries'
cp=[lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar',*pins]
for base,dirs,files in os.walk(server,followlinks=False):
 for name in dirs+files:
  f=Path(base)/name
  if f.is_symlink() or getattr(f.lstat(),'st_file_attributes',0)&1024:raise SystemExit('Linked library path: '+str(f))
 cp += [Path(base)/name for name in files if name.endswith('.jar')]
classes=out/'classes';classes.mkdir()
args=['--release','21','-encoding','UTF-8','-proc:none','-classpath',os.pathsep.join(map(str,cp)),'-d',str(classes),*map(str,sorted((HERE/'src').rglob('*.java')))]
argfile=out/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf8')
subprocess.run([str(launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/javac.exe'),'@'+str(argfile)],check=True)
meta='''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_combat_scope"
version="1.0.1"
displayName="Apocalypse Industries: Combat Scope"
description="Better Combat for swords, axes and tridents; native spear and gun controls."
[[mixins]]
config="apocalypse_combat_scope.mixins.json"
'''
for mod,version in [('minecraft','[1.21.1]'),('neoforge','[21.1.248,)'),('bettercombat','[2.4.0+1.21.1]'),('spears','[1.8.0]')]:
 meta+=f'\n[[dependencies.apocalypse_combat_scope]]\nmodId="{mod}"\ntype="required"\nversionRange="{version}"\nordering="AFTER"\nside="BOTH"\n'
mixins={'required':True,'minVersion':'0.8','package':'dev.apocalypse.combat.mixin','compatibilityLevel':'JAVA_21','mixins':['WeaponRegistryMixin','SpearsNeoForgeMixin'],'injectors':{'defaultRequire':1}}
artifact=out/'apocalypse-combat-scope-1.0.1.jar'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for folder,prefix in [(classes,''),(HERE/'src','sources/')]:
  for f in sorted(folder.rglob('*')):
   if f.is_file():z.writestr(prefix+f.relative_to(folder).as_posix(),f.read_bytes())
 z.writestr('META-INF/neoforge.mods.toml',meta)
 z.writestr('apocalypse_combat_scope.mixins.json',json.dumps(mixins,indent=2)+'\n')
record={'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),'pins_sha1':{str(f):h for f,h in pins.items()}}
(out/'build.json').write_text(json.dumps(record,indent=2)+'\n');print(json.dumps(record,indent=2))
