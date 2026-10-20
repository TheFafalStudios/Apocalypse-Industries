"""Stage only the Vortex bridge additions to the exact WIP shader baseline."""
from pathlib import Path
import argparse,hashlib,json,zipfile
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[1]
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--output',type=Path,required=True)
p.add_argument('--baseline',type=Path,default=ROOT/'shaderpacks/ApocalypseAtmosphere_NetherDH_v1.2.zip')
a=p.parse_args();out=a.output.resolve();out.mkdir(parents=True,exist_ok=True)
source=a.baseline.resolve()
assert hashlib.sha256(source.read_bytes()).hexdigest()=='b25316d9c946022f6915e98cba8fed33b55b1402874c0b6ea88e2726ea87f92a','Shader baseline changed'
with zipfile.ZipFile(source) as z:entries={n:z.read(n) for n in z.namelist()}
name='shaders/program/composite1.glsl';s=entries[name].decode().replace('\r','')
anchor='//Program//\nvoid main() {'
assert s.count(anchor)==2
s=s.replace(anchor,'#include "/lib/lighting/apocalypseVortex.glsl"\n\n'+anchor,1)
anchor='    color = pow(color, vec3(2.2));'
assert s.count(anchor)==1
s=s.replace(anchor,'    AiApplyVortex(color, texCoord, z0, z1);\n\n'+anchor)
entries[name]=s.encode()
entries['shaders/lib/lighting/apocalypseVortex.glsl']=(HERE/'shaders/apocalypseVortex.glsl').read_bytes()
name='shaders/shaders.properties';s=entries[name].decode().replace('\r','')
s=s.replace('uniform.float.inPaleGarden=0.0 # MC 1.21.1 compatibility: Pale Garden biome ID does not exist yet','uniform.float.inPaleGarden=0.0')
s=s.replace('variable.float.endFlashFactor0=min(endFlashIntensity, 0.5) * 2.0','variable.float.endFlashFactor0=0.0')
entries[name]=s.encode()
target=out/source.name
assert not target.exists()
with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
 for name,data in entries.items():z.writestr(name,data)
(out/'shader-build.json').write_text(json.dumps({'source_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),
 'artifact':str(target),'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),
 'changed_entries':['shaders/program/composite1.glsl','shaders/lib/lighting/apocalypseVortex.glsl','shaders/shaders.properties']},indent=2)+'\n')
print(target)
