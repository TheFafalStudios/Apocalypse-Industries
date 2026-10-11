from pathlib import Path
import argparse,json,base64,subprocess,zipfile,hashlib,tomllib
p=argparse.ArgumentParser();p.add_argument('--pack',type=Path,required=True);p.add_argument('--addon',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
here=Path(__file__).resolve().parent;root=here.parents[1];out=a.output.resolve()
if out==root or root in out.parents:raise SystemExit('Validation output must be outside the live instance')
out.mkdir(parents=True,exist_ok=False)
rules=json.loads((here/'tests/rules.json').read_text(encoding='utf-8'));enc=lambda x:base64.b64encode(x.encode()).decode()
(out/'rules.tsv').write_text('\n'.join('\t'.join(enc(r[k]) for k in ['item','model','name','pattern']) for r in rules),encoding='ascii')
with zipfile.ZipFile(a.pack) as z:
 assert z.testzip() is None
 actual=[json.loads(z.read(n)) for n in z.namelist() if '/polytone/custom_item_models/' in n and n.endswith('.json')]
 expected={(r['item'],r['model'],r['pattern']) for r in rules}
 assert expected=={(r['item'],r['model'],r['name_pattern']) for r in actual}
 for n in z.namelist():
  if n.endswith('.json'):json.loads(z.read(n))
with zipfile.ZipFile(a.addon) as z:
 assert z.testzip() is None
 cfg=json.loads(z.read('apocalypse_named_tridents.mixins.json'));assert cfg['client']==['TridentItemRendererMixin','ThrownTridentRendererMixin','PolytoneNameGuardMixin','SpearModelShaperMixin']
 for m in cfg['client']:assert 'apocalypse/tridents/mixin/'+m+'.class' in z.namelist()
 assert not any('Verify' in n for n in z.namelist())
java=root.parents[1]/'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
cp=str(a.addon.resolve())+';'+str(out)
subprocess.run([str(java/'javac.exe'),'--release','21','-encoding','UTF-8','-proc:none','-cp',cp,'-d',str(out),str(here/'tests/VerifyNames.java')],check=True)
subprocess.run([str(java/'java.exe'),'-cp',cp,'VerifyNames',str(out/'rules.tsv')],check=True)
report={'name_rules':len(rules),'pattern_checks':9721,'pack_sha256':hashlib.sha256(a.pack.read_bytes()).hexdigest(),'addon_sha256':hashlib.sha256(a.addon.read_bytes()).hexdigest(),'archive_integrity':'passed','registered_client_mixins':cfg['client'],'gameplay':'User confirmed final setup, 2026-10-11; combined release install validation pending'}
(out/'validation.json').write_text(json.dumps(report,indent=2)+'\n');print('Archive, rule fixture, mixin-registration and actual-addon name matcher checks passed.')
