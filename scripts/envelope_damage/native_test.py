from pathlib import Path
import subprocess,zipfile,shutil,os,time,threading,queue,json,hashlib,argparse
p=argparse.ArgumentParser();p.add_argument('--output',type=Path,required=True);p.add_argument('--attempt',default='1');a=p.parse_args();r=a.output.resolve()
live=Path(__file__).resolve().parents[2];launcher=live.parents[1];lib=launcher/'servers/Chunkserve - Apocalypse Industries/libraries';java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
if r==live or live in r.parents or not (r/'build.json').is_file():raise SystemExit('Expected isolated build output')
test=r/('native-server-'+a.attempt);test.mkdir();classes=r/('test-classes-'+a.attempt);classes.mkdir()
cp=(r/'classpath.txt').read_text().split(os.pathsep)
for base,dirs,files in os.walk(lib,followlinks=False):
 for n in dirs+files:
  f=Path(base)/n
  if f.is_symlink() or getattr(f.lstat(),'st_file_attributes',0)&1024:raise SystemExit('Linked library path')
 for n in files:
  f=Path(base)/n;dst=test/'libraries'/f.relative_to(lib);dst.parent.mkdir(parents=True,exist_ok=True);os.link(f,dst)
mods=[live/'mods'/n for n in ['create-1.21.1-6.0.10.jar','weather2-neoforge-1.21.0-2.8.7.jar','tornadophysics-1.0.3.jar','coroutil-neoforge-1.21.0-1.3.9.jar','sable-neoforge-1.21.1-2.0.3.jar','create-aeronautics-bundled-1.21.1-1.3.0.jar','apocalypse-wind-height-0.1.0-prototype.jar']]+[r/'apocalypse-envelope-damage-1.0.0+mc1.21.1.jar']
(test/'mods').mkdir()
for m in mods:shutil.copyfile(m,test/'mods'/m.name)
(test/'config').mkdir();shutil.copyfile(live/'config/tornadophysics-common.toml',test/'config/tornadophysics-common.toml');shutil.copyfile(live/'config/apocalypse-envelope-damage-common.toml',test/'config/apocalypse-envelope-damage-common.toml')
cp.append(r/'apocalypse-envelope-damage-1.0.0+mc1.21.1.jar')
src=Path(__file__).resolve().parent/'tests/ModStartupCheck.java'
args=['--release','21','-proc:none','-classpath',os.pathsep.join(map(str,cp)),'-d',str(classes),str(src)]
arg=r/'test-javac.args';arg.write_text('\n'.join('"'+str(s).replace('\\','/')+'"' for s in args));subprocess.run([str(java/'javac.exe'),'@'+str(arg)],check=True)
with zipfile.ZipFile(test/'mods/mod-startup-check.jar','w',zipfile.ZIP_DEFLATED) as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes).as_posix())
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="mod_startup_check"\nversion="1"\ndisplayName="Disposable verification"\n')
(test/'eula.txt').write_text('eula=true\n')
(test/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25592\nonline-mode=false\nlevel-name=disposable-test-world\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=120000\n')
proc=subprocess.Popen([str(java/'java.exe'),'-Dmixin.debug.export=true','-Xms512M','-Xmx3G','@libraries/net/neoforged/neoforge/21.1.248/win_args.txt','nogui'],cwd=test,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf8',errors='replace')
q=queue.Queue()
def reader():
 for line in proc.stdout:q.put(line)
 q.put(None)
threading.Thread(target=reader,daemon=True).start();start=time.time();passed=False;fail=False
with (r/('native-validation-'+a.attempt+'.log')).open('w',encoding='utf8') as log:
 while time.time()-start<360:
  try:line=q.get(timeout=1)
  except queue.Empty:continue
  if line is None:break
  log.write(line);log.flush()
  if 'MOD STARTUP' in line or 'ERROR' in line or 'FATAL' in line:print(line.strip(),flush=True)
  if 'MOD STARTUP PASS' in line:passed=True
  if 'MOD STARTUP FAIL' in line:fail=True
 else:proc.kill();fail=True
 try:proc.wait(timeout=20)
 except subprocess.TimeoutExpired:proc.kill();proc.wait();fail=True
result={'startup_passed':passed and not fail and proc.returncode==0,'exit_code':proc.returncode,'scope':'Dedicated server, actual Mixin local captures and real Aeronautics envelope blocks on assembled Sable ships in a disposable flat world. Automated puncture/geometry/cooldown checks; flight feel, containment pressure and full pack gameplay not tested.','mods':{m.name:hashlib.sha256(m.read_bytes()).hexdigest() for m in mods}}
(r/('NATIVE_VALIDATION-'+a.attempt+'.json')).write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result),flush=True)
if not result['startup_passed']:raise SystemExit(1)
exported=test/'.mixin.out/class/net/killey/tornadophysics/event/TornadoEvent.class'
disassembly=subprocess.check_output([str(java/'javap.exe'),'-c','-p',str(exported)],text=True)
(r/'transformed-TornadoEvent.txt').write_text(disassembly)
assert 'envelopeTag' in disassembly and 'envelopeId' in disassembly
