# Aeronautics dependency-chain review — 2026-10-09

## Result

A usable static update point exists. No inherently unresolvable blocker was identified. There are two paths:

| Component | Smallest update point | Newer Sable update point |
|---|---|---|
| Aeronautics bundled | 1.3.2 | 1.3.2 |
| Aeronautics / Simulated / Offroad inside bundle | 1.3.2 / 1.3.2 / 1.3.2 | same |
| Deep Seas | 3.3.0 | 3.3.0 |
| Sable | keep 2.0.3 | update to 2.0.6 |
| Sable Photomancy | keep original 1.0.1 | requires compatibility repair |
| Create | keep 6.0.10 | keep 6.0.10 |
| NeoForge | keep 21.1.248 | keep 21.1.248 |
| Flywheel | keep 1.0.6 | keep 1.0.6 |
| JEI | keep 19.39.0.368 | keep 19.39.0.368 |

Neither path has been launched or gameplay tested. Prefer the smaller, published-jar-only path for an initial test. The newer path has a statically verified repair prototype, not a release-ready patch.

## Dependency logic

1. Deep Seas 3.3.0 requires Aeronautics >=1.3.2, Sable >=2.0.3 and Simulated >=1.3.0.
2. Aeronautics bundled 1.3.2 supplies Aeronautics, Simulated and Offroad 1.3.2 together. Its actual nested metadata requires Create >=6.0.10, Sable >=2.0.0 and <3.0.0, NeoForge >=21.1.228, Flywheel >=1.0.6 and, when present, JEI >=19.21.0.247.
3. Installed Create 6.0.10, Sable 2.0.3, NeoForge 21.1.248, Flywheel 1.0.6 and JEI 19.39.0.368 satisfy those requirements. No loader, Minecraft, Create or Flywheel update is forced.
4. Installed Climbable Ropes 2.1.0 accepts Simulated >=1.3.0 and <1.4.0, so bundled 1.3.2 stays inside its compatibility range.
5. Actual candidate Aeronautics and Deep Seas references were compared against Sable 2.0.3 versus 2.0.6. No reference found that resolves only in the newer Sable core. The advertised Sable 2.0.3 minimum is therefore consistent with this static check.
6. Choosing Sable >=2.0.4 introduces the Photomancy problem below. It is optional for the requested Aeronautics/Deep Seas update, so the chain can stop at Sable 2.0.3.

## Photomancy blocker and repair feasibility

Sable 2.0.4, 2.0.5 and 2.0.6 change SubLevelLoadingTicket to a Java record. Photomancy 1.0.1 still calls:

- getType(), replaced by type(), with the same return descriptor.
- getKey(), replaced by key(), with the same return descriptor.

Both calls occur in ProjectorForceLoadTickets. This is a concrete potential NoSuchMethodError in projector loading-ticket handling, not evidence that every launch or ship would immediately crash. Metadata alone misses it.

Photomancy's latest published version on both checked feeds remains 1.0.1; no released newer fix was found. This prevents an unmodified all-latest Sable stack from being accepted as compatible while preserving Photomancy.

An isolated prototype changes only the two method references in that one class. All other jar entries are unchanged, the archive passes integrity checks, and the changed references resolve against Sable 2.0.6. Repeating the consumer linkage scan with this prototype removes both identified regressions. The latest-stack Mixin target audit also finds zero newly missing targets.

Prototype: `.local/aero-update-chain-2026-10-09/sable-photomancy-1.0.1-sable-ticket-compat-PROTOTYPE.jar`.

It has not been installed, given a production mod version, or exercised in Minecraft. Runtime validation and proper packaging would be required before adopting it. No schematic/blueprint storage was accessed.

## Wider group inspected

All 224 installed top-level jars and their nested mod metadata were scanned; 274 metadata records parsed without errors. The bytecode scan identified 32 jars in the core/API-consumer group. This includes Aerocables, Aeroworks, Climbable Ropes, Compact Flap, Automated Logistics, Toolgun, Transmission & Linkage, Create Connected, Create Radar, Simulated Thrusters, Aeronautics Curios, Big Cannons, Diesel Generators, Dragons Plus, Propulsion, DriveByWire, Escalated, KineticGrip, Power Grid, Photomancy, Sable Cleanup, Sable Jade, Synaxis, Weather Physics and Tracks, plus shared-library consumers and the proposed core candidates.

Existing custom fixes were included in the full installed scan; none introduced a newly missing reference to the proposed physics core.

- Minimal point: zero newly missing field/method/class references compared with the installed core.
- Latest Sable without Photomancy repair: two newly missing methods, both in Photomancy.
- Latest Sable with isolated repair: zero newly missing references.
- Relevant Mixin class targets, injection method selectors, shadows/overwrites and direct injection member targets: 83 covered; zero newly missing targets in either chosen point.

These are differential static checks. They do not verify every inherited Minecraft/library symbol, access or invocation-mode change, injection ordinal, transformed class, optional integration branch, native physics behavior or save compatibility. Unresolved inherited references in the raw audit output are not being presented as confirmed breakages or passes.

## Optional addon updates

The following official candidates were downloaded and their metadata/core references inspected. None forces Sable beyond 2.0.3 through the checked declared ranges or references that exist in the newer core but not in 2.0.3:

| Addon | Installed | Candidate | Notes |
|---|---|---|---|
| Climbable Ropes | 2.1.0 | 2.1.4 | Changelog development stack bumped to Sable 2.0.5, but actual minimum remains 2.0.0. Installed Player Animator 2.0.4 satisfies its requirement. |
| Automated Logistics | 0.5.2 | 0.6.2 | Adds route/vehicle-restoration behavior and a default-enabled force-loading option; needs operational testing. |
| Transmission & Linkage | 0.2.5 | 0.2.8 | Vehicle-unloading stability fix. |
| Escalated | 1.3.1 | 1.3.2 | Author explicitly fixes a crash with Sable 2.0.x; a worthwhile separate compatibility fix, including on the existing baseline. |
| Sable Jade | 1.2.1 | 1.3.0 | Optional Jade >=15.10.0 requirement needs confirming against its manifest/runtime version before adoption. |
| Propulsion | 1.1.4 | 1.1.5 | Adds content, fuel data and changes consumption/thrust; requires recipe/progression and vehicle checks. |

These are optional branches, not changes necessary to satisfy Deep Seas. They were not combined into a full-pack runtime test. Unrelated latest-version upgrades were not silently included. For example, Enchantment Industry 2.5.4 requires Dragons Plus >=1.11.9, but the requested core update does not force replacing either installed version.

## Shader branch

This client uses enabled custom Complementary shaders, Iris 1.8.14-beta.1, Sodium 0.8.12-beta.2 and Iris Flywheel Compat 2.3.1. Deep Seas' author flags Iris/Veil lighting issues.

Iris Veil Compat 0.3.0-beta and 0.4.0-alpha were inspected as optional bridge candidates. Both actual metadata declarations accept the installed Iris and Veil versions; 0.4.0 requires NeoForge >=21.1.228, already met. The newer release's development stack lists Create 6.0.11, Sodium 0.8.13 and other newer dependencies, but those development pins are not its runtime minimums.

Direct bridge references checked against the relevant supplied APIs show no definite missing members for Sable 2.0.3 or 2.0.6. The bridges' older bundled GLSL transformer is superseded by Iris's 3.0.0-pre3 through the shared JarJar identity/ranges; raw differences in the unselected old library were excluded from the bridge application check.

This does not validate the bridges' injected render behavior or compatibility with this custom shader. The bridge can be tested independently; it is not a confirmed fix for every Deep Seas spotlight/water effect. No renderer upgrade is proven mandatory by this audit.

## Distant Horizons

DH 3.3.3 remains a separate update branch. It does not force the physics group forward through a discovered dependency. Its configuration reset and shader/depth-rendering checks from the earlier review still apply. Keep its test separate initially so failures can be attributed.

## What remains before adoption

Run isolated client and dedicated-server startup tests for the chosen point, followed by disposable-world gameplay checks for pressure/oxygen, moving sealed ships, ropes, Tracks, docking/logistics, Synaxis, propulsion and Weather Physics. For the repaired newer point, specifically exercise Photomancy projector loading tickets and preserve untouched backups before any authorized test with existing world data. Confirm hull-config migration and inspect new Deep Seas recipes/content against pack progression. Test custom shaders, water seams, shadows and window/fullscreen transitions.

Nothing was installed or deployed. Existing mod/config files remain unchanged. Downloaded jars, scripts, raw feeds, candidate metadata, scenario results and hashes are kept under `.local/aero-update-chain-2026-10-09/`; the Deep Seas candidate remains in the earlier isolated review folder. No server compatibility marker was advanced.

## Evidence and sources

- `.local/aero-update-chain-2026-10-09/update-points.json`
- `installed-metadata.json`, `related-metadata.json`, `affected-api-consumers.json`
- `linkage-regressions.json` and per-Sable scenario variants
- `repaired-linkage-regressions-sable-neoforge-1.21.1-2.0.6.json`
- `minimal-mixin-audit.json`, `latest-mixin-audit.json`
- `candidate-needs-newer-sable.json`, `photomancy-repair-feasibility.json`
- `optional-addon-candidates.json`, `optional-addon-api-matrix.json`, `render-bridge-linkage.json`
- https://modrinth.com/mod/create-aeronautics/version/44pLdPGg
- https://modrinth.com/mod/sable/versions
- https://github.com/ryanhcode/sable/releases
- https://www.curseforge.com/minecraft/mc-mods/sable-schematic-tool/files/all
- https://www.curseforge.com/minecraft/mc-mods/create-deep-seas/files/9042070
- https://www.curseforge.com/minecraft/mc-mods/iris-veil-compat/files/9059655
- Official Modrinth addon version feeds, archived locally.
