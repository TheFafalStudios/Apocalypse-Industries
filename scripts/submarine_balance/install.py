"""Install a validated candidate into WIP only, preserving exact rollback bytes."""
from pathlib import Path
import argparse,datetime,hashlib,json,re,shutil,tomllib
from build import ROOT
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
p=argparse.ArgumentParser();p.add_argument('--build',type=Path,required=True);p.add_argument('--native',type=Path,required=True);a=p.parse_args()
assert json.loads((a.build/'validation.json').read_text())['status']=='PASS'
assert json.loads((a.native/'balance-native-validation.json').read_text())['pass']
artifact=a.build/'apocalypse-submarine-balance-1.0.0.jar'
nativejar=a.native/'mods'/artifact.name
assert sha(artifact)==sha(nativejar),'Native test was for another artifact'
assert not (ROOT/'mods'/artifact.name).exists(),'Use a separate correction/rollback operation'
record=ROOT/'.local/submarine-balance-20261020'/('install-'+datetime.datetime.now().strftime('%H%M%S-%f'))
record.mkdir(parents=True,exist_ok=False)
rels=['config/submarine_hull.json','index.toml','pack.toml']
for rel in rels:
    dst=record/'rollback'/rel;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/rel,dst)
families=json.loads((Path(__file__).parent/'resources/hull-families.json').read_text())
mapping={k:family for family,ids in families.items() for k in ids}
caps={'metal':100,'steel':150,'netherite_decoration':150,'solid_netherite':225}
original=(ROOT/rels[0]).read_bytes();hull=json.loads(original);text=original.decode('utf-8');changes=[]
for k,v in hull.items():
    if not isinstance(v,dict):continue
    name=k.split(':',1)[1]
    window=any(s in name for s in ['glass','window','porthole']) or name.endswith('_pane') or k in ['create_submarine:iron_pressurizer','create_submarine:copper_pressurizer']
    cap=60 if window else caps.get(mapping.get(k),v['maxWaterDepth'])
    after=min(cap,v['maxWaterDepth'])
    if after!=v['maxWaterDepth']:
        text,n=re.subn(r'("'+re.escape(k)+r'"\s*:\s*\{\s*"maxWaterDepth"\s*:\s*)\d+',lambda m:m[1]+str(after),text);assert n==1
        changes.append({'id':k,'before':v['maxWaterDepth'],'water':after,'family':'viewport' if window else mapping[k]})
new=json.loads(text);assert new.keys()==hull.keys()
for k,v in hull.items():
    if isinstance(v,dict):assert new[k]['implosionChance']==v['implosionChance']
(ROOT/rels[0]).write_bytes(text.encode('utf-8'))
shutil.copy2(artifact,ROOT/'mods'/artifact.name)
index=ROOT/'index.toml';idx=index.read_bytes().decode('utf-8')
idx,n=re.subn(r'(file = "config/submarine_hull\.json"\s*\r?\nhash = ")[0-9a-f]+',lambda m:m[1]+sha(ROOT/rels[0]),idx);assert n==1
idx+='\n[[files]]\nfile = "mods/'+artifact.name+'"\nhash = "'+sha(artifact)+'"\n'
index.write_bytes(idx.encode('utf-8'))
pack=ROOT/'pack.toml';value=pack.read_bytes().decode('utf-8')
value,n=re.subn(r'(\[index\]\s*\r?\nfile = "index\.toml"\s*\r?\nhash-format = "sha256"\s*\r?\nhash = ")[0-9a-f]+',lambda m:m[1]+sha(index),value);assert n==1
pack.write_bytes(value.encode('utf-8'))
assert tomllib.loads(pack.read_text())['index']['hash']==sha(index)
for f in tomllib.loads(index.read_text())['files']:
    if f['file'] in [rels[0],'mods/'+artifact.name]:assert f['hash']==sha(ROOT/f['file'])
result={'scope':'WIP only; next-update candidate','installedAddon':artifact.name,'addonSha256':sha(artifact),'changes':changes,
    'files':{r:{'before':sha(record/'rollback'/r),'after':sha(ROOT/r)} for r in rels},
    'nativeEvidence':str(a.native),'activation':'User-managed client relaunch required; none performed',
    'limitations':['Complete vessel gameplay, reinforcement interaction/persistence across assembly, fluid transitions and performance still require gameplay evaluation.']}
(record/'installation.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps({'record':str(record),'waterEntriesChanged':len(changes),'addonSha256':sha(artifact)},indent=2))
