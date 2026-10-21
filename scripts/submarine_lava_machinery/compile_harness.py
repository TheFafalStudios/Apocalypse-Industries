"""Recompile only isolated test code, preserving the exact production artifact under test."""
from pathlib import Path
import argparse,hashlib,json,subprocess,zipfile
from build import HERE,JAVA,JAR
p=argparse.ArgumentParser();p.add_argument('--build',required=True,type=Path);a=p.parse_args();out=a.build.resolve()
before=hashlib.sha256((out/JAR).read_bytes()).hexdigest()
args=['--release','21','-proc:none','-encoding','UTF-8','-cp',(out/'classpath.txt').read_text(),'-d',str(out/'classes'),*map(str,sorted((HERE/'tests').rglob('*.java')))]
argfile=out/'harness-javac.args';argfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(argfile)],check=True)
artifact=out/'ballast-native-harness.jar'
with zipfile.ZipFile(artifact) as z:metadata=z.read('META-INF/neoforge.mods.toml')
if b'ballast-fixture.mixins.json' not in metadata:metadata+=b'\n[[mixins]]\nconfig="ballast-fixture.mixins.json"\n'
with zipfile.ZipFile(artifact,'w',zipfile.ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml',metadata)
    z.writestr('ballast-fixture.mixins.json',json.dumps({'required':True,'minVersion':'0.8','compatibilityLevel':'JAVA_21','package':'dev.apocalypse.lavamachinery.tests.trace','mixins':['RemovalTrace','MarkTrace'],'injectors':{'defaultRequire':1}}))
    for path in sorted((out/'classes/dev/apocalypse/lavamachinery/tests').rglob('*.class')):
        if path.parent.name=='tests' and path.name=='RemovalTrace.class':continue
        z.write(path,path.relative_to(out/'classes').as_posix())
assert hashlib.sha256((out/JAR).read_bytes()).hexdigest()==before
print('Test harness compiled; production artifact unchanged: '+before)
