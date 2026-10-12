"""Build the pinned client phantom texture guard outside the live instance."""
import argparse,hashlib,json,pathlib,subprocess,zipfile
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--instance',type=pathlib.Path,default=pathlib.Path(r'Q:\Other Games\ATLauncher\instances\Apocalypse Industries WIP Build'))
p.add_argument('--output',type=pathlib.Path,required=True)
a=p.parse_args();src=pathlib.Path(__file__).parent/'src';root=a.instance.resolve();out=a.output.resolve()
if root==out or root in out.parents:raise SystemExit('Build output must be outside the instance')
out.mkdir(parents=True,exist_ok=False)
launcher=root.parents[1];lib=launcher/'libraries'
rail=root/'mods/railways-0.2.1+neoforge-mc1.21.1.jar'
assert hashlib.sha256(rail.read_bytes()).hexdigest()=='b7636c8b1b0352ed1a130dfe67f8bb574e2fc08803ed1cda4d3ea00505193914'
d=json.loads((root/'instance.json').read_text(encoding='utf-8'))
cp=[str(lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar'),str(lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar'),str(lib/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar'),str(lib/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-extra.jar'),str(rail)]
for x in d['libraries']:
    item=x.get('downloads',{}).get('artifact',{})
    if item and (lib/item['path']).is_file():cp.append(str(lib/item['path']))
classes=out/'classes';classes.mkdir()
args=['--release','21','-encoding','UTF-8','-proc:none','-classpath',';'.join(dict.fromkeys(cp)),'-d',str(classes),*map(str,src.rglob('*.java'))]
af=out/'javac.args';af.write_text('\n'.join(json.dumps(x) for x in args))
java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
subprocess.run([str(java/'javac.exe'),'@'+str(af)],check=True)
meta='modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="apocalypse_phantom_reload_guard"\nversion="1.0.0"\ndisplayName="Apocalypse Phantom Texture Reload Guard"\ndescription="Defers Steam n Rails phantom texture uploads while the loading overlay is active."\n[[mixins]]\nconfig="apocalypse-phantom-reload-guard.mixins.json"\n[[dependencies.apocalypse_phantom_reload_guard]]\nmodId="minecraft"\ntype="required"\nversionRange="[1.21.1]"\nside="CLIENT"\n[[dependencies.apocalypse_phantom_reload_guard]]\nmodId="neoforge"\ntype="required"\nversionRange="[21.1.248,21.2)"\nside="CLIENT"\n[[dependencies.apocalypse_phantom_reload_guard]]\nmodId="railways"\ntype="required"\nversionRange="[0.2.1+neoforge-mc1.21.1]"\nside="CLIENT"\n'
jar=out/'apocalypse-phantom-reload-guard-1.0.0.jar'
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
    for path in sorted(classes.rglob('*.class')):z.write(path,path.relative_to(classes).as_posix())
    for path in sorted(src.rglob('*.java')):z.write(path,'sources/'+path.relative_to(src).as_posix())
    z.writestr('META-INF/neoforge.mods.toml',meta)
    z.writestr('apocalypse-phantom-reload-guard.mixins.json',json.dumps({'required':True,'minVersion':'0.8','package':'dev.apocalypse.railways','compatibilityLevel':'JAVA_21','client':['PhantomReloadGuard'],'injectors':{'defaultRequire':1}},indent=2))
(out/'build.json').write_text(json.dumps({'artifact':jar.name,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'railways_sha256':hashlib.sha256(rail.read_bytes()).hexdigest(),'classpath':cp},indent=2))
print(jar)
