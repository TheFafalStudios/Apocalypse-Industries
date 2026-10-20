"""Validate indexed files against a Git export, not the developer working tree.

This catches ignored/untracked files that can make local checks pass while the
published snapshot is incomplete. No network or working-tree writes.
"""
import hashlib
import subprocess
import tempfile
import tomllib
import zipfile
from pathlib import Path

root=Path(__file__).resolve().parents[1]
with tempfile.TemporaryDirectory(prefix='apocalypse-release-export-') as directory:
    archive=Path(directory)/'repository.zip'
    subprocess.run(['git','-c','safe.directory='+root.as_posix(),'archive','--format=zip',
                    '--output='+str(archive),'HEAD'],cwd=root,check=True)
    with zipfile.ZipFile(archive) as z:
        names=set(z.namelist())
        pack=tomllib.loads(z.read('pack.toml').decode('utf-8-sig'))
        assert z.read('VERSION').decode().strip()==pack['version']
        index=z.read(pack['index']['file'])
        assert hashlib.sha256(index).hexdigest()==pack['index']['hash']
        entries=tomllib.loads(index.decode('utf-8-sig'))['files']
        for entry in entries:
            rel=entry['file']
            assert rel in names,'Indexed file missing from Git export: '+rel
            assert hashlib.sha256(z.read(rel)).hexdigest()==entry['hash'],rel
        print(f'Validated {len(entries)} indexed files in the committed Git export')
