"""Restore four island fields with two lava-contact and two airborne bands."""
import json
from pathlib import Path


def configure(destination):
    base = Path(destination) / 'data/diabolical_islands/worldgen/density_function/islands'
    def read(path): return json.loads(path.read_text())
    def write(path, value): path.write_text(json.dumps(value, indent=2) + '\n')
    def strip(value):
        if isinstance(value, dict):
            if value.get('type') in {'minecraft:flat_cache', 'minecraft:cache_2d',
                                      'minecraft:cache_once', 'minecraft:interpolated'}:
                return strip(value['argument'])
            return {k: strip(v) for k, v in value.items()}
        if isinstance(value, list): return [strip(v) for v in value]
        return value
    def minimum(a,b): return {'type':'minecraft:min','argument1':a,'argument2':b}
    def maximum(a,b): return {'type':'minecraft:max','argument1':a,'argument2':b}
    def gradient(a,b,c,d):
        return {'type':'minecraft:y_clamped_gradient','from_y':a,'to_y':b,'from_value':c,'to_value':d}
    bands = []
    # low gradient ends below sea level, ensuring low islands reach lava even
    # when their individual noise peak is too weak for the original thick base.
    for layer, bottom, waist, top in [(1,-16,24,112),(2,-16,24,112),
                                       (3,128,160,208),(4,224,248,288)]:
        folder = base / f'layer{layer}'
        noise = strip(read(folder/'noise.json'))
        noise['argument1'] = -0.55
        noise['argument2']['xz_scale'] = 1.15 if layer<=2 else 1.05
        write(folder/'noise.json', noise)
        for side in ['top','bottom']:
            offset = strip(read(folder/f'offset_{side}.json'))
            if offset.get('type') == 'apocalypse_islands:cache_columns': offset = offset['argument']
            write(folder/f'offset_{side}.json', {'type':'apocalypse_islands:cache_columns','argument':offset})
            depth = read(folder/f'depth_{side}.json')
            depth['argument1'] = gradient(bottom,waist,-1,0) if side=='bottom' else gradient(
                47 if layer<=2 else waist-1,top,0,-1)
            write(folder/f'depth_{side}.json',depth)
        field = minimum(f'diabolical_islands:islands/layer{layer}/depth_bottom',
                        f'diabolical_islands:islands/layer{layer}/depth_top')
        envelope = minimum(gradient(bottom-8,bottom,-1,1),gradient(top,top+8,1,-1))
        bands.append(minimum(field,envelope))
    result = maximum(maximum(bands[0],bands[1]),maximum(bands[2],bands[3]))
    write(base/'final.json',result)
