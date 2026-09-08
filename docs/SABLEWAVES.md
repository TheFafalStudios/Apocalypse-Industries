# SableWaves local repair and activation

## Active build

- Minecraft 1.21.1 / NeoForge 21.1.248 / Java 21 / Sable 2.0.3.
- Output: `sablewaves-2.21.0-nf21.1.248-hotfix-v4-safety-render.jar`.
- SHA-256: `7f39ebd18545ada4963334b9f7ddc6cbb3f7c834e84f9ca03c1313f79a3d4118`.
- Input: `disabledmods/sablewaves-2.21.0-nf21.1.248-hotfix-v3-waterblend.jar`.
- Input SHA-256: `bb84dd40ae5e8389a095b88d3d608c5f74c5012042cdc63a2e85509af8349bfe`.

## Changes verified from the actual JAR

The v3 input already contains the NeoForge payload SAM descriptor repair, the
multiplied coordinate cache keys, and Minecraft's averaged biome water tint.
The current config already disables sun glitter (`sunGlitter=false`, strength
zero); those settings are retained. Waves, the full water sheet, foam, physics,
Weather2 integration, and wakes remain enabled as configured.

V4 adds:

- Nonblocking server fluid reads in WaterProbe, CrestParticles and OceanEffects
  using `ServerChunkCache.getChunkNow` and the returned loaded chunk directly.
- A loaded 3x3 neighborhood guard before WaterProbe's original biome lookup.
  Sea-only effects can be deferred near unloaded terrain until that neighborhood
  becomes available.
- Fetch and shoreline rays that stop at unknown terrain without requesting
  chunks or treating missing chunks as coastline. Unknown fetch defaults to
  unrestricted fetch; this is an approximation until terrain is available.
- Shoreline caches separated by Level object, with weak world keys, bounded
  entries and the existing two-second TTL. Keys include sampling height and
  ray length. No client/server, dimension, or rejoined-world cache sharing.
- Bilinear water tint interpolation at mesh vertices from cached neighboring
  water columns, preserving the v3 biome averaging and coordinate hashing.
- Renderer state reset on world change (terrain, wet sand, timing, wind sample,
  quality and error counters), atlas sprite re-acquisition for resource reloads,
  and standard blend/depth/cull cleanup even when drawing fails.

The Sable, Weather2, shaders and other mod JARs were not modified. V4 changes
seven existing classes and adds four classes. The original SableWaves mod
metadata and WaveNetwork class are preserved byte-for-byte; the internal mod
version remains 2.21.0, so use the filename/hash to identify this hotfix.

## Validation on 2026-09-07

- Built against the instance's selected launcher libraries and Sable's embedded
  companion library, rather than compile-time API stubs.
- ASM BasicVerifier data-flow checks: all 631 methods pass. This does not replace
  full in-game class transformation or integration testing.
- Static method/field reference audit: 3,924 references, zero unresolved.
- JVM `-Xverify:all`: actual renderer construction and 66,074 color, coordinate
  hash and reset assertions pass. Maximum tested hash bucket occupancy: 5.
- 23 instrumented terrain regression assertions pass: zero blocking world reads
  or missing-chunk requests, known terrain preserved, unknown edges handled,
  distinct worlds/heights/ranges separated, and cache clearing works.
- The same test harness reproduces a missing-chunk request and cross-world
  cache reuse in the original v3 JAR.
- ZIP integrity and preserved network/metadata byte comparisons pass.

No Minecraft session was launched for this change. Shader appearance, boat and
Sable craft behavior, Weather2-driven storm rendering, resource reload behavior
in-game and sustained frametimes still require gameplay validation. The custom
translucent overlay is not a native Iris water-material integration; v4 does not
claim to solve every possible shader artifact. Sun-glitter suppression remains
the existing setting, not a newly runtime-proven visual fix.

## Rebuild

From the instance root, with its existing Java 21 runtime and ASM libraries:

```powershell
python scripts/sablewaves/build.py
python scripts/sablewaves/verify.py
```

The verifier also runs `terrain_tests.py`. Build output, extracted dependencies,
decompilation evidence, argument files and reports stay under
`local/sablewaves-dev/`. The build refuses input with a different SHA-256.
`Patch.java` transplants selected compiled methods into the original renderer;
the compilation fragment is not a replacement for the full renderer.

## Configuration changes and rollback

Per the user's instruction, inactive mod JARs belong in `disabledmods/` and have
`disabled: true` in ATLauncher's `instance.json`. Only the selected variant goes
in `mods/`, with `disabled: false`. Preserve inactive variants for A/B testing;
do not put duplicate versions of the same mod in the active folder.

V3 remains disabled. To return to the earlier SableWaves-off state, disable v4
in ATLauncher so it moves to `disabledmods/`. To compare v3, disable v4 first,
then enable v3. Backups of pre-activation launcher metadata and SableWaves config
are in `local/sablewaves-dev/backups/`.

For the next gameplay check, use an ocean in a test-world copy. Inspect water
color boundaries and foam with the current shader, cross shorelines and chunk
edges, ride a boat/test a Sable craft, and reload resources. Capture an unfiltered
five-minute Spark server profile plus a client frame-time profile. Look for
new `sablewaves` errors, missing wakes, graphics-state corruption and blocking
chunk acquisition. Do not interpret the headless tests as measured FPS gains.

This is a local activation, not a published Packwiz release. The custom JAR is
not included in the current Packwiz download metadata; the release work area
must arrange a legal patch/download route before distributing it. The JAR's
metadata declares MIT; no third-party binaries were staged or published here.
