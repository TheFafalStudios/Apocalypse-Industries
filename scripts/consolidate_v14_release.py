"""Merge frozen Nether/Deep Seas and shader preparations into a NEW local release.

No publication, deployment, game activation or live-instance changes. Python 3.11+.
"""
import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
import tomllib
import zipfile
from pathlib import Path
from release_safety import files, excluded
from build_server_deployment_record import prepare, checksums

ROOT = Path(__file__).resolve().parents[1]
BASE = Path('Q:/GPT General/l2-format-v1.3.2-20261016')
PREP = ROOT.parents[1]/'servers/Apocalypse Industries versions/_preparation/v1.4-20261020'
SHADER_PREP = Path('Q:/GPT General/shader-release-prep-20261020/attempt-03')
BASE_COMMIT = '72526d32176ea3bcae70d2960ad215cab6f39821'
SHADER = 'shaderpacks/ApocalypseAtmosphere_NetherDH_v1.2.zip'
EXACT = {'config/biomesoplenty/biome_toggles.json',
         'config/create_submarine-common.toml', 'config/create_submarine-server.toml',
         'config/create_submarine-client.toml', 'config/submarine_hull.json',
         'mods/apocalypse-nether-islands-0.9.0.jar',
         'mods/apocalypse-submarine-balance-1.0.0.jar',
         'kubejs/data/sable/tags/block/light.json',
         'kubejs/data/sable/tags/block/super_light.json'}

def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest() if p.is_file() else None

def save(p, value):
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(value, indent=2)+'\n', encoding='utf-8')

def copy(p, q):
    assert not p.is_symlink() and not p.is_junction()
    q.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(p, q)
    assert sha(p) == sha(q)

def nether_path(rel):
    return rel in EXACT or rel.startswith('config/paxi/datapacks/diabolical_islands_wip/')

def main(out):
    assert not out.exists() and ROOT not in out.parents
    out.mkdir(parents=True)
    candidate = out/'github-candidate'
    subprocess.run(['git', 'clone', '--local', '--no-hardlinks', '--no-checkout', str(BASE), str(candidate)], check=True, capture_output=True)
    assert subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=candidate, text=True).strip() == BASE_COMMIT
    subprocess.run(['git', 'branch', '-m', 'codex/v1.4-release'], cwd=candidate, check=True)
    # No checkout: copy only the already filtered shader snapshot, never excluded baseline files.
    for p in files(SHADER_PREP/'candidate'):
        rel = p.relative_to(SHADER_PREP/'candidate').as_posix()
        if not excluded(rel): copy(p, candidate/rel)
    entries = {e['file']: e for e in tomllib.loads((candidate/'index.toml').read_text())['files']}
    previous = PREP/'github-candidate'
    for e in tomllib.loads((previous/'index.toml').read_text())['files']:
        rel = e['file']
        if nether_path(rel):
            copy(previous/rel, candidate/rel)
            entries[rel] = dict(e)
    # Obsolete patcher state is not an input to this custom Complementary derivative.
    for rel in list(entries):
        if rel.startswith('config/euphoria_patcher/') or excluded(rel):
            entries.pop(rel)
            if (candidate/rel).is_file(): (candidate/rel).unlink()
    # Include the reviewed source/evidence for the actual new common-side payload.
    for folder in ['scripts/nether_islands', 'scripts/submarine_balance']:
        for p in files(ROOT/folder):
            if p.suffix in {'.py', '.java', '.json', '.toml', '.md', '.txt', '.mcmeta'}:
                copy(p, candidate/p.relative_to(ROOT))
    for name in ['NETHER_ISLANDS_WIP_TEST.md', 'NETHER_STRUCTURE_FIX_2026-10-19.md',
                 'SUBMARINE_PRESSURE_BALANCE_2026-10-20.md', 'LAVA_SEA_BALANCE_ANALYSIS_2026-10-19.md',
                 'DH_NETHER_LAVA_BLEND.md', 'SUBMARINE_LAVA_VISUALS.md', 'VORTEX_SHADER_BRIDGE.md',
                 'AERONAUTICS_UPDATE_CHAIN_2026-10-09.md', 'WIP_DEEP_SEAS_3_4_4_2026-10-19.md',
                 'TREATED_WOOD_PHYSICS.md', 'TEST_WORKFLOW_IMPROVEMENTS.md']:
        copy(ROOT/'docs'/name, candidate/'docs'/name)
    for name in ['consolidate_v14_release.py', 'validate_v14_consolidation.py', 'release_safety.py', 'build_server_deployment_record.py']:
        copy(ROOT/'scripts'/name, candidate/'scripts'/name)
    # Resolve the documented integration mismatch in the release copy only.
    noise = json.loads((candidate/'config/paxi/datapacks/diabolical_islands_wip/data/minecraft/worldgen/noise_settings/nether.json').read_text())
    assert noise['sea_level'] == 100
    with zipfile.ZipFile(candidate/SHADER) as z:
        original = {n: z.read(n) for n in z.namelist()}
        infos = {i.filename: i for i in z.infolist()}
    patched = dict(original)
    for name, count in [('shaders/lib/atmospherics/fog/netherGapDistance.glsl', 2), ('shaders/program/deferred1.glsl', 1)]:
        text = original[name].decode('utf-8')
        assert text.count('32.0') == count
        patched[name] = text.replace('32.0', '100.0').encode()
    with zipfile.ZipFile(candidate/SHADER, 'w', zipfile.ZIP_DEFLATED) as z:
        for name, data in patched.items(): z.writestr(infos[name], data)
    save(out/'SHADER_INTEGRATION.json', {'method': 'sea-level-from-frozen-datapack-v1', 'seaLevel': 100,
        'beforeSha256': sha(SHADER_PREP/'candidate'/SHADER), 'afterSha256': sha(candidate/SHADER),
        'changedEntries': [n for n in original if original[n] != patched[n]],
        'liveWipShaderChanged': False, 'gameplayValidated': False})
    # Prefer the shader preparation's conservative DH defaults and portable Iris settings.
    for rel in ['config/iris.properties', SHADER+'.txt']:
        assert entries[rel]['preserve'] is True
    (candidate/'VERSION').write_text('v1.4\n')
    lines = ['hash-format = "sha256"', '']
    for rel, e in sorted(entries.items()):
        e['hash'] = sha(candidate/rel)
        assert e['hash'], rel
        lines += ['[[files]]', 'file = '+json.dumps(rel), 'hash = "'+e['hash']+'"']
        lines += [k+' = true' for k in ['metafile', 'preserve'] if e.get(k)]
        lines += ['']
    (candidate/'index.toml').write_text('\n'.join(lines)+'\n')
    pack = (candidate/'pack.toml').read_text()
    pack = re.sub(r'(?m)^version\s*=.*$', 'version = "v1.4"', pack)
    pack = re.sub(r'(?m)^hash\s*=.*$', 'hash = "'+sha(candidate/'index.toml')+'"', pack)
    (candidate/'pack.toml').write_text(pack)
    notes = '''# Apocalypse Industries v1.4 — Nether, Deep Seas and shaders

- Rebuild the Nether around a Y100 lava sea, varied islands, restored Nether biomes and glowstone, protected floor layers and richer debris deposits at greater depth. Ruined portals remain disabled.
- Add safe portal placement, raised structures and post-placement foundations covering all five enabled Nether structure IDs and every bastion layout. Existing terrain is unchanged.
- Update Aeronautics to 1.3.2, Deep Seas to 3.4.4 and Distant Horizons to 3.3.3. Keep Sable 2.0.3 and Photomancy 1.0.1 for compatibility.
- Add submarine balance 1.0.0: separate water/lava pressure tiers, weaker viewports and costly netherite jackets for opaque fittings. Deepest vessels require opaque hulls and sonar. Server candidates use HYBRID.
- Add Apocalypse Atmosphere Nether/DH shaders, shared animated lava, corrected emission/LOD blending, biome tint/fog and missing-terrain camouflage aligned with the Y100 sea.
- Include client lava visuals/optional Vortex bridge 1.1.2 and Aeronautics Iris Fix 2.1.0. Vortex starts disabled each session. Remove the unused Euphoria Patcher. Existing Iris and shader preferences are preserved.
- Carry forward the treated-wood ship-physics tags prepared with the submarine work.

The Nether fix has user gameplay confirmation. Automated packaging, isolated pressure and offline shader checks are separate evidence; combined multiplayer, complete floor-going submarine operation, new shader visual acceptance and performance remain unverified. No FPS gain is claimed.

Existing players may need to select ApocalypseAtmosphere_NetherDH_v1.2.zip once in Iris because preserved preferences can retain the removed filename. Java changes require a full client restart. Server activation requires a user-managed restart after deployment. Worlds, schematics, generated LOD data, diagnostics and the unfinished endgame quest are excluded.
'''
    (out/'RELEASE_NOTES.md').write_text(notes, encoding='utf-8')
    (candidate/'docs/releases').mkdir(exist_ok=True)
    (candidate/'docs/releases/V1_4_CONSOLIDATED.md').write_text(notes, encoding='utf-8')
    baseline_changelog = (BASE/'CHANGELOG.md').read_text(encoding='utf-8-sig')
    (candidate/'CHANGELOG.md').write_text('# Changelog\n\n## v1.4 — 2026-10-20\n\n'+notes.split('\n', 1)[1]+'\n'+baseline_changelog.removeprefix('# Changelog').lstrip(), encoding='utf-8')
    # Keep historical shader-only evidence labelled as superseded, never as final v1.4 instructions.
    for rel in ['docs/SHADER_RELEASE.md', 'docs/SHADER_RELEASE_NOTES.md']:
        p = candidate/rel
        p.write_text('> Historical shader-only preparation. Superseded for release assembly by [v1.4 consolidated notes](releases/V1_4_CONSOLIDATED.md). The Y32 mismatch is corrected in the v1.4 candidate; validation limits still apply.\n\n'+p.read_text(), encoding='utf-8')
    subprocess.run([sys.executable, str(candidate/'scripts/validate_distribution.py')], check=True)
    subprocess.run(['git', 'remote', 'set-url', 'origin', 'https://github.com/TheFafalStudios/Apocalypse-Industries.git'], cwd=candidate, check=True)
    subprocess.run(['git', 'add', '--all'], cwd=candidate, check=True, capture_output=True)
    subprocess.run(['git', '-c', 'user.name=Codex', '-c', 'user.email=codex@openai.com', 'commit', '-m', 'Prepare v1.4 Nether, Deep Seas and shader release'], cwd=candidate, check=True, capture_output=True)
    commit = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=candidate, text=True).strip()
    # Reuse original target bytes, narrowing out unrelated generated WIP config changes.
    server = {}
    for target in ['twin', 'chunkserve']:
        selected = json.loads((PREP/(target+'-paths.json')).read_text())
        paths = [r for r in selected if nether_path(r) or r.startswith(('mods/DistantHorizons-', 'mods/create-aeronautics-bundled-', 'mods/create_submarine-'))]
        record = out/(target+'-record')
        changes = prepare(PREP/(target+'-before-affected'), PREP/(target+'-candidate'), record,
            'v1.4', 'v1.4', commit, paths, notes='Consolidated v1.4 preparation only. Recheck original target hashes immediately before deployment. Baseline remains unverified.', create_zip=True)
        for suffix in ['REMOTE_CHANGE_CATALOGUE', 'RESOLUTIONS', 'HOLDS']:
            copy(PREP/(target+'-'+suffix+'.json'), record/(suffix+'.json'))
        # Packet name is obtained from the generated record rather than assumed.
        packets = list(record.glob('*.zip')); assert len(packets) == 1
        with zipfile.ZipFile(packets[0]) as z:
            assert tomllib.loads(z.read('config/create_submarine-server.toml').decode())['hullStrength']['pressureModel'] == 'HYBRID'
            assert not any('lava-visuals' in n or 'shaderpack' in n or 'aero-shadow' in n for n in z.namelist())
        checksums(record)
        server[target] = {'changes': len(changes), 'sourceSnapshot': str(PREP/(target+'-before-affected')),
                          'baselineVerified': False, 'deployed': False, 'hybridCandidateVerified': True}
    save(out/'IDENTITY.json', {'version': 'v1.4', 'baselineCommit': BASE_COMMIT, 'candidateCommit': commit,
        'branch': 'codex/v1.4-release', 'published': False, 'deployed': False, 'servers': server,
        'inputPreparations': [str(PREP), str(SHADER_PREP)]})
    print(json.dumps({'candidate': str(candidate), 'commit': commit, 'indexEntries': len(entries), 'servers': server}, indent=2))

if __name__ == '__main__':
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--output', type=Path, required=True)
    main(p.parse_args().output.resolve())
