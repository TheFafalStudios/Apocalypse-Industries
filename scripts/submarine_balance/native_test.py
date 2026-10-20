"""Load only the submarine dependency stack and exit before any server world starts."""
from pathlib import Path
import argparse,json,shutil,subprocess,zipfile
from build import ROOT,LAUNCHER,JAVA
p=argparse.ArgumentParser();p.add_argument('--build',required=True,type=Path);p.add_argument('--output',required=True,type=Path);a=p.parse_args()
out=a.output.resolve();out.mkdir(parents=True,exist_ok=False);(out/'mods').mkdir();(out/'config').mkdir()
(out/'eula.txt').write_text('eula=true\n')
(out/'server.properties').write_text('level-name=unused-audit-world\nserver-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nmax-tick-time=-1\n')
for pattern in ['create-1.21.1-*.jar','create_submarine-3.4.4.jar','sable-neoforge-1.21.1-2.0.3.jar','create-aeronautics-bundled-1.21.1-1.3.2.jar','Design-n-Decor-*.jar']:
    matches=list((ROOT/'mods').glob(pattern));assert len(matches)==1,(pattern,matches)
    shutil.copy2(matches[0],out/'mods'/matches[0].name)
for name in ['apocalypse-submarine-balance-1.0.0.jar','balance-native-harness.jar']:
    shutil.copy2(a.build/name,out/'mods'/name)
for name in ['submarine_hull.json','create_submarine-server.toml']:
    shutil.copy2(ROOT/'config'/name,out/'config'/name)
lib=LAUNCHER/'servers/Chunkserve - Apocalypse Industries/libraries'
q=lambda s:"'"+str(s).replace("'","''")+"'"
subprocess.run(['powershell','-NoProfile','-Command','New-Item -ItemType Junction -Path '+q(out/'libraries')+' -Target '+q(lib)+' | Out-Null'],check=True)
with (out/'console.log').open('w') as log:
    process=subprocess.Popen([str(JAVA/'java.exe'),'-Xms512M','-Xmx3G','@libraries/net/neoforged/neoforge/21.1.248/win_args.txt','nogui'],cwd=out,stdout=log,stderr=subprocess.STDOUT)
print(json.dumps({'pid':process.pid,'output':str(out),'stop':'Test harness exits at FMLLoadComplete before any world start'}),flush=True)
try:code=process.wait(timeout=240)
except subprocess.TimeoutExpired:
    process.terminate();process.wait(timeout=30);raise SystemExit('Native mod-load test timed out')
report=out/'balance-native-validation.json'
assert code==0 and report.exists(),'Native load failed; inspect '+str(out/'console.log')
data=json.loads(report.read_text());assert data['pass'];print(json.dumps(data,indent=2))
