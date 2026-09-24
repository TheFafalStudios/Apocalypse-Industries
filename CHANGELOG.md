# Changelog

## Apocalypse Industries v1.1.2 — 2026-09-24

- Add an unheated Create Mechanical Mixer recipe: 4 sand, 2 gravel, 2 clay balls and 1,000 mB water produce 8 Immersive Engineering concrete blocks. Shared ingredient tags match IE's crafting recipe.
- Existing concrete recipes remain available. No additional mods required.
- JavaScript syntax and distribution hashes validated; in-game mixing verification remains pending.

## Apocalypse Industries v1.1.1 — 2026-09-24

- Restore axe effectiveness for BOP dead logs, dead wood, and both stripped variants after the existing tag removal.
- Preserve custom dead-wood recipes and item-tag restrictions. No other gameplay changes.
- Distribution hashes validated; in-game mining verification remains pending.

## Apocalypse Industries v1.1 — 2026-09-23

- Removed the retired ocean-wave mod, its dedicated fix, configuration, registry and launcher references, and isolated test copies. Core Sable/Aeronautics is retained. Historical diagnostics were archived outside the pack.
- Enabled sky visibility for the nine remaining nighttime additional-zombie rules: all 27 additional-zombie rules now require sky visibility. Other spawn settings and natural cave spawning are unchanged. Gameplay verification remains in progress.
- Rebuilt Protection Pixel integration as version **1.1** and removed the obsolete 1.0.0 runtime JAR.
- User confirms published v1 is working and playable; this is not a claim about the new candidate.
- User manages server deployment and waived timing coordination as a release blocker. Local server-twin changes still require their existing separate confirmation.

- Verification after these changes: isolated dedicated server started and stopped cleanly; 452 native quest round-trip checks, 236 addon assertions and all 18 saved definitions passed. Static checks passed 445 assertions; mocked zombie/drowned drop tests passed 230 cases. Surface-spawn gameplay verification remains in progress.

## v1.1 detailed changes (audited 2026-09-23)

Baseline: published v1, release commit `1e685ca`; latest GitHub main `6c2e739917ffde3e04035af0fd3ae26f1f0238bf` (README update). Minecraft 1.21.1 and NeoForge 21.1.248 remain unchanged. Client pack release. Chunkserve deployment is managed separately by the pack author.

### Changes since the September 20 catalogue

#### Echo Plates now use native Protection Pixel fitting

- Replaced the separate Echo attachment/socket interaction with the Armor Load Platform's normal plate-fitting menus. The old main-hand/offhand service interaction is removed.
- Echo Plates now occupy ordinary armor plate slots, are unstackable, and use native durability, recovery and Armor Plate Repair Kit handling.
- One serviceable Echo Plate in EACH worn armor piece, plus a working reactor, grants Resistance II and Strength II. Extra plates in one piece cannot substitute for another piece or increase effect levels. Effects expire within one second after requirements cease; unrelated stronger/longer effects are preserved.
- Echo uses the configured Alloy statistics: currently 1.5 armor, 1 toughness and 1.5 weight, with 100 durability. Native wear saturates at damage 99; a plate showing 1/100 remaining no longer qualifies for resonance.
- Exhausted Echo Plates use Alloy's 15% destruction chance on recovery. Recovering and repairing earlier avoids that exhaustion risk.
- Added missing native fitting/protection tags to Float Shield and Link Plate equipment, enabling ordinary plates as well as Echo. Native capacities remain two helmet slots, five chest slots, four leggings slots and two boot slots.
- Boots already have a native fitting path; the earlier claim that a custom socket was required was incorrect.
- No migration or refund of old attachment markers was implemented, as requested. Old markers no longer provide resonance. Recipe and approved texture are retained. Updated addon requires a full client/server restart.

#### Nine approved Protection Pixel recipe revisions

- Alloy Plate: replace the iron sheet step with an Immersive Engineering steel plate; retain the one-loop assembly and magnet requirement.
- Steam Exoskeleton: replace four iron sheets with four IE steel plates; retain steam engines, copper and magnet.
- Steel-core cannon shells: replace the iron ingot with IE steel; retain output of two.
- Reinforced Fiber: add a hemp-fabric plus two-quartz alternative yielding two fiber; retain the original fiber recipe.
- Hunter helmet: replace the added Precision Mechanism with a Create Connected control chip; preserve the other optical ingredients.
- Blood Dialysis Device: replace its Precision Mechanism with a control chip and remove the two upper-corner electron tubes.
- Float Shield: replace the direct Precision Mechanism with a Power Grid battery, and two copper ingots with two electrum ingots; retain lunar material and Heart of the Sea.
- Evasion Wing: replace two Precision Mechanisms with two constant-speed motors; retain lunar material and other ingredients.
- Armor Hanger: restore its native recipe, removing the added Precision Mechanism and retaining output of two.
- Batteries/motors are consumed crafting ingredients; these recipes do not add FE-powered runtime behavior. Installed outputs total 51 Protection Pixel recipe JSONs plus Echo Plate and hemp-fiber recipes under the addon namespace.

#### Zombie-to-drowned armor-drop loophole

- Extended the steel-tier armor-drop filter to drowned, including converted zombies and previously existing drowned.
- Diamond, Netherite, Echo, spacesuit and other non-allowlisted armor cannot be recovered through drowned death drops. Worn combat equipment remains unchanged.
- Steel-tier eligible armor, tridents, fishing rods, copper, nautilus shells and ordinary loot remain unaffected.
- Standard conversion retains equipment; the resulting drowned death event now applies the same filter. Tests model this path; an actual underwater conversion/kill test is still outstanding.

#### World generation

- Structurify now disables `terralith:fortified_desert_village` and `terralith:fortified_village`, alongside the previously disabled villages. Existing generated structures are not removed by this configuration change.

#### Local cleanup and release workflow

- Removed the retired experimental Lost Cities datapack and its local references as requested; this is local cleanup of content already absent from v1.
- Aligned the local Git baseline with current GitHub main while preserving testing files. Local VERSION and pack.toml now identify the unpublished build as `v1-dev`.
- Restored current distribution preparation, validation, CI and release instructions; preserved the custom ATLauncher instance icon.
- Builder selects a Python 3.11+ runtime, compares deletion reports against the published Git reference, and writes filtered snapshots outside the live instance.
- Excluded local build/test directories from copied tooling. Preserved CI and icon in generated snapshots. Removed inherited runtime-test claims from newly generated validation reports.
- A September 20 snapshot passed validation with 217 mods and 1,174 indexed files; those counts describe that test snapshot, not a fresh September 23 release build.

### Cumulative changes since v1

#### Mods

215 active top-level JARs versus 209 in published v1: eight additions, one version replacement and two removals.

| Change | Mod/version |
| --- | --- |
| Added | Protection Pixel 2.2.1 |
| Added | Apocalypse Protection Pixel Integration 1.1 |
| Added | Compact Gearbox 1.0.2 |
| Added | Create Framed 1.8.2 |
| Added | LDLib2 2.2.37 |
| Added | Railways 0.2.1 |
| Added | Sable Photomancy 1.0.1 |
| Added | Synaxis 1.5.0 |
| Updated | JEI 19.32.0.359 -> 19.39.0.368 |

All other published mod binaries match the published download/direct-file hashes. Enchantment Industry, Dragons Plus, Gunsmithing/NTGL upgrades, Hadal Depths and the four original standalone compatibility fixes were already in v1 and are not new additions here.

#### Protection Pixel integration and progression

- Added recipe serialization fixes for installed Create/NeoForge and pack-specific material gates while retaining processing and sequenced assembly.
- Brass entry uses Precision Mechanisms where applicable; Electric upgrades use Power Grid magnets. Float Shield, Evasion Wing and Lancer-AS require Moon Regolith. Approved recipe revisions above supersede earlier ingredient choices.
- Added material-crafted Echo Plates without consuming completed armor. Their current fitting/stat behavior is the native system described above, replacing the earlier weightless custom socket.
- Reduced ordinary Brass/Alloy plate armor from 2 to 1.5 each; toughness and weight unchanged.
- Added Lancer bonus caps of +6/+9, invalid/stale movement-sample handling and equipment/dimension reset handling.
- Improved reactor validation for wrong fuel, spent water, stacked rods and stale power. Clear Exoskeleton attack/mining bonuses when inactive or outside upper-body mode.
- Disposable Water Tanks and Flare Rods reject Mending and are excluded from Reforger repair selection. Existing Mending is cleaned on access/inventory processing, not by scanning all world storage. Ordinary equipment, other enchantments and native wear costs remain unchanged.
- Added/refined Echo Plate artwork; current installed sprite is the normalized 16x16 version. Earlier instructions to retain the full-resolution PNG are superseded.

#### Quests and economy

- Added/reworked Protection Pixel progression across Brass, Electric, Space and Special Requests with player-facing titles, subtitles and equipment instructions.
- Earlier work described 19 quests; the current maintained quest map contains 18 definitions. The changelog uses the current count rather than treating the historical count as still verified.
- Repaired invalid signed-long IDs, stale translations and missing prerequisite links while preserving live IDs/progress.
- Added powered-equipment use and Echo crafting/resonance milestones; current text describes native plate service and repair.
- Added modest nonrepeatable team rewards: lava bucket, empty water tanks and Iron Armor Plates.
- Added Brass and Alloy supply contracts paying $1,000 and $8,000. Finished Echo or experimental armor is not consumed by those contracts.

#### Other configuration and presentation

- Zombie armor recovery is capped at steel tier without changing worn armor, natural loot, spawning frequency or progression scaling; now also covers drowned as above.
- Tornado Physics ordinary wind is `SAILS_ONLY` instead of `ALL`; tornado forces remain separately enabled.
- Disabled Create Connected redstone-link wildcard.
- Submarine hull registry gained 912 entries for added mod namespaces; the September 20 semantic comparison found no altered or removed pre-existing entries.
- Added LDLib2, Railways, Synaxis/control-chair and Sable schematic configuration. A local Lost Cities server config remains excluded by the release builder.
- Iris shaders are disabled locally; the selected atmosphere shader name remains unchanged. Local stock Complementary Unbound and OPPRESSIVE_SUN variants/options are testing assets, not automatically approved distribution assets.
- JEI gained toast reflow configuration and expanded sorting. Published resource-pack binaries and the atmosphere shader still match their published hashes.
- Full live options, biome/dimension caches, recipe dumps, map/JEI histories and backups are local state. FTB Chunks entity-icon ordering and zfastnoise timestamp differences are not presented as gameplay changes.

### Verification and corrections

Audit performed 2026-09-23:

- Confirmed GitHub main remains `6c2e739`; compared current installed files against published Packwiz hashes and the saved September 20 content hashes.
- Re-ran Protection Pixel structural checks: **445 assertions passed**, with **1,024 total pack quests**. This is not a count of added quests.
- Re-ran zombie/drowned checks: **230 progression armor cases passed**, including transferred armor and preservation of steel/tridents/normal loot. These use mocked APIs.
- All 17 compiled addon classes and matching resource entries checked against the installed addon JAR matched.
- Inspected saved September 20 recipe-runtime evidence and September 21 server log: approved recipes passed native loading, FTB quest round-trip passed **452 checks**, and addon selftests passed **236 assertions**. These are historical isolated-server results, not new Minecraft runs today.
- Earlier claims that drowned filtering remained unimplemented and that Echo used a separate socket are obsolete. Earlier assumptions about missing native boot fitting were wrong and are corrected above.
- No fresh install, real multiplayer session, manual plate-menu rendering, combat balance test, or remote Chunkserve validation was performed for this changelog.

### Remaining verification and known limitations

- Surface-only additional-zombie test is approved and applied; gameplay verification remains in progress.
- Wind-altitude ramp remains documentation-only.
- Deferred Protection Pixel recipe reviews remain open: heat-overlocking mechanism, duplicated Precision Mechanism charges, Tosaki ceramics, consumables, Echo Plate recipe and alternate-access/recipe-correctness review.
- Combined native plating/Float Shield/Echo/Exoskeleton balance, real-client service behavior and existing-player quest effects still need gameplay validation.
- Addon version is now 1.1; rebuild/validate the final distribution before release.
- The official server-twin update remains a separate action requiring the prominent post-push confirmation in AGENTS.md. No server update or release is performed by this changelog edit.

---

# Apocalypse Industries v1

Released 2026-09-14. Minecraft 1.21.1 / NeoForge 21.1.248.

## Enchantment automation

- Add Create Enchantment Industry 2.5.3b and its required Create Dragons Plus 1.11.8b library, with the current live configuration files. Both additions are confirmed working in-game by the pack author.
- Adds machinery for experience handling, enchanting, disenchanting and printing, with the mods' Create/Sable integrations.
- Sophisticated Core XP remains only partially interchangeable with CEI XP; use CEI XP for Blaze Enchanter and Blaze Forger.

## Weapons, oceans and atmosphere

- Update Create Gunsmithing 1.4.6 to 1.4.9 and NTGL 3.1.6 to 3.1.8.
- Update ADS Transition Fix 1.1.0 to 1.5.0; include the Gunsmithing shader companion and CBCAT recoil compatibility fix.
- Update Abyssal Ocean/Hadal Depths 2.0.0 to 2.1.7. Enable Create diving equipment and sealed-compartment compatibility; configure small interior island removal.
- Include Apocalypse Atmosphere v1.1 parser-fix shader and the stock-opacity toxic-rain resource pack.
- Provide portable resource-pack selections for fresh installs while preserving existing players' options.

## Stability and cleanup

- Remove superseded mod versions and patched base JARs that conflicted with the standalone fixes during startup.
- Exclude the abandoned Lost Cities files. Retain Big Lost City and its Apocalypse biome compatibility datapack.
- Remove the cancelled farming test script; crop damage and greenhouse protection are not implemented release features.
- Exclude player histories, generated caches, recipe dumps, credentials, saves, logs and launcher state from the release payload.

## Distribution and verification

- Ship the complete current mod selection: 209 active JARs represented by 193 verified download references and 16 exact bundled JARs.
- Refresh the Packwiz index and remove obsolete mod references. Add an isolated distribution builder and GitHub manifest checks.
- Include an ATLauncher import ZIP that checks GitHub main for pack updates before launch.
- The pack author confirmed startup and server login after the standalone-fix repair, then separately confirmed CEI and Dragons Plus working in-game. Server login with the two new mods is not separately claimed.
- Manifest hashes, mod coverage, duplicate IDs and standalone-fix package collisions pass local validation. A completely fresh remote installation has not been tested.

## Known limitations

CEI 2.5.3b is an upstream beta. Dragons Plus 1.11.8b has upstream reports concerning fluid-hatch bucket extraction and Dragon's Breath cauldron collection. These are tracked for further gameplay testing. This client-pack release does not claim the dedicated server has been updated.
