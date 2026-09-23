# Protection Pixel integration — verification record

Date: 2026-09-16. Result: implemented locally; automated checks pass; gameplay balance remains provisional.

## Automated results
- **237 static assertions passed:** 49 overrides match their audit records, shaped keys and patterns agree, legacy fluid codecs are gone, critical gates exist, four Echo Plates have the intended material totals, all new prerequisites resolve to quest IDs, new branches have no dependency cycles, and no new quest shares an existing quest coordinate.
- **138 native runtime assertions passed on boot and again after `/reload`:** real NeoForge 21.1.248, Minecraft 1.21.1, Protection Pixel 2.2.1 and the current staged gameplay mods.
- All 50 integration recipes exist in the actual RecipeManager and return their expected output items. This verifies deserialization/loading; it is not a complete assembly-line playthrough.
- Four separate equipped slots are required; wrong-slot items fail. Plate consumption, duplicate rejection, removal and preservation of unrelated custom data/durability pass. Tests include Float Shield and boots.
- Resistance II / Strength II activate with a valid active reactor; three pieces or no reactor do not activate them. Stronger existing Resistance is preserved.
- Compact reactor containers work. Wrong fuel, missing fuel, spent water and stale cached power are rejected.
- Native Lancer sampling resets on initial equip and teleport, caps movement, and rejects nonfinite values. Actual PP incoming-damage handler receives base damage 2 and produces **8 Brass / 11 Alloy**; stale reactor power produces **2** before the next tick. These test the PP bonus handler, not final damage after every other mod's defenses.
- Stale upper Exoskeleton attack state is cleared.
- FTB Quests loads **22 chapters and 1,025 quests**, including 19 additions. Every pre-existing quest and chapter setting in the four modified chapters matches the pre-work backup semantically.
- Recipe/quest generation is idempotent; rebuilding the addon produced an identical JAR. Integration Packwiz hashes and the pack-to-index hash match. Whitespace checks pass when respecting the existing CRLF format.

## Environment and evidence
The disposable server is `Q:\GPT General\PP_Integration_Test_2026-09-16`, bound to loopback port 25586, with its own newly generated `pp_test_world`. No production worlds/saves or pre-existing Distant Horizons data were copied. Known client-only mods were excluded from this test server; live client mods were not removed. Local twin libraries were only read/copied; the twin was not changed. The test server has been stopped.

The startup/reload log still contains errors outside PP: examples include malformed Create Deco placard / CBC AT mould / Create Cafe blood tea recipes, references to absent ComputerCraft cable items, malformed Tracks/Create RNS tags, and unresolved decorative loot items. These remain broader-pack issues. The KubeJS no-error message is not a statement that all upstream datapacks are clean. Reload caused about a 20-second server pause in this disposable environment.

Native evidence: `protection_pixel_runtime_evidence.log.txt`. Structural detail: `protection_pixel_static_tests.json`. Recipe audit: `protection_pixel_recipe_changes.json` and `protection_pixel_gate_audit.json`.

## What these tests do not establish
- Normal client UI/rendering, actual Armor Load Platform interaction packets, full-inventory refunds, or two simultaneous human players. Core install/remove logic is tested, not the whole interaction flow.
- Death, corpse recovery, rejoin, dimension travel with real ships, or quest advancement/team synchronization. The teleport sampler test does not simulate an Aeronautics ship.
- Sustained combat balance, actual fuel lifetime, shield damage-handler ordering/stacking, environmental protection, or the enjoyable cost of seven-loop upgrades.
- Every loot, command, trade, custom scripted or machine automation bypass. The targeted installed-JAR recipe scan found only the overridden production routes for experimental devices; Moon Regolith also comes from lunar stone crushing and its block drop. Existing player trade is intentionally allowed.
- Preservation of Echo Plates through native chassis upgrades. Remove plates first; this limitation is documented in game.

These unresolved cases are retained in `PROTECTION_PIXEL.md`; no player testing is required to use this implementation, but it should be treated as a local release candidate rather than a final balance certification.

## Artifact
Addon SHA-256: `81b8d08daf2f80f8d8092574113b0f0bc62ea657cdbbd6c374f285b71caf08d2`.

Reproduce structural checks with `python scripts/protection_pixel/verify.py`. On a disposable server with the same addon/data, run `/ppresonance selftest` as an operator or from its console. A full restart is required after changing the addon; `/reload` only reloads data/scripts.


## Quest regression repair — 2026-09-17
The original quest checks were insufficient: they did not detect IDs outside FTB's signed-long range or ensure titles/descriptions still matched after FTB saved the quests. They must not be cited as evidence that the original quest integration was ready.

After repair, 336 static assertions pass. A disposable NeoForge server using the installed FTB Quests 2101.1.27 ran 177 runtime checks before saving and the same checks after FTB's actual `writeDataFull` / `load` cycle: **354 passed**. These verify all 19 exact titles/descriptions, all expected prerequisites, and all 50 current quest/task/reward IDs. The saved chapter/language files were independently parsed afterward and all 19 matched again.

The tests preserve all existing live quest/task/reward IDs, coordinates, item quantities, reward amounts and unrelated content. The before-repair backup fails the new title-link checks for exactly ten quests. Running the repaired generator with `--check` changes no files. The isolated server is stopped; no live worlds or saved progress were modified.

Evidence: `protection_pixel_quest_repair_evidence.txt`. Test instance and exact test harness: `scripts/protection_pixel/build/quest-roundtrip-server` (excluded from distribution). The live files were checked directly; no claim is made that the live client UI was opened during this repair.


## Quest rework - 2026-09-20
See PROTECTION_PIXEL_QUEST_REWORK.md for the approved player-facing rewrite, modest team rewards, powered-loadout milestone, preserved live progression, and validation results. Native regression harness: python scripts/protection_pixel/run_quest_tests.py (uses the existing isolated test server only). The current Echo Plate uses the exact user-selected PNG; older texture-export instructions are superseded.


## Consumable repair restriction tests - 2026-09-20
33 additional native checks passed (178 total), covering both consumables, existing Mending components, enchantment APIs, XP-repair selection, ordinary wear, and the actual Reforger curioTick with a normal-tool positive control. 437 structural checks and 456 FTB roundtrip checks also passed. Evidence: protection_pixel_consumable_repair_tests.txt. Normal client enchanting GUI and a running CEI contraption were not exercised; their item/component APIs were tested. Restart the client and any server using this addon to load the patch.
