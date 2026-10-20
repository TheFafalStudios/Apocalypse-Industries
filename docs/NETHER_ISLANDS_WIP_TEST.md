# Nether lava-sea WIP test — updated 2026-10-18

## Current addon correction — 2026-10-19

WIP now uses `apocalypse-nether-islands-0.9.0.jar`. Structure clearance covers all eligible Nether structures, including Golem Dungeons' Piglin Factory and LTAB's Nether house. Complete complexes receive a final foundation pass after template placement, and relocated jigsaw terrain junctions move with their pieces. The native audit covered all five enabled structure types and all four bastion roots; 1,338 base samples passed at two depths. See `NETHER_STRUCTURE_FIX_2026-10-19.md` for scope, evidence and remaining visual-test limits. User-managed full WIP relaunch and fresh terrain are required; existing chunks are unchanged. Historical sections below describe earlier revisions.

WIP-only experiment through the existing Paxi loader plus
`mods/apocalypse-nether-islands-0.8.0.jar`. No release, server deployment, existing
world edit, or WIP/live-server restart or reload was performed. Native verification
uses fresh, disposable test-server worlds under `.local/nether-islands-test`.

Upstream: Diabolical Islands by Klinbee, MIT license, version 1.0.0 (datapack),
Modrinth version BV7GWyEF. Source SHA-1:
`5129ea20a4749272fca362834dba28dcf3ad836f`.
https://modrinth.com/datapack/diabolical-islands

## Original single-layer preset (historical)

- Only layer 1 contributes; layers 2–5 remain as unused upstream data.
- Layer 1 horizontal noise scale: 0.85 -> 1.15 (smaller horizontal features).
- Layer 1 noise offset: -0.425 -> -0.55 (less land coverage).
- Layer center moved from Y 0 to Y 48; bottom gradient -16..48, top gradient 47..112.
- Single low island band; no upper island stacks.
- Lava sea level: -32 -> 32, 16 blocks below the layer center.
- Upstream contours, cave functions, dimension height, biome logic and structure types retained.
- Applied upstream 1.21 structure overlay and pinned pack format to 48.

## Revision 2 changes

### Structures receive supporting islands

The addon reads actual planned structure starts during generation and adds a
foundation under each piece's ground footprint. Jigsaw pieces use their ground
level delta. The pieces' foundations merge, have rounded 16-block margins, and
taper beneath the lava surface. They are added during noise generation, before
surface painting and biome features. Existing structures, loot and spawn boxes
are not relocated or replaced.

The normal structure-reference pass includes foundation margins, avoiding abrupt
cuts where the apron crosses a chunk boundary. It uses vanilla's existing
eight-chunk structure-start scan, with no additional chunk-status requests.
Very large third-party structures extending beyond vanilla's reference radius
still require individual verification.

Foundation activation requires both the Nether dimension and the
`apocalypse_islands:foundations_enabled` density-function marker in this datapack.
Ordinary Nether worlds without the marker and other dimensions are unaffected.

### Squared edges and the zFastNoise height mismatch

The original active island fields use `flat_cache`, sampled on a four-block
horizontal grid, and the final terrain function interpolates the whole island
body on four-block cells. This can produce visibly faceted island outlines. The
first WIP preset also used abrupt vertical range cutoffs.

Revision 2 caches the two horizontal island shape fields at exact one-block
coordinates with the addon's `apocalypse_islands:cache_columns` density type.
The island body is evaluated at block resolution while cave interpolation is
retained; horizontal noise fields are not recomputed throughout every vertical
column. Hard height cutoffs become continuous density fades.

A separate concrete compatibility bug was reproduced with the installed
zFastNoise 1.0.13: upstream noise generation begins at Y -48 but the dimension
begins at Y -64. The optimized writer indexes chunk sections relative to the
noise floor. All 6,542 comparable land columns in the baseline audit were written
**16 blocks below** their expected heights. Aligning noise min_y to -64 and height
to 384 removes the displacement while retaining the same upper limit, Y 320.
The intermediate aligned-floor audit matched expected heights in all 6,542
comparable columns and passed every sampled fortress/bastion floor check.
No global zFastNoise settings were changed.

These remain experimental settings. Biome surface appearance, portal arrival,
ancient debris availability and the visual fit of large foundations require
gameplay testing. Vehicle use is encouraged by geography, not enforced.

## Testing

Launch/relaunch WIP and create a **fresh disposable test world**. Paxi automatically
loads `config/paxi/datapacks/diabolical_islands_wip`. Check it appears in
`/datapack list enabled`, then inspect multiple Nether biomes.
Do not use an established world for this dimension-height experiment; existing
chunks do not become the new terrain. Do not remove the datapack from worlds
created with it and continue playing those worlds.

The official ZIP remains in `.local/nether-islands-test`, outside distribution.
`scripts/nether_islands/build_test_pack.py` records the edits and refuses to
overwrite an existing destination. `--revision 1` reconstructs the first preset;
`--revision 2` reconstructs the smooth single-layer preset with the current ore
settings; `--revision 3` includes restored island layers, revision 4 adds the
basalt seabed, revision 5 raises lava and rounds the islands, and default revision
6 targets glowstone at island undersides, and default revision 7 adds the ore
depth gradient. These require
the foundation addon.

`build_addon.py` builds the addon without installing it; `--tests` adds an isolated
native audit mod and must not be used for the installed artifact.
`run_native_audit.py` creates a fresh world with seed 104729 and checks actual
noise chunks, structure centers/corners, dimension scoping and registry decoding.
`compare_audits.py` produces the diagnostic height map and validation summary.

See `NETHER_ISLANDS_VALIDATION.json` for final native results and
`assets/nether-islands-density-comparison.png` for an actual noise-chunk height
comparison. The native tests are terrain and structure-support checks, not a
client gameplay or shader visual test. Rollback files and before/after hashes are
kept in the timestamped `.local/nether-islands-test/install-*` record.

Final native result: **1,570/1,570** sampled floor centers/corners supported
(145 fortress pieces and 169 bastion pieces), versus 0/1,570 in the baseline.
All 6,695 comparable land columns in the final revision matched expected heights.
The integration run loaded Biomes O' Plenty 21.1.0.14, TerraBlender 4.1.0.8,
GlitchCore 2.1.0.2, Lithostitched 1.8.0+beta4 and zFastNoise 1.0.13 on NeoForge
21.1.248. The installed production classes were compared byte-for-byte with
those in the passing test artifact; the audit entrypoint is excluded from WIP.

Existing generated Nether chunks keep their previous terrain, including the old
height offset. Use another fresh disposable test world for comparing revision 2;
simply entering more chunks in the old experiment can produce terrain borders.

## Solid bedrock floor (2026-10-18)

The WIP preset now generates a continuous, one-block bedrock layer at the
dimension's lowest level, **Y -64**. The final density explicitly supplies solid
terrain at that level, and the first surface rule replaces it with bedrock.
There is no added bedrock roof or bedrock at Y -63. Island shapes and the lava
sea level are unchanged above the floor.

The isolated native `--floor-only` audit checks all 256 bottom blocks in each of
nine chunks, including positive and negative coordinates, after the FEATURES
generation stage. This includes surface rules, carvers and features, with the
same biome/terrain mod set listed above. The installation record and results
are in `NETHER_ISLANDS_BEDROCK_INSTALLATION.json`; rollback copies are separate
from the earlier revision 2 installation record.

Relaunch WIP to load the change. It applies to newly generated chunks; existing
Nether chunks are not retroactively repaired. Use a fresh disposable test world
for a continuous floor throughout the test Nether. No existing saves were edited.

## Floating ancient debris — initial preset (2026-10-18)

The initial 0.3.0 preset formed small connected clumps suspended in the lava ocean.
Each successful clump contains **3–6 ancient debris blocks**. Before placement,
every block and its six neighboring positions must be lava; the clump is planned
completely before writing blocks. It therefore starts fully submerged with a
lava shell, without attaching to islands or the bedrock floor. Normal mining,
smelting and netherite recipes remain unchanged.

The initial WIP balance is one placement attempt per four chunks on average,
with a uniformly sampled origin between **Y -48 and 20**. Attempts obstructed by
terrain are skipped. The configured size can extend a clump up to five blocks
from its origin, still clear of both the Y -64 floor and Y 32 lava surface.
Generation replaces the vanilla large/small ancient debris features in every
biome tagged `minecraft:is_nether`, including tagged modded biomes. It does not
add buried deposits on top of this new supply.

Controls within `config/paxi/datapacks/diabolical_islands_wip` (current values are
described in the cased-deposit section below):

- `data/apocalypse_islands/worldgen/configured_feature/floating_debris.json`:
  `min_blocks` and `max_blocks` set clump size (minimum must not exceed maximum).
- `data/apocalypse_islands/worldgen/placed_feature/floating_debris.json`:
  `rarity_filter.chance` is the average chunks per attempt (lower means more),
  and `height_range` sets origin depth. Keep enough margin for the maximum clump
  extent when changing depth/size; the lava-shell check also rejects exposed spots.
- `data/apocalypse_islands/neoforge/biome_modifier/` adds the floating feature
  and removes the vanilla buried features from Nether biomes.

`--debris-only` performs an isolated native 64-chunk audit after FEATURES,
checking connected clump sizes, lava surroundings, depth, biome replacement and
the bedrock floor. See `NETHER_ISLANDS_DEBRIS_INSTALLATION.json` for measured
results, file hashes and rollback location. This is generation validation;
in-game discoverability and mining balance still need player testing.

Passing seed-104729 sample: 64 chunks, 20 complete clumps containing 96 debris
blocks, 414 exposed clump faces touching lava, and 16,384 intact bedrock-floor
blocks. All 10 tagged Nether biomes had the custom feature and neither vanilla
debris feature. Observed clump sizes were 4–6 in this sample (configured 3–6).

Relaunch WIP and use fresh test terrain. Existing chunks keep their previous ore
generation; no existing world was edited. The addon update replaces 0.2.0 with
0.3.0 and preserves the existing foundation and column-sampling implementation.

## Larger, rarer deposits with rock casing (2026-10-18)

Addon 0.4.0 increases each connected ancient debris core to **9–18 blocks** and
changes rarity to **one placement attempt per 12 chunks**. The nominal debris
budget stays at **1.125 blocks per chunk**: the old average was 4.5 / 4, and the
new average is 13.5 / 12. These are expected rates before rejected placements;
individual regions vary, and terrain/casing clearance can cause attempts to fail.
The finite native generation sample validates placement, not exact equality of
realized ore density across every terrain seed.

Every exposed face of the ore core is covered by a one-block rocky casing,
selected from **60% blackstone, 25% basalt and 15% netherrack**. These percentages
are placement weights, not fixed ratios per deposit. The complete casing must
have lava outside it before anything is written; the deposit remains submerged
and detached from terrain. Failed checks leave no partial ore or rock scraps.
The core is limited to a four-block radius around its origin to keep deposits
compact and safely within neighboring feature-generation chunks.

Origin depth remains Y -48..20. With the four-block core radius and one-block
casing, all deposit blocks remain within Y -53..25, clear of both the floor and
the lava surface. Mining and processing recipes remain unchanged.

The configured feature JSON controls core `min_blocks`/`max_blocks` (1–64) and
the weighted `casing` block-state provider. The placed feature JSON controls
`rarity_filter.chance` (now 12) and the height range. Match the ratio of average
core size to rarity when adjusting density; rock casing does not consume any
of the configured ancient debris count.

The native `--debris-only` audit now scans 256 generated chunks, checks 9–18-block
connected cores, all ore-facing casing, the outer lava shell and all 65,536
floor blocks. See `NETHER_ISLANDS_CASED_DEBRIS_INSTALLATION.json` for results,
file hashes and a separate rollback record. Prior installation records remain
unchanged. Relaunch WIP and use newly generated terrain; no existing saves were
edited and existing deposits are not converted.

Passing seed-104729 sample: 301 debris blocks across 256 chunks (1.176 per chunk),
21 complete cores of 9–18 debris plus one core clipped by the sample boundary.
Checks covered 821 casing blocks, 2,503 outer faces touching lava, all 10 tagged
Nether biomes, and 65,536 intact bedrock-floor blocks. All three casing materials
were present. The previous 64-chunk sample and this larger sample cover different
areas and are not a direct before/after density measurement.

## Restored island layers — revision 3, historical contact target (2026-10-18)

Layers **1–4** are active; layer 5 remains disabled. Layers 1 and 2 overlap in
the lower band and extend down through the Y 32 lava surface. Their lower
gradient now ends at Y 24, so even small low-layer bodies reach the lava.
Layers 3 and 4 form separate airborne bands, with geometric limits Y 128–208
and Y 224–288 respectively. The continuous fades extend beyond those limits
only into negative density. The gaps prevent the bands from merging vertically.

All four active layers use noise offset -0.55. The lower layers retain horizontal
scale 1.15; the upper layers use 1.05 for somewhat broader, fewer airborne bodies.
Upstream coordinate shifts and differing top splines provide shape variety.
The smooth one-block column cache and vertical
fades apply to every active layer; the old flat-cache faceting is not restored.

The intended split is **roughly 50% lava-contact islands and 50% airborne**, by
island count rather than total land area. `--layers-only` samples the connected
terrain footprints at four-block spacing in three separate 1,024-square-block
regions, excluding boundary-cut bodies and bodies smaller than 64 square blocks.
It combines both low fields before counting, so overlapping low islands are not
double-counted. This estimates the designed terrain bodies before caves and
structure foundations; it is not a guarantee of exactly 50% in every seed or
local area. The audit also checks actual generated noise chunks for the lower,
middle and high bands, lava contact and the solid floor.

See `NETHER_ISLANDS_LAYERS_INSTALLATION.json` for measured results, changed-file
hashes and rollback location. The bedrock settings, structure addon, lava level
and larger cased-debris configuration are retained. Relaunch WIP and use fresh
test terrain. Existing chunks are not converted and may form generation seams.

Final seed-104729 footprint sample: **99 lava-contact bodies and 100 airborne
bodies (49.75% lava contact)** across the three regions. Actual generation in
39 noise chunks contained solid islands in all three height bands, 256 solid
columns at the lava surface and 9,984 intact solid floor columns. The reported
50/50 split is the wider pre-cave footprint estimate, not a 3D census of those
39 chunks or a per-region guarantee.

## Basalt seabed — revision 4 (2026-10-18)

A continuous basalt bed now covers the bedrock floor. Its thickness varies
smoothly between **3 and 5 blocks**, with its top at **Y -61..-59** and the
single bedrock safety layer retained at **Y -64**. Three noise octaves produce
coherent patches and gently terraced transitions instead of independent random
column heights. Vertical-axis basalt is used throughout this thin seabed.

The `apocalypse_islands:seabed` density function supplies the floor volume after
the island/cave density calculation, so caves cannot punch holes through it.
The low surface rule paints basalt beneath Y -58. TerraBlender can prepend its
own bedrock rules, so addon **0.5.0** also finalizes these five possible basalt
layers after surface generation, replacing only solid blocks at Y -63..-59
and retaining bedrock at -64. The compatibility pass is scoped to this Nether
preset and the presence of the seabed density resource; other worlds/dimensions
are unaffected.

The highest seabed is still five lava blocks below the lowest possible debris
casing at Y -53. Island layers, lava level, ore size/rarity and structure supports
are unchanged. The native `--seabed-only` audit checks all 16,384 columns in
64 chunks after FEATURES for continuous bedrock, basalt thickness 3/4/5, lava
above, and neighboring height steps no larger than one block.

Controls: `data/apocalypse_islands/worldgen/noise/seabed.json` sets the noise
octaves; `data/apocalypse_islands/worldgen/density_function/seabed.json` sets
the height gradient and noise strength. The finalization pass is deliberately
limited to this five-block band, so changing the vertical range requires updating
the addon too. See `NETHER_ISLANDS_SEABED_INSTALLATION.json` for results and rollback
evidence. Relaunch WIP and use fresh terrain; existing saves were not edited.

## Rounded floating islands and lava Y100 — revision 5, current (2026-10-18)

The user retired the enforced half-touching-lava target. Islands can float above
the ocean naturally; lava traversal is required to reach ancient debris instead.
Lava sea level is now **Y 100** (source blocks top out at Y 99).

The old lower profile had an overlapping, clamped vertical interval between its
bottom and top gradients. Its horizontal footprint stayed constant through that
interval, producing straight-sided columns even with one-block noise sampling.
The new profile uses one shared horizontal strength for both ends, with squared
vertical falloff from a single waist. As the footprint weakens toward an island
edge, both top and bottom taper toward that waist; there is no constant-width
vertical interval. Normal Minecraft block steps and cave cuts remain possible.
Client visual verification is still needed for the final appearance.

Layers 1/2 are centered on **Y 128**, with lower/upper extents up to 64/48 blocks;
layer 3 is centered on **Y 208**, and layer 4 on **Y 280**, each with extents up
to 24 blocks. The actual extent depends on local shape noise. There is no
minimum lava contact or statistical contact-ratio requirement.

Ancient debris cores remain **9–18 blocks, one attempt per 12 chunks** with the
same rock casing. Origins are now **Y 20..64**. The core radius is at most four
blocks, plus one casing block, so the highest possible deposit block is **Y 69**.
This leaves **30 full lava blocks at Y 70..99 above the entire deposit**, including
its casing. The seabed stays at Y -61..-59 over bedrock Y -64.

Addon 0.6.0 makes structure foundations use the actual lava level and limits
their underside to a local taper below the structure rather than stretching
them down into the lava. The vanilla fortress height range moves up by 68 blocks
in this Y100 preset to avoid submerging it. Structure islands remain supported;
ordinary worlds without this preset are unchanged.

The combined `--elevated-only --structures-only` native audit checks raised
island bands, debris casing and lava overhead, the basalt/bedrock floor and
fortress/bastion support. Contact counts are informational estimates rather than
acceptance criteria. See `NETHER_ISLANDS_RAISED_INSTALLATION.json` for the measured
results, hashes and rollback evidence.

**Fully relaunch Minecraft** to load addon 0.6.0 before testing. Use a fresh
disposable world; existing chunks retain old terrain/lava levels and will form
seams. No existing saves were edited and no WIP/server restart was performed.

Final combined native result (seed 104729): all three raised island bands were
present in actual noise chunks; the wider footprint estimate contained 38
lava-contact and 161 airborne bodies (informational only). The 256-chunk ore
sample contained 301 debris blocks, with its highest casing at Y65 and **34 full
lava blocks overhead**. All 65,536 sampled floor blocks were bedrock; the
separate 64-chunk seabed check retained the three-to-five basalt thicknesses.
All **1,570/1,570** sampled fortress/bastion floor points remained supported.
These are native generation checks, not a client visual approval of the shapes.

## Modded biomes and island glowstone — revision 6 (2026-10-18)

WIP's Biomes O' Plenty toggles disabled all five installed Nether biomes. This
change enables only **Crystalline Chasm, Erupting Inferno, Undergrowth, Visceral
Heap and Withered Abyss** in `config/biomesoplenty/biome_toggles.json`. Overworld
and End biome decisions remain unchanged. Registry/tag membership alone does
not prove a biome is enabled for generation; the ecology audit samples the
actual biome source across an 8,192-block-wide region at Y128.

Vanilla glowstone picks a random height and succeeds only when that position is
immediately below suitable ceiling stone. With sparse roofless islands across
a 384-block dimension this wastes most attempts. The two existing placed
features (`minecraft:glowstone` and `minecraft:glowstone_extra`) now sample air
above the lava, scan upward by at most 32 blocks for netherrack/basalt/blackstone,
and place the ordinary hanging glowstone blob directly below that underside.
The original attempt counts and configured glowstone feature are retained;
there is no artificial Nether roof or floating glowstone ore substitute.

The `--ecology-only` audit uses WIP's actual TerraBlender configuration and an
explicit copy of the five-toggle test configuration. It requires all five
modded Nether biomes to appear in sampled biome selections and counts actual
glowstone after FEATURES in island-rich chunks and representative modded-biome
chunks. The runner now copies WIP biome toggles by default for other biome tests
too; earlier isolated tests used BOP's defaults rather than WIP's toggle file.

See `NETHER_ISLANDS_ECOLOGY_INSTALLATION.json` for results, hashes, example biome
coordinates in the disposable seed-104729 world and rollback evidence. Those
coordinates are seed-specific. The production addon stays at 0.6.0. Fully
relaunch Minecraft, then use fresh Nether terrain; existing chunks do not acquire
new features or biome assignments. No existing worlds were edited.

Passing seed-104729 ecology result: all five BOP Nether biomes appeared in the
16,641-point actual biome-source sample. The 69 generated chunks contained
**721 glowstone blocks across 384 columns**, at Y109..155 in this sample.
This validates actual feature placement with the selected worldgen mods; it is
not a full-client visual test or a guarantee of biome proximity in another seed.

## Safe automatic Nether portals — addon 0.7.0 (2026-10-18)

Vanilla portal creation uses a fallback minimum of Y70 when it cannot find a
natural site. That is below this preset's Y100 lava surface. The addon now rejects
natural portal sites whose lowest interior block is below **sea level + 4** and
raises the emergency fallback minimum to that same level (**Y104** here).
Vanilla still searches nearby natural sites and creates its supported obsidian
landing platform when a fallback is required. Portal size, orientation and normal
coordinate scaling are unchanged.

Existing destination portals are filtered before the nearest one is selected.
The check walks down to the portal's actual bottom, so an upper portal block
cannot make a low frame appear safe. Portals below the safety minimum, or with
lava directly against either exit face anywhere across the portal, are ignored.
Their blocks are not moved or deleted. The rules apply only to the custom WIP
Nether marker; normal Overworld portal behavior is preserved.

The `--portals-only` native audit creates bounded arenas in a disposable world,
with lava below Y100 and no natural islands above. It tests both X/Z portal
orientations from a below-lava target, dry headroom and solid exit floors, rejection
of an existing submerged portal, reuse of the safe portal, and rejection after
lava is placed against its exit. A low Overworld portal must remain selectable.
See `NETHER_ISLANDS_PORTALS_INSTALLATION.json` for results, hashes and rollback.

Fully relaunch Minecraft to activate the addon. Unlike terrain-generation changes,
this applies to future portal arrivals in existing worlds too. No existing save
files were edited, and no client/server restart was performed by the agent.

Passing native portal result: both X/Z orientations created emergency portals
with the lowest interior block at **Y104**, with dry three-block headroom and
solid exit platforms. Both reused their safe portals while ignoring a submerged
Y70 frame, and rejected the entire safe portal after lava was placed against
only one exit column. A Y20 Overworld portal remained selectable.

## Complete bastion islands and debris depth gradient — addon 0.8.0 (2026-10-18)

Bastions now receive **one continuous island below the entire actual template
bounding box**, in addition to existing piece support. The base is below the
lowest template bound, so template air placement cannot erase it. Its curved,
lightly irregular perimeter extends up to 24 blocks outside the complex and
tapers into an underside about 40 blocks deep. Structure references include
that margin, preventing an island from ending abruptly at neighboring chunks.

If the bastion's lowest template bound is below Y112, the complete piece layout
is lifted just enough to put that bound at Y112. The island top is then at least
Y111, visibly above the Y100 sea, without burying lower rooms. Bastions already
high enough stay at their chosen elevation. The layout, loot and piece contents
are preserved; existing generated bastions are not relocated or retrofitted.

Debris origins now span **Y -44..64**, with a symmetric trapezoid distribution:
60 blocks of central plateau and 24-block fades at both ends. This spreads ore
through the lava depth and reduces hard concentration edges. Core yield changes
continuously with origin depth: **3–6 blocks near Y64**, about **12–15 at Y10**,
and **21–24 near Y-44**. The same casing and radius-four core are retained.
The deepest possible casing is Y-49, clear of the basalt seabed; the highest
possible casing remains Y69, leaving at least 30 full lava blocks overhead.

The expected ore budget stays **13.5 debris per successful placement attempt**
and one rarity attempt per 12 chunks: nominal **1.125 debris per chunk before
terrain rejects placements**. The symmetric height distribution and symmetric
rounding preserve that mean while rewarding deeper dives. Individual regions
still vary. `min_blocks`/`max_blocks` now set deep yield; optional `depth_gradient`
sets shallow yield and the depth interval. Omitting it preserves constant-yield
behavior for older presets.

The combined native audit compares shallow/deep clump sizes and the lava casing
clearance, rechecks floor and fortress support, and checks the bastion island
**after FEATURES**, including actual template placement, across core, rim and
twelve blocks below its cap. Earlier noise-only floor tests did not establish
that the visible island would survive template air placement.

See `NETHER_ISLANDS_BASTION_GRADIENT_INSTALLATION.json` for measured results,
hashes and rollback evidence. Fully relaunch Minecraft, then use a fresh test
world or wholly new structure regions. Existing generated islands and deposits
are unchanged; no existing saves were edited.

Passing combined result (seed 104729): the 169-piece bastion had its lowest
template bound at Y112 and island cap at Y111. After FEATURES, all **190/190**
core samples, **190/190** samples twelve blocks deeper, and **132/132** outer-rim
samples were solid. The normal 1,570 structure-floor checks also passed.
In 256 ore-sample chunks, deep clumps (mean Y below 0) averaged **17.5 debris**
across eight complete clumps; shallow clumps (mean Y above 25) averaged **6.8**
across five. Observed sizes ranged 3–22; the highest casing was Y64, leaving
35 full lava blocks overhead. The sampled seabed and bedrock remained intact.
