"""Prepare a narrow v1.4.1 release from the published v1.4 checkout.

No network publication, server mutation or game activation. Explicit source and
evidence paths only; never copy the WIP index or traverse its saved storage.
"""
import hashlib, json, re, shutil, subprocess, sys, tomllib, zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = Path('Q:/GPT General/apocalypse-v1.4.1-hotfix-20261021/attempt-01')
C = OUT/'github-candidate'
BASE = '0f81a196b4c79e9470b6c8a7ce40e5dbb2bd9093'
JAR = 'mods/apocalypse-submarine-lava-ballast-1.0.0.jar'
EXPECTED = 'ac2090061afde70e0132987c2380740289f03a0d83930f7b038a31423ab33f1e'

def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def git(*args): return subprocess.check_output(['git', '-c', 'safe.directory='+C.as_posix(), *args], cwd=C, text=True).strip()
def copy(src, dest):
    assert not src.is_symlink() and not src.is_junction()
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dest)
    assert sha(src) == sha(dest)

def main():
    assert git('rev-parse', 'HEAD') == BASE
    assert not git('status', '--porcelain')
    git('switch', '-c', 'codex/v1.4.1-lava-ballast')
    old_index = tomllib.loads((C/'index.toml').read_text())['files']
    assert (C/'VERSION').read_text().strip() == 'v1.4'
    assert sha(ROOT/JAR) == EXPECTED
    checks = ROOT/'.local/lava-ballast-20261021'
    native = json.loads((checks/'world-candidate-10/ballast-native-validation.json').read_text())
    comparison = json.loads((checks/'comparison-01.json').read_text())
    assert native['pass'] and native['checks'] == 304
    assert comparison['pass'] and comparison['candidateSha256'] == EXPECTED
    for rel, value in comparison['configurationSha256'].items():
        if rel.startswith('config/'):
            assert sha(C/rel) == value['compared'], 'Published configuration differs from tested fixture: '+rel
    with zipfile.ZipFile(ROOT/JAR) as z:
        assert json.loads(z.read('apocalypse-lava-ballast.mixins.json'))['mixins'] == ['TankAccess','TankLiquids','VentLiquids','FloaterLiquids']
        for name in z.namelist():
            assert '/tests/' not in name
            if name.endswith('.class'):
                for forbidden in [b'net/minecraft/client/', b'SubmarinePropellerBlockEntity', b'WaterThrusterBlockEntity', b'BoatBuoyancySystem']:
                    assert forbidden not in z.read(name)
    copy(ROOT/JAR, C/JAR)
    source = ROOT/'scripts/submarine_lava_machinery'
    from release_safety import files
    for path in files(source):
        if path.suffix in {'.py','.java','.md'}: copy(path, C/path.relative_to(ROOT))
    copy(Path(__file__), C/'scripts'/Path(__file__).name)
    copy(ROOT/'docs/LAVA_BALLAST_2026-10-21.md', C/'docs/LAVA_BALLAST_2026-10-21.md')
    evidence = C/'docs/releases/v1.4.1-evidence'
    for rel in ['build-02/build.json','build-02/validation.json','comparison-01.json',
                'world-candidate-10/ballast-native-validation.json','world-candidate-10/ballast-world-validation.json',
                'world-baseline-01/ballast-native-validation.json','world-baseline-01/ballast-world-validation.json']:
        copy(checks/rel, evidence/rel)
    notes = '''# Apocalypse Industries v1.4.1 — Lava ballast hotfix

- Ballast tanks, vents and floaters work in lava while preserving their existing force, speed and pressure limits.
- Vents and clusters retain actual water/lava identity, reject mixing and conserve fluid during transfers. Empty water-filled tanks before taking in lava.
- Propulsion remains unchanged: this hotfix does not enable propellers or water thrusters in lava.

Built on published v1.4 with no other gameplay changes. The exact shipped addon passed 304 runtime assertions and matched assembled water/lava tests; the original water response is preserved. Separate manual testing was waived by the user. A piloted full-size submarine and multiplayer operation remain unverified.

Update clients and server together. Client relaunch and a user-managed server restart are required. Server pressure mode stays HYBRID. Worlds, schematics and generated LOD data are excluded.
'''
    (OUT/'RELEASE_NOTES.md').write_text(notes, encoding='utf-8')
    (C/'docs/releases/V1_4_1_HOTFIX.md').write_text(notes, encoding='utf-8')
    p = C/'CHANGELOG.md'
    p.write_text('# Changelog\n\n## v1.4.1 — 2026-10-21\n\n'+notes.split('\n',1)[1]+'\n'+p.read_text(encoding='utf-8-sig').removeprefix('# Changelog').lstrip(), encoding='utf-8')
    p = C/'docs/TEST_WORKFLOW_IMPROVEMENTS.md'
    p.write_text(p.read_text(encoding='utf-8-sig')+'''

## 2026-10-21 — v1.4.1 ballast hotfix publication

Observed weakness: a passing WIP artifact can accidentally pick up unrelated runtime changes when released from a dirty development instance. The server twin also lags the published release and cannot safely stand in for the live baseline.

Improvement: prepare from the exact published v1.4 commit, enforce an index delta of one common-side addon, freeze only explicit JSON evidence, and require the published pressure/hull config hashes to match the assembled test fixture. Validate the committed export as well as working files. The deployment compares actual server dependencies and pressure mode before transferring the narrow hotfix, keeps rollback copies and verifies read-back hashes before advancing its unverified-baseline marker.

Validation: the 304-assertion and matched assembled-test evidence is bound to the shipped artifact SHA-256. Distribution/export validation checks the complete release; target-specific deployment results are recorded separately. No new manual test is required by this method, and it does not establish full-vessel or multiplayer correctness. The historical local twin remains explicitly behind rather than being relabelled or silently synchronized.
''', encoding='utf-8')
    (C/'index.toml').write_bytes((C/'index.toml').read_bytes()+('\n[[files]]\nfile = "'+JAR+'"\nhash = "'+EXPECTED+'"\n').encode())
    new_index = tomllib.loads((C/'index.toml').read_text())['files']
    assert new_index[:-1] == old_index and len(new_index) == len(old_index)+1
    pack = C/'pack.toml'
    text = re.sub(r'(?m)^version\s*=.*$', 'version = "v1.4.1"', pack.read_text())
    text = re.sub(r'(?m)^hash\s*=.*$', 'hash = "'+sha(C/'index.toml')+'"', text)
    pack.write_text(text, encoding='utf-8')
    (C/'VERSION').write_text('v1.4.1\n')
    p = C/'.gitignore'
    p.write_text(p.read_text()+'\n# Common-side lava ballast hotfix\n!/'+JAR+'\n')
    subprocess.run([sys.executable, str(C/'scripts/validate_distribution.py')], check=True)
    subprocess.run(['powershell.exe','-NoProfile','-File',str(C/'scripts/Build-ATLauncherBootstrap.ps1'),'-InstanceRoot',str(C)], check=True)
    git('add','--all')
    git('-c','user.name=Codex','-c','user.email=codex@openai.com','commit','-m','Release v1.4.1 lava ballast hotfix without propulsion changes')
    subprocess.run([sys.executable, str(C/'scripts/validate_release_export.py')], check=True)
    commit = git('rev-parse','HEAD')
    changed = git('diff','--name-only',BASE,'HEAD').splitlines()
    for rel in changed:
        assert rel in {JAR,'.gitignore','VERSION','pack.toml','index.toml','CHANGELOG.md','dist/Apocalypse-Industries-ATLauncher.zip','docs/LAVA_BALLAST_2026-10-21.md','docs/TEST_WORKFLOW_IMPROVEMENTS.md','docs/releases/V1_4_1_HOTFIX.md','scripts/prepare_v141_ballast_hotfix.py'} or rel.startswith(('scripts/submarine_lava_machinery/','docs/releases/v1.4.1-evidence/')), rel
    result = {'version':'v1.4.1','baselineCommit':BASE,'commit':commit,'runtimeChanges':[{'path':JAR,'sha256':EXPECTED}], 'indexedFiles':len(new_index),'changedRepositoryPaths':changed,'nativeChecks':304,'matchedAssembledTests':True,'manualTesting':'waived by user','publication':False,'deployment':False,'localTwin':'Historical v1.3.2; Deep Seas 2.2.4; not a valid hotfix target. Preserved without false version advancement.'}
    (OUT/'PREPARATION.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps(result,indent=2))

if __name__ == '__main__': main()
