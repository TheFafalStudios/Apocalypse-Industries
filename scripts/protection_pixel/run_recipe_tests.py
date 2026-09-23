from pathlib import Path
import subprocess,time,threading,queue,shutil,os,json,re
root=Path(__file__).resolve().parents[2];test=root/'scripts/protection_pixel/build/quest-roundtrip-server'
java=root.parents[1]/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/java.exe'
rows=[]
for rid,body in re.findall(r'### Point [^\n]+ - ([\w:]+).*?```json\n(.*?)\n```',(root/'docs/PROTECTION_PIXEL_RECIPE_PROPOSALS.md').read_text(),re.S):
 rows.append(dict(id=rid,expected=json.loads(body)))
for folder in ['kubejs/data','kubejs/server_scripts']:
 for src in (root/folder).rglob('*'):
  if src.is_file():
   dst=test/src.relative_to(root);dst.parent.mkdir(parents=True,exist_ok=True);tmp=dst.with_suffix('.recipe-test-tmp');shutil.copy2(src,tmp);os.replace(tmp,dst)
script="var approvedRecipes = "+json.dumps(rows)+";\n"+r"""
ServerEvents.commandRegistry(event => {
 event.register(event.commands.literal('ppapprovedrecipes').executes(ctx => {
  try {
   var RL=Java.loadClass('net.minecraft.resources.ResourceLocation');
   var Recipe=Java.loadClass('net.minecraft.world.item.crafting.Recipe');
   var JsonOps=Java.loadClass('com.mojang.serialization.JsonOps');
   var server=ctx.source.server;
   var ops=server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
   approvedRecipes.forEach(row => {
    var holder=server.getRecipeManager().byKey(RL.parse(row.id));
    if (!holder.isPresent()) throw new Error('Missing '+row.id);
    var recipe=holder.get().value();
    var encoded=Recipe.CODEC.encodeStart(ops,recipe).getOrThrow();
    console.info('PP APPROVED RECIPE '+row.id+' '+encoded.toString());
   });
   if (!server.getRecipeManager().byKey(RL.parse('protection_pixel:fiberloot')).isPresent()) throw new Error('Original fiber route missing');
   console.info('PP APPROVED RECIPES PASS');
  } catch(e) { console.error('PP APPROVED RECIPES FAIL '+e); }
  return 1;
 }));
});
"""
(test/'kubejs/server_scripts/zz_pp_approved_recipes.js').write_text(script)
log=root/'docs/protection_pixel_approved_recipes_runtime.txt';q=queue.Queue()
p=subprocess.Popen([str(java),'-Xms2G','-Xmx5G','@libraries/net/neoforged/neoforge/21.1.248/win_args.txt','nogui'],cwd=test,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,encoding='utf8',errors='replace',bufsize=1)
def read():
 for line in p.stdout:q.put(line)
 q.put(None)
threading.Thread(target=read,daemon=True).start();start=time.time();passed=False;sent=False
with log.open('w',encoding='utf8') as f:
 while time.time()-start<600:
  try:line=q.get(timeout=1)
  except queue.Empty:continue
  if line is None:break
  f.write(line);f.flush()
  if 'Done (' in line and not sent:
   print('Server loaded; checking native recipe serialization',flush=True);p.stdin.write('ppapprovedrecipes\n');p.stdin.flush();sent=True
  if 'PP APPROVED RECIPE' in line:
   print(line.strip(),flush=True)
   if 'RECIPES PASS' in line or 'RECIPES FAIL' in line:
    passed='RECIPES PASS' in line;p.stdin.write('stop\n');p.stdin.flush();break
 if p.poll() is None:
  try:p.stdin.write('stop\n');p.stdin.flush();p.wait(timeout=45)
  except subprocess.TimeoutExpired:p.terminate();p.wait(timeout=20)
 # Drain remaining output without requiring the reader thread to stay alive.
 while not q.empty():
  line=q.get()
  if line:f.write(line)
if not passed:raise SystemExit('Native recipe check failed; see '+str(log))
print('PASS: nine native recipes serialized; original fiber recipe retained',flush=True)

# Validate the effective recipes after Almost Unified and KubeJS processing.
loaded={rid:json.loads(body) for rid,body in re.findall(r'PP APPROVED RECIPE ([\w:]+) (\{[^\n]+\})',log.read_text(encoding='utf8'))}
unified={'immersiveengineering:plate_steel':'c:plates/steel','immersiveengineering:ingot_steel':'c:ingots/steel','immersiveengineering:ingot_electrum':'c:ingots/electrum','create:copper_sheet':'c:plates/copper','create:brass_sheet':'c:plates/brass','create:iron_sheet':'c:plates/iron','create:sturdy_sheet':'c:plates/obsidian','minecraft:iron_ingot':'c:ingots/iron','minecraft:iron_nugget':'c:nuggets/iron'}
def normalized(x):
 if isinstance(x,list):return [normalized(v) for v in x]
 if isinstance(x,dict):
  if set(x)=={'item'} and x['item'] in unified:return {'tag':unified[x['item']]}
  return {k:normalized(v) for k,v in x.items()}
 return x
for row in rows:
 expected=normalized(row['expected']);actual=loaded[row['id']]
 assert actual['type']==expected['type'],row['id']+' type'
 if 'pattern' in expected:
  for key in ['pattern','key','result']:assert actual[key]==expected[key],row['id']+' '+key
 elif 'sequence' in expected:
  assert actual.get('loops',1)==expected['loops'],row['id']+' loops'
  for key in ['ingredient','results','transitional_item']:assert actual[key]==expected[key],row['id']+' '+key
  assert len(actual['sequence'])==len(expected['sequence'])
  for i,(a,e) in enumerate(zip(actual['sequence'],expected['sequence'])):
   if i==0:
    # Create permits both the initial plate and transition item at the first press.
    a=dict(a);assert a['ingredients']==[[e['ingredients'][0],expected['ingredient']]]
    a['ingredients']=e['ingredients']
   assert a==e,row['id']+' sequence '+str(i)
 else:
  for key in ['ingredients','result']:assert actual[key]==expected[key],row['id']+' '+key
print('PASS: all nine effective ingredient quantities, patterns, outputs and assembly steps match approval (allowing verified material unification)',flush=True)
