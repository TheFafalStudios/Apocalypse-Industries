"""Audit both preparations against the final install plans; run the new sea-level GPU fixture."""
import argparse, hashlib, json, re, subprocess, sys, tomllib, zipfile
from pathlib import Path
from consolidate_v14_release import ROOT, BASE, PREP, SHADER_PREP, SHADER, nether_path, sha, save
from release_safety import excluded

p=argparse.ArgumentParser(description=__doc__); p.add_argument('--preparation',required=True,type=Path)
a=p.parse_args(); out=a.preparation.resolve(); c=out/'github-candidate'; build=out/'addon-validation'
idx={e['file']:e for e in tomllib.loads((c/'index.toml').read_text())['files']}
old={e['file']:e for e in tomllib.loads((BASE/'index.toml').read_text())['files']}
shader=json.loads((SHADER_PREP/'manifest.json').read_text())
shader_changes={e['path'] for e in shader['changes']}
changes=[]
for r in sorted(old.keys()|idx.keys()):
 if old.get(r,{}).get('hash')!=idx.get(r,{}).get('hash'):
  assert nether_path(r) or r in shader_changes or r.startswith(('config/euphoria_patcher/','config/spark/')) or (r not in idx and excluded(r)), 'Unrelated runtime change: '+r
  changes.append({'path':r,'before':old.get(r,{}).get('hash'),'after':idx.get(r,{}).get('hash')})
save(out/'CLIENT_CHANGES.json',changes)
plans={}; local_downloads=[]
for side in ['client','server']:
 destinations={}
 for r,e in idx.items():
  assert not any(x in r.lower() for x in ['apocalypse-performance','endgame_prototype','config/axiom/','euphoria_patcher/'])
  if e.get('metafile'):
   m=tomllib.loads((c/r).read_text(encoding='utf-8-sig')); target='mods/'+m['filename']
   if m.get('side','both') not in ['both',side]:continue
   if 'raw.githubusercontent.com/TheFafalStudios/Apocalypse-Industries/main/' in m['download'].get('url',''):
    f=c/target; assert f.is_file(),target
    assert hashlib.new(m['download']['hash-format'].replace('-',''),f.read_bytes()).hexdigest()==m['download']['hash'],target
    local_downloads.append(target)
  else:
   target=r
   if side=='server' and r.startswith('shaderpacks/'):continue
  assert target not in destinations,target
  destinations[target]=r
 assert ('mods/apocalypse-submarine-lava-visuals-1.1.2.jar' in destinations)==(side=='client')
 assert ('mods/aero-shadow-fix-2.1.0.jar' in destinations)==(side=='client')
 for r in ['mods/apocalypse-nether-islands-0.9.0.jar','mods/apocalypse-submarine-balance-1.0.0.jar']:
  assert r in destinations
 plans[side]={'destinations':len(destinations),'clientRenderingAddonsCorrectlyScoped':True}
for r in ['config/iris.properties', SHADER+'.txt']:assert idx[r]['preserve']
assert 'EuphoriaPatcher' not in '\n'.join(idx)
assert tomllib.loads((c/'config/create_submarine-server.toml').read_text())['hullStrength']['pressureModel']=='HYBRID'
server={}
for target in ['twin','chunkserve']:
 r=out/(target+'-record'); rows=json.loads((r/'CHANGES.json').read_text())['changes']
 verified=0
 for line in (r/'SHA256SUMS.txt').read_text().splitlines():
  expected,name=line.split('  ',1);assert sha(r/name)==expected,name;verified+=1
 with zipfile.ZipFile(next(r.glob('*.zip'))) as z:
  assert z.testzip() is None
  for e in rows:
   name=e['path']
   if e['before_sha256']:assert sha(r/'ROLLBACK'/name)==e['before_sha256'],name
   if e['after_sha256']:
    assert hashlib.sha256(z.read(name)).hexdigest()==e['after_sha256'],name
    if (c/name).is_file() and name!='config/submarine_hull.json':assert sha(c/name)==e['after_sha256'],name
   else:assert name not in z.namelist()
  assert tomllib.loads(z.read('config/create_submarine-server.toml').decode())['hullStrength']['pressureModel']=='HYBRID'
 server[target]={'changes':len(rows),'recordChecksumsVerified':verified,'rollbackAndPacketHashesVerified':True,'hybridVerifiedInPacket':True}
# Compile and execute the actual final shader helper, plus its complete deferred fragment.
with zipfile.ZipFile(c/SHADER) as z:
 def expand(name):
  return re.sub(r'^\s*#include\s+"([^"]+)"',lambda m:expand('shaders/'+m[1].lstrip('/')),z.read(name).decode(),flags=re.M)
 (build/'gap.glsl').write_bytes(z.read('shaders/lib/atmospherics/fog/netherGapDistance.glsl'))
 (build/'old-gap.glsl').write_text((build/'gap.glsl').read_text().replace('100.0','32.0'))
 (build/'deferred.glsl').write_text(expand('shaders/world-1/deferred1.fsh'),encoding='utf-8')
cp=(build/'classpath.txt').read_text();java=ROOT.parents[1]/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
source=ROOT/'scripts/submarine_lava/tests/NetherSeaRegression.java'
def run(exe,values,name):
 f=build/(name+'.args');f.write_text('\n'.join('"'+str(x).replace('\\','/')+'"' for x in values))
 return subprocess.run([str(java/exe),'@'+str(f)],capture_output=True,text=True)
compiled=run('javac.exe',['--release','21','-proc:none','-cp',cp,'-d',build/'classes',source],'compile-sea')
assert compiled.returncode==0,compiled.stdout+compiled.stderr
def sea(helper,name):return run('java.exe',['-cp',cp,'dev.apocalypse.submarinelava.NetherSeaRegression',helper,build/'deferred.glsl'],name)
negative=sea(build/'old-gap.glsl','old-sea');assert negative.returncode!=0 and 'Gap distance at camera y=40.0' in negative.stderr,negative.stdout+negative.stderr
positive=sea(build/'gap.glsl','sea');assert positive.returncode==0,positive.stdout+positive.stderr
(build/'sea-gpu.log').write_text(positive.stdout+positive.stderr,encoding='utf-8')
save(out/'FINAL_VALIDATION.json',{'status':'static-and-offline-GPU-passed; gameplay-and-deployment-unverified',
 'method':'cross-preparation-install-plan-and-sea-level-v1','indexedFiles':len(idx),'logicalClientChanges':len(changes),
 'installPlans':plans,'repositoryHostedDownloadHashesVerified':len(set(local_downloads)),
 'servers':server,'seaGpu':positive.stdout.strip(),'oldY32FixtureRejected':True,
 'excluded':['unrelated WIP settings','performance recorder','unfinished endgame','world/schematic/LOD storage'],
 'gates':['combined multiplayer/fresh-and-update gameplay unverified','moving submarine/jackets/fluid transitions unverified','visual acceptance and performance unverified','server snapshots must be rechecked for drift before deployment']})
print((out/'FINAL_VALIDATION.json').read_text())
