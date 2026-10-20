"""Add a gently uneven three-to-five-block basalt bed above the bedrock."""
import json
from pathlib import Path


def configure(destination):
    root=Path(destination)
    def write(name,value):
        path=root/name
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps(value,indent=2)+'\n')
    write('data/apocalypse_islands/worldgen/noise/seabed.json',{
        'firstOctave':-5,'amplitudes':[1.0,0.5,0.25]})
    write('data/apocalypse_islands/worldgen/density_function/seabed.json',{
        'type':'minecraft:add',
        'argument1':{'type':'minecraft:y_clamped_gradient','from_y':-61,'to_y':-58,
                     'from_value':1.5,'to_value':-1.5},
        'argument2':{'type':'apocalypse_islands:cache_columns','argument':{
            'type':'minecraft:clamp','min':-1,'max':1,'input':{
                'type':'minecraft:mul','argument1':3,'argument2':{
                    'type':'minecraft:noise','noise':'apocalypse_islands:seabed',
                    'xz_scale':1.0,'y_scale':0.0}}}}})
    name='data/diabolical_islands/worldgen/density_function/terrain/final.json'
    terrain=json.loads((root/name).read_text())
    terrain['when_out_of_range']={'type':'minecraft:max',
        'argument1':terrain['when_out_of_range'],'argument2':'apocalypse_islands:seabed'}
    write(name,terrain)
    name='data/minecraft/worldgen/noise_settings/nether.json'
    settings=json.loads((root/name).read_text())
    # Bedrock remains first. Paint only the bottom five possible terrain layers.
    settings['surface_rule']['sequence'].insert(1,{
        'type':'minecraft:condition','if_true':{'type':'minecraft:not','invert':{
            'type':'minecraft:y_above','anchor':{'absolute':-58},
            'surface_depth_multiplier':0,'add_stone_depth':False}},
        'then_run':{'type':'minecraft:block','result_state':{
            'Name':'minecraft:basalt','Properties':{'axis':'y'}}}})
    write(name,settings)
