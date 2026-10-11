"""Build pinned standalone addon without installation or Minecraft launch."""
from pathlib import Path
import argparse, hashlib, json, subprocess, zipfile
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--output',type=Path,required=True)
p.add_argument('--baseline',type=Path,help='Original 1.0.0 addon, e.g. saved rollback copy')
a=p.parse_args()
out=a.output.resolve()
old=a.baseline.resolve() if a.baseline else ROOT/'mods/apocalypse-weather2compat-threadsafe-fix-1.0.0.jar'
if out==ROOT or ROOT in out.parents or out.exists():raise SystemExit('Use a new external build directory')
pins={
'weather2-neoforge-1.21.0-2.8.7.jar':'a9eefd33a8966767e9eee62aacfcb001466460c5adcce9d71540007e97824e45',
'apocalypse-weather2compat-threadsafe-fix-1.0.0.jar':'e83466504f8517d17937b532c790298be21d19d80ab023185b807c3decae9d1c',
'weather2compat-1.6.0.jar':'53b2155a7d823d26299b9ce7439bdd86f6567493da64620b9fd7eabaa3613e63',
}
for name,expected in pins.items():
 if hashlib.sha256((old if name=='apocalypse-weather2compat-threadsafe-fix-1.0.0.jar' else ROOT/'mods'/name).read_bytes()).hexdigest()!=expected:raise SystemExit('Input mismatch: '+name)
out.mkdir(parents=True)
java=ROOT.parents[1]/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
classes=out/'classes';classes.mkdir()
source=HERE/'src/net/obf/weather2compat/hotfix/Weather2CompatSafety.java'
subprocess.run([str(java/'javac.exe'),'--release','21','-proc:none','-cp',str(ROOT/'mods/weather2-neoforge-1.21.0-2.8.7.jar'),'-d',str(classes),str(source)],check=True)
helper='net/obf/weather2compat/hotfix/Weather2CompatSafety.class'
with zipfile.ZipFile(old) as z:entries={n:z.read(n) for n in z.namelist()}
entries[helper]=(classes/helper).read_bytes()
entries['META-INF/neoforge.mods.toml']=entries['META-INF/neoforge.mods.toml'].decode().replace('version="1.0.0"','version="1.0.1"').replace('Thread Safety Fix','Storm Type and Thread Safety Fix').encode()
entries['sources/net/obf/weather2compat/hotfix/Weather2CompatSafety.java']=source.read_bytes()
artifact=out/'apocalypse-weather2compat-threadsafe-fix-1.0.1.jar'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for n,data in sorted(entries.items()):
  info=zipfile.ZipInfo(n,(2026,10,11,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,data)
report={'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),'inputs':pins,'baseline':str(old),'changed_entries':[helper,'META-INF/neoforge.mods.toml'],'validation':'Compilation only; regression test required before installation'}
(out/'build.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))
