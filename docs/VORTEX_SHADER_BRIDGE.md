# Vortex / Apocalypse shader bridge â€” installed WIP build

Current WIP default (2026-10-19): addon **1.1.2** starts the Vortex bridge **disabled**. `/apovortex enable` opts in for the current session; restarting returns to disabled. This supersedes earlier enabled-by-default statements below. The independent water/lava fog fixes remain active. Only the bridge initializer and version metadata changed; compilation and bytecode verification passed. Evidence/rollback: `.local/submarine-lava/startup-disabled-20261019-192802/installation.json`. Loads on next client restart; none performed.


## WIP correction: 2026-10-19, addon 1.1.1

This correction supersedes the installed artifact hashes and sampling/update budget below. The original 1.1.0 evidence remains historical.

The user reported a gray Overworld view and 28 FPS / 99% GPU usage. Live logs show `/apovortex disable` did disable the bridge (`enabled=false`, `active=false`, stopped update counter), despite the scene appearing unchanged to the user. No FPS recovery or fog disappearance was verified from that command.

Inspection found that upstream Deep Seas binds `CdsHullFar` to fixed texture unit 9 at `CompositeRenderer.renderAll` HEAD. Its reservation is program-specific, while earlier passes can use that unit before the fog-consuming pass. This is an unsafe lifetime for the mask; a bad mask can apply exterior submarine water fog to air. The addon now renames this sampler to `aiWaterHullFar` and registers it through Iris, alongside the lava mask, at the consuming program. An explicit `aiHullMedium` uniform (0 outside, 1 water cabin, 2 lava cabin) also gates both helpers. Outside views reject both effects even with an invalid black mask. This fixes an identified mechanism; it does not prove that every source of the reported gray atmosphere has been eliminated. Existing Overworld atmospheric styling was preserved.

Vortex's native 128-cubed field propagation and GL state snapshots now run at most 12.5 times/second. The shader continues sampling the latest field each rendered frame. Volumetric integration uses 6 samples instead of 12; the bridge exits before scene reconstruction when no field or mist is available. These reduce work; actual FPS gains and lighting responsiveness require an in-game check. The 80 ms cadence can introduce a small delay in light response.

Installed addon: `apocalypse-submarine-lava-visuals-1.1.1.jar`, SHA-256 `e4f1d149a4a779c1e9bdd744605c19ed2722e224ff29a92850ae8dc5a44c7ac9`.
Paired shader SHA-256: `2ba51916b036fdd5044efb20784c0776f0aaf7790df8c388a76bafea763ac0be`.

Validation: 31 fluid/source regression checks, 4 Iris/parser/GPU fog variants, 114 Vortex/GPU checks (including bad-mask air/water/lava isolation and white-mask fallback), and 16 installed target/accessor signatures passed. Complete Overworld/Nether/End composite fragments compiled and linked. Offline checks establish correctness of these fixtures, not live game appearance or performance.

Evidence and rollback files: `.local/submarine-lava/fog-performance-20261019-184319/installation.json`. Build/test outputs: `Q:/GPT General/vortex-bridge-20261019-performance2`; paired shader staging: `Q:/GPT General/vortex-bridge-20261019-performance-shader1`. Only WIP addon/shader and their manifest entries changed. Native Deep Seas configuration, general shader quality settings, server files and protected storage were preserved.

A full Minecraft client restart is required to load 1.1.1. None was performed. `/apovortex disable` remains session-only; after restart the bridge defaults to enabled.

## Original 1.1.0 installation record


Installed 2026-10-19. Client addon `apocalypse-submarine-lava-visuals-1.1.0.jar` and the paired `ApocalypseAtmosphere_NetherDH_v1.2.zip` implement the bridge for Deep Seas 3.4.4 / Iris 1.8.14 Beta 1 / Minecraft 1.21.1. The existing mod ID is retained so the lava integration is upgraded in place.

## Behavior

- Vortex's real emitter discovery, moving alarm/sonar/staff lights, sublevel transforms, occlusion grid, injection and light-field propagation run with the selected Apocalypse shader. The Vortex configuration still controls the master switch, lights, brightness, pixel lighting, haze and mist. Other shaderpacks retain upstream behavior.
- The bridge uses Vortex's FLOOD/VANILLA field data and HYBRID field component. Native VOLUMES/PIXEL styles use the FLOOD computation while the bridge is active, without modifying their saved configuration. The native full-screen lighting, atmosphere and puff rendering passes are suppressed for the selected shader; their calculations are not drawn a second time over Iris.
- Iris allocates the field, occlusion and hull samplers after its standard sampler registration. The renderer preserves framebuffer, viewport, program, VAO/buffer, low texture-unit bindings, sampler objects, pixel alignment and depth/blend/cull/scissor state around Vortex's field update. Shadow-pass callbacks do not update the field.
- The custom shader adds colored surface light and ray-marched colored haze before its existing gamma/bloom/tonemapping path. It reconstructs normal or DH scene depth, samples the world-aligned field, fades field boundaries, rejects solid cells, and protects first-person hands. The existing hull mask starts haze and mist outside dry submarine interiors. Lava receives stronger attenuation than water.
- Up to 16 nearest native mist volumes are rendered as animated, colored, light-responsive volumetric fog. Ordinary air views without visible mist skip volumetric sampling. The ray budget is 12 samples within 112 blocks, with actual field coverage limited by Vortex's 128-block grid.
- A required built-in client resource pack repairs `vortex_pixel.fsh` by renaming its reserved GLSL `packed` variable. The original Deep Seas JAR remains unchanged. The selected shader's Pale Garden inline-expression comment and unsupported End-flash input were also corrected for Minecraft 1.21.1.

The bridge is a shader-side rendering approximation. It preserves native light data and timing, not every native visual pass pixel for pixel. Complementary supplies sun shafts, caustics, bloom and exposure. Vortex's separate puff geometry, plankton/marine-snow sprites and dedicated native screen-space beam pass are not drawn on top; propagated light haze and volumetric mist supply the integrated appearance. Light propagation, moving-hull alignment, distant terrain transitions and performance need in-game verification.

## Activation and controls

**Fully restart Minecraft** to load the new addon and built-in resource pack. No restart/reload was performed. The existing Iris selection and enabled setting were preserved.

Client-only commands:

- `/apovortex status` reports enabled/active state, update count, light-field readiness and native mist-volume count.
- `/apovortex disable` disables the bridge for this session, restoring the normal Iris/Vortex exclusion behavior.
- `/apovortex enable` enables it again. The bridge defaults to enabled after a client restart.

Field readiness can remain false when no nearby lights require a field. To assess the result, use a lit Industrial Alarm or Sonar on a stationary submarine first; check the dry cabin and the view through a window, then moving/rotating hulls, water and lava, DH terrain, mist and frame time. Status/enable/disable commands affect this client only and do not restart/reload anything or send server commands.

## Validation and evidence

- 28 existing fluid/source regression checks passed.
- Four lava-fog variants passed installed Iris parser/token-filter checks and GPU compilation. The exact prior unit-0 failure is reproduced by a negative control; standard-first sampler registration succeeds.
- 108 Vortex GPU/native-shader/state/registration checks passed, covering all eight native Vortex fragment shaders, the original reserved-identifier failure, five bridge shader variants, red-only surface illumination, solid-cell rejection, missing/outside-grid fallback, water haze, lava attenuation, mist, hull interior preservation and sampler/framebuffer restoration.
- Complete Overworld, Nether and End composite fragment sources were expanded/preprocessed using the installed Iris preprocessor, patched with the installed Deep Seas Complementary transformer plus the local lava extension, compiled and linked on the GTX 1060.
- 16 target/accessor signatures were checked against installed artifacts. Runtime Minecraft mixin application and full gameplay/performance are not established by these offline checks.

Source/build/test tools: `scripts/submarine_lava/`. Final build outputs: `Q:/GPT General/vortex-bridge-20261019-final`; paired shader outputs: `Q:/GPT General/vortex-bridge-20261019-shader-final2`.

Installed addon SHA-256: `2f3f6de8f48cd16dab290a50426fb98cf2f32a09488e7216b0a9827144f0d843`.
Installed shader SHA-256: `51cd2ce0d6f9740ff545be7da1a4087f5c4e5ce9a75f9729ebc91a7817ef6f54`.

Exact installation evidence, before files and rollback instructions: `.local/submarine-lava/vortex-bridge-20261019-175546/installation.json`. Restore the saved shader and 1.0.4 addon together, plus the saved index/pack metadata, to roll back this integration. Earlier 1.0.3 had a confirmed load failure and is not a gameplay rollback target.

This was a local WIP client installation. Packwiz addon/shader entries and index hash were updated, including the stale shader filename. Pack release remains v1.3.2. No server, world, schematic, LOD or pressure configuration changes; no Git publication or deployment.

Camera-relative alignment follow-up: .local/submarine-lava/vortex-view-alignment-20261019-175957/installation.json. The GPU checks include inverse-view translation. This follow-up preserves the initial integration record and adds the final shader hash and updated validation.
