# Deep Seas submarine lava visuals — WIP candidate, 2026-10-18

Current WIP correction: client addon **1.1.1** adds Iris-managed water-mask binding and explicit cabin-medium gating for both water and lava fog. It also reduces Vortex field/haze work. See [Vortex correction](VORTEX_SHADER_BRIDGE.md) and `.local/submarine-lava/fog-performance-20261019-184319/installation.json` for validation, hashes, rollback and limitations. Client restart required; live appearance/FPS remain unverified.


## Current installed version — 1.1.0, 2026-10-19

The installed addon is now `apocalypse-submarine-lava-visuals-1.1.0.jar`, paired with the Vortex-enabled custom shader. It retains the corrected standard-first sampler registration, extends native Vortex light-field/mist data into the shader, and supplies the native Vortex GLSL repair as a required built-in resource pack. See `VORTEX_SHADER_BRIDGE.md` for behavior, validation, client commands and rollback evidence. Full client restart and in-game validation remain required. Sections below record earlier builds.

## Sampler registration order repair — 2026-10-19, version 1.0.4

The live 1.0.3 error was `Texture unit 0 is already used` from Iris' `addDefaultSampler`. Registering a dynamic sampler when the builder was created consumed unit 0 before Iris registered its mandatory color texture. The earlier scope test incorrectly pre-reserved unit 0 and missed this lifecycle failure.

Installed 1.0.4 registers its dynamic sampler at the head of `ProgramSamplers.Builder.build`, after normal sampler assignment. The GPU regression now uses the actual order without pre-reserving unit 0, registers the default color sampler first, and completes the builder. Its negative control reproduces the exact live exception with the old ordering. The 28 source/fluid regressions, four Iris parser/GPU cases, ordinary/lava sampler cases, and target signatures pass. Runtime mixin application and gameplay remain unverified; full client restart is required. No restart/reload performed.

Build: `Q:/GPT General/submarine-lava-20261019-order-repair2`.
Installed JAR SHA-256: `4eac269258f1051f0d2841cad92ee420dc62065604f79cc3fdf230a2feee1ed3`.
Rollback and installation evidence: `.local/submarine-lava/sampler-order-*`.
Shader ZIP and Vortex configuration remain unchanged. Packwiz entry/hash updated without advancing the pack version or publishing/deploying.

## Managed sampler correction — 2026-10-19, version 1.0.3

After the reported broken Overworld view, inspection found the lava addon reserved texture unit 10 in every Iris program and bound an external texture at each composite stage, including outside submarines. This unnecessarily affected ordinary terrain/shadow/DH programs and bypassed Iris' managed texture binding. It is a plausible interference source, not a confirmed explanation of the screenshot. The shader ZIP still matches SHA-256 `b25316d9c946022f6915e98cba8fed33b55b1402874c0b6ea88e2726ea87f92a`.

Installed `mods/apocalypse-submarine-lava-visuals-1.0.3.jar` removes that global reservation and binding. A dynamic sampler is registered through Iris only when the linked program actually contains `CdsLavaHullFar`. Iris selects the unit and schedules the binding with the program's other textures. Static invokers access the existing upstream hull/fallback texture. Water behavior, shader source logic, Aeronautics Iris Fix, DH configuration and pack version remain unchanged.

Validation: 28 regression checks, four installed Iris parser/token-filter/GPU combinations, six target method signatures, and two GPU-linked registration cases passed. An ordinary shader program consumes no new sampler; a program using the lava mask consumes one managed sampler and leaves the reserved-unit set unchanged. These checks do not prove in-game visual recovery. Full client restart and Overworld/Nether comparison remain necessary. Iris was already disabled by the user and its setting was preserved.

Build: `Q:/GPT General/submarine-lava-20261019-sampler-repair3`.
Installed SHA-256: `556fbd054a764e2df2cf6728d8419f2fbf213643ee8311157fd5ff64916a0e1e`.
Backup and exact deployment evidence: `.local/submarine-lava/managed-sampler-*`.
Only the addon entry in the existing packwiz index and its pack hash were updated; no publication, server changes, restart/reload, world or LOD operations were performed. Earlier sections below are historical.

## Current compatibility update — 2026-10-19

Installed addon is now `mods/apocalypse-submarine-lava-visuals-1.0.2.jar`, rebuilt against Deep Seas 3.4.4 with an exact matching dependency pin. Shader and visual logic are unchanged. The 28 regression checks, four Iris parser/GPU option combinations and six target/shadow signature checks passed again against 3.4.4. Client relaunch is required; gameplay and runtime mixin application remain unverified. See `WIP_DEEP_SEAS_3_4_4_2026-10-19.md` for installation/rollback evidence. The sections below record earlier versions.

Installed client addon: `mods/apocalypse-submarine-lava-visuals-1.0.1.jar`.
Source and reproducible build/test scripts: `scripts/submarine_lava/`.
Build and validation outputs: `Q:/GPT General/submarine-lava-20261018-attempt3`.
Local installation evidence and rollback artifact: `.local/submarine-lava/` timestamped attempt.

## Behavior

Extends Deep Seas 3.4.1's existing Complementary shader integration to lava. A sealed cabin containing the actual camera is checked against the real world fluid rather than Deep Seas' culled fluid state. The check uses an already loaded chunk and the first unoccluded cell above the camera, with no chunk requests. Air and water do not activate the lava mask.

The existing hull far-depth buffer is bound to a separate sampler, `CdsLavaHullFar`, on reserved texture unit 10 while the sealed cabin is submerged in lava. An all-white inactive mask is bound otherwise. Existing `CdsHullFar` water handling is untouched. The shader patch is added after Deep Seas patches a recognized Complementary composite program; unsupported shader families are not patched.

Exterior scene distance is measured from the far hull surface. Geometry in front of that surface receives no added fog, preserving the cabin and window frame. Exterior color fades into Minecraft's orange lava fog color, with approximately 95% fog at 19 blocks. Complementary's `LESS_LAVA_FOG` option uses the gentler square-root curve instead. Sky beyond the hull is fully fogged. Exterior light shafts and colored fog are attenuated with the lava fog so they cannot reveal the distant floor through it. This approximates the visual medium; it does not restore hidden lava geometry, add refraction, change submarine physics, or change camera fluid status.

Deep Seas' original JAR and `ApocalypseAtmosphere_NetherDH_v1.2.zip` remain byte-for-byte unchanged. No pack release version markers, server files, world data, or DH data were changed.

## Validation and activation

- Compiled against installed Minecraft 1.21.1 / NeoForge 21.1.248 and Deep Seas 3.4.1.
- 16 Java regression checks passed against the installed upstream Complementary transformer and expanded active Nether shader, including idempotence, interior depth rejection, water preservation, air/unloaded rejection, and a bounded fluid scan.
- The new GLSL helper and fog branch compiled on the GTX 1060 with reduced lava fog and colored light fog individually enabled and disabled (four combinations).
- Six mixin target/shadow method signatures verified against installed Deep Seas and Iris JARs.
- Installed artifact SHA-256: `405760326baef6b270046da965bfeb705e0aace3410880ca255541ec05362653`.

**Client restart required.** Not performed. Offline checks do not prove runtime mixin application, full Iris pipeline compilation, visual appearance, or gameplay correctness. After restarting, compare a sealed submarine below the lava surface, the cabin interior/window frame, a surfaced cabin, an ordinary water submarine, and ordinary lava immersion outside the cabin. Moving/rotating the submarine should keep fog attached to the exterior hull face. This is a local WIP client candidate, not a published release or server deployment.

Rollback: remove only the installed `apocalypse-submarine-lava-visuals-1.0.1.jar` from WIP's `mods` folder and restart the client. Version 1.0.0 is retained for evidence but has a confirmed shader load failure and should not be restored for gameplay.

## Iris preprocessing repair — 1.0.1

The running client rejected composite1 because 1.0.0 inserted `#ifdef` directives after Iris had already preprocessed the shader. The earlier GPU checks accepted those directives and missed the Iris parser restriction. Version 1.0.1 resolves reduced lava fog and optional colored fog from the preprocessed source in Java, then inserts plain GLSL without conditional directives.

Validation: 28 regression checks passed. Four combinations of the two fog options passed the installed Iris AST parser and its token filter, then compiled on the GTX 1060. Negative controls verify that the same Iris token filter rejects late directives reproducing the original error. The public Iris transformer and later compatibility transformations require a running NeoForge environment and were not exercised offline; complete pipeline/gameplay verification remains pending. Six mixin target/shadow signatures were rechecked.

Build: `Q:/GPT General/submarine-lava-20261018-repair5`.
Installed 1.0.1 SHA-256: `6d3a2977a55a37f830fac8d4af6a30e998ea688cba04c981d7f6399cd711a3b3`.
Backup and installation evidence: `.local/submarine-lava/iris-preprocessor-repair-*`.
The 1.0.0 JAR was removed from active mods; the shader ZIP remains unchanged. A full client restart is required to load the fixed Java code; shader reload alone cannot replace the addon. No restart or reload was performed.
