"""Build the isolated Weather2 height prototype; never installs or publishes it."""
from pathlib import Path
import argparse,os,subprocess,zipfile,hashlib,json
here=Path(__file__).resolve().parent;root=here.parents[1];launcher=root.parents[1]
p=argparse.ArgumentParser();p.add_argument('--output',type=Path,required=True);a=p.parse_args();out=a.output.resolve()
if out==root or root in out.parents or out.exists():raise SystemExit('Use a new output directory outside the instance')
out.mkdir(parents=True);classes=out/'classes';classes.mkdir()
java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
lib=launcher/'libraries';server=launcher/'servers/Chunkserve - Apocalypse Industries/libraries'
weather=root/'mods/weather2-neoforge-1.21.0-2.8.7.jar'
if hashlib.sha256(weather.read_bytes()).hexdigest()!='a9eefd33a8966767e9eee62aacfcb001466460c5adcce9d71540007e97824e45':raise SystemExit('Weather2 JAR differs from the audited 2.8.7 build')
if server.is_symlink() or server.is_junction():raise SystemExit('Linked library root')
cp=[lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar',lib/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar',weather]
for base,dirs,files in os.walk(server,followlinks=False):
 for name in dirs+files:
  f=Path(base)/name
  if f.is_symlink() or f.is_junction():raise SystemExit('Linked library path: '+str(f))
 cp.extend(Path(base)/n for n in files if n.endswith('.jar'))
deps=out/'deps';deps.mkdir()
for name in ['tornadophysics-1.0.3.jar','sable-neoforge-1.21.1-2.0.3.jar','create-1.21.1-6.0.10.jar']:
 mod=root/'mods'/name;cp.append(mod)
 if name.startswith('tornadophysics') and hashlib.sha256(mod.read_bytes()).hexdigest()!='b72c8faaa9d75e346ed39451224afaa7895ed83e5477e3728a0d7a670623f91b':raise SystemExit('Weather Physics differs from audited build')
 with zipfile.ZipFile(mod) as z:
  for entry in z.namelist():
   if entry.endswith('.jar'):
    dst=deps/(name+'-'+Path(entry).name);dst.write_bytes(z.read(entry));cp.append(dst)
args=['--release','21','-proc:none','-encoding','UTF-8','-classpath',os.pathsep.join(map(str,cp)),'-d',str(classes)]+[str(f) for f in sorted((here/'src').rglob('*.java'))]
arg=out/'javac.args';arg.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args));subprocess.run([str(java/'javac.exe'),'@'+str(arg)],check=True)
artifact=out/'apocalypse-wind-height-0.1.0-prototype.jar'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for directory,prefix in [(classes,''),(here/'resources',''),(here/'src','sources/')]:
  for f in sorted(directory.rglob('*')):
   if f.is_file():
    info=zipfile.ZipInfo(prefix+f.relative_to(directory).as_posix(),(2026,10,8,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,f.read_bytes())
report={'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),'weather2_sha256':hashlib.sha256(weather.read_bytes()).hexdigest(),'java':'21','minecraft':'1.21.1','neoforge':'21.1.248','installed':False,'gameplay_tested':False}
(out/'build.json').write_text(json.dumps(report,indent=2)+'\n');(out/'classpath.txt').write_text(os.pathsep.join(map(str,cp)));print(json.dumps(report,indent=2))
