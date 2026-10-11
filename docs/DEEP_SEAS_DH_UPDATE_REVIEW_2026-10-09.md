# Deep Seas / Distant Horizons update review — 2026-10-09

Static review of the development client. No installed mods/configs changed, game launched, worlds accessed or servers deployed. Candidate jars downloaded into `.local/update-compatibility-2026-10-09/` only.

## Deep Seas: 2.2.4 -> 3.3.0

BLOCKED as a standalone update. Candidate jar requires Aeronautics >=1.3.2. The installed bundled jar contains Aeronautics 1.3.0 (nested metadata inspected). Sable 2.0.3 and Simulated 1.3.0 satisfy the other new requirements. Updating Aeronautics requires a separate review of dependent physics addons.

Author calls 3.3.0 an experimental-branch fix despite the release label. Project page flags Iris/Veil lighting issues. This client uses Iris 1.8.14-beta.1, Sodium 0.8.12-beta.2, Iris Flywheel Compat 2.3.1 and enabled custom Complementary shaders. Iris Veil Compat is absent. Rendering remains untested.

All 43 old recipe/loot/dimension JSON paths remain (78 in candidate). Base item/block language keys remain; only two WIP tooltip keys disappeared. Quest submarine_propeller and underwater_mine references have no obvious identifier removal. This does not prove complete registry/gameplay compatibility.

Hull keys submarine_hull.json / maxWaterDepth / implosionChance remain in both jars. Current upstream source migrates hull schema 2 to 3 with backup; runtime migration was not tested. Preserve local hull config and settings (including globalMaxDepthCap=100 and enableDeeperOceans=false) before testing.

## Distant Horizons: 3.2.0-b -> 3.3.3

No explicit dependency blocker identified in installed client. Official changelog confirms config version 4 -> 5 clears the configuration. Local config is version 4, using six threads, 256-chunk LOD radius, SURFACE generation and server transfer/generation settings. Reapply intended settings to the new schema.

DH 3.3 changes depth rendering, adds temporal antialiasing and changes Iris integration. The candidate rejects SSRD <=1.8.6; no installed/nested SSRD declaration found. No Chunky jar found. No evidence here that Iris 1.11.x is mandatory for this Minecraft 1.21.1 build; exact shader combination still requires testing.

Test DH independently in an isolated instance: distant terrain/water seams, shadows, resizing/fullscreen, moving Aeronautics ships and multiplayer LOD transfer. Server baseline not inspected. Protect existing LOD data and keep it out of releases. For eventual Deep Seas testing, also verify oxygen, sealed compartments, pressure, hull migration and propulsion in a disposable world or protected test copy.

Baseline: Minecraft 1.21.1; NeoForge 21.1.248; Create 6.0.10; Sable 2.0.3; Aeronautics and Simulated 1.3.0. Active shader: ComplementaryUnbound_r5.8.1_APOCALYPSE_ATMOSPHERE_v1.1_PARSEFIX_TESTED.zip.

Candidate SHA-1:
- Deep Seas 3.3.0: b1aaaf64423d9e80aa750c71de426f6aaa33989e
- DH 3.3.3: 4357b4d19fab818780c94f0e43dad1cc931634b2

Sources:
- https://www.curseforge.com/minecraft/mc-mods/create-deep-seas/files/9042070
- https://www.curseforge.com/minecraft/mc-mods/create-deep-seas
- https://github.com/MaxCreateMC/Create-Deep-Seas
- https://modrinth.com/mod/distanthorizons/version/3.3.0-1.21.1
- https://modrinth.com/mod/distanthorizons/version/3.3.2-1.21.1
- https://modrinth.com/mod/distanthorizons/version/3.3.3-1.21.1
- Actual installed/candidate jar metadata, nested mods, configuration and quests.
