"""Build a fail-closed v4 patch from the exact disabled v3 JAR; does not activate it."""
from pathlib import Path
import hashlib, json, subprocess, zipfile
ROOT = Path(__file__).resolve().parents[2]
LAUNCHER = ROOT.parents[1]
WORK = ROOT / 'local/sablewaves-dev'
SOURCE = ROOT / 'scripts/sablewaves'
JAVA = LAUNCHER / 'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
INPUT = ROOT / 'disabledmods/sablewaves-2.21.0-nf21.1.248-hotfix-v3-waterblend.jar'
OUTPUT = WORK / 'sablewaves-2.21.0-nf21.1.248-hotfix-v4-safety-render.jar'
EXPECTED = 'bb84dd40ae5e8389a095b88d3d608c5f74c5012042cdc63a2e85509af8349bfe'

def run(tool, args, label):
    argfile = WORK / (label + '.args')
    argfile.write_text('\n'.join('"' + str(a).replace('\\', '/').replace('"', '\\"') + '"' for a in args), encoding='utf-8')
    subprocess.run([str(JAVA / (tool + '.exe')), '@' + str(argfile)], check=True, cwd=ROOT)

def main():
    if hashlib.sha256(INPUT.read_bytes()).hexdigest() != EXPECTED:
        raise SystemExit('Source JAR differs from audited v3; refusing to patch.')
    WORK.mkdir(parents=True, exist_ok=True)
    priority = [LAUNCHER / 'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-client.jar',
                LAUNCHER / 'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar']
    priority.append(LAUNCHER / 'libraries/net/minecraft/client/1.21.1-20240808.144430/client-1.21.1-20240808.144430-srg.jar')
    instance = json.loads((ROOT / 'instance.json').read_text(encoding='utf-8'))
    libraries = [LAUNCHER / 'libraries' / item['downloads']['artifact']['path']
                 for item in instance['libraries'] if item.get('downloads', {}).get('artifact', {}).get('path')]
    nested = WORK / 'dependencies'; nested.mkdir(exist_ok=True)
    with zipfile.ZipFile(ROOT / 'mods/sable-neoforge-1.21.1-2.0.3.jar') as sable:
        for name in sable.namelist():
            if name.startswith('META-INF/jarjar/') and name.endswith('.jar'):
                dest = nested / Path(name).name
                dest.write_bytes(sable.read(name)); priority.append(dest)
    cp_paths = [INPUT] + priority + [ROOT / 'mods/sable-neoforge-1.21.1-2.0.3.jar'] + libraries + list((ROOT / 'mods').glob('*.jar'))
    cp = ';'.join(str(p) for p in dict.fromkeys(cp_paths))
    (WORK / 'classpath.txt').write_text(cp, encoding='utf-8')
    (WORK / 'classpath.json').write_text(json.dumps([str(p) for p in dict.fromkeys(cp_paths)]), encoding='utf-8')
    compiled, fragment, patcher = (WORK / n for n in ['build/classes', 'build/fragment', 'build/patcher'])
    for p in [compiled, fragment, patcher]: p.mkdir(parents=True, exist_ok=True)
    common = ['--release', '21', '-proc:none', '-encoding', 'UTF-8']
    run('javac', common + ['-classpath', cp, '-d', compiled] + list((SOURCE / 'src').rglob('*.java')), 'compile')
    run('javac', common + ['-classpath', str(compiled) + ';' + cp, '-d', fragment] + list((SOURCE / 'fragments').rglob('*.java')), 'fragment')
    asm = ';'.join(str(LAUNCHER / f'libraries/org/ow2/asm/{n}/9.8/{n}-9.8.jar') for n in ['asm', 'asm-tree', 'asm-analysis', 'asm-util'])
    run('javac', common + ['-classpath', asm, '-d', patcher, SOURCE / 'Patch.java'], 'patcher')
    run('java', ['-Xverify:all', '-classpath', str(patcher) + ';' + asm, 'Patch', INPUT, compiled,
                 fragment / 'dev/ashok/sablewaves/client/WaveOverlayRenderer.class', OUTPUT], 'patch')
    with zipfile.ZipFile(INPUT) as before, zipfile.ZipFile(OUTPUT) as after:
        assert after.testzip() is None
        changed = [n for n in before.namelist() if not n.endswith('/') and before.read(n) != after.read(n)]
        added = sorted(set(after.namelist()) - set(before.namelist()))
        # Preserve the two previously repaired payload handlers byte for byte.
        for name in ['dev/ashok/sablewaves/net/WaveNetwork.class', 'META-INF/neoforge.mods.toml']:
            assert before.read(name) == after.read(name), name
    report = {'source_sha256': EXPECTED, 'output_sha256': hashlib.sha256(OUTPUT.read_bytes()).hexdigest(),
              'changed': changed, 'added': added, 'runtime_tested': False}
    (WORK / 'build-report.json').write_text(json.dumps(report, indent=2), encoding='utf-8')
    print(json.dumps(report, indent=2))

if __name__ == '__main__': main()

