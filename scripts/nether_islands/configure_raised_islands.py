"""Rounded island profiles with a Y100 lava ocean and deeply submerged ore."""
import json
from pathlib import Path


def configure(destination):
    root=Path(destination)
    def read(n):return json.loads((root/n).read_text())
    def write(n,v):(root/n).write_text(json.dumps(v,indent=2)+'\n')
    def op(t,a,b):return {'type':'minecraft:'+t,'argument1':a,'argument2':b}
    def gradient(a,b,c,d):return {'type':'minecraft:y_clamped_gradient','from_y':a,'to_y':b,'from_value':c,'to_value':d}
    base='data/diabolical_islands/worldgen/density_function/islands/'
    profiles=[]
    for layer,center,lower,upper in [(1,128,64,48),(2,128,64,48),(3,208,24,24),(4,280,24,24)]:
        horizontal=op('min',f'diabolical_islands:islands/layer{layer}/offset_bottom',
                             f'diabolical_islands:islands/layer{layer}/offset_top')
        sides=[]
        for side,a,b,c,d in [('bottom',center-lower,center,-1,0),('top',center,center+upper,0,1)]:
            curve=op('mul',-1,{'type':'minecraft:square','argument':gradient(a,b,c,d)})
            value=op('add',horizontal,curve)
            write(base+f'layer{layer}/depth_{side}.json',value)
            sides.append(value)
        body=op('min',*sides)
        envelope=op('min',gradient(center-lower-8,center-lower,-1,1),
                         gradient(center+upper,center+upper+8,1,-1))
        profiles.append(op('min',body,envelope))
    write(base+'final.json',op('max',op('max',profiles[0],profiles[1]),op('max',profiles[2],profiles[3])))
    name='data/minecraft/worldgen/noise_settings/nether.json'
    settings=read(name);settings['sea_level']=100;write(name,settings)
    name='data/apocalypse_islands/worldgen/placed_feature/floating_debris.json'
    feature=read(name)
    height=feature['placement'][2]['height']
    # A radius-four core plus casing can extend five blocks upward. Y69 leaves
    # thirty complete lava blocks at Y70..99 before the surface at Y100.
    height['min_inclusive']={'absolute':20};height['max_inclusive']={'absolute':64}
    write(name,feature)
