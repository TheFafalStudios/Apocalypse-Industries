import json,zipfile,math,gzip,base64,io,hashlib,datetime,shutil
from pathlib import Path
from PIL import Image
root=Path.cwd();out=root/'docs/weapon-library';z=zipfile.ZipFile(root/'resourcepacks/Apocalypse_Weapon_Library.zip');rows=json.loads((out/'model-categories.json').read_text(encoding='utf-8'))['models'];textures={};models={};missing=[]
def resolve(mid,seen=()):
 if mid in seen:raise ValueError('Parent cycle '+mid)
 ns,p=mid.split(':');m=json.loads(z.read('assets/'+ns+'/models/'+p+'.json'));par=m.get('parent','');base={}
 if ':' in par and 'assets/'+par.replace(':','/models/')+'.json' in z.namelist():base=resolve(par,seen+(mid,))
 return {**base,**m,'textures':{**base.get('textures',{}),**m.get('textures',{})}}
def transform(v,rot):
 if not rot:return v
 o=rot.get('origin',[8,8,8]);p=[v[i]-o[i] for i in range(3)];a=math.radians(rot['angle']);c=math.cos(a);s=math.sin(a);axis=rot['axis']
 if axis=='x':p=[p[0],p[1]*c-p[2]*s,p[1]*s+p[2]*c]
 if axis=='y':p=[p[0]*c+p[2]*s,p[1],-p[0]*s+p[2]*c]
 if axis=='z':p=[p[0]*c-p[1]*s,p[0]*s+p[1]*c,p[2]]
 if rot.get('rescale'):
  for i in range(3):
   if 'xyz'[i]!=axis:p[i]/=c
 return [p[i]+o[i] for i in range(3)]
for r in rows:
 m=resolve(r['model']);groups={};bounds=[]
 for e in m.get('elements',[]):
  x,y,zz=e['from'];X,Y,Z=e['to'];verts={'north':[[X,Y,zz],[x,Y,zz],[x,y,zz],[X,y,zz]],'south':[[x,Y,Z],[X,Y,Z],[X,y,Z],[x,y,Z]],'east':[[X,Y,Z],[X,Y,zz],[X,y,zz],[X,y,Z]],'west':[[x,Y,zz],[x,Y,Z],[x,y,Z],[x,y,zz]],'up':[[x,Y,zz],[X,Y,zz],[X,Y,Z],[x,Y,Z]],'down':[[x,y,Z],[X,y,Z],[X,y,zz],[x,y,zz]]}
  normals={'north':[0,0,-1],'south':[0,0,1],'east':[1,0,0],'west':[-1,0,0],'up':[0,1,0],'down':[0,-1,0]}
  for side,f in e.get('faces',{}).items():
   if side not in verts:continue
   tex=f['texture'];seen=set()
   while tex.startswith('#'):
    if tex in seen:raise ValueError(tex)
    seen.add(tex);tex=m['textures'].get(tex[1:],'minecraft:missingno')
   if tex not in textures:
    ns,p=tex.split(':') if ':' in tex else ('minecraft',tex);path='assets/'+ns+'/textures/'+p+'.png'
    if path not in z.namelist():missing.append((r['name'],tex));continue
    raw=z.read(path)
    if path+'.mcmeta' in z.namelist():
     try:
      meta=json.loads(z.read(path+'.mcmeta')).get('animation',{});im=Image.open(io.BytesIO(raw));w=meta.get('width',min(im.size));h=meta.get('height',w);frames=meta.get('frames',[0]);frame=frames[0] if frames else 0;frame=frame.get('index',0) if isinstance(frame,dict) else frame;cols=max(1,im.width//w);im=im.crop(((frame%cols)*w,(frame//cols)*h,(frame%cols+1)*w,(frame//cols+1)*h));b=io.BytesIO();im.save(b,format='PNG');raw=b.getvalue()
     except Exception:pass
    textures[tex]='data:image/png;base64,'+base64.b64encode(raw).decode()
   if tex not in textures:continue
   uv=f.get('uv')
   if uv is None:
    uv={'down':[x,16-Z,X,16-zz],'up':[x,zz,X,Z],'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],'west':[zz,16-Y,Z,16-y],'east':[16-Z,16-Y,16-zz,16-y]}[side]
   u,v,U,V=[a/16 for a in uv];coords=[[u,v],[U,v],[U,V],[u,V]];turn=int(f.get('rotation',0)/90);coords=coords[turn:]+coords[:turn]
   vv=[transform(p,e.get('rotation')) for p in verts[side]];bounds+=vv;normal=normals[side]
   if e.get('rotation'):
    rot={**e['rotation'],'origin':[0,0,0],'rescale':False};normal=transform(normal,rot)
   arr=groups.setdefault(tex,[])
   for i in [0,1,2,0,2,3]:arr.extend(vv[i]+coords[i]+normal)
 if bounds:
  lo=[min(p[i] for p in bounds) for i in range(3)];hi=[max(p[i] for p in bounds) for i in range(3)];center=[(lo[i]+hi[i])/2 for i in range(3)];extent=max(hi[i]-lo[i] for i in range(3)) or 1
  for arr in groups.values():
   for i in range(0,len(arr),8):
    for j in range(3):arr[i+j]=(arr[i+j]-center[j])/extent*1.6
   arr[:]=[round(v,5) for v in arr]
 models[r['model']]={'groups':groups,'elements':len(m.get('elements',[]))}
payload={'rows':rows,'models':models,'textures':textures};encoded=base64.b64encode(gzip.compress(json.dumps(payload,separators=(',',':'),ensure_ascii=False).encode())).decode();template=(root/'scripts/weapon_library/preview-template.html').read_text(encoding='utf-8');page=template.replace('__PAYLOAD__',encoded)
record=Path(r'Q:\GPT General\Weapon Model Library\2026-10-11')/('preview-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'));record.mkdir(parents=True);shutil.copy2(out/'catalog.html',record/'catalog-before.html');(out/'catalog.html').write_text(page,encoding='utf-8');shutil.copy2(out/'catalog.html',record/'catalog.html');(record/'validation.json').write_text(json.dumps({'models':len(models),'textures':len(textures),'missing_textures':missing,'pack_sha256':hashlib.sha256((root/'resourcepacks/Apocalypse_Weapon_Library.zip').read_bytes()).hexdigest(),'html_bytes':len(page.encode()),'animated_textures':'first frame preview'},indent=2));print('Models:',len(models),'Textures:',len(textures),'Missing:',missing,'HTML bytes:',len(page.encode()),'Record:',record)

