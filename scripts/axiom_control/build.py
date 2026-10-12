"""Build the command-controlled Axiom server addon outside the instance."""
import argparse,hashlib,json,pathlib,subprocess,zipfile
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--instance',type=pathlib.Path,default=pathlib.Path(r'Q:\Other Games\ATLauncher\instances\Apocalypse Industries WIP Build'))
p.add_argument('--output',type=pathlib.Path,required=True)
a=p.parse_args();src=pathlib.Path(__file__).parent/'src';root=a.instance.resolve();out=a.output.resolve()
if root==out or root in out.parents:raise SystemExit('Build outside the instance')
out.mkdir(parents=True,exist_ok=False);launcher=root.parents[1];lib=launcher/'libraries'
d=json.loads((root/'instance.json').read_text(encoding='utf-8'))
cp=[str(launcher/'servers/Chunkserve - Apocalypse Industries/libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-server.jar'),str(lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar'),str(lib/'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar'),str(lib/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar'),str(lib/'net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-extra.jar')]
for x in d['libraries']:
    item=x.get('downloads',{}).get('artifact',{})
    if item and (lib/item['path']).is_file():cp.append(str(lib/item['path']))
cp.append(str(root/'.cache/connector/Axiom-6.1.3-for-MC1.21.1_mapped_moj_1.21.1.jar'))
classes=out/'classes';classes.mkdir()
args=['--release','21','-encoding','UTF-8','-proc:none','-classpath',';'.join(dict.fromkeys(cp)),'-d',str(classes),*map(str,src.rglob('*.java'))]
af=out/'javac.args';af.write_text('\n'.join(json.dumps(x) for x in args))
java=launcher/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
subprocess.run([str(java/'javac.exe'),'@'+str(af)],check=True)
mod='apocalypse_axiom_control'
meta='modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="'+mod+'"\nversion="1.0.0"\ndisplayName="Apocalypse Axiom Server Control"\ndescription="Axiom starts disabled on dedicated servers. Operators opt in with /axiomserver enable; /axiomserver disable revokes access. Integrated singleplayer is preserved."\n[[mixins]]\nconfig="apocalypse-axiom-control.mixins.json"\n'
for mid,version in [('minecraft','[1.21.1]'),('neoforge','[21.1.248,21.2)'),('axiom','[6.1.3]'),('connector','[2.0.0-beta.17,)')]:
    meta+='[[dependencies.'+mod+']]\nmodId="'+mid+'"\ntype="required"\nversionRange="'+version+'"\nside="SERVER"\n'
jar=out/'apocalypse-axiom-control-1.0.0.jar'
with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED)as z:
    for path in sorted(classes.rglob('*.class')):z.write(path,path.relative_to(classes).as_posix())
    for path in sorted(src.rglob('*.java')):z.write(path,'sources/'+path.relative_to(src).as_posix())
    z.writestr('META-INF/neoforge.mods.toml',meta)
    z.writestr('apocalypse-axiom-control.mixins.json',json.dumps({'required':True,'minVersion':'0.8','package':'dev.apocalypse.axiomcontrol.mixin','compatibilityLevel':'JAVA_21','mixins':['AxiomPayloads','AxiomAuthorization'],'injectors':{'defaultRequire':1}},indent=2))
(out/'build.json').write_text(json.dumps({'artifact':jar.name,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'classpath':cp},indent=2))
print(jar)
