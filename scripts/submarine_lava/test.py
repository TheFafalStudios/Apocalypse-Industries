"""Regression-test the built addon against installed Deep Seas, Iris, and shader sources."""
from pathlib import Path
import argparse, hashlib, json, re, subprocess, zipfile
from build import ROOT, JAVA
p=argparse.ArgumentParser(description=__doc__); p.add_argument('--build',type=Path,required=True)
p.add_argument('--shader',type=Path,default=ROOT/'shaderpacks/ApocalypseAtmosphere_NetherDH_v1.2.zip')
a=p.parse_args();out=a.build.resolve();shader=a.shader.resolve()
deep=Path(json.loads((out/'build.json').read_text())['deep_seas_jar'])
iris=ROOT/'mods/iris-neoforge-1.8.14-beta.1+mc1.21.1.jar'
with zipfile.ZipFile(shader) as z:
    def expand(name):
        return re.sub(r'^\s*#include\s+"([^"]+)"',lambda m:expand('shaders/'+m[1].lstrip('/')),
                      z.read(name).decode(),flags=re.M)
    (out/'composite1.glsl').write_text(expand('shaders/world-1/composite1.fsh'),encoding='utf-8')
    for world in ['world0','world-1','world1']:
        (out/(world+'-composite1.glsl')).write_text(expand('shaders/'+world+'/composite1.fsh'),encoding='utf-8')
with zipfile.ZipFile(deep) as z:
    (out/'ShaderpackSupport.class').write_bytes(z.read('com/maxenonyme/createsubmarine/submarine/compat/iris/ShaderpackSupport.class'))
cp=(out/'classpath.txt').read_text()
for path in (ROOT/'mods').glob('sodium-neoforge-*.jar'):
    cp += ';' + str(path)
    with zipfile.ZipFile(path) as z:
        for name in z.namelist():
            if name.endswith('.jar') and name.startswith('META-INF/jarjar/'):
                target = out/'deps'/Path(name).name
                target.write_bytes(z.read(name)); cp += ';' + str(target)
def run(main,args):
    values=['-cp',cp,main,*map(str,args)]
    f=out/(main.rsplit('.',1)[-1]+'.args')
    f.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in values))
    r=subprocess.run([str(JAVA/'java.exe'),'@'+str(f)],capture_output=True,text=True)
    if r.returncode:
        print(r.stdout); print(r.stderr); r.check_returncode()
    print(r.stdout.strip()); return r.stdout.strip()
results=[run('dev.apocalypse.submarinelava.Regression',[out/'composite1.glsl',out/'ShaderpackSupport.class',out/'patched-composite1.glsl']),
         run('dev.apocalypse.submarinelava.GlRegression',[out/'patched-composite1.glsl'])]
native=out/'native';native.mkdir(exist_ok=True)
with zipfile.ZipFile(deep) as z:
    for name in z.namelist():
        if '/vortex_' in name and name.endswith('.fsh'):
            (native/Path(name).name).write_bytes(z.read(name))
results.append(run('dev.apocalypse.submarinelava.VortexRegression',[
    Path(__file__).parent/'shaders/apocalypseVortex.glsl',native,
    *[out/(w+'-composite1.glsl') for w in ['world0','world-1','world1']]]))
targets={
    'com.maxenonyme.createsubmarine.submarine.client.SubmarineFogHandler': ('onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent$Post)',),
    'com.maxenonyme.createsubmarine.submarine.compat.iris.ShaderpackSupport': ('patchFragment(java.lang.String)', 'void bindHull()', 'int hullDepth()', 'DynamicTexture outside()'),
    'net.irisshaders.iris.gl.program.ProgramSamplers$Builder': ('ProgramSamplers build()',),
    'com.maxenonyme.vortexengine.light.LightVolumes': ('onRenderStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent)', 'draw(net.neoforged.neoforge.client.event.RenderLevelStageEvent, net.minecraft.world.phys.Vec3)'),
    'com.maxenonyme.vortexengine.light.VortexLights': ('boolean usable()',),
    'com.maxenonyme.vortexengine.mist.MistField': ('boolean usable()',),
    'com.maxenonyme.vortexengine.atmosphere.Atmosphere': ('onRenderStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent)',),
    'com.maxenonyme.vortexengine.mist.MistRenderer': ('onRenderStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent)',),
    'com.maxenonyme.vortexengine.light.LightField': ('int originX;', 'int originY;', 'int originZ;', 'int mode;'),
}
for name, signatures in targets.items():
    jar=iris if name.startswith('net.irisshaders') else deep
    text=subprocess.run([str(JAVA/'javap.exe'),'-p','-classpath',str(jar),name],check=True,capture_output=True,text=True).stdout
    for signature in signatures: assert signature in text,(name,signature)
results.append(str(sum(map(len,targets.values())))+' mixin target/accessor signatures found in installed jars')
report=json.loads((out/'build.json').read_text())
report.update(validation=results,shader_sha256=hashlib.sha256(shader.read_bytes()).hexdigest(),
              iris_sha256=hashlib.sha256(iris.read_bytes()).hexdigest(),runtime='Minecraft not launched; mixin application and gameplay unverified')
(out/'validation.json').write_text(json.dumps(report,indent=2)+'\n')
print(results[-1])
