"""Run response tests and sound-instance linkage against actual installed client classes."""
from pathlib import Path
import argparse,subprocess,json
p=argparse.ArgumentParser();p.add_argument('--build',type=Path,required=True);a=p.parse_args();r=a.build.resolve();here=Path(__file__).resolve().parent;root=here.parents[1];java=root.parents[1]/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
cp=str(r/'classes')+';'+(r/'classpath.txt').read_text();src=here/'tests/WindAudioCheck.java'
arg=r/'test.args';arg.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in ['-proc:none','--release','21','-cp',cp,'-d',str(r/'test-classes'),str(src)]));subprocess.run([str(java/'javac.exe'),'@'+str(arg)],check=True)
arg=r/'test-run.args';arg.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in ['-cp',str(r/'test-classes')+';'+cp,'WindAudioCheck']))
p=subprocess.run([str(java/'java.exe'),'@'+str(arg)],capture_output=True,text=True,cwd=r);(r/'test-results.txt').write_text(p.stdout+p.stderr);print(p.stdout+p.stderr);raise SystemExit(p.returncode)
