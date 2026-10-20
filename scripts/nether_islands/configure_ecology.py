"""Aim existing Nether glowstone attempts at island undersides."""
import json
from pathlib import Path

NETHER_BIOMES=['crystalline_chasm','erupting_inferno','undergrowth','visceral_heap','withered_abyss']


def configure(destination):
    root=Path(destination)
    for name,count in [('glowstone',10),('glowstone_extra',{
            'type':'minecraft:biased_to_bottom','min_inclusive':0,'max_inclusive':9})]:
        data={'feature':'minecraft:glowstone_extra','placement':[
            {'type':'minecraft:count','count':count},
            {'type':'minecraft:in_square'},
            {'type':'minecraft:height_range','height':{'type':'minecraft:uniform',
                'min_inclusive':{'absolute':101},'max_inclusive':{'absolute':303}}},
            {'type':'minecraft:environment_scan','direction_of_search':'up','max_steps':32,
                'allowed_search_condition':{'type':'minecraft:matching_blocks','blocks':['minecraft:air','minecraft:cave_air']},
                'target_condition':{'type':'minecraft:matching_blocks','blocks':[
                    'minecraft:netherrack','minecraft:basalt','minecraft:blackstone']}},
            {'type':'minecraft:random_offset','xz_spread':0,'y_spread':-1},
            {'type':'minecraft:biome'}]}
        path=root/f'data/minecraft/worldgen/placed_feature/{name}.json'
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(data,indent=2)+'\n')


def toggles(source,destination):
    data=json.loads(Path(source).read_text())
    for biome in NETHER_BIOMES:data[biome+'_enabled']=True
    Path(destination).write_text(json.dumps(data,indent=2)+'\n')
