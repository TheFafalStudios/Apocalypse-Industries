from pathlib import Path
import subprocess,zipfile,shutil,os,time,threading,queue,json,hashlib,argparse
parser=argparse.ArgumentParser();parser.add_argument('--output',type=Path,required=True);r=parser.parse_args().output.resolve();live=Path(__file__).resolve().parents[2];
if r==live or live in r.parents or not (r/'build.json').is_file():raise SystemExit('Expected isolated prototype build output')
launcher=live.parents[1];lib=launcher/'servers/Chunkserve - Apocalypse Industries/libraries';java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin';test=r/'server-startup-retry1';test.mkdir();deps=r/'test-deps-retry1';deps.mkdir();classes=r/'test-classes-retry1';classes.mkdir()
cp=[launcher/'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',launcher/'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar']
assert not lib.is_symlink() and not lib.is_junction()
for base,dirs,files in os.walk(lib,followlinks=False):
 for n in dirs+files:
  f=Path(base)/n
  assert not f.is_symlink() and not (getattr(f.lstat(),'st_file_attributes',0)&1024),str(f)
 for n in files:
  f=Path(base)/n;dst=test/'libraries'/f.relative_to(lib);dst.parent.mkdir(parents=True,exist_ok=True);os.link(f,dst)
  if n.endswith('.jar'):cp.append(f)
mods=[live/'mods'/n for n in ['create-1.21.1-6.0.10.jar','weather2-neoforge-1.21.0-2.8.7.jar','tornadophysics-1.0.3.jar','coroutil-neoforge-1.21.0-1.3.9.jar','sable-neoforge-1.21.1-2.0.3.jar']]+[r/'apocalypse-wind-height-0.1.0-prototype.jar']
(test/'mods').mkdir()
for m in mods:
 shutil.copyfile(m,test/'mods'/m.name);cp.append(m)
 with zipfile.ZipFile(m) as z:
  assert z.testzip() is None
  for n in z.namelist():
   if n.endswith('.jar'):dst=deps/Path(n).name;dst.write_bytes(z.read(n));cp.append(dst)
src=r/'ModStartupCheck.java';src.write_text('''package apocalypse.verify;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.core.registries.BuiltInRegistries;
@Mod("mod_startup_check")
public class ModStartupCheck {
 public ModStartupCheck() { NeoForge.EVENT_BUS.addListener(this::check); }
 private void check(ServerStartedEvent e) {
  try {
   var target=Class.forName("net.killey.tornadophysics.logic.WindPhysics");
   boolean redirected=java.util.Arrays.stream(target.getDeclaredMethods()).anyMatch(m->m.getName().contains("positionalWind"));
   if(!redirected) throw new AssertionError("Mixin handler missing");
   var manager=new weather2.weathersystem.WeatherManagerServer(e.getServer().overworld());
   var wind=manager.getWindManager();
   wind.windSpeedGlobal=0.2f;wind.windTimeGust=0;wind.windTimeEvent=0;
   float global=wind.getWindSpeed();
   float low=wind.getWindSpeed(new net.minecraft.core.BlockPos(0,-63,0));
   float high=wind.getWindSpeed(new net.minecraft.core.BlockPos(0,250,0));
   if(Math.abs(global-0.2f)>0.00001f || high<=low || high>1) throw new AssertionError("Invalid positional response "+low+" "+high);
   System.out.println("MOD STARTUP PASS mixin=true global="+global+" low="+low+" high="+high);

  } catch(Throwable t) {System.out.println("MOD STARTUP FAIL "+t);t.printStackTrace();}
  finally {e.getServer().halt(false);}
 }
}
''')
args=['--release','21','-proc:none','-classpath',os.pathsep.join(map(str,cp)),'-d',str(classes),str(src)];arg=r/'test-javac.args';arg.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(java/'javac.exe'),'@'+str(arg)],check=True)
with zipfile.ZipFile(test/'mods/mod-startup-check.jar','w',zipfile.ZIP_DEFLATED) as z:
 for f in classes.rglob('*.class'):z.write(f,f.relative_to(classes).as_posix())
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="mod_startup_check"\nversion="1"\ndisplayName="Disposable startup verification"\n')
(test/'eula.txt').write_text('eula=true\n');(test/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25591\nonline-mode=false\nlevel-name=disposable-test-world\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[{"block":"minecraft:bedrock","height":1}],"biome":"minecraft:plains"}\ngenerate-structures=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=60000\n')
p=subprocess.Popen([str(java/'java.exe'),'-Dmixin.debug.export=true','-Xms512M','-Xmx3G','@libraries/net/neoforged/neoforge/21.1.248/win_args.txt','nogui'],cwd=test,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf8',errors='replace');q=queue.Queue()
def reader():
 for line in p.stdout:q.put(line)
 q.put(None)
threading.Thread(target=reader,daemon=True).start();start=time.time();passed=False;fail=False
with (r/'native-startup-retry1.log').open('w',encoding='utf8') as log:
 while time.time()-start<240:
  try:line=q.get(timeout=1)
  except queue.Empty:continue
  if line is None:break
  log.write(line);log.flush()
  if 'MOD STARTUP' in line or 'ERROR' in line or 'FATAL' in line:print(line.strip(),flush=True)
  if 'MOD STARTUP PASS' in line:passed=True
  if 'MOD STARTUP FAIL' in line:fail=True
 else:p.kill();fail=True
 try:p.wait(timeout=20)
 except subprocess.TimeoutExpired:p.kill();p.wait();fail=True
result={'startup_passed':passed and not fail and p.returncode==0,'exit_code':p.returncode,'scope':'Dedicated server mod loading, actual Mixin application, actual Weather2 positional speed at low/high altitude on a disposable flat world. Ship motion and full pack gameplay not tested.','mods':{m.name:hashlib.sha256(m.read_bytes()).hexdigest() for m in mods}}
(r/'NATIVE_VALIDATION.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result),flush=True)
if not result['startup_passed']:raise SystemExit(1)

exported=test/'.mixin.out/class/net/killey/tornadophysics/logic/WindPhysics.class'
disassembly=subprocess.check_output([str(java/'javap.exe'),'-c','-p',str(exported)],text=True)
(r/'transformed-WindPhysics.txt').write_text(disassembly)
assert 'WindManager.getWindSpeed:()F' not in disassembly
assert disassembly.count('WindManager.getWindSpeed:(Lnet/minecraft/core/BlockPos;)F')==1
assert 'invokestatic' in disassembly and 'positionalWind' in disassembly
print('Transformed wind call inspection passed')
