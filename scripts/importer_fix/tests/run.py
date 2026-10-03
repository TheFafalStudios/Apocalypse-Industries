"""Run native importer regression checks in a NEW disposable server directory."""
from pathlib import Path
import argparse, subprocess, zipfile, shutil, os, time, threading, queue
p=argparse.ArgumentParser(description=__doc__)
for name in ['java-home','libraries','minecraft-jar','mod-jar','create-jar','curios-jar','workdir']:
 p.add_argument('--'+name,type=Path,required=True)
p.add_argument('--accept-eula',action='store_true')
p.add_argument('--expect-hang',action='store_true')
a=p.parse_args()
if not a.accept_eula:p.error('Accept the Minecraft EULA explicitly with --accept-eula')
if a.workdir.exists():p.error('--workdir must not exist')
test=a.workdir.resolve();test.mkdir(parents=True)
java=a.java_home.resolve()/'bin'/('java.exe' if os.name=='nt' else 'java')
javac=java.with_name('javac.exe' if os.name=='nt' else 'javac')
# Copy runtime libraries only, rejecting symbolic links / Windows junctions.
def linked(path):
 return path.is_symlink() or bool(getattr(path.lstat(),'st_file_attributes',0)&1024)
if linked(a.libraries):p.error('Linked library root is not supported')
for base,dirs,files in os.walk(a.libraries,followlinks=False):
 for name in dirs+files:
  if linked(Path(base)/name):raise RuntimeError('Linked library path: '+str(Path(base)/name))
 for name in files:
  src=Path(base)/name;dst=test/'libraries'/src.relative_to(a.libraries)
  dst.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(src,dst)
mods=test/'mods';mods.mkdir()
for src in [a.mod_jar,a.create_jar,a.curios_jar]:shutil.copy2(src,mods/src.name)
cp=[a.minecraft_jar.resolve(),a.mod_jar.resolve(),a.create_jar.resolve(),a.curios_jar.resolve()]
cp+=list((test/'libraries').rglob('*.jar'))
deps=test/'compile-deps';deps.mkdir()
with zipfile.ZipFile(a.create_jar) as z:
 for n in z.namelist():
  if n.endswith('.jar'):
   f=deps/Path(n).name;f.write_bytes(z.read(n));cp.append(f)
classes=test/'test-classes';classes.mkdir()
args=['--release','21','-proc:none','-classpath',os.pathsep.join(map(str,cp)),'-d',str(classes),str(Path(__file__).with_name('ImporterRegression.java').resolve())]
argfile=test/'javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
subprocess.run([str(javac),'@'+str(argfile)],check=True)
with zipfile.ZipFile(mods/'importer-regression.jar','w',zipfile.ZIP_DEFLATED) as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes).as_posix())
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="AGPL-3.0-only"\n[[mods]]\nmodId="importer_regression"\nversion="1"\ndisplayName="Importer regression tests"\n')
(test/'eula.txt').write_text('eula=true\n')
(test/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25587\nonline-mode=false\nlevel-name=disposable-test-world\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nview-distance=2\nsimulation-distance=2\nmax-tick-time=60000\n')
argname='win_args.txt' if os.name=='nt' else 'unix_args.txt'
launch=list((test/'libraries/net/neoforged/neoforge').glob('*/'+argname))
if len(launch)!=1:raise RuntimeError('Expected exactly one installed NeoForge runtime')
process=subprocess.Popen([str(java),'-Xms512M','-Xmx3G','@'+str(launch[0]),'nogui'],cwd=test,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf8',errors='replace')
messages=queue.Queue()
def read():
 for line in process.stdout:messages.put(line)
 messages.put(None)
threading.Thread(target=read,daemon=True).start()
started=time.monotonic();switch=None;passed=False;hung=False
try:
 with (test/'regression.log').open('w',encoding='utf8') as log:
  while time.monotonic()-started<180:
   try:line=messages.get(timeout=1)
   except queue.Empty:line=''
   if line is None:break
   log.write(line);log.flush()
   if 'IMPORTER ' in line or 'ERROR' in line:print(line.strip(),flush=True)
   if 'switching andesite' in line:switch=time.monotonic()
   if 'IMPORTER REGRESSION PASS' in line:passed=True
   if a.expect_hang and switch and time.monotonic()-switch>8:
    hung=True;break
finally:
 if process.poll() is None:
  if passed:
   try:process.wait(timeout=20)
   except subprocess.TimeoutExpired:process.kill()
  else:process.kill()
 process.wait()
print('pass=',passed,'expected_hang=',hung,'exit=',process.returncode)
raise SystemExit(0 if (passed and process.returncode==0 and not a.expect_hang) or (a.expect_hang and hung) else 1)
