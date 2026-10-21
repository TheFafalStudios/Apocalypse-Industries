"""Install the validated ballast-only addon into WIP and update only its Packwiz entries."""
from pathlib import Path
import argparse,datetime,hashlib,json,re,shutil,tomllib
from build import ROOT,JAR
p=argparse.ArgumentParser();p.add_argument('--build',required=True,type=Path);a=p.parse_args()
validation=json.loads((a.build/'validation.json').read_text());assert validation['pass']
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(a.build/JAR)==validation['sha256'];assert not (ROOT/'mods'/JAR).exists()
record=ROOT/'.local/lava-ballast-20261021'/('install-'+datetime.datetime.now().strftime('%H%M%S-%f'));record.mkdir(parents=True)
before={}
for rel in ['index.toml','pack.toml']:
    dest=record/'rollback'/rel;dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/rel,dest);before[rel]=sha(dest)
shutil.copy2(a.build/JAR,ROOT/'mods'/JAR)
index=ROOT/'index.toml';text=index.read_bytes().decode('utf-8')
assert 'mods/'+JAR not in text
text+='\n[[files]]\nfile = "mods/'+JAR+'"\nhash = "'+sha(ROOT/'mods'/JAR)+'"\n'
index.write_bytes(text.encode('utf-8'))
pack=ROOT/'pack.toml';text=pack.read_bytes().decode('utf-8')
text,n=re.subn(r'(\[index\]\s*\r?\nfile = "index\.toml"\s*\r?\nhash-format = "sha256"\s*\r?\nhash = ")[0-9a-f]+',lambda m:m[1]+sha(index),text);assert n==1
pack.write_bytes(text.encode('utf-8'));assert tomllib.loads(pack.read_text())['index']['hash']==sha(index)
report={'scope':'WIP only; ballast/vents/floaters, propulsion excluded','addon':JAR,'sha256':sha(ROOT/'mods'/JAR),'before':before,'after':{rel:sha(ROOT/rel) for rel in before},'validation':validation,'activation':'User-managed client relaunch required; no live server changes/restarts/reloads','rollback':'Remove only the new ballast addon and restore saved index/pack files after checking for subsequent unrelated edits.'}
(record/'installation.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({'record':str(record),'sha256':report['sha256']}))
