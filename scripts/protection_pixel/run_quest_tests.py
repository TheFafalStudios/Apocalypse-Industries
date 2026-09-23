from pathlib import Path
import subprocess,time,threading,queue
root=Path(__file__).resolve().parents[2];test=root/'scripts/protection_pixel/build/quest-roundtrip-server'
java=root.parents[1]/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/java.exe'
# Prepare copies atomically so hardlinked libraries/mods are never modified in place.
import json,os,shutil
from quest_format import parse,canonical_counts
rows=json.loads((root/'docs/protection_pixel_quest_map.json').read_text(encoding='utf8'))
ids=json.loads((root/'scripts/protection_pixel/quest_ids.json').read_text())
script='var ppQuestExpected = '+json.dumps(rows)+';\nvar ppObjectIds = '+json.dumps(list(ids.values()))+';\n'+(root/'scripts/protection_pixel/quest_roundtrip.js').read_text()
(test/'kubejs/server_scripts/zz_pp_quest_roundtrip.js').write_text(script)
for src in list((root/'config/ftbquests/quests').rglob('*.snbt'))+list((root/'kubejs/data').rglob('*.json'))+[root/'mods/apocalypse-protection-pixel-integration-1.1.jar']:
 dst=test/src.relative_to(root);dst.parent.mkdir(parents=True,exist_ok=True)
 tmp=dst.with_suffix('.quest-test-tmp');shutil.copy2(src,tmp);os.replace(tmp,dst)
log=root/'docs/protection_pixel_quest_rework_runtime.log';q=queue.Queue()
p=subprocess.Popen([str(java),'-Xms2G','-Xmx5G','@libraries/net/neoforged/neoforge/21.1.248/win_args.txt','nogui'],cwd=test,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf8',errors='replace',bufsize=1)
def read():
 for line in p.stdout:q.put(line)
 q.put(None)
threading.Thread(target=read,daemon=True).start();start=time.time();stage=0;failed=False
with log.open('w',encoding='utf8') as out:
 while time.time()-start<900:
  try:line=q.get(timeout=1)
  except queue.Empty:continue
  if line is None:break
  out.write(line);out.flush()
  if 'Done (' in line and stage==0:
   print('Server ready; running FTB roundtrip test',flush=True);p.stdin.write('ppquesttest\n');p.stdin.flush();stage=1
  if 'PP QUEST ROUNDTRIP' in line:
   print(line.strip(),flush=True)
   if 'FAIL' in line:failed=True
   p.stdin.write('ppresonance selftest\n');p.stdin.flush();stage=2
  if stage==2 and ('Echo native plate:' in line or 'PP selftest' in line or 'PP SELFTEST' in line or 'PP integration selftest' in line):
   print(line.strip(),flush=True)
   if 'PASS' in line or 'passed' in line or 'FAIL' in line or 'failed' in line or 'Echo native plate:' in line:
    failed=failed or 'FAIL' in line or 'failed' in line or 'Echo native plate:' in line;p.stdin.write('stop\n');p.stdin.flush();stage=3
 else:
  failed=True;p.stdin.write('stop\n');p.stdin.flush()
try:p.wait(timeout=60)
except subprocess.TimeoutExpired:p.terminate();p.wait(timeout=20)
print('Test server stopped; stage',stage,'exit',p.returncode,'log',log,flush=True)
if failed or stage<3:raise SystemExit(1)

# Inspect what FTB actually saved, including effective item quantities and team scope.
for row in rows:
 chapter=parse((test/'config/ftbquests/quests/chapters'/(row['chapter']+'.snbt')).read_text(encoding='utf8'))
 quest=next(q for q in chapter['quests'] if q['id']==row['id'])
 assert canonical_counts(quest['tasks'])==canonical_counts(row['runtime_tasks']),row['key']+' saved tasks'
 assert canonical_counts(quest.get('rewards',[]))==canonical_counts(row['runtime_rewards']),row['key']+' saved rewards'
 assert not quest.get('can_repeat',False),row['key']+' repeatability'
print('PASS: all '+str(len(rows))+' saved task/reward definitions and nonrepeatable settings',flush=True)
