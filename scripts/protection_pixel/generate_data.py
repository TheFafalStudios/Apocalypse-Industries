"""Generate bounded PP recipe overrides and additive quest branches. Existing unrelated data is preserved."""
from pathlib import Path
import zipfile,json,hashlib,re,copy
ROOT=Path(__file__).resolve().parents[2]
PP='protection_pixel:'
def write(path,data):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2)+'\n',encoding='utf8')
def normalize(x):
 if isinstance(x,dict):
  return {k:('neoforge:single' if k=='type' and v=='fluid_stack' else normalize(v)) for k,v in x.items()}
 if isinstance(x,list):return [normalize(v) for v in x]
 return x
experimental={'floatshield_chestplate','evasionwing','lanceras_helmet'}
infra={'armorloadplatform','powerengine'}
changes=[]
with zipfile.ZipFile(ROOT/'mods/protection_pixel-2.2.1-neoforge-1.21.1.jar') as jar:
 tags={name:json.loads(jar.read('data/protection_pixel/tags/item/'+name+'.json'))['values'] for name in ['brass','alloy','equipment']}
 for name in jar.namelist():
  if not name.startswith('data/protection_pixel/recipe/') or not name.endswith('.json'):continue
  original=json.loads(jar.read(name));d=normalize(original)
  results=d.get('results',[d.get('result',{})]);out=results[0].get('id','')
  short=out.removeprefix(PP);gate=None;reason=[]
  if d!=original:reason.append('Correct legacy fluid ingredient serializer for NeoForge/Create 6')
  if short in experimental:gate='creatingspace:moon_regolith';reason.append('Post-lunar material gate; no quest-only lock')
  elif short=='alloyarmorplate' or short=='steamectoskeleton':gate='powergrid:magnet';reason.append('Electric magnetization gate')
  elif short=='heatoverlockingmechanism':
   for step in d['sequence']:
    for ing in step.get('ingredients',[]):
     if ing.get('item')=='create:copper_sheet':ing['item']='powergrid:magnet'
   reason.append('Shared Alloy/equipment upgrade gate: replace seven repeated copper sheets with seven magnets; keep seven-loop manufacturing')
  elif short in infra or out in tags['brass'] or out in tags['equipment']:
   # Retain recipes already containing processed Brass components.
   if 'create:precision_mechanism' not in json.dumps(d):gate='create:precision_mechanism';reason.append('Processed Brass entry component; economy access deliberately retained')
  if gate:
   symbol=next((c for c in 'GHIJKLMNOPQRSTUVWXYZabcdefghi' if c not in d.get('key',{})),None)
   if d['type']=='create:sequenced_assembly':
    transition=d['transitional_item']['id'];d['sequence'].append({'type':'create:deploying','ingredients':[{'item':transition},{'item':gate}],'results':[{'id':transition}]})
   elif 'pattern' in d:
    if d['type']=='create:mechanical_crafting':
     width=max(map(len,d['pattern']));d['pattern'].append(symbol.center(width));d['key'][symbol]={'item':gate}
    else:
     patterns=d['pattern'];width=max(map(len,patterns));patterns=[row.ljust(3) for row in patterns]
     while len(patterns)<3:patterns.append('   ')
     found=False
     for i,row in enumerate(patterns):
      if ' ' in row:patterns[i]=row.replace(' ',symbol,1);found=True;break
     if not found:raise RuntimeError('No empty shaped slot '+name)
     d['pattern']=patterns;d['key'][symbol]={'item':gate}
   else:raise RuntimeError('Unsupported recipe gate '+name)
  # Approved 2026-09-20 recipe substitutions, after existing progression gates.
  recipe_id=Path(name).stem
  if recipe_id=='alloyplate':
   for step in d['sequence']:
    for ing in step.get('ingredients',[]):
     if ing.get('item')=='create:iron_sheet':ing['item']='immersiveengineering:plate_steel'
   reason.append('Approved: steel reinforcement replaces iron sheet')
  substitutions={
   'steamexoskeletonloot':('E','immersiveengineering:plate_steel'),
   'cannonshellloot':('b','immersiveengineering:ingot_steel'),
   'hunterloot':('G','create_connected:control_chip'),
   'blooddialysisdeviceloot':('C','create_connected:control_chip'),
   'floatshieldloot':('J','powergrid:battery'),
   'evasionwingloot':('D','powergrid:constant_speed_motor')}
  if recipe_id in substitutions:
   symbol,item=substitutions[recipe_id];d['key'][symbol]={'item':item}
   reason.append('Approved: functional industrial component substitution')
  if recipe_id=='blooddialysisdeviceloot':d['pattern'][0]=' BCB '
  if recipe_id=='floatshieldloot':d['key']['C']={'item':'immersiveengineering:ingot_electrum'}
  if recipe_id=='armorhangerloot':
   d=copy.deepcopy(original)
   reason=['Approved: restore native hanger recipe without Precision Mechanism']
  if d!=original or recipe_id=='armorhangerloot':
   write(Path('kubejs')/name,d);changes.append({'recipe':PP+Path(name).stem,'output':out,'reason':reason,'old':original,'new':d})
 for tag in ['brass','alloy','equipment']:
  values=[v for v in tags[tag] if v.removeprefix(PP) not in experimental]
  write(Path('kubejs/data/apocalypse_pp/tags/item')/(tag+'.json'),{'replace':False,'values':values})
write(Path('docs/protection_pixel_recipe_changes.json'),changes)
write(Path('kubejs/data/apocalypse_pp/recipe/echo_plate.json'),{
 'type':'create:mechanical_crafting','accept_mirrored':True,'pattern':['DED','DND','DTD',' U '],
 'key':{'D':{'item':'minecraft:diamond'},'E':{'item':'create_deep_dark:echo_ingot'},'N':{'item':'minecraft:netherite_ingot'},'T':{'item':'create_deep_dark:echo_upgrade_smithing_template'},'U':{'item':'minecraft:netherite_upgrade_smithing_template'}},
 'result':{'id':'apocalypse_pp:echo_plate','count':1}})
write(Path('kubejs/data/apocalypse_pp/recipe/reinforced_fiber_from_hemp.json'),{
 'type':'minecraft:crafting_shapeless',
 'ingredients':[{'item':'immersiveengineering:hemp_fabric'},{'item':'minecraft:quartz'},{'item':'minecraft:quartz'}],
 'result':{'id':'protection_pixel:reinforcedfiber','count':2}})
# Native Echo tier and native fitting routes for PP armor omitted upstream.
for tag,values in {
 'plates':['apocalypse_pp:echo_plate'],
 'head':['protection_pixel:linkplate_helmet'],
 'chest':['protection_pixel:linkplate_chestplate','protection_pixel:floatshield_chestplate'],
 'leg':['protection_pixel:linkplate_leggings'],
 'foot':['protection_pixel:linkplate_boots'],
 'protection':['protection_pixel:linkplate_helmet','protection_pixel:linkplate_chestplate','protection_pixel:linkplate_leggings','protection_pixel:linkplate_boots','protection_pixel:floatshield_chestplate']}.items():
 write(Path('kubejs/data/protection_pixel/tags/item')/(tag+'.json'),{'replace':False,'values':values})
# Explicit JSON files under kubejs/data override original IDs before KubeJS recipe parsing.
# Avoid copying raw legacy nested Create recipes through KubeJS's recipe wrapper.
config=ROOT/'config/protection-pixel.toml';text=config.read_text();text=re.sub(r'(\[(?:brassplates|alloyplates)\][\s\S]*?armor = )2\.0',r'\g<1>1.5',text);config.write_text(text,encoding='utf8')
print('Recipe overrides:',len(changes))

# Keep native runtime recipe assertions aligned with generated overrides.
checks=[dict(recipe=c['recipe'],output=c['output']) for c in changes]+[dict(recipe='apocalypse_pp:echo_plate',output='apocalypse_pp:echo_plate')]
checks.append(dict(recipe='apocalypse_pp:reinforced_fiber_from_hemp',output='protection_pixel:reinforcedfiber'))
write(Path('scripts/protection_pixel/resources/apocalypse_pp_recipe_checks.json'),checks)
