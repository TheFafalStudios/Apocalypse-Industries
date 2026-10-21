"""Isolated runtime/assembled-sublevel fixture; never uses existing saves or server processes."""
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess,zipfile
from build import ROOT,LAUNCHER,JAVA,JAR
p=argparse.ArgumentParser();p.add_argument('--build',required=True,type=Path);p.add_argument('--output',required=True,type=Path)
p.add_argument('--world-test',action='store_true');p.add_argument('--baseline',action='store_true');a=p.parse_args()
out=a.output.resolve()
if ROOT/'.local' not in out.parents:raise ValueError('Use a fresh .local isolated test directory')
out.mkdir(parents=True,exist_ok=False);(out/'mods').mkdir();(out/'config').mkdir()
(out/'eula.txt').write_text('eula=true\n')
(out/'server.properties').write_text('level-name=ballast-audit-world\nlevel-seed=104729\nserver-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nmax-tick-time=-1\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\nlevel-type=minecraft:flat\ngenerator-settings={"layers":[],"biome":"minecraft:plains"}\n')
for pattern in ['create-1.21.1-*.jar','create_submarine-3.4.4.jar','sable-neoforge-1.21.1-2.0.3.jar','create-aeronautics-bundled-1.21.1-1.3.2.jar','Design-n-Decor-*.jar','apocalypse-submarine-balance-1.0.0.jar']:
    paths=list((ROOT/'mods').glob(pattern));assert len(paths)==1,(pattern,paths);shutil.copy2(paths[0],out/'mods'/paths[0].name)
for name in [JAR,'ballast-native-harness.jar']:shutil.copy2(a.build/name,out/'mods'/name)
if a.baseline:
    # Negative control: exact production classes with only this addon's mixin registration disabled.
    target=out/'mods'/JAR
    with zipfile.ZipFile(target) as z:entries={n:z.read(n) for n in z.namelist()}
    config=json.loads(entries['apocalypse-lava-ballast.mixins.json']);config['mixins']=[]
    entries['apocalypse-lava-ballast.mixins.json']=json.dumps(config).encode()
    with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
        for name,data in entries.items():z.writestr(name,data)
for name in ['submarine_hull.json','create_submarine-server.toml']:shutil.copy2(ROOT/'config'/name,out/'config'/name)
lib=LAUNCHER/'servers/Chunkserve - Apocalypse Industries/libraries';q=lambda v:"'"+str(v).replace("'","''")+"'"
subprocess.run(['powershell','-NoProfile','-Command','New-Item -ItemType Junction -Path '+q(out/'libraries')+' -Target '+q(lib)+' | Out-Null'],check=True)
(out/'context.json').write_text(json.dumps({'method':'ballast-native-v1','worldTest':a.world_test,'baseline':a.baseline,'candidateSha256':hashlib.sha256((a.build/JAR).read_bytes()).hexdigest(),'testedJarSha256':hashlib.sha256((out/'mods'/JAR).read_bytes()).hexdigest(),'harnessSha256':hashlib.sha256((out/'mods/ballast-native-harness.jar').read_bytes()).hexdigest()},indent=2)+'\n')
with (out/'console.log').open('w') as log:
    process=subprocess.Popen([str(JAVA/'java.exe'),'-Xms512M','-Xmx3G','-Dapocalypse.ballast.worldTest='+str(a.world_test).lower(),'-Dapocalypse.ballast.baseline='+str(a.baseline).lower(),'@libraries/net/neoforged/neoforge/21.1.248/win_args.txt','nogui'],cwd=out,stdout=log,stderr=subprocess.STDOUT)
print(json.dumps({'pid':process.pid,'output':str(out),'isolated':True}),flush=True)
try:code=process.wait(timeout=300)
except subprocess.TimeoutExpired:
    process.terminate();process.wait(timeout=30);raise SystemExit('Native fixture timed out; only its own process was terminated')
report=out/('ballast-world-validation.json' if a.world_test else 'ballast-native-validation.json')
assert code==0 and report.exists(),'Native fixture failed; inspect '+str(out/'console.log')
data=json.loads(report.read_text());assert data['pass'];print(json.dumps(data,indent=2))
