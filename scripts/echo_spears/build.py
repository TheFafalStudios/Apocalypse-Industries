from pathlib import Path
import subprocess, zipfile, json, hashlib
ROOT=Path(__file__).resolve().parents[2]
HERE=Path(__file__).resolve().parent
OUT=Path(r'Q:\GPT General\echo-spears-20261010')
LAUNCHER=ROOT.parents[1]
JAVA=LAUNCHER/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/javac.exe'
BACKPORT=OUT/'spear-backport-neoforge-1.8.0-1.21.1.jar'
assert hashlib.sha1(BACKPORT.read_bytes()).hexdigest()=='944b22a7fa34c1e87e70f4558c9164d950646dd6'
classes=OUT/'classes-1.0.4'; classes.mkdir(exist_ok=True)
cp=[LAUNCHER/'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',LAUNCHER/'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar',BACKPORT]
cp.append(ROOT/'mods/create_deep_dark-3.0.2-neoforge-1.21.1.jar')
cp.append(LAUNCHER/'libraries/net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar')
cp += [p for p in (LAUNCHER/'servers/Chunkserve - Apocalypse Industries/libraries').rglob('*.jar') if 'neoforge-21.' not in p.name]
args=['--release','21','-encoding','UTF-8','-proc:none','-classpath',';'.join(map(str,cp)),'-d',str(classes)]+[str(p) for p in (HERE/'src').rglob('*.java')]
for p in (HERE/'src').rglob('*.java'): p.write_text(p.read_text(encoding='utf-8-sig'),encoding='utf8')
argfile=OUT/'javac.args'; argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf8')
subprocess.run([str(JAVA),'@'+str(argfile)],check=True)
resources=HERE/'resources'
def write(name, value):
 p=resources/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n',encoding='utf8')
id='apocalypse_echo_spears:echo_spear'
write('pack.mcmeta',{'pack':{'pack_format':34,'description':'Apocalypse Industries Echo Spears'}})
write('assets/apocalypse_echo_spears/lang/en_us.json',{'item.apocalypse_echo_spears.echo_spear':'Echo Spear'})
write('data/apocalypse_echo_spears/recipe/echo_spear_smithing.json',{'type':'minecraft:smithing_transform','template':{'item':'create_deep_dark:echo_upgrade_smithing_template'},'base':{'item':'minecraft:netherite_spear'},'addition':{'item':'create_deep_dark:echo_ingot'},'result':{'id':id}})
for tag in ['spears','enchantable/durability','enchantable/fire_aspect','enchantable/lunge','enchantable/melee_weapon','enchantable/sharp_weapon']:
 write('data/minecraft/tags/item/'+tag+'.json',{'replace':False,'values':[id]})
# Preserve upstream models/transforms with the chestplate-derived Echo textures.
with zipfile.ZipFile(BACKPORT) as upstream:
 model=json.loads(upstream.read('assets/minecraft/models/item/netherite_spear.json'))
 model['textures']['layer0']='apocalypse_echo_spears:item/echo_spear'
 model['overrides'][0]['model']='apocalypse_echo_spears:item/echo_spear_in_hand'
 write('assets/apocalypse_echo_spears/models/item/echo_spear.json',model)
 held=json.loads(upstream.read('assets/minecraft/models/item/netherite_spear_in_hand.json'))
 held['textures']['layer0']='apocalypse_echo_spears:item/echo_spear_in_hand'
 write('assets/apocalypse_echo_spears/models/item/echo_spear_in_hand.json',held)
 for name in ['echo_spear','echo_spear_in_hand']:
  assert (resources/('assets/apocalypse_echo_spears/textures/item/'+name+'.png')).is_file(), 'Run recolor.py using Python with Pillow first'
# Upstream optional integration resources lack existence guards in 1.8.0.
with zipfile.ZipFile(BACKPORT) as upstream:
 for name in upstream.namelist():
  if name.endswith('.json') and '/recipe/' in name:
   obj=json.loads(upstream.read(name)); ids=set()
   def items(v):
    if isinstance(v,dict):
     for k,x in v.items():
      if k in ('item','id') and isinstance(x,str) and ':' in x: ids.add(x)
      else:items(x)
    elif isinstance(v,list):
     for x in v:items(x)
   items(obj)
   optional=sorted(x for x in ids if x.split(':')[0] in ['dragonloot','ender_dragon_loot','enderitemod','oreganized'] or x=='minecraft:copper_spear')
   if optional:
    obj['neoforge:conditions']=[{'type':'neoforge:item_exists','item':x} for x in optional]
    write(name,obj)
  if name in ['data/dragonloot/tags/item/explosion_resistant.json','data/enderitemod/tags/item/enderite_items.json']:
   obj=json.loads(upstream.read(name));obj['replace']=True;obj['values']=[{'id':v,'required':False} if isinstance(v,str) else v for v in obj['values']];write(name,obj)
write('apocalypse_echo_spears.mixins.json',{'required':True,'minVersion':'0.8','package':'dev.apocalypse.spears.mixin','compatibilityLevel':'JAVA_21','client':['SpearInventoryModelMixin'],'injectors':{'defaultRequire':1}})
meta='''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_echo_spears"
version="1.0.8"
displayName="Apocalypse Industries: Echo Spears"
description="Echo spear integration for Backported Spears and Create: Deep Dark."
'''
for mod,version in [('neoforge','[21.1.248,)'),('minecraft','[1.21.1,1.21.2)'),('spears','[1.8.0,1.9.0)'),('create_deep_dark','[3.0.2,3.1.0)')]:
 meta+=f'\n[[dependencies.apocalypse_echo_spears]]\nmodId="{mod}"\ntype="required"\nversionRange="{version}"\nordering="AFTER"\nside="BOTH"\n'
meta+='\n[[mixins]]\nconfig="apocalypse_echo_spears.mixins.json"\n'
p=resources/'META-INF/neoforge.mods.toml';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(meta)
artifact=OUT/'apocalypse-echo-spears-1.0.8.jar'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
 for folder in [classes,resources]:
  for f in sorted(folder.rglob('*')):
   if f.is_file():z.writestr(f.relative_to(folder).as_posix(),f.read_bytes())
print(artifact,hashlib.sha256(artifact.read_bytes()).hexdigest())
