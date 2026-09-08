"""Activate the verified local v4 JAR, preserving v3 in disabledmods."""
from pathlib import Path
import hashlib, json, shutil, os
from build import ROOT, WORK, OUTPUT, EXPECTED, INPUT

def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    report=json.loads((WORK/'validation.json').read_text(encoding='utf-8'))
    expected='7f39ebd18545ada4963334b9f7ddc6cbb3f7c834e84f9ca03c1313f79a3d4118'
    assert digest(OUTPUT)==report['output_sha256']==expected, 'Output does not match verified build'
    assert all(report[k]=='passed' for k in ('linkage_audit','renderer_regression_tests','instrumented_terrain_tests'))
    assert digest(INPUT)==EXPECTED, 'Disabled baseline changed'
    destination=ROOT/'mods'/OUTPUT.name
    assert not destination.exists(), 'Already activated; inspect rather than overwrite'
    assert not list((ROOT/'mods').glob('sablewaves*.jar')), 'Another SableWaves variant is active'
    metadata=ROOT/'instance.json'; original=metadata.read_bytes(); text=original.decode('utf-8')
    instance=json.loads(text)
    matches=[m for m in instance['launcher']['mods'] if m.get('file')==INPUT.name]
    assert len(matches)==1 and matches[0]['disabled'] is True
    old=matches[0];new=dict(old)
    new.update(name=OUTPUT.name,file=OUTPUT.name,version='2.21.0',disabled=False)
    marker='"file": "'+INPUT.name+'"'
    assert text.count(marker)==1
    start=text.rfind('{',0,text.index(marker))
    decoded,length=json.JSONDecoder().raw_decode(text[start:]);assert decoded==old
    end=start+length
    addition='\n'.join('      '+line for line in json.dumps(new,ensure_ascii=False,indent=2).splitlines())
    updated=text[:end]+',\n'+addition+text[end:]
    parsed=json.loads(updated)
    expected_mods=list(instance['launcher']['mods']);expected_mods.insert(expected_mods.index(old)+1,new)
    expected_instance=dict(instance);expected_instance['launcher']=dict(instance['launcher']);expected_instance['launcher']['mods']=expected_mods
    assert parsed==expected_instance, 'Unexpected metadata changes'
    backups=WORK/'backups';backups.mkdir(exist_ok=True)
    backup=backups/'instance-before-v4.json';assert not backup.exists()
    backup.write_bytes(original)
    shutil.copy2(ROOT/'config/sablewaves.json',backups/'sablewaves-before-v4.json')
    temp=WORK/'instance-activated.tmp';temp.write_bytes(updated.encode('utf-8'))
    assert metadata.read_bytes()==original, 'Launcher metadata changed during preparation'
    shutil.copy2(OUTPUT,destination)
    assert digest(destination)==expected
    try:
        assert metadata.read_bytes()==original, 'Concurrent metadata modification'
        os.replace(temp,metadata)
    except BaseException:
        # Roll back only the newly created active file, within the instance.
        rollback=WORK/'activation-failed-v4.jar'
        assert not rollback.exists();destination.rename(rollback)
        raise
    current=json.loads(metadata.read_text(encoding='utf-8'))
    active=[m for m in current['launcher']['mods'] if m.get('file','').startswith('sablewaves') and not m.get('disabled',False)]
    assert len(active)==1 and active[0]['file']==destination.name
    assert INPUT.exists() and digest(INPUT)==EXPECTED
    assert (ROOT/'config/sablewaves.json').read_bytes()==(backups/'sablewaves-before-v4.json').read_bytes()
    result={'active':str(destination),'active_sha256':digest(destination),'inactive_baseline':str(INPUT),
            'launcher_disabled':False,'config_changed':False,'minecraft_launched':False}
    (WORK/'activation.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(json.dumps(result,indent=2))
if __name__=='__main__': main()
