"""Pinned bytecode call-site audit and executable policy boundary tests (no game launch)."""
from pathlib import Path
import argparse,json,subprocess,re,zipfile
from build import ROOT,JAVA
p=argparse.ArgumentParser();p.add_argument('--build',required=True,type=Path);a=p.parse_args();out=a.build.resolve()
cp=(out/'classpath.txt').read_text()
args=out/'policy-test.args';args.write_text('-cp\n"'+cp.replace('\\','/')+'"\ndev.apocalypse.subbalance.PolicyRegression\n')
r=subprocess.run([str(JAVA/'java.exe'),'@'+str(args)],text=True,capture_output=True)
assert r.returncode==0,r.stdout+r.stderr
target='com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem'
dump=subprocess.run([str(JAVA/'javap.exe'),'-p','-c','-s','-classpath',str(ROOT/'mods/create_submarine-3.4.4.jar'),target],capture_output=True,text=True,check=True).stdout
(out/'upstream-pressure-bytecode.txt').write_text(dump)
checks=[]
for method in ['processSubmarine','materials','scanWeakest','copiedMaterial']:
    block=next(b for b in re.split(r'\n  (?=(?:public|private|protected) )',dump) if re.search(r'\b'+method+r'\(',b.split('\n')[0]))
    assert block.count('HullStrengthConfig.getFor:')==1,(method,'changed call site')
    checks.append(method+': exactly one getFor redirect target')
for method in ['effectiveDepth','getActualBlockState','invalidate']:
    assert re.search(r'\b'+method+r'\(',dump),method
    checks.append(method+': target exists')
assert '(Ljava/util/UUID;Lnet/minecraft/core/BlockPos;I)I' in dump
assert 'java/util/Map' in dump and all(n in dump for n in ['MATERIALS','WEAK_CACHE'])
checks.append('effectiveDepth argument descriptor and cache fields match')
with zipfile.ZipFile(out/'apocalypse-submarine-balance-1.0.0.jar') as z:
    for name in z.namelist():
        if name.endswith('.class'):
            assert b'net/minecraft/client/' not in z.read(name),name
    assert not any(n.endswith('Regression.class') for n in z.namelist())
checks.append('common-side artifact contains no client class dependencies or test entry point')
report={'policy':r.stdout.strip(),'bytecodeChecks':checks,'status':'PASS','limitations':'Compilation, policy execution and pinned call-site audit; not a Minecraft mixin application or full vessel gameplay test.'}
(out/'validation.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
