"""Compare matched disabled-patch and enabled-patch fixtures; reject incompatible evidence."""
from pathlib import Path
import argparse,hashlib,json,zipfile
from build import JAR
p=argparse.ArgumentParser();p.add_argument('--baseline',required=True,type=Path);p.add_argument('--candidate',required=True,type=Path);p.add_argument('--output',required=True,type=Path);a=p.parse_args()
assert not a.output.exists(),'Completed evidence must not be overwritten'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
load=lambda p:json.loads(p.read_text())
bc=load(a.baseline/'context.json');cc=load(a.candidate/'context.json')
assert bc['baseline'] and not cc['baseline'] and bc['method']==cc['method']
assert bc['harnessSha256']==cc['harnessSha256'] and bc['candidateSha256']==cc['testedJarSha256']
configs=['server.properties','config/create_submarine-server.toml','config/submarine_hull.json']
def config_sha(path):
    if path.name=='server.properties':
        # Minecraft rewrites a wall-clock timestamp comment on every isolated launch.
        data='\n'.join(line for line in path.read_text().splitlines() if line and not line.startswith('#')).encode()
        return hashlib.sha256(data).hexdigest()
    return sha(path)
assert all(config_sha(a.baseline/f)==config_sha(a.candidate/f) for f in configs),'Test configuration differs'
with zipfile.ZipFile(a.baseline/'mods'/JAR) as b,zipfile.ZipFile(a.candidate/'mods'/JAR) as c:
    assert set(b.namelist())==set(c.namelist())
    changed=[n for n in b.namelist() if b.read(n)!=c.read(n)]
    assert changed==['apocalypse-lava-ballast.mixins.json'],changed
b=load(a.baseline/'ballast-world-validation.json');c=load(a.candidate/'ballast-world-validation.json')
assert b['pass'] and c['pass']
def rows(report):return {row['fluid']:row for row in report['assemblies']}
br=rows(b);cr=rows(c)
assert br['lava']['emptyTankForceY']==br['lava']['fullTankForceY']==0
assert cr['lava']['fullTankForceY']<0 and cr['lava']['initialVelocityY']>0
for key in ['emptyTankForceY','fullTankForceY','initialVelocityY']:
    assert abs(br['water'][key]-cr['water'][key])<1e-6,('Water regression',key)
result={'pass':True,'method':cc['method'],'harnessSha256':cc['harnessSha256'],'candidateSha256':cc['testedJarSha256'],
    'configurationSha256':{f:{'baselineRaw':sha(a.baseline/f),'candidateRaw':sha(a.candidate/f),'compared':config_sha(a.candidate/f)} for f in configs},
    'normalization':'Only server.properties comment/blank lines excluded; all setting lines and both config files match. Raw hashes retained.',
    'baseline':b,'candidate':c,
    'result':'Original lava control absent; patched lava ascent and commanded descent work; matched water control values preserved.',
    'limit':'Small assembled fixture, real fluid capabilities and force/velocity response; not a piloted full-size vessel or long-duration benchmark.'}
a.output.write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result,indent=2))
