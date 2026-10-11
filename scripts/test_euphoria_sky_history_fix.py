from pathlib import Path
from zipfile import ZipFile
import json,hashlib,re,unittest
root=Path.cwd()
report_path=root/'docs/EUPHORIA_SKY_HISTORY_FIX_v1.json'
r=json.loads(report_path.read_text())
source=root/'shaderpacks'/r['source']; candidate=root/'shaderpacks'/r['candidate']
with ZipFile(candidate) as z: taa=z.read('shaders/lib/antialiasing/taa.glsl').decode()
# CPU reference for categorical interpolation-footprint validity; no GPU claim.
def valid(history,current): return all(abs(h-current)<=0.1 for h in history)
def resolve(history,current,old_color,new_color):
    return new_color*.1+old_color*.9 if valid(history,current) else new_color
class Regression(unittest.TestCase):
    def test_revealed_sky_discards_dark_silhouette(self):
        self.assertEqual(resolve([.5]*16,1.,.1,.8),.8)
    def test_incoming_geometry_discards_sky(self):
        self.assertEqual(resolve([1.]*16,.5,.8,.1),.1)
    def test_stable_cloud_history_is_retained(self):
        self.assertAlmostEqual(resolve([1.]*16,1.,.6,.8),.62)
    def test_catmull_outer_foreground_tap_rejects(self):
        h=[1.]*16; h[0]=.5
        self.assertFalse(valid(h,1.))
    def test_particle_trail_is_rejected(self):
        self.assertFalse(valid([.25]*16,1.))
    def test_first_frame_is_rejected(self):
        self.assertFalse(valid([0.]*16,1.))
    def test_stable_terrain_is_retained(self):
        self.assertTrue(valid([.5]*16,.5))
    def test_exact_payload_scope_and_original_hash(self):
        self.assertEqual(hashlib.sha256(source.read_bytes()).hexdigest(),r['source_sha256'])
        with ZipFile(source) as a,ZipFile(candidate) as b:
            self.assertEqual(set(a.namelist()),set(b.namelist()))
            self.assertEqual({n for n in a.namelist() if a.read(n)!=b.read(n)},set(r['changes']))
        self.assertEqual(Path(str(source)+'.txt').read_bytes(),Path(str(candidate)+'.txt').read_bytes())
    def test_glsl_guards_and_brackets(self):
        self.assertIn('#if LONG_EXPOSURE == 0 && !defined RENKO_CUT',taa)
        self.assertIn('#elif defined DISTANT_HORIZONS',taa)
        self.assertIn('#ifdef VOXY',taa)
        self.assertLess(taa.index('if (!ApocalypseHistoryValid'),taa.index('vec3 tempColor = textureCatmullRom'))
        with ZipFile(candidate) as z:
            for name in r['changes']:
                s=z.read(name).decode(); depth=0
                for line in s.splitlines():
                    if re.match(r'\s*#\s*(if|ifdef|ifndef)\b',line): depth+=1
                    if re.match(r'\s*#\s*endif\b',line): depth-=1
                    self.assertGreaterEqual(depth,0)
                self.assertEqual(depth,0)
                clean=re.sub(r'/\*.*?\*/|//[^\n]*','',s,flags=re.S)
                self.assertEqual(clean.count('{'),clean.count('}'))
result=unittest.TextTestRunner(verbosity=2).run(unittest.defaultTestLoader.loadTestsFromTestCase(Regression))
r['cpu_regression_tests']={'run':result.testsRun,'failures':len(result.failures),'errors':len(result.errors),'scope':'CPU reference scenarios and source/archive checks; not GLSL execution'}
report_path.write_text(json.dumps(r,indent=2)+'\n')
raise SystemExit(not result.wasSuccessful())
