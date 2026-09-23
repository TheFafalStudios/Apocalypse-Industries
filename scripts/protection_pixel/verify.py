"""Static structural checks; native selftest separately validates actual recipe loading."""
from pathlib import Path
import re,json,hashlib,zipfile
ROOT=Path(__file__).resolve().parents[2]
checks=[]
def check(ok,name):
 if not ok:raise AssertionError(name)
 checks.append(name)
from quest_format import parse,canonical_counts
changes=json.loads((ROOT/'docs/protection_pixel_recipe_changes.json').read_text())
for c in changes:
 p=ROOT/'kubejs/data/protection_pixel/recipe'/(c['recipe'].split(':')[1]+'.json');d=json.loads(p.read_text())
 check(d==c['new'],c['recipe']+' matches generated audit')
 check('"fluid_stack"' not in json.dumps(d),c['recipe']+' modern fluid codec')
 if 'pattern' in d:
  symbols=set(''.join(d['pattern']))-{' '}
  check(symbols==set(d['key']),c['recipe']+' recipe symbol integrity')
  check(len(set(map(len,d['pattern'])))==1,c['recipe']+' rectangular pattern')
 for reason in c['reason']:
  if 'Post-lunar' in reason:check('creatingspace:moon_regolith' in json.dumps(d),c['recipe']+' lunar ingredient')
  if 'Electric magnetization' in reason:check('powergrid:magnet' in json.dumps(d),c['recipe']+' magnet ingredient')
echo=json.loads((ROOT/'kubejs/data/apocalypse_pp/recipe/echo_plate.json').read_text());flat=''.join(echo['pattern'])
check(flat.count('D')==6 and all(flat.count(c)==1 for c in 'ENTU'),'Echo plate material totals')
check(not any('helmet' in str(x) or 'chestplate' in str(x) or 'leggings' in str(x) or 'boots' in str(x) for x in echo['key'].values()),'Echo plate never consumes armor')
chapters=[parse(f.read_text(encoding='utf-8-sig')) for f in (ROOT/'config/ftbquests/quests/chapters').glob('*.snbt')]
quests=[q for c in chapters for q in c.get('quests',[])];ids=[q['id'] for q in quests]
check(len(ids)==len(set(ids)),'quest IDs unique across all chapters')
byid={q['id']:q for q in quests};mapping=json.loads((ROOT/'docs/protection_pixel_quest_map.json').read_text())
for row in mapping:
 q=byid[row['id']]
 check(q.get('optional') is True,row['key']+' optional')
 check(q.get('dependencies',[])==row['parent_ids'] and all(p in byid for p in row['parent_ids']),row['key']+' valid quest prerequisites')
 if row['chapter']=='special_requests':check(all(t.get('consume_items') is True for t in q['tasks']) and q.get('can_repeat') is False,row['key']+' consuming nonrepeatable contract')
visited=set();active=set()
def visit(k):
 if k in visited:return
 check(k not in active,'no dependency cycle '+k);active.add(k)
 for dep in byid[k].get('dependencies',[]):
  if dep in byid:visit(dep)
 active.remove(k);visited.add(k)
for row in mapping:visit(row['id'])
collisions=[]
for c in chapters:
 qs=c.get('quests',[])
 for q in qs:
  if q['id'] in {r['id'] for r in mapping}:
   for other in qs:
    if q['id']!=other['id'] and q.get('x',0)==other.get('x',0) and q.get('y',0)==other.get('y',0):collisions.append((q['id'],other['id']))
check(not collisions,'new quests do not overlap existing coordinates: '+str(collisions))

lang=parse((ROOT/'config/ftbquests/quests/lang/en_us.snbt').read_text(encoding='utf8'))
owned_ids=json.loads((ROOT/'scripts/protection_pixel/quest_ids.json').read_text(encoding='utf8'))
all_objects=[]
for quest in quests:
 all_objects += [quest]+quest.get('tasks',[])+quest.get('rewards',[])
object_ids=[o['id'] for o in all_objects]
check(len(object_ids)==len(set(object_ids)),'quest/task/reward IDs globally unique')
for key,id in owned_ids.items():
 check(0<int(id,16)<2**63 and object_ids.count(id)==1,key+' stable FTB signed-long ID')
for row in mapping:
 check(lang.get('quest.'+row['id']+'.title')==row['title'],row['key']+' exact linked title')
 check(lang.get('quest.'+row['id']+'.quest_desc')==row['text'],row['key']+' exact linked description')
 check(lang.get('quest.'+row['id']+'.quest_subtitle')==row['subtitle'],row['key']+' exact linked subtitle')
 for i,title in enumerate(row.get('task_titles',[])):
  check(lang.get('task.'+row['runtime_tasks'][i]['id']+'.title')==title,row['key']+' readable task label '+str(i))
 check(canonical_counts(byid[row['id']]['tasks'])==canonical_counts(row['runtime_tasks']),row['key']+' exact tasks and counts')
 check(canonical_counts(byid[row['id']].get('rewards',[]))==canonical_counts(row['runtime_rewards']),row['key']+' exact rewards and counts')
 check(not byid[row['id']].get('can_repeat',False),row['key']+' nonrepeatable')
 for reward in row['runtime_rewards']:
  check(reward.get('team_reward') is True,row['key']+' reward claimed once per team')
 check(not any(term in ' '.join(row['text']).lower() for term in ['pending measurement','balance is provisional','invalid samples','stale upper-mode','never a substitute for a damage limit']),row['key']+' player-facing text')
 if row.get('previous_id')!=row['id']:
  check('quest.'+row['previous_id']+'.title' not in lang,row['key']+' no orphan original translation')

print('PASS',len(checks),'static assertions;',len(quests),'total quests')
(ROOT/'docs/protection_pixel_static_tests.json').write_text(json.dumps({'passed':len(checks),'quest_count':len(quests),'checks':checks},indent=2)+'\n')
