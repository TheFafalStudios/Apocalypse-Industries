import argparse,collections,hashlib,json,pathlib,re,shutil,subprocess,tomllib,zipfile
p=argparse.ArgumentParser();p.add_argument('--instance',required=True);p.add_argument('--output',required=True);p.add_argument('--baseline-ref',default='origin/main');a=p.parse_args()
src=pathlib.Path(a.instance).resolve();out=pathlib.Path(a.output).resolve()
if out.exists(): raise SystemExit('Output must be a new directory')
if src==out or src in out.parents: raise SystemExit('Output must be outside the live instance')
repo=out/'repository';repo.mkdir(parents=True)
def sha(p,algo='sha256'): return hashlib.new(algo,p.read_bytes()).hexdigest()
def copy(p):
 rel=p.relative_to(src);dest=repo/rel;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,dest)
blocked=[];payload=[];refs=[];direct=[];mods={p.name:p for p in (src/'mods').glob('*.jar')};ids=collections.defaultdict(list);packages=collections.defaultdict(set);fixpackages=set()
for jar in mods.values():
 with zipfile.ZipFile(jar) as z:
  for meta in ('META-INF/neoforge.mods.toml','META-INF/mods.toml'):
   if meta in z.namelist():
    for m in tomllib.loads(z.read(meta).decode('utf-8-sig')).get('mods',[]):ids[m['modId']].append(jar.name)
    break
  pkgs={n.rsplit('/',1)[0] for n in z.namelist() if n.endswith('.class') and '/' in n and not n.startswith('META-INF/')}
  for pkg in pkgs:packages[pkg].add(jar.name)
  if jar.name.startswith('apocalypse-'):fixpackages.update(pkgs)
dups={k:v for k,v in ids.items() if len(v)>1};collisions={k:sorted(packages[k]) for k in fixpackages if len(packages[k])>1}
if dups or collisions:raise SystemExit(json.dumps({'duplicates':dups,'fix_collisions':collisions}))
covered=set()
for meta in sorted((src/'mods').glob('*.pw.toml')):
 d=tomllib.loads(meta.read_text(encoding='utf-8-sig'));name=d['filename'];dl=d['download']
 if name not in mods:blocked.append({'path':str(meta.relative_to(src)).replace('\\','/'),'reason':'superseded mod reference'});continue
 if sha(mods[name],dl['hash-format'].replace('-',''))!=dl['hash'].lower():raise SystemExit('Download hash differs from working JAR: '+name)
 if name in covered:raise SystemExit('Duplicate download destination: '+name)
 copy(meta);refs.append(meta.relative_to(src).as_posix());covered.add(name)
for name,jar in sorted(mods.items()):
 if name not in covered:copy(jar);direct.append(jar.relative_to(src).as_posix())
for root in ('config','defaultconfigs','kubejs','datapacks','resourcepacks'):
 if not (src/root).exists():continue
 for f in sorted((src/root).rglob('*')):
  if not f.is_file():continue
  rel=f.relative_to(src).as_posix();low=rel.lower()
  excluded=(f.suffix.lower() in {'.bak','.old','.log','.db','.sqlite','.sqlite3','.pyc'} or low in {'kubejs/config/web_server.json','config/lostcities-server.toml','config/forgeendertech/cached.dat','config/biomecontrolengine/dimensions_cache.json','config/spark/activity.json','kubejs/all_recipes_dump.json'} or low.startswith(('config/jei/world/','config/xaero/','kubejs/exported/','kubejs/.cache/')))
  if excluded:blocked.append({'path':rel,'reason':'local cache/private data/abandoned Lost Cities content'});continue
  copy(f);payload.append(rel)
shader='shaderpacks/ComplementaryUnbound_r5.8.1_APOCALYPSE_ATMOSPHERE_v1.1_PARSEFIX_TESTED.zip'
copy(src/shader);payload.append(shader)
# Only portable resource-pack selections become fresh-install defaults, not keybinds or account preferences.
opts={}
for line in (src/'options.txt').read_text(encoding='utf-8-sig').splitlines():
 if line.startswith(('resourcePacks:','incompatibleResourcePacks:')):
  k,v=line.split(':',1);opts[k]=[x for x in json.loads(v) if x in ('vanilla','mod_resources') or (x.startswith('file/') and (repo/'resourcepacks'/x[5:]).is_file())]
(repo/'options.txt').write_text(''.join(k+':'+json.dumps(v,separators=(',',':'))+'\n' for k,v in opts.items()),encoding='utf-8');payload.append('options.txt')
# Preserve repository documentation and tools, but replace the obsolete index builder below.
for root in ('docs','scripts','bootstrap','.github'):
 if (src/root).exists():
  for f in (src/root).rglob('*'):
   if f.is_file() and not any(x in f.relative_to(src/root).parts for x in ('__pycache__', 'build', '.git')) and not (root == 'docs' and (f.suffix == '.log' or 'runtime' in f.name.lower() or 'BACKUP' in f.name)) :copy(f)
for name in ('README.md','CHANGELOG.md','VERSION','.gitattributes','AGENTS.md','instance.png'):
 if (src/name).exists():copy(src/name)
shutil.copy2(pathlib.Path(__file__),repo/'scripts'/'prepare_distribution.py')
entries=[]
for rel in sorted(set(payload+refs+direct)):
 e={'file':rel,'hash':sha(repo/rel)}
 if rel in refs:e['metafile']=True
 if rel=='options.txt':e['preserve']=True
 entries.append(e)
index='hash-format = "sha256"\n\n'+''.join('[[files]]\nfile = '+json.dumps(e['file'])+'\nhash = "'+e['hash']+'"\n'+('metafile = true\n' if e.get('metafile') else '')+('preserve = true\n' if e.get('preserve') else '')+'\n' for e in entries)
(repo/'index.toml').write_text(index,encoding='utf-8')
pack=(src/'pack.toml').read_text(encoding='utf-8-sig');pack=re.sub(r'(?m)^hash = "[^"]+"', 'hash = "'+sha(repo/'index.toml')+'"',pack,count=1);(repo/'pack.toml').write_text(pack,encoding='utf-8')
(repo/'.gitignore').write_text('# This repository is a filtered release snapshot. Rebuild it from the live instance.\n.git/\n__pycache__/\n*.pyc\n',encoding='utf-8')
(repo/'.packwizignore').write_text('/*\n'+''.join('!/'+x+'/\n!/'+x+'/**\n' for x in ('config','defaultconfigs','kubejs','datapacks','mods','resourcepacks','shaderpacks'))+'!/options.txt\n',encoding='utf-8')
def baseline_text(rel):
 return subprocess.check_output(['git','-C',str(src),'show',a.baseline_ref+':'+rel]).decode('utf-8-sig')
baseline_commit=subprocess.check_output(['git','-C',str(src),'rev-parse',a.baseline_ref]).decode().strip()
old=tomllib.loads(baseline_text('index.toml'));oldpaths={e['file'] for e in old['files']};newpaths={e['file'] for e in entries}
removed=sorted(oldpaths-newpaths)
# Old external references imply deletion of their downloaded JARs too.
for rel in oldpaths:
 if rel.endswith('.pw.toml') and rel not in newpaths:removed.append('mods/'+tomllib.loads(baseline_text(rel))['filename'])
(out/'deletions-from-previous-manifest.txt').write_text('\n'.join(sorted(set(removed)))+'\n')
validation={'baseline_commit':baseline_commit,'version':tomllib.loads(pack)['version'],'active_jars':len(mods),'external_references':len(refs),'direct_jars':len(direct),'indexed_files':len(entries),'duplicate_mod_ids':dups,'standalone_fix_package_collisions':collisions,'excluded':blocked,'user_validation':'Not tested for this generated snapshot. No runtime validation is inferred from prior releases.','network_downloads_tested':False}
(out/'validation.json').write_text(json.dumps(validation,indent=2));(out/'direct-jars.json').write_text(json.dumps([{'path':x,'sha256':sha(repo/x)} for x in direct],indent=2))
(out/'working-mods.json').write_text(json.dumps([{'filename':n,'sha256':sha(f)} for n,f in sorted(mods.items())],indent=2))
# Validate the serialized manifests independently of generation.
for e in tomllib.loads((repo/'index.toml').read_text())['files']:
 assert sha(repo/e['file'])==e['hash'],e['file']
assert sha(repo/'index.toml')==tomllib.loads((repo/'pack.toml').read_text())['index']['hash']
assert len(covered)+len(direct)==len(mods)
print(json.dumps({k:v for k,v in validation.items() if k!='excluded'},indent=2));print(out)
