"""Install only tested island-layer JSON changes; keep ore and addon intact."""
from pathlib import Path
from datetime import datetime
import argparse, hashlib, json, shutil, zipfile

ROOT = Path(__file__).resolve().parents[2]
DEST = ROOT / 'config/paxi/datapacks/diabolical_islands_wip'


def main(stage, audit):
    result = json.loads(audit.read_text())
    assert result['pass']
    assert 45 <= result['lavaContactIslandPercent'] <= 55
    assert result['actualLavaContactColumns'] > 0
    assert all(n > 0 for n in result['solidBlocksByHeightBand'])
    addon = 'apocalypse-nether-islands-0.4.0.jar'
    with zipfile.ZipFile(ROOT/'mods'/addon) as p, zipfile.ZipFile(audit.parent/'mods'/addon) as t:
        for name in p.namelist():
            if name.endswith('.class'): assert p.read(name)==t.read(name), name
    sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
    changed = []
    for source in stage.rglob('*'):
        if source.is_file():
            rel = source.relative_to(stage)
            target = DEST / rel
            assert target.is_file(), rel
            if source.read_bytes() != target.read_bytes():
                assert rel.as_posix().startswith('data/diabolical_islands/worldgen/density_function/islands/'), rel
                changed.append((source,target))
    assert changed
    assert {p.relative_to(stage) for p in stage.rglob('*') if p.is_file()} == {
        p.relative_to(DEST) for p in DEST.rglob('*') if p.is_file()}
    baseline = json.loads((ROOT / 'docs/NETHER_ISLANDS_CASED_DEBRIS_INSTALLATION.json').read_text())
    for entry in baseline['files']:
        if entry['afterSha256'] is not None:
            assert sha(ROOT / entry['path']) == entry['afterSha256'], entry['path']
    record = ROOT / '.local/nether-islands-test' / datetime.now().strftime('layers-install-%Y%m%d-%H%M%S')
    record.mkdir(exist_ok=False)
    entries = []
    for source,target in changed:
        backup = record / 'rollback' / target.relative_to(ROOT)
        backup.parent.mkdir(parents=True,exist_ok=True)
        shutil.copy2(target,backup)
        before = sha(target)
        shutil.copy2(source,target)
        assert sha(source)==sha(target)
        entries.append({'path':str(target.relative_to(ROOT)), 'beforeSha256':before,'afterSha256':sha(target)})
    evidence = {'scope':'WIP only','validation':result,'nativeAudit':str(audit.relative_to(ROOT)),
        'files':entries,'rollbackDirectory':str(record / 'rollback'),
        'interpretation':'50% target by connected terrain footprints, not land area. Sample excludes boundary-cut islands and footprints smaller than 64 square blocks. Caves and structure foundations can change actual island counts.',
        'activation':'User-managed relaunch; fresh terrain required. Existing worlds untouched.'}
    content = json.dumps(evidence,indent=2)+'\n'
    (record / 'installation.json').write_text(content)
    (ROOT / 'docs/NETHER_ISLANDS_LAYERS_INSTALLATION.json').write_text(content)
    print(json.dumps({'changedFiles':len(entries),'validation':result,'rollbackDirectory':str(record/'rollback')},indent=2))


if __name__ == '__main__':
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--pack',type=Path,required=True)
    p.add_argument('--audit',type=Path,required=True)
    a=p.parse_args()
    main(a.pack.resolve(),a.audit.resolve())
