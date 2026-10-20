"""Create an isolated disposable native test server; never uses WIP or server saves."""
from pathlib import Path
import argparse, json, re, shutil, subprocess, time

ROOT = Path(__file__).resolve().parents[2]
LAUNCHER = ROOT.parents[1]
LIB = LAUNCHER / 'servers/Chunkserve - Apocalypse Industries/libraries'
JAVA = LAUNCHER / 'runtimes/minecraft/java-runtime-delta/windows-x64/java-runtime-delta/bin/java.exe'

if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--output', type=Path, required=True)
    p.add_argument('--addon', type=Path, required=True)
    p.add_argument('--pack', type=Path, required=True)
    p.add_argument('--fastnoise', action='store_true')
    p.add_argument('--biomes', action='store_true')
    p.add_argument('--structures-only', action='store_true')
    p.add_argument('--floor-only', action='store_true')
    p.add_argument('--debris-only', action='store_true')
    p.add_argument('--layers-only', action='store_true')
    p.add_argument('--seabed-only', action='store_true')
    p.add_argument('--elevated-only', action='store_true')
    p.add_argument('--ecology-only', action='store_true')
    p.add_argument('--portals-only', action='store_true')
    p.add_argument('--all-structures', action='store_true')
    p.add_argument('--biome-toggles',type=Path)
    a = p.parse_args()
    out = a.output.resolve()
    out.mkdir(parents=True, exist_ok=False)
    (out / 'mods').mkdir()
    shutil.copy2(a.addon, out / 'mods' / a.addon.name)
    if a.all_structures:
        for pattern in ['create-1.21.1-*.jar','create_ltab-*.jar','golemdungeons-*.jar',
                        'l2library-*.jar','modulargolems-*.jar','apocalypse-l2-serial-fix-*.jar',
                        'no-ruined-nether-portals-*.jar']:
            matches=list((ROOT/'mods').glob(pattern))
            if len(matches)!=1:raise SystemExit('Ambiguous structure dependency: '+pattern)
            shutil.copy2(matches[0],out/'mods'/matches[0].name)
    # This is only a dependency-directory junction. All saves/config/logs are
    # created fresh under out; nothing is linked to an existing game world.
    quote = lambda value: "'" + str(value).replace("'", "''") + "'"
    subprocess.run(['powershell', '-NoProfile', '-Command',
        'New-Item -ItemType Junction -Path ' + quote(out / 'libraries') +
        ' -Target ' + quote(LIB) + ' | Out-Null'], check=True)
    shutil.copytree(a.pack, out / 'audit-world/datapacks/diabolical_islands')
    if a.fastnoise:
        for mod in ['zfastnoise-1.0.13+1.21.1+neoforge.jar']:
            shutil.copy2(ROOT / 'mods' / mod, out / 'mods' / mod)
    if a.biomes:
        (out/'config/biomesoplenty').mkdir(parents=True)
        shutil.copy2(a.biome_toggles or ROOT/'config/biomesoplenty/biome_toggles.json',
                     out/'config/biomesoplenty/biome_toggles.json')
        shutil.copy2(ROOT/'config/terrablender.toml',out/'config/terrablender.toml')
        for pattern in ['BiomesOPlenty-*.jar', 'TerraBlender-*.jar', 'GlitchCore-*.jar', 'lithostitched-*.jar']:
            matches = list((ROOT / 'mods').glob(pattern))
            if len(matches) != 1:
                raise SystemExit('Ambiguous dependency: ' + pattern)
            shutil.copy2(matches[0], out / 'mods' / matches[0].name)
    (out / 'eula.txt').write_text('eula=true\n')
    (out / 'server.properties').write_text('level-name=audit-world\nlevel-seed=104729\n'
        'server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\n'
        'view-distance=2\nsimulation-distance=2\nspawn-protection=0\nmax-tick-time=-1\n'
        'initial-enabled-packs=vanilla,file/diabolical_islands\n')
    with (out / 'native-console.log').open('w', encoding='utf-8') as log:
        process = subprocess.Popen([str(JAVA), '-Xms512M', '-Xmx4G',
            '-Dapocalypse.islands.structuresOnly=' + str(a.structures_only).lower(),
            '-Dapocalypse.islands.floorOnly=' + str(a.floor_only).lower(),
            '-Dapocalypse.islands.debrisOnly=' + str(a.debris_only).lower(),
            '-Dapocalypse.islands.layersOnly=' + str(a.layers_only).lower(),
            '-Dapocalypse.islands.seabedOnly=' + str(a.seabed_only).lower(),
            '-Dapocalypse.islands.elevatedOnly=' + str(a.elevated_only).lower(),
            '-Dapocalypse.islands.ecologyOnly=' + str(a.ecology_only).lower(),
            '-Dapocalypse.islands.portalsOnly=' + str(a.portals_only).lower(),
            '-Dapocalypse.islands.allStructures=' + str(a.all_structures).lower(),
            '@libraries/net/neoforged/neoforge/21.1.248/win_args.txt', 'nogui'],
            cwd=out, stdout=log, stderr=subprocess.STDOUT, stdin=subprocess.DEVNULL)
    print(json.dumps({'pid': process.pid, 'output': str(out)}), flush=True)
    try:
        code = process.wait(timeout=480)
    except subprocess.TimeoutExpired:
        process.terminate()
        process.wait(timeout=30)
        raise SystemExit('Native audit timed out; see native-console.log')
    report = out / 'island-audit.json'
    if not report.exists():
        raise SystemExit('Native audit failed before reporting; see native-console.log')
    data = json.loads(report.read_text())
    print(json.dumps({k:({sid:{name:value for name,value in structure.items() if name!='samples'}
                         for sid,structure in v.items()} if k=='structures' else v)
                      for k,v in data.items() if k not in ['baseHeights','actualHeights']}, indent=2))
    if not data.get('pass'):
        raise SystemExit('Native audit assertions failed')
