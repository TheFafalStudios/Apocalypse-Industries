"""Release inventory v2: prune protected storage and links before traversal."""
import os,re
from pathlib import Path

PROTECTED=re.compile(r'(?i)^(world(?:s|$|[_0-9.-]).*|saves?|dimensions|DIM-?\d+|schematics?|blueprints?|lods?|distant.?horizons?|backups?|logs|crash-reports|\.git|\.local|__pycache__|build)$')
PRIVATE={'kubejs/config/web_server.json','kubejs/all_recipes_dump.json','config/lostcities-server.toml',
         'config/forgeendertech/cached.dat','config/biomecontrolengine/dimensions_cache.json',
         'config/spark/activity.json','config/apocalypse-performance.properties',
         'config/apocalypse-performance.properties.tmp','config/create_submarine_client_state.json',
         'config/ftbquests/quests/chapters/endgame_prototype.snbt'}


def linked(p):
    return p.is_symlink() or bool(getattr(p.lstat(),'st_file_attributes',0)&1024)


def excluded(rel):
    rel=Path(rel).as_posix();low=rel.lower();name=Path(low).name
    return (low in PRIVATE or low.startswith(('config/jei/world/','config/xaero/','config/axiom/',
        'config/spark/','kubejs/exported/','kubejs/.cache/'))
        or name.startswith('apocalypse-performance-diagnostics-')
        or re.search(r'\.(bak|old|log|db|sqlite3?|pyc|tmp)(\.|$)',name) is not None
        or name in {'shadercache','cached.dat'})


def files(root):
    root=Path(root)
    if not root.exists() or linked(root):return
    def visit(directory):
        with os.scandir(directory) as entries:
            for entry in sorted(entries,key=lambda e:e.name):
                p=Path(entry.path)
                if linked(p):continue
                if entry.is_dir(follow_symlinks=False):
                    recipe_blueprint=entry.name.lower()=='blueprint' and directory.name in {'recipe','recipes'} and 'data' in directory.parts
                    if recipe_blueprint or not PROTECTED.fullmatch(entry.name):yield from visit(p)
                elif entry.is_file(follow_symlinks=False):yield p
    yield from visit(root)
