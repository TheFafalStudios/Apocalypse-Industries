# Optional loot guard fix — v1.1.12

This release fixes the largest error group found during the 2026-10-09 Chunkserve log audit. The latest reviewed log reports 1,036 missing-item loot parsing errors across two resource-loading passes.

| Mod | Affected tables | Log messages |
| --- | ---: | ---: |
| Design n' Decor 2.2b | 256 | 512 |
| Steam 'n' Rails 0.2.1 | 246 | 492 |
| Create Connected 1.3.2 | 16 | 32 |

Each override is a copy of the source mod's observed failing block loot table with one added root condition: `neoforge:item_exists` for the original item entry. If the item exists, all original pools, drop entries, rolls, explosion conditions and random sequences remain unchanged. If it does not exist, NeoForge supplies an empty table before decoding the missing registry reference. This does not enable optional items or remove working blocks.

Checks: 518 JSON files validated; source bodies exactly preserved; conditions match the missing item in the log; no pre-existing affected client/twin paths or remote loot directories found during preparation. Source mod payloads match published v1.1.10, the development client, local twin and downloaded Chunkserve bytes. Release builds on current GitHub main, preserving later unrelated published changes.

No mod updates, recipe edits, configuration/quest changes, world/save/schematic/LOD operations or blanket synchronization. Existing server baseline stays explicitly unverified; this is targeted payload verification only. Restart the server to activate. The user manages restarts and reloads; none is performed automatically. Restart updated clients to ensure their local single-player/integrated-server resources reload.

Actual startup error reduction and representative block-drop gameplay tests remain pending after activation. The expected reduction is based on observed table IDs and supported loader conditions, not a completed runtime test. Remaining recipe/mixin/Curios/config warnings, Distant Horizons network exceptions and the hosting GLIBC incompatibility with Power Grid are separate follow-up work.

Specification: https://docs.neoforged.net/docs/1.21.1/resources/server/conditions/
