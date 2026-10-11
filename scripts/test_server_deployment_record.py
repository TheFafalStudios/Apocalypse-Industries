import importlib.util
from pathlib import Path
import json
import tempfile
import unittest
from unittest.mock import patch
import zipfile

spec=importlib.util.spec_from_file_location('records', Path(__file__).with_name('build_server_deployment_record.py'))
m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m)

class RecordTests(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory()
        self.root=Path(self.tmp.name); self.before=self.root/'before'; self.after=self.root/'after'
        self.before.mkdir(); self.after.mkdir()
        self.rel='kubejs/server_scripts/fix.js'
        for root, text in [(self.before,'old'),(self.after,'new')]:
            p=root/self.rel; p.parent.mkdir(parents=True); p.write_text(text)
    def tearDown(self): self.tmp.cleanup()
    def run_record(self, **kw):
        return m.prepare(self.before,self.after,self.root/'record','v2','v2','abcdef1',[self.rel],**kw)
    def test_record_only_rollback_and_no_marker_writes(self):
        marker=self.before/'SERVER_PACK_COMPATIBILITY.toml'; marker.write_text('old marker')
        self.run_record()
        out=self.root/'record'
        self.assertFalse(list(out.glob('*.zip')))
        self.assertFalse((out/'DELETE_THESE_FILES.txt').exists())
        self.assertEqual((out/'ROLLBACK'/self.rel).read_text(),'old')
        self.assertEqual(marker.read_text(),'old marker')
        self.assertEqual((self.after/self.rel).read_text(),'new')
        self.assertEqual(json.loads((out/'DEPLOYMENT_STATUS.json').read_text())['status'],'prepared-not-deployed')
        with self.assertRaises(ValueError): self.run_record()
    def test_zip_and_delete(self):
        (self.before/'config').mkdir(); (self.before/'config/obsolete.toml').write_text('old')
        out=self.root/'record'
        m.prepare(self.before,self.after,out,'v2','v2','abcdef1',[self.rel,'config/obsolete.toml'],create_zip=True)
        with zipfile.ZipFile(out/'Server-Update-v2.zip') as z: self.assertEqual(z.namelist(),[self.rel])
        self.assertEqual((out/'DELETE_THESE_FILES.txt').read_text().strip(),'config/obsolete.toml')
        self.assertEqual((out/'ROLLBACK/config/obsolete.toml').read_text(),'old')
    def test_protection_and_mismatch_preflight(self):
        for rel in ['world/level.dat','../escape','config/schematics/build.nbt','config/test.sqlite','config/../secret','server.properties']:
            with self.assertRaises(ValueError): m.safe_path(self.before,rel)
        (self.before/'server.properties').write_text('level-name=config/custom-save\n')
        with self.assertRaises(ValueError): m.safe_path(self.before,'config/custom-save/level.dat')
        with self.assertRaises(ValueError): m.prepare(self.before,self.after,self.root/'record','v2','v1','abcdef1',[self.rel])
        self.assertFalse((self.root/'record').exists())
    def test_full_prunes_before_traversal(self):
        for root in (self.before,self.after):
            for folder in ['world','config/schematics','config/custom-save','kubejs/blueprints','BACKUPS/worlds']:
                p=root/folder; p.mkdir(parents=True); (p/'sentinel').write_text('must not be read')
            (root/'server.properties').write_text('level-name=config/custom-save\n')
        scan=m.os.scandir
        def guarded(p):
            self.assertFalse(any(x in Path(p).parts for x in ['world','schematics','custom-save','blueprints','BACKUPS']))
            return scan(p)
        with patch.object(m.os,'scandir',side_effect=guarded): self.run_record(full=True)
        entries=json.loads((self.root/'record/SERVER_FILE_MANIFEST.json').read_text())['files']
        self.assertEqual([x['path'] for x in entries],[self.rel])
    def test_catalogue_does_not_expose_values(self):
        baseline=self.root/'published'; (baseline/'config').mkdir(parents=True)
        (baseline/'config/settings.toml').write_text('password=baseline-secret')
        (self.before/'config').mkdir(); (self.before/'config/settings.toml').write_text('password=remote-secret')
        self.run_record(baseline_root=baseline,review_paths=['config/settings.toml'])
        text=(self.root/'record/REMOTE_CHANGE_CATALOGUE.json').read_text()
        self.assertNotIn('baseline-secret',text)
        self.assertNotIn('remote-secret',text)
        self.assertIn('pending-review',text)

if __name__=='__main__': unittest.main()