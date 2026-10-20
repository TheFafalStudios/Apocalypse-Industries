"""Install the pinned Diabolical Islands release with the WIP lava-sea preset."""
import hashlib
import json
from pathlib import Path
import zipfile
import argparse

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / '.local/nether-islands-test/Diabolical_Islands_1.0.0.zip'
DEST = ROOT / 'config/paxi/datapacks/diabolical_islands_wip'


def main(destination=DEST, revision=7):
    global DEST
    DEST = destination
    assert hashlib.sha1(SOURCE.read_bytes()).hexdigest() == '5129ea20a4749272fca362834dba28dcf3ad836f'
    if DEST.exists():
        raise SystemExit('Destination already exists; preserve it before rebuilding.')
    with zipfile.ZipFile(SOURCE) as archive:
        for entry in archive.infolist():
            if entry.is_dir() or entry.filename.startswith('overlay_'):
                continue
            target = DEST / entry.filename
            assert target.resolve().is_relative_to(DEST.resolve())
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(archive.read(entry))
        overlay = 'overlay_1_21/data/minecraft/worldgen/structure/bastion_remnant.json'
        (DEST / 'data/minecraft/worldgen/structure/bastion_remnant.json').write_bytes(archive.read(overlay))

    def read(name):
        return json.loads((DEST / name).read_text(encoding='utf-8'))

    def write(name, data):
        (DEST / name).write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')

    write('pack.mcmeta', {'pack': {'pack_format': 48, 'description': 'Diabolical Islands by Klinbee | Apocalypse WIP lava-sea test'}})
    base = 'data/diabolical_islands/worldgen/density_function/islands/'
    name = base + 'layer1/noise.json'
    noise = read(name)
    noise['argument']['argument']['argument1'] = -0.55
    noise['argument']['argument']['argument2']['xz_scale'] = 1.15
    write(name, noise)
    for side, low, high in [('bottom', -16, 48), ('top', 47, 112)]:
        name = base + 'layer1/depth_' + side + '.json'
        depth = read(name)
        depth['argument1']['from_y'] = low
        depth['argument1']['to_y'] = high
        write(name, depth)
    write(base + 'final.json', {
        'type': 'minecraft:range_choice', 'input': 'diabolical_islands:y',
        'min_inclusive': -16, 'max_exclusive': 112,
        'when_in_range': {'type': 'minecraft:min',
                          'argument1': 'diabolical_islands:islands/layer1/depth_bottom',
                          'argument2': 'diabolical_islands:islands/layer1/depth_top'},
        'when_out_of_range': -0.1,
    })
    name = 'data/minecraft/worldgen/noise_settings/nether.json'
    settings = read(name)
    settings['sea_level'] = 32
    write(name, settings)
    if revision >= 2:
        # zFastNoise indexes generated sections relative to noise min_y. Keep it
        # aligned with the dimension's -64 floor to avoid a one-section Y shift.
        settings['noise']['min_y'] = -64
        settings['noise']['height'] = 384
        write('data/minecraft/worldgen/noise_settings/nether.json', settings)
        # Coarse flat caches quantize the horizontal field to four-block columns.
        # Only the final 3D density should be interpolated; evaluate the smooth
        # horizontal fields at their requested world coordinates.
        def uncached(value):
            if isinstance(value, dict):
                if value.get('type') in {'minecraft:flat_cache', 'minecraft:cache_2d',
                                        'minecraft:cache_once', 'minecraft:interpolated'}:
                    return uncached(value['argument'])
                return {k: uncached(v) for k, v in value.items()}
            if isinstance(value, list):
                return [uncached(v) for v in value]
            return value
        for name in [base + 'layer1/noise.json', base + 'layer1/offset_top.json',
                     base + 'layer1/offset_bottom.json',
                     'data/diabolical_islands/worldgen/density_function/ridges.json']:
            write(name, uncached(read(name)))
        for name in [base + 'layer1/offset_top.json', base + 'layer1/offset_bottom.json']:
            write(name, {'type': 'apocalypse_islands:cache_columns', 'argument': read(name)})
        name = 'data/diabolical_islands/worldgen/density_function/terrain/final.json'
        terrain = read(name)
        # Keep cave interpolation, but evaluate the cached island outline at
        # one-block resolution rather than interpolating the entire island body.
        terrain['argument1']['argument'] = terrain['argument1']['argument']['argument']
        def interpolate_caves(value):
            if value == 'minecraft:overworld/caves/entrances':
                return {'type': 'minecraft:interpolated', 'argument': value}
            if isinstance(value, dict):
                return {k: interpolate_caves(v) for k, v in value.items()}
            if isinstance(value, list):
                return [interpolate_caves(v) for v in value]
            return value
        # Surface rules only replace solid terrain, so supply a solid bottom
        # voxel before painting it with bedrock. No roof or extra floor layers.
        write(name, {
            'type': 'minecraft:range_choice', 'input': 'diabolical_islands:y',
            'min_inclusive': -64, 'max_exclusive': -63,
            'when_in_range': 1.0, 'when_out_of_range': interpolate_caves(terrain),
        })
        settings['surface_rule']['sequence'].insert(0, {
            'type': 'minecraft:condition',
            'if_true': {'type': 'minecraft:not', 'invert': {
                'type': 'minecraft:y_above', 'anchor': {'above_bottom': 1},
                'surface_depth_multiplier': 0, 'add_stone_depth': False}},
            'then_run': {'type': 'minecraft:block', 'result_state': {'Name': 'minecraft:bedrock'}},
        })
        write('data/minecraft/worldgen/noise_settings/nether.json', settings)
        # Replace the abrupt vertical switch with a continuous density envelope.
        write(base + 'final.json', {
            'type': 'minecraft:min',
            'argument1': {'type': 'minecraft:min',
                          'argument1': 'diabolical_islands:islands/layer1/depth_bottom',
                          'argument2': 'diabolical_islands:islands/layer1/depth_top'},
            'argument2': {'type': 'minecraft:min',
                          'argument1': {'type': 'minecraft:y_clamped_gradient',
                                        'from_y': -24, 'to_y': -16, 'from_value': -1, 'to_value': 1},
                          'argument2': {'type': 'minecraft:y_clamped_gradient',
                                        'from_y': 112, 'to_y': 120, 'from_value': 1, 'to_value': -1}}})
        marker = DEST / 'data/apocalypse_islands/worldgen/density_function/foundations_enabled.json'
        marker.parent.mkdir(parents=True, exist_ok=True)
        marker.write_text('1.0\n')
        # Replace buried debris with small, fully submerged connected clumps.
        def extra(name, data):
            (DEST / name).parent.mkdir(parents=True, exist_ok=True)
            write(name, data)
        extra('data/apocalypse_islands/worldgen/configured_feature/floating_debris.json', {
            'type': 'apocalypse_islands:floating_debris',
            'config': {'min_blocks': 9, 'max_blocks': 18, 'casing': {
                'type': 'minecraft:weighted_state_provider', 'entries': [
                    {'data': {'Name': 'minecraft:blackstone'}, 'weight': 60},
                    {'data': {'Name': 'minecraft:basalt'}, 'weight': 25},
                    {'data': {'Name': 'minecraft:netherrack'}, 'weight': 15}]}}})
        extra('data/apocalypse_islands/worldgen/placed_feature/floating_debris.json', {
            'feature': 'apocalypse_islands:floating_debris', 'placement': [
                {'type': 'minecraft:rarity_filter', 'chance': 12},
                {'type': 'minecraft:in_square'},
                {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform',
                    'min_inclusive': {'absolute': -48}, 'max_inclusive': {'absolute': 20}}},
                {'type': 'minecraft:biome'}]})
        extra('data/apocalypse_islands/neoforge/biome_modifier/floating_debris.json', {
            'type': 'neoforge:add_features', 'biomes': '#minecraft:is_nether',
            'features': 'apocalypse_islands:floating_debris', 'step': 'underground_decoration'})
        extra('data/apocalypse_islands/neoforge/biome_modifier/remove_buried_debris.json', {
            'type': 'neoforge:remove_features', 'biomes': '#minecraft:is_nether',
            'features': ['minecraft:ore_ancient_debris_large', 'minecraft:ore_debris_small'],
            'steps': ['underground_ores', 'underground_decoration']})
    if revision >= 3:
        from configure_layers import configure
        configure(DEST)
    if revision >= 4:
        from configure_seabed import configure
        configure(DEST)
    if revision >= 5:
        from configure_raised_islands import configure
        configure(DEST)
    if revision >= 6:
        from configure_ecology import configure
        configure(DEST)
    if revision >= 7:
        from configure_depth_gradient import configure
        configure(DEST)
    files = list(DEST.rglob('*.json')) + [DEST / 'pack.mcmeta']
    for path in files:
        data = json.loads(path.read_text(encoding='utf-8'))
        def check(value):
            if isinstance(value, dict):
                for nested in value.values():
                    check(nested)
            elif isinstance(value, list):
                for nested in value:
                    check(nested)
            elif isinstance(value, str) and value.startswith('diabolical_islands:'):
                relative = value.split(':', 1)[1] + '.json'
                candidates = [DEST / 'data/diabolical_islands/worldgen' / kind / relative
                              for kind in ('density_function', 'noise')]
                assert any(candidate.is_file() for candidate in candidates), value
        check(data)
    print(f'Installed and statically validated {len(files)} JSON files at {DEST}')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=DEST)
    parser.add_argument('--revision', type=int, choices=[1, 2, 3, 4, 5, 6, 7], default=7)
    args = parser.parse_args()
    main(args.output.resolve(), args.revision)
