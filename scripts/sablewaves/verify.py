import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parent))
from build import WORK,SOURCE,LAUNCHER,run,OUTPUT
cp = str(OUTPUT) + ';' + (WORK/'classpath.txt').read_text(encoding='utf-8')
asm = ';'.join(str(LAUNCHER / f'libraries/org/ow2/asm/{n}/9.8/{n}-9.8.jar') for n in ['asm','asm-tree'])
classes=WORK/'build/audit'; classes.mkdir(parents=True,exist_ok=True)
run('javac',['--release','21','-proc:none','-classpath',cp+';'+asm,'-d',classes,SOURCE/'LinkageAudit.java',SOURCE/'tests/RendererTests.java'],'audit-compile')
run('java',['-Xverify:all','-classpath',str(classes)+';'+asm+';'+cp,'LinkageAudit',OUTPUT],'linkage-audit')
run('java',['-Xverify:all','-classpath',str(classes)+';'+cp,'RendererTests'],'renderer-tests')


import terrain_tests
import hashlib, json
(WORK/'validation.json').write_text(json.dumps({
    'output_sha256':hashlib.sha256(OUTPUT.read_bytes()).hexdigest(),
    'linkage_audit':'passed', 'renderer_regression_tests':'passed',
    'instrumented_terrain_tests':'passed', 'v3_regressions_reproduced':True,
    'minecraft_gameplay_tested':False},indent=2),encoding='utf-8')
