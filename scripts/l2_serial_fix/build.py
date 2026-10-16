"""Build the pinned common-side L2 codec compatibility addon; does not install it."""
from pathlib import Path
import argparse, hashlib, json, os, subprocess, zipfile
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
EXPECTED='26038e30873cf505bc289e28f9dcf084561b8a4929b294dd0552ba5e52505dd6'
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--output',type=Path,required=True)
p.add_argument('--instance',type=Path,default=ROOT,help='ATLauncher instance containing the pinned L2Library and launcher libraries')
a=p.parse_args(); out=a.output.resolve(); instance=a.instance.resolve()
if out.exists():raise SystemExit('Use a new output directory')
library=instance/'mods/l2library-3.0.8.jar'
if hashlib.sha256(library.read_bytes()).hexdigest()!=EXPECTED:raise SystemExit('L2Library hash does not match the tested target')
out.mkdir(parents=True); classes=out/'classes'; classes.mkdir()
launcher=instance.parents[1]; lib=launcher/'libraries'
cp=[lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar',lib/'net/neoforged/fancymodloader/loader/4.0.43/loader-4.0.43.jar',lib/'net/fabricmc/sponge-mixin/0.17.3+mixin.0.8.7/sponge-mixin-0.17.3+mixin.0.8.7.jar',lib/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar']
# Annotations/distmarker are provided by the known NeoForge library subtree.
cp += sorted((lib/'net/neoforged').rglob('*.jar'))
args=['--release','21','-proc:none','-classpath',os.pathsep.join(map(str,dict.fromkeys(cp))),'-d',str(classes)]+[str(f) for f in sorted((HERE/'src').rglob('*.java'))]
argfile=out/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/javac.exe'
subprocess.run([str(java),'@'+str(argfile)],check=True)
artifact=out/'apocalypse-l2-serial-fix-1.1.0.jar'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for folder,prefix in [(classes,''),(HERE/'resources',''),(HERE/'src','sources/')]:
  for f in sorted(folder.rglob('*')):
   if f.is_file():
    info=zipfile.ZipInfo(prefix+f.relative_to(folder).as_posix(),(2026,10,16,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,f.read_bytes().replace(b'\r\n',b'\n'))
report={'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),'target_l2library_sha256':EXPECTED,'side':'both','server_payload':'forced NBT/name encoding; requires user-managed server restart'}
(out/'build.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
