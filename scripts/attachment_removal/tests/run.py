from pathlib import Path
import subprocess,zipfile,shutil,os,time,threading,queue,sys,argparse
parser=argparse.ArgumentParser(description='Run attachment regression tests in a new disposable server. No live worlds are read.')
parser.add_argument('--build',type=Path,required=True)
parser.add_argument('--attempt',default='regression')
options=parser.parse_args()
H=options.build.resolve()
if not (H/'build.json').is_file():parser.error('Expected a built addon directory')
if not options.attempt.replace('-','').replace('_','').isalnum():parser.error('Invalid attempt name')
ROOT=Path(__file__).resolve().parents[3]
LAUNCHER=ROOT.parents[1]
JAVA=LAUNCHER/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
args=(H/'javac.args').read_text().splitlines()
with zipfile.ZipFile(ROOT/'mods/create-1.21.1-6.0.10.jar') as z:
 for name in z.namelist():
  if name.endswith('.jar'):
   dep=H/'deps'/Path(name).name;dep.write_bytes(z.read(name));ci=args.index('"-classpath"')+1;args[ci]=args[ci][:-1]+';'+dep.as_posix()+'"'
args=args[:args.index('"-d"')]+['"-d"','"'+(H/'test-classes').as_posix()+'"','"'+(ROOT/'scripts/attachment_removal/tests/AttachmentRegression.java').as_posix()+'"']
ci=args.index('"-classpath"')+1
args[ci]=args[ci][:-1]+';'+(H/'apocalypse-attachment-removal-1.0.0+mc1.21.1.jar').as_posix()+';'+(ROOT/'mods/geckolib-neoforge-1.21.1-4.9.2.jar').as_posix()+'"'
(H/'test-classes').mkdir(exist_ok=True)
(H/'test-javac.args').write_text('\n'.join(args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(H/'test-javac.args')],check=True)
with zipfile.ZipFile(H/'attachment-regression.jar','w',zipfile.ZIP_DEFLATED) as z:
 for f in (H/'test-classes').rglob('*.class'):z.write(f,f.relative_to(H/'test-classes').as_posix())
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="attachment_regression"\nversion="1"\ndisplayName="Gunsmithing attachment regression tests"\n')
mode=options.attempt
test=H/('server-'+mode)
if test.exists():parser.error('Attempt already exists; select a new --attempt')
test.mkdir()
# Traverse only runtime libraries; reject all reparse points.
lib=LAUNCHER/'servers/Chunkserve - Apocalypse Industries/libraries'
if lib.is_symlink() or getattr(lib.lstat(),'st_file_attributes',0)&1024:raise RuntimeError('Linked library root')
for base,dirs,files in os.walk(lib,followlinks=False):
 dirs[:]=[d for d in dirs if not (Path(base)/d).is_symlink() and not ((Path(base)/d).stat().st_file_attributes & 1024)]
 for name in files:
  f=Path(base)/name
  if f.is_symlink() or f.stat().st_file_attributes & 1024:raise RuntimeError('Linked library: '+str(f))
  dst=test/'libraries'/f.relative_to(lib);dst.parent.mkdir(parents=True,exist_ok=True)
  if not dst.exists():os.link(f,dst)
(test/'mods').mkdir(exist_ok=True)
for name in ['create-1.21.1-6.0.10.jar','ntgl-1.21.1-3.1.8.jar','create-gunsmithing-1.21.1-1.4.9.jar','geckolib-neoforge-1.21.1-4.9.2.jar']:
 shutil.copy2(ROOT/'mods'/name,test/'mods'/name)
shutil.copy2(H/'attachment-regression.jar',test/'mods/attachment-regression.jar')
shutil.copy2(H/'apocalypse-attachment-removal-1.0.0+mc1.21.1.jar',test/'mods/apocalypse-attachment-removal-1.0.0+mc1.21.1.jar')
shutil.copy2(ROOT/'mods/apocalypse-gunsmithing-enchantments-1.1.0+mc1.21.1.jar',test/'mods/apocalypse-gunsmithing-enchantments-1.1.0+mc1.21.1.jar')
(test/'eula.txt').write_text('eula=true\n')
(test/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25590\nonline-mode=false\nlevel-name=disposable-test-world\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=60000\n')
p=subprocess.Popen([str(JAVA/'java.exe'),'-Xms512M','-Xmx3G','-Dmixin.debug.export=true','@libraries/net/neoforged/neoforge/21.1.248/win_args.txt','nogui'],cwd=test,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf8',errors='replace')
q=queue.Queue()
def reader():
 for line in p.stdout:q.put(line)
 q.put(None)
threading.Thread(target=reader,daemon=True).start()
start=time.time();passed=False;failed=False
with (H/(mode+'.log')).open('w',encoding='utf8') as log:
 while time.time()-start<180:
  try:line=q.get(timeout=1)
  except queue.Empty:line=''
  if line is None:break
  log.write(line);log.flush()
  if 'ATTACHMENT ' in line or 'ERROR' in line or 'FATAL' in line:print(line.strip(),flush=True)
  if 'ATTACHMENT REGRESSION PASS checks=' in line:passed=True
  if 'ATTACHMENT REGRESSION FAIL' in line or 'Error saving' in line or 'will not persist' in line:failed=True
 else:p.kill();failed=True
 try:p.wait(timeout=20)
 except subprocess.TimeoutExpired:p.kill();p.wait()
print('RESULT',mode,'pass',passed,'exit',p.returncode,flush=True)
sys.exit(0 if passed and not failed and p.returncode==0 else 1)
