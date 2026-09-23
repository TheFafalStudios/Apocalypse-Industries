"""Compile the small PP addon against the installed, pinned 1.21.1 runtime; no Gradle downloads."""
from pathlib import Path
import subprocess, zipfile, hashlib, json, os
ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
LAUNCHER=ROOT.parents[1]
JAVA=LAUNCHER/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/javac.exe'
LIB=LAUNCHER/'libraries'
SERVER=LAUNCHER/'servers/Chunkserve - Apocalypse Industries/libraries'
BUILD=HERE/'build';BUILD.mkdir(exist_ok=True)
classes=BUILD/'classes';classes.mkdir(exist_ok=True)
cp=[LIB/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',LIB/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar',ROOT/'mods/protection_pixel-2.2.1-neoforge-1.21.1.jar']
cp += [p for p in SERVER.rglob('*.jar') if 'neoforge-21.' not in p.name]
args=['--release','21','-proc:none','-classpath',';'.join(map(str,cp)),'-d',str(classes)]+[str(p) for p in (HERE/'src').rglob('*.java')]
argfile=BUILD/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf8')
subprocess.run([str(JAVA),'@'+str(argfile)],check=True,cwd=ROOT)
artifact=ROOT/'mods/apocalypse-protection-pixel-integration-1.1.jar'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for folder in [classes,HERE/'resources']:
  for f in sorted(folder.rglob('*')):
   if f.is_file():
    info=zipfile.ZipInfo(f.relative_to(folder).as_posix(),(2026,9,16,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;z.writestr(info,f.read_bytes())
print(str(artifact),hashlib.sha256(artifact.read_bytes()).hexdigest())
