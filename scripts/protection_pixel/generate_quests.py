"""Update owned PP quest fields from the manifest, preserving identities and layout.

Missing quests or unexpected task/reward IDs fail rather than destroy saved progress.
--check performs a read-only idempotence check.
"""
from pathlib import Path
import json,re,sys
from quest_format import parse,canonical_counts
ROOT=Path(__file__).resolve().parents[2]
BASE=ROOT/'config/ftbquests/quests'
rows=json.loads((ROOT/'docs/protection_pixel_quest_map.json').read_text(encoding='utf8'))
ids=json.loads((Path(__file__).parent/'quest_ids.json').read_text())
assert len(set(ids.values()))==len(ids) and all(0<int(v,16)<2**63 for v in ids.values())
def integers(v):
 if isinstance(v,float) and v.is_integer():return int(v)
 if isinstance(v,dict):return {k:integers(x) for k,x in v.items()}
 if isinstance(v,list):return [integers(x) for x in v]
 return v
def field(block,key,value):
 # Existing FTB files indent root quest fields at three tabs. Child fields are deeper.
 pat=r'^\t\t\t'+re.escape(key)+r':[^\n]*(?:\n(?!\t\t\t[A-Za-z_][A-Za-z_0-9]*:|\t\t\}(?:\n|$))[^\n]*)*'
 line='\t\t\t'+key+': '+json.dumps(integers(value),ensure_ascii=False)
 if re.search(pat,block,re.M):return re.sub(pat,lambda _:line,block,count=1,flags=re.M)
 return block.replace('\n','\n'+line+'\n',1)
files={}
for chapter in dict.fromkeys(r['chapter'] for r in rows):
 path=BASE/'chapters'/(chapter+'.snbt');s=path.read_text(encoding='utf8')
 for row in (r for r in rows if r['chapter']==chapter):
  assert ids[row['key']]==row['id']
  matches=[m for m in re.finditer(r'^\t\t\{\n[\s\S]*?^\t\t\}',s,re.M) if parse(m[0]).get('id')==row['id']]
  assert len(matches)==1,'Missing/duplicate quest: '+row['title']
  m=matches[0];obj=parse(m[0]);new=m[0]
  for key in ('tasks','rewards'):
   desired=row['runtime_'+key]
   assert all(x['id'] in ids.values() for x in desired),'Unpinned object ID'
   assert all(x['id'] in {d['id'] for d in desired} for x in obj.get(key,[])),'Refusing to remove live '+key
   if canonical_counts(obj.get(key,[]))!=canonical_counts(desired):new=field(new,key,desired)
  if obj.get('dependencies',[])!=row['parent_ids']:new=field(new,'dependencies',row['parent_ids'])
  check=parse(new)
  assert check['id']==obj['id'] and check.get('x')==obj.get('x') and check.get('y')==obj.get('y')
  s=s[:m.start()]+new+s[m.end():]
 parse(s);files[path]=s
path=BASE/'lang/en_us.snbt';s=path.read_text(encoding='utf8')
def pattern(id,kind):
 prefix=r'^\tquest\.'+re.escape(id)+r'\.'+kind+r':\s*'
 return prefix+(r'\[(?:[^"\]]|"(?:\\.|[^"\\])*")*\]' if kind=='quest_desc' else r'"(?:\\.|[^"\\])*"')
for row in rows:
 old=row.get('previous_id',row['id'])
 if old!=row['id']:
  for kind in ('title','quest_subtitle','quest_desc'):s=re.sub(pattern(old,kind)+r'\r?\n?','',s,flags=re.M)
 for kind,value in [('title',row['title']),('quest_subtitle',row['subtitle']),('quest_desc',row['text'])]:
  value=json.dumps(value,ensure_ascii=False) if kind!='quest_desc' else '[\n'+''.join('\t\t'+json.dumps(t,ensure_ascii=False)+'\n' for t in value)+'\t]'
  line='\tquest.'+row['id']+'.'+kind+': '+value;pat=pattern(row['id'],kind)
  assert len(re.findall(pat,s,re.M))<=1,'Duplicate translation'
  if re.search(pat,s,re.M):s=re.sub(pat,lambda _:line,s,count=1,flags=re.M)
  else:at=s.rfind('}');s=s[:at]+line+'\n'+s[at:]
for row in rows:
 for i,title in enumerate(row.get('task_titles',[])):
  key='task.'+row['runtime_tasks'][i]['id']+'.title'
  pat=r'^\t'+re.escape(key)+r':\s*"(?:\\.|[^"\\])*"'
  line='\t'+key+': '+json.dumps(title,ensure_ascii=False)
  if re.search(pat,s,re.M):s=re.sub(pat,lambda _:line,s,count=1,flags=re.M)
  else:at=s.rfind('}');s=s[:at]+line+'\n'+s[at:]
parse(s);files[path]=s
changed=[]
for path,s in files.items():
 old=path.read_bytes();newline=b'\r\n' if b'\r\n' in old else b'\n';data=s.encode('utf8').replace(b'\n',newline)
 if data!=old:
  changed.append(str(path.relative_to(ROOT)))
  if '--check' not in sys.argv:path.write_bytes(data)
if '--check' in sys.argv and changed:raise AssertionError('Would change: '+', '.join(changed))
print('PP quests:',len(rows),'; changed files:',len(changed))
