import pathlib,re,zipfile,hashlib,json,difflib
ROOT=pathlib.Path.cwd(); STAGE=ROOT/'.local/shader-port-2026-10-08'
OUT='ComplementaryUnbound_r5.8.1_EuphoriaPatches_1.9.3_APOCALYPSE_ATMOSPHERE_v1.1.zip'
def norm(b):return b.decode('utf8').replace('\r\n','\n').replace('\r','')
def replace_once(s,a,b):
 assert s.count(a)==1,(a,s.count(a));return s.replace(a,b,1)
def resolve(s,fn):
 pattern=r'^<<<<<<<[^\n]*\n(.*?)^=======\n(.*?)^>>>>>>>[^\n]*\n'
 return re.sub(pattern,lambda m:fn(m.group(1),m.group(2)),s,flags=re.M|re.S)
files={f.relative_to(STAGE/'euphoria').as_posix():f.read_bytes() for f in (STAGE/'euphoria').rglob('*') if f.is_file()}
# Official patcher normalizes to Reimagined before patching; restore Unbound afterwards.
files['shaders/lib/common.glsl']=replace_once(norm(files['shaders/lib/common.glsl']),'#define SHADER_STYLE 1','#define SHADER_STYLE 4').encode()
euphoria_baseline=files.copy()
for f in (STAGE/'merged').rglob('*'):
 if not f.is_file():continue
 name=f.relative_to(STAGE/'merged').as_posix();s=norm(f.read_bytes())
 if name.endswith('common.glsl'):
  s=resolve(s,lambda ours,theirs:ours)
  s=replace_once(s,'#define SHADER_STYLE 1','#define SHADER_STYLE 4')
 elif name.endswith('mainFog.glsl'):
  s=resolve(s,lambda ours,theirs:replace_once(theirs,'vec3 baseFogColor = mix(','vec3 baseFogColor = atmFogColor * mix('))
 elif name.endswith('/sky.glsl'):
  def glare(ours,theirs):
   return replace_once(replace_once(ours,'glare *= 0.7;','glare *= 0.95;'),'vec3(1.5, 0.7, 0.3) + vec3(0.0, 0.5, 0.5)','vec3(1.75, 0.58, 0.16) + vec3(0.0, 0.18, 0.12)')
  s=resolve(s,glare)
 elif name.endswith('/stars.glsl'):
  s=resolve(s,lambda ours,theirs:replace_once(ours,'    return stars;','    // Apocalypse Industries: industrial haze attenuates remaining stars.\n    return stars * 0.58;'))
 elif name.endswith('cloudColors.glsl'):
  def clouds(ours,theirs):
   theirs=replace_once(theirs,'    cloudRainColor * 0.5,\n    rainFactor\n);','    cloudRainColor * 0.5,\n    rainFactor\n) * mix(CLOUD_NIGHT_AMBIENT_MULTIPLIER, CLOUD_AMBIENT_MULTIPLIER, sunVisibility2);')
   return replace_once(theirs,'    noonFactor * rainFactor\n);','    noonFactor * rainFactor\n) * mix(CLOUD_NIGHT_LIGHT_MULTIPLIER, CLOUD_LIGHT_MULTIPLIER, sunVisibility2);')
  s=resolve(s,clouds)
 elif name.endswith('skyColors.glsl'):
  s=resolve(s,lambda ours,theirs:replace_once(theirs,'+ skyColor;','+ skyColorTweaked;'))
 elif name.endswith('gbuffers_skybasic.glsl'):
  def sun(ours,theirs):
   ours=replace_once(ours,'float sunBrightness = 25.0;','float sunBrightness = 28.0;')
   ours=replace_once(ours,'vec3(1.0, 0.698, 0.5451)','vec3(1.0, 0.38, 0.10)')
   return replace_once(ours,'vec3(0.9, 0.5, 0.3)','vec3(1.0, 0.38, 0.10)')
  s=resolve(s,sun)
 elif name.endswith('shaders.properties'):
  # Retain Euphoria's version guard and explicitly supply zero on 1.21.1.
  s=resolve(s,lambda ours,theirs:replace_once(ours,'    #endif\n','    #else\n    uniform.float.inPaleGarden=0.0\n    #endif\n'))
 else:raise AssertionError(name)
 assert '<<<<<<<' not in s and '>>>>>>>' not in s,name
 files[name]=s.encode('utf8')
# Euphoria moved these controls out of common.glsl. Change their actual definitions.
for name,values in {
 'shaders/lib/shaderSettings/clouds.glsl':{'CLOUD_UNBOUND_AMOUNT':('1.00','1.12'),'CLOUD_UNBOUND_RAIN_ADD':('0.40','0.70')},
 'shaders/lib/shaderSettings/stars.glsl':{'NIGHT_STAR_AMOUNT':('2','1')},
 'shaders/lib/shaderSettings/composite1.glsl':{'RAINBOWS':('1','0')},
}.items():
 s=norm(files[name])
 for key,(a,b) in values.items():s=replace_once(s,'#define '+key+' '+a+' //','#define '+key+' '+b+' //')
 files[name]=s.encode()
modified=sorted(f for f in files if files[f]!=euphoria_baseline[f])
# All unaffected Euphoria material mappings, rendering programs and assets are byte-identical.
assert len(modified)==10,modified
assert files['shaders/block.properties']==euphoria_baseline['shaders/block.properties']
for f in files:
 if '/dh_' in f:assert files[f]==euphoria_baseline[f],f
errors=[]
for f in modified:
 text=files[f].decode('utf8')
 if f.endswith('.glsl'):
  cleaned=re.sub(r'/\*.*?\*/|//[^\n]*','',text,flags=re.S)
  stack=[];pairs={')':'(',']':'[','}':'{'}
  for ch in cleaned:
   if ch in '([{':stack.append(ch)
   elif ch in pairs:
    if not stack or stack.pop()!=pairs[ch]:errors.append(f+' delimiter mismatch');break
  if stack:errors.append(f+' unclosed delimiter')
  level=0
  for line in cleaned.splitlines():
   if re.match(r'\s*#\s*(if|ifdef|ifndef)\b',line):level+=1
   elif re.match(r'\s*#\s*endif\b',line):level-=1
   if level<0:errors.append(f+' preprocessor underflow')
  if level:errors.append(f+' unclosed preprocessor block')
  for inc in re.findall(r'#include\s+"([^"]+)"',text):
   if '$' in inc:continue
   target='shaders'+inc if inc.startswith('/') else str(pathlib.PurePosixPath(f).parent/inc)
   if target not in files:errors.append(f+' missing include '+target)
 assert '<<<<<<<' not in text
# Regression: color includes must use declaration initializers at global scope.
for f in ['shaders/lib/colors/skyColors.glsl','shaders/lib/colors/cloudColors.glsl']:
 clean=re.sub(r'/\*.*?\*/|//[^\n]*','',files[f].decode(),flags=re.S)
 for statement in clean.split(';'):
  statement=re.sub(r'^\s*#.*$','',statement,flags=re.M).strip()
  if re.match(r'^\w+\s*(?:\.[xyzwrgba]+)?\s*(?:=|\*=|\+=)',statement):errors.append(f+' global assignment '+statement[:80])
assert not errors,errors
archive=ROOT/'shaderpacks'/OUT
assert not archive.exists(),archive
note='Apocalypse Atmosphere v1.1 port to Euphoria 1.9.3 / Complementary Unbound r5.8.1.\nSource edits verified against local stock Complementary. Euphoria-specific controls retained.\nStatic validation passed; Iris compilation and gameplay have NOT been tested.\n'
files['APOCALYPSE_EUPHORIA_PORT.txt']=note.encode()
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 for f in sorted(files):z.writestr(f,files[f])
with zipfile.ZipFile(archive) as z:assert z.testzip() is None
old=ROOT/'shaderpacks/ComplementaryUnbound_r5.8.1_APOCALYPSE_ATMOSPHERE_v1.1_PARSEFIX_TESTED.zip.txt'
(archive.parent/(OUT+'.txt')).write_bytes(old.read_bytes())
record=ROOT/'docs/shader-port-2026-10-08';record.mkdir(exist_ok=True)
(record/'apocalypse-vs-stock.patch').write_bytes((STAGE/'apocalypse-vs-stock.patch').read_bytes())
diff=[]
for f in modified:diff.extend(difflib.unified_diff(norm(euphoria_baseline[f]).splitlines(True),norm(files[f]).splitlines(True),fromfile='euphoria/'+f,tofile='apocalypse-euphoria/'+f))
(record/'apocalypse-vs-euphoria.patch').write_text(''.join(diff),encoding='utf8')
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
meta={'date':'2026-10-08','base':'Complementary Unbound r5.8.1','euphoria':'1.9.3','source':'APOCALYPSE_ATMOSPHERE_v1.1_PARSEFIX_TESTED','output':OUT,'modified_euphoria_files':modified,'validation':'static checks passed; no Iris compilation or gameplay test','base_tar_sha256':sha(STAGE/'stock.tar'),'base_tar_matches_official_expected':sha(STAGE/'stock.tar')=='b73e323a886fe9f67df2c38528ce77e2d73e0af7d0dd30515b516b59e249df24','hashes':{str(p.relative_to(ROOT)):sha(p) for p in [ROOT/'shaderpacks/ComplementaryUnbound_r5.8.1.zip',ROOT/'shaderpacks/ComplementaryUnbound_r5.8.1_APOCALYPSE_ATMOSPHERE_v1.1_PARSEFIX_TESTED.zip',STAGE/'EuphoriaPatcher-1.9.3-r5.8.1-neoforge.jar',archive]},'settings_copied_from':old.name}
assert meta['base_tar_matches_official_expected']
(record/'manifest.json').write_text(json.dumps(meta,indent=2),encoding='utf8')
print(json.dumps(meta,indent=2))
