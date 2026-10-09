"""Build the version-pinned envelope damage addon; no installation or deployment."""
from pathlib import Path
import argparse,os,subprocess,zipfile,hashlib,json
here=Path(__file__).resolve().parent;root=here.parents[1];launcher=root.parents[1]
p=argparse.ArgumentParser();p.add_argument('--output',type=Path,required=True);a=p.parse_args();out=a.output.resolve()
if out==root or root in out.parents or out.exists():raise SystemExit('Use a new build directory outside the instance')
out.mkdir(parents=True);classes=out/'classes';classes.mkdir();deps=out/'deps';deps.mkdir()
java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
lib=launcher/'libraries';server=launcher/'servers/Chunkserve - Apocalypse Industries/libraries'
pins=json.loads((here/'pins.json').read_text())
cp=[lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar',lib/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar']
for base,dirs,files in os.walk(server,followlinks=False):
 for name in dirs+files:
  f=Path(base)/name
  if f.is_symlink() or getattr(f.lstat(),'st_file_attributes',0)&1024:raise SystemExit('Linked library path: '+str(f))
 cp.extend(Path(base)/n for n in files if n.endswith('.jar'))
for name,expected in pins.items():
 f=root/'mods'/name
 if hashlib.sha256(f.read_bytes()).hexdigest()!=expected:raise SystemExit('Pinned JAR differs: '+name)
 cp.append(f)
for f in list(dict.fromkeys(cp)):
 with zipfile.ZipFile(f) as z:
  for entry in z.namelist():
   if entry.endswith('.jar') and entry.startswith('META-INF/jarjar/'):
    dst=deps/(f.name+'-'+Path(entry).name);dst.write_bytes(z.read(entry));cp.append(dst)
args=['--release','21','-proc:none','-encoding','UTF-8','-classpath',os.pathsep.join(map(str,cp)),'-d',str(classes)]+[str(f) for f in sorted((here/'src').rglob('*.java'))]
arg=out/'javac.args';arg.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args));subprocess.run([str(java/'javac.exe'),'@'+str(arg)],check=True)
artifact=out/'apocalypse-envelope-damage-1.0.0+mc1.21.1.jar'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for directory,prefix in [(classes,''),(here/'resources',''),(here/'src','sources/')]:
  for f in sorted(directory.rglob('*')):
   if f.is_file():
    info=zipfile.ZipInfo(prefix+f.relative_to(directory).as_posix(),(2026,10,9,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,f.read_bytes())
report={'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),'pins':pins,'installed':False,'gameplay_tested':False}
(out/'build.json').write_text(json.dumps(report,indent=2)+'\n');(out/'classpath.txt').write_text(os.pathsep.join(map(str,cp)));print(json.dumps(report,indent=2))
