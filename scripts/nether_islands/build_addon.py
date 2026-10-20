"""Build the WIP structure-foundation addon and optional isolated native test mod."""
from pathlib import Path
import argparse, hashlib, json, os, subprocess, zipfile

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
LAUNCHER = ROOT.parents[1]
JAVA = LAUNCHER / 'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin'
SERVER_LIB = LAUNCHER / 'servers/Chunkserve - Apocalypse Industries/libraries'


def build(out, tests):
    out.mkdir(parents=True, exist_ok=False)
    cp = [SERVER_LIB / 'net/neoforged/neoforge/21.1.248/neoforge-21.1.248-server.jar',
          LAUNCHER / 'libraries/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-universal.jar']
    for base, dirs, files in os.walk(SERVER_LIB, followlinks=False):
        for directory in dirs:
            path = Path(base) / directory
            if path.is_symlink() or getattr(path.lstat(), 'st_file_attributes', 0) & 1024:
                raise SystemExit('Linked library directory: ' + str(path))
        cp.extend(Path(base) / name for name in files if name.endswith('.jar'))
    cp = list(dict.fromkeys(cp))
    deps = out / 'deps'
    deps.mkdir()
    for jar in list(cp):
        with zipfile.ZipFile(jar) as archive:
            for entry in archive.namelist():
                if entry.startswith('META-INF/jarjar/') and entry.endswith('.jar'):
                    target = deps / (jar.name + '-' + Path(entry).name)
                    target.write_bytes(archive.read(entry))
                    cp.append(target)
    classes = out / 'classes'
    classes.mkdir()
    sources = list((HERE / 'src').rglob('*.java'))
    if tests:
        sources.extend((HERE / 'tests').rglob('*.java'))
    args = ['--release', '21', '-proc:none', '-encoding', 'UTF-8', '-classpath',
            os.pathsep.join(map(str, cp)), '-d', str(classes), *map(str, sources)]
    argfile = out / 'javac.args'
    argfile.write_text('\n'.join('"' + s.replace('\\', '/') + '"' for s in args))
    subprocess.run([str(JAVA / 'javac.exe'), '@' + str(argfile)], check=True)
    artifact = out / 'apocalypse-nether-islands-0.9.0.jar'
    metadata = '''modLoader="javafml"
loaderVersion="[4,)"
license="MIT"
[[mods]]
modId="apocalypse_nether_islands"
version="0.9.0"
displayName="Apocalypse Nether Island Foundations"
description="Structure support islands and submerged ancient debris for the WIP Nether preset."
[[mixins]]
config="apocalypse-nether-islands.mixins.json"
[[dependencies.apocalypse_nether_islands]]
modId="minecraft"
type="required"
versionRange="[1.21.1]"
side="BOTH"
[[dependencies.apocalypse_nether_islands]]
modId="neoforge"
type="required"
versionRange="[21.1.248,21.2)"
side="BOTH"
'''
    if tests:
        metadata += '\n[[mods]]\nmodId="apocalypse_island_tests"\nversion="1.0.0"\ndisplayName="Isolated Island Tests"\n'
    with zipfile.ZipFile(artifact, 'w', zipfile.ZIP_DEFLATED) as z:
        def put(name, data):
            info = zipfile.ZipInfo(name, (2026, 10, 17, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            z.writestr(info, data)
        for path in sorted(classes.rglob('*.class')):
            put(path.relative_to(classes).as_posix(), path.read_bytes())
        for path in sources:
            put('sources/' + path.name, path.read_bytes())
        put('META-INF/neoforge.mods.toml', metadata)
        put('apocalypse-nether-islands.mixins.json', json.dumps({
            'required': True, 'minVersion': '0.8', 'compatibilityLevel': 'JAVA_21',
            'package': 'dev.apocalypse.netherislands.mixin',
            'mixins': ['StructureManagerAccess', 'StructureReferences', 'StructureFoundations', 'ExactIslandColumns', 'BasaltSeabed', 'RaisedFortresses', 'SafeNetherPortals', 'BastionClearance', 'FinalStructureFoundations'],
            'injectors': {'defaultRequire': 1}}))
    report = {'artifact': str(artifact), 'sha256': hashlib.sha256(artifact.read_bytes()).hexdigest(), 'tests': tests}
    (out / 'build.json').write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report, indent=2))


if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--output', type=Path, required=True)
    p.add_argument('--tests', action='store_true')
    a = p.parse_args()
    build(a.output.resolve(), a.tests)
