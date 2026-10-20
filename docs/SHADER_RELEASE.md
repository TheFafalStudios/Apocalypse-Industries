> Historical shader-only preparation. Superseded for release assembly by [v1.4 consolidated notes](releases/V1_4_CONSOLIDATED.md). The Y32 mismatch is corrected in the v1.4 candidate; validation limits still apply.

# Shader release candidate â€” 2026-10-20

Prepared as proposed **v1.3.3**, based on GitHub main v1.3.2 at commit `72526d32176ea3bcae70d2960ad215cab6f39821`. The latest GitHub release tag is v1.1.1; the launcher tracks main, so preparation must preserve the newer main payload. No publication or deployment is authorized by preparing this candidate.

## Scope

- Custom Complementary Unbound r5.8.1 derivative `ApocalypseAtmosphere_NetherDH_v1.2.zip`. Original license remains inside the ZIP. This is not an official Euphoria build. Remove the old custom shader and Euphoria Patcher together.
- Shared animated Nether lava, emission/blending corrections, approximately 750â€“1250-block fog, biome-depth corrections, and the existing lower-sky missing-chunk fallback. The fallback's hardcoded Y32 plane does not match the current WIP Y100 sea; this remains a known release gate, not a verified fix.
- Client-only submarine visuals addon 1.1.2: Iris-managed water/lava hull masks, explicit medium gates and exterior fog. Vortex integration is experimental and defaults off every startup. The native pixel-shader correction is supplied through the addon's required built-in resource pack.
- Client-only Aeronautics Iris Render Fix 2.1.0. Preserve core Sable/Aeronautics; do not restore retired ocean-wave mods.
- Shared dependencies: Deep Seas 3.4.4 requires Aeronautics >=1.3.2, so upgrade the bundle to 1.3.2. Retain existing Sable 2.0.3, Minecraft 1.21.1, NeoForge 21.1.248 and Iris 1.8.14 Beta 1. Use DH 3.3.3 to match the shader's tested WIP pairing. The release manifest pins artifact URLs and SHA-256 values.
- Copy only the intentional DH cloud toggle onto the published DH config. Preserve every other published DH setting. Iris defaults enable the new shader only for fresh installs; existing settings are marked preserve=true. The shader option file also uses preserve=true.

## Existing installation migration

Packwiz replaces the indexed old shader, Euphoria Patcher and old dependency metafiles. Existing preserved Iris preferences can still reference the removed filename: choose `ApocalypseAtmosphere_NetherDH_v1.2.zip` once in Iris. Do not copy WIP options, runtime caches, server pressure configuration or personal histories. Published hull-strength configuration remains unchanged. Do not run the repository deletion list against a live game directory.

Client-only compatibility addons use `side = "client"` metafiles. The local addon JAR is hosted in the repository, but is not also indexed as an unconditional direct JAR. This avoids accidentally sending client rendering mixins to dedicated servers.

## Validation and limits

The release preparation verifies the pinned baseline's index, checks an explicit allowlist of changed paths, retains unrelated published bytes, checks upstream download hashes against installed inputs, excludes protected/runtime data before extraction/copying, and records exact before/after hashes plus rollback bytes. Regenerated index and pack hashes are checked with the existing distribution validator. A clean rebuild of addon 1.1.2 has identical ZIP entry contents to the installed artifact; the ZIP file hash itself differs due to archive timestamps. The installed artifact is the release payload.

The October 20 fresh regression run passed 31 fluid/source checks, four installed Iris parser/GPU fog variants, 114 Vortex/GPU/state checks and 16 target/accessor signatures. Complete composite fragments for Overworld, Nether and End compile/link. These establish offline fixture behavior. Runtime mixin application, fresh/update installs, moving hull alignment and improved FPS are not proven by them. The earlier performance recording contained shader-off gameplay and cannot substantiate shader performance gains.

## Publication and server gate

The proposed version is changed only inside external release staging. WIP remains v1.3.2. Obtain the user's decision on the matching client/server release before publication. This preparation does not authorize updating either server. Compare applicable server dependencies and intentional overrides before any later deployment; publish only after the recorded gates are resolved or explicitly accepted.

For a future Deep Seas server patch, explicitly set and verify `[hullStrength] pressureModel = "HYBRID"` in the effective `config/create_submarine-server.toml` on both the local twin and Chunkserve. Never carry WIP's local CLASSIC testing value into that patch. Record deployed hashes. Server restart/reload remains entirely user-managed. Clients need a full restart to activate the Java addons; no restart or reload was performed during preparation.

## Reproduction

Use Python 3.11+ and `scripts/prepare_shader_release.py` from the development instance, with the downloaded source archive of the exact pinned commit and a new external output directory. Do not use the older broad distribution builder for this narrow release. It would import unrelated WIP changes and temporary diagnostics. The generated candidate retains the repository's bootstrap builder; its ZIP points to live main and is not an unpublished preview installer.

Sources for the local addon are in `scripts/submarine_lava/`; its build/test scripts currently expect the ATLauncher development layout and installed dependency libraries. The candidate includes these sources for review, not a claim of a standalone build environment.
