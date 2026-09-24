import hashlib,pathlib,tomllib,zipfile
r=pathlib.Path(__file__).resolve().parents[1]
p=tomllib.loads((r/'pack.toml').read_text(encoding='utf-8-sig'))
assert p['version']==(r/'VERSION').read_text().strip(), 'VERSION differs from pack.toml'
idx=r/p['index']['file']
assert hashlib.sha256(idx.read_bytes()).hexdigest()==p['index']['hash']
seen=set();destinations=set()
for e in tomllib.loads(idx.read_text())['files']:
 rel=e['file'];path=(r/rel).resolve()
 assert path.is_relative_to(r) and rel not in seen, rel
 seen.add(rel)
 assert path.is_file(), rel
 assert hashlib.sha256(path.read_bytes()).hexdigest()==e['hash'], rel
 if e.get('metafile'):
  m=tomllib.loads(path.read_text(encoding='utf-8-sig'));target='mods/'+m['filename']
  assert target not in destinations, target
  destinations.add(target)
 else:
  assert rel not in destinations,rel
  destinations.add(rel)
 assert not any(x in pathlib.PurePosixPath(rel).parts for x in ('saves','logs','crash-reports','backups')),rel
 assert not rel.endswith(('.db','.sqlite','.sqlite3','.bak')),rel
 assert rel not in ('config/spark/activity.json','kubejs/config/web_server.json'),rel
installer=r/'dist'/'Apocalypse-Industries-ATLauncher.zip'
icon=r/'instance.png'
assert installer.is_file(), 'ATLauncher installer is missing'
assert icon.is_file(), 'instance.png source icon is missing'
with zipfile.ZipFile(installer) as archive:
 assert archive.testzip() is None, 'ATLauncher installer ZIP is corrupt'
 cfg=archive.read('instance.cfg').decode('utf-8')
 props={line.split('=',1)[0]:line.split('=',1)[1] for line in cfg.splitlines() if '=' in line}
 icon_key=props.get('iconKey')
 assert icon_key, 'instance.cfg has no iconKey'
 icon_entry=icon_key+'.png'
 assert icon_entry in archive.namelist(), f'ATLauncher icon entry missing: {icon_entry}'
 assert archive.read(icon_entry)==icon.read_bytes(), 'ATLauncher installer icon differs from instance.png'
 assert 'instance.png' not in archive.namelist(), 'Bare instance.png is ignored by the ATLauncher MultiMC importer'
print(f'Validated {len(seen)} index entries and pack index hash')
