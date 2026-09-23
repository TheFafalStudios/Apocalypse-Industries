"""Refresh only PP integration index entries; never publish or rewrite unrelated hashes."""
from pathlib import Path
import hashlib,re
ROOT=Path(__file__).resolve().parents[2]
files=[ROOT/'mods/protection-pixel.pw.toml',ROOT/'mods/apocalypse-protection-pixel-integration-1.1.jar',ROOT/'config/protection-pixel.toml',ROOT/'config/ftbquests/quests/lang/en_us.snbt']
files += [ROOT/'config/ftbquests/quests/chapters'/f'{c}.snbt' for c in ['brass_age','electric_age','space_age','special_requests']]
files += list((ROOT/'kubejs/data/protection_pixel').rglob('*.json'))+list((ROOT/'kubejs/data/apocalypse_pp').rglob('*.json'))
p=ROOT/'index.toml';s=p.read_text()
for f in files:
 name=f.relative_to(ROOT).as_posix();block='[[files]]\nfile = "'+name+'"\nhash = "'+hashlib.sha256(f.read_bytes()).hexdigest()+'"\n'
 if name.endswith('.pw.toml'):block+='metafile = true\n'
 pattern=r'\[\[files\]\]\s*file = "'+re.escape(name)+r'"[\s\S]*?(?=\[\[files\]\]|\Z)'
 if re.search(pattern,s):s=re.sub(pattern,lambda _:block+'\n',s,count=1)
 else:s=s.rstrip()+'\n\n'+block
p.write_text(s,newline='')
p=ROOT/'pack.toml';s=p.read_text();s=re.sub(r'(\[index\][\s\S]*?\nhash = ")[^"]+',lambda m:m[1]+hashlib.sha256((ROOT/'index.toml').read_bytes()).hexdigest(),s,count=1);p.write_text(s,newline='')
print('Indexed',len(files),'integration files; unrelated entries preserved.')
