"""Compile the version-pinned importer compatibility addon; never installs it."""
from pathlib import Path
import argparse,subprocess,zipfile,json,hashlib,os
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--output',type=Path,required=True,help='New build directory outside the game instance')
a=p.parse_args()
out=a.output.resolve()
if out==ROOT or ROOT in out.parents:raise SystemExit('Build output must be outside the game instance')
if out.exists():raise SystemExit('Use a new output directory to preserve prior build records')
out.mkdir(parents=True)
launcher=ROOT.parents[1]
java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/javac.exe'
lib=launcher/'libraries'
server=launcher/'servers/Chunkserve - Apocalypse Industries/libraries'
for name,expected in json.loads((HERE/'pins.json').read_text()).items():
 actual=hashlib.sha256((ROOT/'mods'/name).read_bytes()).hexdigest()
 if actual!=expected:raise SystemExit('Pinned mod hash mismatch: '+name)
cp=[lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar']
cp += [ROOT/'mods'/name for name in json.loads((HERE/'pins.json').read_text())]
# Only inspect the known library tree, rejecting links/junctions.
for base,dirs,files in os.walk(server,followlinks=False):
 for name in dirs+files:
  item=Path(base)/name
  if item.is_symlink() or getattr(item.lstat(),'st_file_attributes',0)&1024:raise SystemExit('Linked library path: '+str(item))
 cp += [Path(base)/name for name in files if name.endswith('.jar')]
deps=out/'deps';deps.mkdir()
with zipfile.ZipFile(ROOT/'mods/create-1.21.1-6.0.10.jar') as z:
 for name in z.namelist():
  if name.endswith('.jar'):
   f=deps/Path(name).name;f.write_bytes(z.read(name));cp.append(f)
classes=out/'classes';classes.mkdir()
args=['--release','21','-proc:none','-classpath',os.pathsep.join(map(str,cp)),'-d',str(classes)]+[str(f) for f in sorted((HERE/'src').rglob('*.java'))]
argfile=out/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
subprocess.run([str(java),'@'+str(argfile)],check=True)
artifact=out/'apocalypse-importer-safety-fix-1.0.0.jar'
entries={}
for folder,prefix in [(classes,''),(HERE/'resources',''),(HERE/'src','sources/')]:
 for f in folder.rglob('*'):
  if f.is_file():entries[prefix+f.relative_to(folder).as_posix()]=f.read_bytes()
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for name,data in sorted(entries.items()):
  info=zipfile.ZipInfo(name,(2026,10,3,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED
  z.writestr(info,data)
report={'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),'pinned_mods':json.loads((HERE/'pins.json').read_text())}
(out/'build.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
