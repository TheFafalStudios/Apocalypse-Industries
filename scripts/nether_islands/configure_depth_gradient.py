"""Spread debris vertically and interpolate ore yield continuously with depth."""
import json
from pathlib import Path


def configure(destination):
    root=Path(destination)/'data/apocalypse_islands/worldgen'
    name=root/'configured_feature/floating_debris.json'
    data=json.loads(name.read_text());c=data['config']
    c['min_blocks']=21;c['max_blocks']=24
    c['depth_gradient']={'min_y':-44,'max_y':64,'shallow_min':3,'shallow_max':6}
    name.write_text(json.dumps(data,indent=2)+'\n')
    name=root/'placed_feature/floating_debris.json'
    data=json.loads(name.read_text())
    data['placement'][2]['height']={'type':'minecraft:trapezoid','plateau':60,
        'min_inclusive':{'absolute':-44},'max_inclusive':{'absolute':64}}
    name.write_text(json.dumps(data,indent=2)+'\n')
