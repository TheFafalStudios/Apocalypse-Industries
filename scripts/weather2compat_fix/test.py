"""Regression-test the actual addon helper with minimal binary-compatible storm fixtures.
Does not launch Minecraft or read any world. Also audits both packaged call sites.
"""
from pathlib import Path
import argparse,json,subprocess,zipfile
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--build',type=Path,required=True)
a=p.parse_args();out=a.build.resolve()
root=Path(__file__).resolve().parents[2]
java=root.parents[1]/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
test=out/'regression';test.mkdir()
src=test/'src';classes=test/'classes';src.mkdir();classes.mkdir()
fixtures={
'weather2/weathersystem/storm/WeatherObject.java':'package weather2.weathersystem.storm; public class WeatherObject {}',
'weather2/weathersystem/storm/StormObject.java':'package weather2.weathersystem.storm; public class StormObject extends WeatherObject { public boolean isDead; }',
'weather2/weathersystem/storm/WeatherObjectParticleStorm.java':'package weather2.weathersystem.storm; public class WeatherObjectParticleStorm extends WeatherObject {}',
'Regression.java':'''import java.util.*;
import weather2.weathersystem.storm.*;
import net.obf.weather2compat.hotfix.Weather2CompatSafety;
public class Regression {
 static void check(boolean ok,String why) {if(!ok) throw new AssertionError(why);}
 static class Tornado extends StormObject {}
 public static void main(String[] args) {
  StormObject rain=new StormObject(), dead=new StormObject();dead.isDead=true;
  Tornado tornado=new Tornado();
  WeatherObjectParticleStorm sand=new WeatherObjectParticleStorm(),snow=new WeatherObjectParticleStorm();
  List<WeatherObject> source=new ArrayList<>(Arrays.asList(sand,rain,snow,dead,tornado,null,new WeatherObject()));
  Iterator<?> snapshot=Weather2CompatSafety.safeIterator(source);
  check(source.size()==7 && source.get(0)==sand && source.get(2)==snow,"must preserve source weather events");
  source.clear(); // Earlier concurrent-modification fix must still hold.
  List<StormObject> result=new ArrayList<>();
  while(snapshot.hasNext()) result.add((StormObject)snapshot.next());
  check(result.equals(Arrays.asList(rain,dead,tornado)),"keep normal storms, order, subclasses and original dead-state handling");
  check(!Weather2CompatSafety.safeIterator(List.of(sand,snow)).hasNext(),"particle-only list");
  check(!Weather2CompatSafety.safeIterator(List.of()).hasNext(),"empty list");
  check(Weather2CompatSafety.safeIterator(List.of(rain)).next()==rain,"normal weather unchanged");
  System.out.println("PASS: mixed storms, particle-only, empty, normal, subclass/dead preservation, source preservation, stable snapshot");
 }
}'''}
for n,s in fixtures.items():
 f=src/n;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s)
artifact=out/'apocalypse-weather2compat-threadsafe-fix-1.0.1.jar'
subprocess.run([str(java/'javac.exe'),'--release','21','-proc:none','-cp',str(artifact),'-d',str(classes)]+[str(src/n) for n in fixtures],check=True)
run=subprocess.run([str(java/'java.exe'),'-Xverify:all','-cp',str(classes)+';'+str(artifact),'Regression'],capture_output=True,text=True)
print(run.stdout,run.stderr);run.check_returncode()
old=Path(json.loads((out/'build.json').read_text()).get('baseline',str(root/'mods/apocalypse-weather2compat-threadsafe-fix-1.0.0.jar')))
if not old.exists(): old=out/'rollback/apocalypse-weather2compat-threadsafe-fix-1.0.0.jar'
negative=subprocess.run([str(java/'java.exe'),'-Xverify:all','-cp',str(classes)+';'+str(old),'Regression'],capture_output=True,text=True)
assert negative.returncode!=0 and 'ClassCastException' in negative.stderr,negative.stderr
print('PASS: old addon fails the same mixed-storm regression with ClassCastException')
with zipfile.ZipFile(old) as z,zipfile.ZipFile(artifact) as fixed:
 changed=[n for n in z.namelist() if z.read(n)!=fixed.read(n)]
 assert sorted(changed)==sorted(['META-INF/neoforge.mods.toml','net/obf/weather2compat/hotfix/Weather2CompatSafety.class']),changed
 replacement='META-INF/apocalypse-patches/net/obf/weather2compat/weather2/Weather2Utils.class'
 f=test/'Weather2Utils.class';f.write_bytes(fixed.read(replacement))
bytecode=subprocess.check_output([str(java/'javap.exe'),'-c','-p',str(f)],text=True)
methods={};current=None
for line in bytecode.splitlines():
 if line.startswith('  ') and not line.startswith('    ') and '(' in line and line.endswith(';'):
  current=line;methods[current]=[]
 elif current:methods[current].append(line)
callers=[k for k,v in methods.items() if any('invokestatic' in l and 'Weather2CompatSafety.safeIterator' in l for l in v)]
assert len(callers)==2 and any('isStormAbove' in x for x in callers) and any('getPrecipitationStrength' in x for x in callers),callers
report={'result':'PASS','runtime':'Java 21 -Xverify:all, minimal storm fixtures; no Minecraft gameplay test','old_addon_negative_control':'ClassCastException as expected','changed_existing_entries':changed,'guarded_methods':callers,'stdout':run.stdout.strip()}
(out/'regression.json').write_text(json.dumps(report,indent=2)+'\n')
print('PASS: both rain and precipitation loops use the fixed helper; all other existing addon entries unchanged')
