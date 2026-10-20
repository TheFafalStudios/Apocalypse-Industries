"""Build the pinned client-only Deep Seas lava visuals addon outside the instance."""
from pathlib import Path
import argparse, hashlib, json, os, subprocess, zipfile
HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
LAUNCHER = ROOT.parents[1]
JAVA = LAUNCHER / 'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'

def build(out, upstream, expected):
    local_build = (ROOT / '.local').resolve() in out.parents
    if out.exists() or out == ROOT or (ROOT in out.parents and not local_build):
        raise SystemExit('Use a new build directory outside distribution paths (or under .local)')
    assert hashlib.sha256(upstream.read_bytes()).hexdigest() == expected, 'Deep Seas input changed'
    out.mkdir(parents=True)
    libs = LAUNCHER / 'libraries'
    cp = [libs / 'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',
          libs / 'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar', upstream,
          ROOT/'mods/iris-neoforge-1.8.14-beta.1+mc1.21.1.jar']
    for base, dirs, files in os.walk(libs, followlinks=False):
        dirs[:] = [d for d in dirs if not (Path(base)/d).is_symlink() and
                   not getattr((Path(base)/d).lstat(), 'st_file_attributes', 0) & 1024]
        cp.extend(Path(base)/n for n in files if n.endswith('.jar'))
    cp = list(dict.fromkeys(cp))
    deps = out / 'deps'; deps.mkdir()
    for jar in list(cp):
        with zipfile.ZipFile(jar) as z:
            for name in z.namelist():
                if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
                    target = deps / (jar.name + '-' + Path(name).name)
                    target.write_bytes(z.read(name)); cp.append(target)
    classes = out / 'classes'; classes.mkdir()
    sources = sorted((HERE/'src').rglob('*.java')) + sorted((HERE/'tests').rglob('*.java'))
    args = ['--release','21','-proc:none','-encoding','UTF-8','-cp',os.pathsep.join(map(str,cp)),
            '-d',str(classes),*map(str,sources)]
    argfile = out / 'javac.args'
    argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
    subprocess.run([str(JAVA/'javac.exe'),'@'+str(argfile)],check=True)
    (out/'classpath.txt').write_text(os.pathsep.join(map(str,[classes,*cp])))
    metadata = '''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_submarine_lava"
version="1.1.2"
displayName="Apocalypse Submarine and Vortex Shader Bridge"
description="Hull-aware lava fog for Deep Seas and Complementary shaders. Client only."
[[mixins]]
config="apocalypse-submarine-lava.mixins.json"
[[dependencies.apocalypse_submarine_lava]]
modId="create_submarine"
type="required"
versionRange="[3.4.4]"
side="CLIENT"
[[dependencies.apocalypse_submarine_lava]]
modId="iris"
type="required"
versionRange="[1.8.14-beta.1+mc1.21.1]"
side="CLIENT"
[[dependencies.apocalypse_submarine_lava]]
modId="minecraft"
type="required"
versionRange="[1.21.1]"
side="CLIENT"
'''
    artifact = out/'apocalypse-submarine-lava-visuals-1.1.2.jar'
    with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
        for path in sorted(classes.rglob('*.class')):
            if not path.name.endswith('Regression.class'): z.write(path,path.relative_to(classes).as_posix())
        for path in (HERE/'src').rglob('*.java'): z.write(path,'sources/'+path.relative_to(HERE/'src').as_posix())
        for path in (HERE/'resources').rglob('*'):
            if path.is_file(): z.write(path,path.relative_to(HERE/'resources').as_posix())
        z.writestr('META-INF/neoforge.mods.toml',metadata)
        z.writestr('apocalypse-submarine-lava.mixins.json',json.dumps({
            'required':True,'minVersion':'0.8','compatibilityLevel':'JAVA_21',
            'package':'dev.apocalypse.submarinelava.mixin','client':['FogState','HullBinding','HullAccess','LavaSampler','SamplerProgramAccess','VortexEnabled','VortexMistEnabled','VortexCompute','VortexNativeAtmosphere','VortexFieldAccess'],
            'injectors':{'defaultRequire':1}}))
    report = {'artifact':str(artifact),'sha256':hashlib.sha256(artifact.read_bytes()).hexdigest(),
              'deep_seas_sha256':expected,'deep_seas_jar':str(upstream),'validation':'Compiled; regression and runtime checks pending'}
    (out/'build.json').write_text(json.dumps(report,indent=2)+'\n')
    print(json.dumps(report,indent=2))

if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__); p.add_argument('--output',required=True,type=Path)
    p.add_argument('--deep-seas', type=Path, default=ROOT/'mods/create_submarine-3.4.4.jar')
    p.add_argument('--deep-seas-sha256', required=True)
    args = p.parse_args()
    build(args.output.resolve(), args.deep_seas.resolve(), args.deep_seas_sha256)
