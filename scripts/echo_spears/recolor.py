"""Derive the Echo chestplate recolor and apply it without changing spear pixels or alpha.
Run using Python with Pillow installed; outputs are checked-in pack assets.
"""
from pathlib import Path
from collections import Counter,defaultdict
from PIL import Image,ImageDraw,ImageFont
import zipfile,io,json,hashlib,csv
ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
OUT=Path(r'Q:\GPT General\echo-spears-20261010/texture-conversion-shaft-outline-fix')
OUT.mkdir(exist_ok=True)
VANILLA=ROOT.parents[1]/'libraries/net/minecraft/client/1.21.1/client-1.21.1.jar'
ECHO=ROOT/'mods/create_deep_dark-3.0.2-neoforge-1.21.1.jar'
SPEARS=ROOT/'mods/spear-backport-neoforge-1.8.0-1.21.1.jar'
def read(jar,name):
 raw=zipfile.ZipFile(jar).read(name)
 return Image.open(io.BytesIO(raw)).convert('RGBA'),hashlib.sha256(raw).hexdigest()
a,ha=read(VANILLA,'assets/minecraft/textures/item/netherite_chestplate.png')
b,hb=read(ECHO,'assets/create_deep_dark/textures/item/echo_shard_chestplate.png')
assert a.size==b.size==(16,16)
def luminance(c):return .2126*c[0]+.7152*c[1]+.0722*c[2]
def hexcolor(c):return '#'+''.join(f'{v:02x}' for v in c[:3])
counts=defaultdict(Counter);trace=[]
for y in range(a.height):
 for x in range(a.width):
  p,q=a.getpixel((x,y)),b.getpixel((x,y))
  if p[3]:trace.append({'x':x,'y':y,'netherite_rgba':p,'echo_rgba':q})
  if p[3] and q[3]:counts[p[:3]][q[:3]]+=1
# Use dominant opaque correspondence. Resolve exact frequency ties by the darker Echo shade.
mapping={p:min(targets,key=lambda q:(-targets[q],luminance(q),q)) for p,targets in counts.items()}
report={'method':'Most frequent opaque correspondence at the same chestplate coordinates; tied counts use darker target. Spear-only colors use nearest Netherite RGB color (squared RGB distance); ties use darker source. Original wood colors #603432 and #734543 are preserved. The purple outer edge uses the darker observed Echo shade; the intermediate bevel highlight is interpolated between chestplate anchors to preserve original shading. Original shaft outline pixels are restored by spatial mask; spearhead pixels sharing those colors remain recolored. Alpha is unchanged.',
 'reference_sha256':{'netherite_chestplate':ha,'echo_chestplate':hb},'chestplate_pixel_trace':trace,
 'chestplate_palette':[{'netherite':hexcolor(p),'chosen_echo':hexcolor(mapping[p]),'alternatives':[{'echo':hexcolor(q),'pixels':n} for q,n in sorted(targets.items())]} for p,targets in sorted(counts.items())], 'spears':{}}
textures=HERE/'resources/assets/apocalypse_echo_spears/textures/item';textures.mkdir(parents=True,exist_ok=True)
results={}
# Original light brown wood highlights are not Netherite and must stay unchanged.
WOOD_COLORS={(96,52,50),(115,69,67)}
# Preserve distinct bevel shades that nearest RGB lookup collapsed.
# Purple outer edge takes the darker observed Echo shade; intermediate highlight
# interpolates the chestplate's #4d494d -> #0a4950 and #5d565d -> #0d626c anchors.
lo,hi=(77,73,77),(93,86,93)
t=(luminance((88,78,86))-luminance(lo))/(luminance(hi)-luminance(lo))
intermediate=tuple(round(mapping[lo][i]+t*(mapping[hi][i]-mapping[lo][i])) for i in range(3))
SHADING_COLORS={(74,41,64):mapping[(55,53,55)],(88,78,86):intermediate}
def shaft_outline(variant,x,y,p):
 # Spatial mask keeps shared dark colors on the spearhead recolored.
 in_shaft=y>=7 and (14<=x+y<=16 if not variant else -1<=x-y<=1)
 return in_shaft and p[3] and p[:3] in {(35,16,18),(47,33,34),(74,41,64)}
for variant in ['','_in_hand']:
 original,h=read(SPEARS,'assets/minecraft/textures/item/netherite_spear'+variant+'.png')
 converted=original.copy();palette=Counter(p for p in original.get_flattened_data() if p[3]);rows=[]
 for p,n in sorted(palette.items()):
  source=min(mapping,key=lambda q:(sum((p[i]-q[i])**2 for i in range(3)),luminance(q),q))
  rows.append({'spear_source':hexcolor(p),'chestplate_source':hexcolor(source),'echo_target':hexcolor(p) if p[:3] in WOOD_COLORS else hexcolor(SHADING_COLORS.get(p[:3],mapping[source])),'preserved_wood':p[:3] in WOOD_COLORS,'exact_chestplate_color':p[:3] in mapping,'pixels':n})
 lookup={p:tuple(int(row['echo_target'][i:i+2],16) for i in (1,3,5))+(p[3],) for p,row in zip(sorted(palette),rows)}
 for y in range(original.height):
  for x in range(original.width):
   p=original.getpixel((x,y))
   if p[3]:converted.putpixel((x,y),p if shaft_outline(variant,x,y,p) else lookup[p])
 assert all(p==q for p,q in zip(original.get_flattened_data(),converted.get_flattened_data()) if p[:3] in WOOD_COLORS)
 assert original.size==converted.size
 assert [p[3] for p in original.get_flattened_data()]==[p[3] for p in converted.get_flattened_data()]
 assert all(p==q for p,q in zip(original.get_flattened_data(),converted.get_flattened_data()) if not p[3])
 path=textures/('echo_spear'+variant+'.png');converted.save(path)
 report['spears'][variant or 'inventory']={'source_sha256':h,'output_sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'size':original.size,'palette_conversion':rows,'preserved_outline_coordinates':[[x,y] for y in range(original.height) for x in range(original.width) if shaft_outline(variant,x,y,original.getpixel((x,y)))]}
 results[variant]=(original,converted)
(OUT/'palette-conversion.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf8')
with (OUT/'palette-conversion.csv').open('w',newline='',encoding='utf8') as f:
 writer=csv.writer(f);writer.writerow(['variant','spear_source','chestplate_source','echo_target','exact_reference_color','pixels'])
 for variant,info in report['spears'].items():
  for row in info['palette_conversion']:writer.writerow([variant,row['spear_source'],row['chestplate_source'],row['echo_target'],row['exact_chestplate_color'],row['pixels']])
# Nearest-neighbor preview for visual inspection; source textures are not resampled in the pack.
preview=Image.new('RGB',(680,380),'#24272b');draw=ImageDraw.Draw(preview)
for label,im,x,y,scale in [('Netherite chestplate',a,20,35,8),('Echo chestplate',b,185,35,8),('Netherite spear',results[''][0],355,35,8),('Echo spear',results[''][1],520,35,8),('Netherite held',results['_in_hand'][0],105,200,5),('Echo held',results['_in_hand'][1],415,200,5)]:
 draw.text((x,y-20),label,fill='#eeeeee');scaled=im.resize((im.width*scale,im.height*scale),Image.Resampling.NEAREST);preview.paste(scaled,(x,y),scaled)
preview.save(OUT/'preview.png')
print('Generated both textures; alpha and transparent pixels unchanged')
print('Reference mappings:',{hexcolor(p):hexcolor(q) for p,q in mapping.items()})
