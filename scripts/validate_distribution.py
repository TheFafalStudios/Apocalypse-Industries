import hashlib,pathlib,tomllib
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
print(f'Validated {len(seen)} index entries and pack index hash')
