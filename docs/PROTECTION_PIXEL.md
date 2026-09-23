# Protection Pixel integration â€” working record

## Authority and scope
User authorized implementation on 2026-09-16. The original packet is reference material, not an instruction to override subsequent decisions. Do not push, publish, or update the Chunkserve twin as part of this work.

## Confirmed requirements
- Craft a special Echo Plate from materials; never consume completed armor.
- One installed plate in EACH equipped armor slot is required for the Echo buff. No concentration into fewer slots.
- Preserve assumptions and potential problems; perform available tests without requiring the user to test first.

## Provisional assumptions (implementation defaults, not user confirmations)
- Full resonance gives Resistance II and Strength II, matching current Echo strong_armor=true.
- Require a valid operating PP reactor. Existing Echo armor and recipes remain unchanged.
- All PP ArmorItem variants, including boots, Link-Plate and Float Shield, can receive one resonance plate.
- Echo now occupies ordinary native plate slots. Boots use the native two-slot Headgui route. Added native fitting/protection tags for Link Plate armor and Float Shield.
- Echo inherits current Alloy plate stats (1.5 armor, 1 toughness, 1.5 weight), 100 durability, and native repair/recovery behavior. Ordinary Brass/Alloy balance is unchanged.
- Install/remove Echo through the ordinary Armor Load Platform fitting menus. No separate attachment interaction. At least one serviceable Echo plate in each correctly equipped armor piece plus a powered reactor gives Resistance II and Strength II. Extra Echo plates do not increase effect levels.
- Four plates cost four Echo ingots, four Netherite ingots, 24 diamonds, four Echo upgrade templates, and four Netherite upgrade templates (six diamonds per plate approximates the complete diamond armor material cost). No completed armor consumed. This does not claim exact template-duplication or enchantment cost equivalence.
- Ordinary equipment upgrades are Electric; distinct experimental devices are post-lunar. Do not pretend the shared native upgrade item can gate its third installation separately.
- Normal player trade is allowed. Material availability gates technology; quests guide rather than impose personal equipment locks.
- Lancer capped at +6 Brass / +9 Alloy, preserving ordinary sampled movement below the cap; reject invalid samples and reset on equip/dimension changes.

## Potential problems / follow-up register
- Peak survival balance remains provisional, especially four Echo Plates + Float Shield + Exoskeleton.
- Position sampling on moving Aeronautics ships may differ; hard cap must hold regardless.
- Native PP plate bonuses modify base attributes: test equip/rejoin/death; never reset unrelated attribute bases globally.
- Native Float Shield has two damage handlers; do not assume double subtraction without runtime trace.
- Fuel/tank durability constants disagree; preserve stock costs initially rather than introducing an unmeasured maintenance grind.
- Native fuel inventory capability permits items its GUI rejects; automation/custom-container paths need scrutiny.
- Mending on Water Tanks/Flare Rods is now blocked at eligibility and enchantment-component boundaries, including existing copies on access/inventory tick. Unbreaking is unchanged and remains a separate balance consideration.
- Echo enchantment/set effects from other sources must not be deleted; short resonance effect expiration is intentional.
- Native sequenced upgrades may discard custom components, including resonance markers. Remove resonance before upgrading unless verified otherwise; tutorial must warn.
- Custom attachments preserve equipment across death; corpse recovery, keepInventory and duplication require runtime testing.
- Plate repair/removal, server lag, nearby simultaneous service users and inventory-full behavior need coverage.
- PP oxygen/environment equipment is not automatically compatible with Creating Space, toxic rain, Hordes infection or Spore.
- Live client needs a full restart for the addon/item; /reload only reloads recipes/quests.
- No remote Chunkserve access or validation is possible.

## Backup
Q:\GPT General\Protection_Pixel_Implementation_Backup_20260916_202309

## Implemented files and behavior
- Pinned Protection Pixel 2.2.1 and the source-built `apocalypse-protection-pixel-integration-1.1.jar`; both sides require the addon. No upstream JAR is modified.
- 49 exact-ID PP recipe overrides and one Echo Plate recipe under `kubejs/data`. These are datapack JSON overrides, not fragile KubeJS wrappers around nested legacy Create recipes.
- Correct PP legacy `fluid_stack` ingredients to the installed NeoForge/Create serializer. Native Create processing and sequenced assembly remain intact.
- Brass entry uses processed Precision Mechanisms where missing; these remain purchasable in the existing economy. This is a materials gate, not a personal Brass-age lock.
- Alloy plates, Exoskeleton and the shared heat-overlocking mechanism require Power Grid magnets. The mechanism retains seven loops and therefore consumes seven magnets; this cost is provisional.
- Float Shield, Evasion Wing and Lancer-AS require Moon Regolith. Ordinary Alloy equipment is Electric; only these three experimental systems require lunar material.
- Brass/Alloy plate armor reduced from 2 to 1.5 per plate; toughness and weight unchanged. A full ordinary Alloy chassis with eleven Alloy plates has 23.5 armor and 11 toughness, versus 29/11 previously. Verify native stacking alongside other attribute-changing equipment before treating this as final balance.
- 19 optional quests added with stable IDs. Existing progression IDs are preserved. Echo resonance completion uses a server-awarded advancement, not merely plate possession.
- Two team-level, nonrepeatable consuming Special Requests: Brass supplies for $1,000; Alloy supplies for $8,000. No completed Echo or experimental armor is consumed.
- Lancer server sampling caps at +6/+9, resets on new helmet/dimension/stale sample, and rejects jumps over 20 blocks per sample. This discontinuity threshold may suppress legitimate fast flight and is a provisional safety tradeoff.
- Reactor validation rejects wrong fuel, spent water, stacked rods and stale cached power. Compact containers with omitted empty trailing slots are supported.
- Exoskeleton attack/mining bonuses clear when inactive or out of upper mode. Native movement, shield and fuel formulas otherwise remain unchanged.

## Build and maintenance
Run `python scripts/protection_pixel/generate_data.py`, `python scripts/protection_pixel/generate_quests.py`, `python scripts/protection_pixel/verify.py`, then `python scripts/protection_pixel/build.py` and `python scripts/protection_pixel/refresh_index.py` from this instance. Build requires the pinned local Java 21/NeoForge 21.1.248 runtime and server libraries; it does not download dependencies. The server libraries are read only.

The addon includes pinned PP 2.2.1 installation/recovery hooks as well as the earlier combat and consumable hooks. A PP upgrade requires source inspection and rerunning the dedicated-server tests; the exact mod dependency intentionally rejects other versions. Do not silently widen it.

Quest generation updates only its stable generated IDs and translations, preserving unrelated quests. Generated recipe changes and quest maps are checked in as audit records. Rebuild the addon and refresh its Packwiz hash after any source/resource change. The current index update touched only integration entries; unrelated dirty files still need their own release audit.

## Acceptance boundaries
Passing static/native tests establishes mechanics and recipe loading, not enjoyable combat balance. Play sessions with multiple real clients, Aeronautics moving ships, death/rejoin, actual Create assembly contraptions, plate service UI and all external equipment combinations remain unverified. Those are release-candidate checks, not claims made by this implementation.

Unchanged original Echo armor remains a simpler generalist alternative without reactor upkeep. The PP route offers specialization but requires four material-crafted plates, power and equipment management. Echo replaces ordinary plates in native slots and supplies the same physical stats as Alloy; resonance itself adds no further plate stats.

## Prioritized follow-up problems
1. Strongly recommended: measure Float Shield + four Echo Plates + Exoskeleton under sustained mixed damage. Native shield recharge (0.8 points/second) and 25-point capacity are retained pending measurements; no arbitrary shield nerf was made.
2. Strongly recommended: verify native armor upgrades preserve components or explicitly prevent upgrading plated pieces. Until then, tooltips and quests instruct removing Echo Plates before upgrades; upgrading without removal can lose the installed plate.
3. Strongly recommended: test normal clients and two simultaneous players for service synchronization, full-inventory refunds, death/rejoin and advancement/team sharing. Dedicated fake-player tests do not establish these.
4. Strongly recommended: measure seven-magnet mechanism manufacturing time and fuel upkeep during ordinary sessions. Costs and payouts are provisional, not mathematically proven fun.
5. Strongly recommended: audit existing broader-pack recipe/tag errors recorded during the test boot. These originate outside the PP namespace and were not silently repaired in this change.
6. Optional polish: localize service/tooltip strings. The Echo Shard placeholder was replaced with the dedicated 16x16 Echo Plate texture on 2026-09-18.

No GitHub push, local Chunkserve twin update, deployment packet or remote-server validation was performed. Restart the game fully to load the new addon/item; `/reload` alone cannot register them.


## Quest repair — 2026-09-17
The initial generator incorrectly used unrestricted 64-bit hex IDs. The installed FTB Quests parser calls Java `Long.parseLong(..., 16)`, which rejects the generated IDs above `7FFFFFFFFFFFFFFF`. Live chapter files consequently had replacement IDs while ten titles/descriptions still referenced the rejected originals. Missing prerequisites were also found. The original validation checked file shape and quest count but did not check the accepted ID range or a native save/reload cycle; its earlier quest-readiness conclusion was insufficient.

The repair preserves all 19 current quest IDs and all 31 current task/reward IDs. `scripts/protection_pixel/quest_ids.json` pins those identities. Existing live Brass prerequisites and all coordinates remain unchanged. Missing Electric/Space links are restored using current IDs. The ten obsolete title/description pairs are removed and relinked to their live quests; all other wording remains, except the malformed resonance counter is now `0/4 to 4/4`.

`generate_quests.py` now updates links/text from the checked quest map while preserving live tasks, rewards and layout. It handles FTB's reordered fields and multiline descriptions. Missing/duplicate IDs fail explicitly instead of creating duplicate quests. `--check` verifies no-op regeneration. The verifier checks signed-long ID validity, uniqueness across quests/tasks/rewards, exact active title/description links, prerequisites and stale translations. Do not restore the original invalid IDs or run the old hash-based generator.

Pre-repair backup: `scripts/protection_pixel/build/quest-repair-backup-20260917_195525`. No worlds, saved progress, recipes, armor mechanics or reward quantities were changed by this repair.


## Quest rework - 2026-09-20
See PROTECTION_PIXEL_QUEST_REWORK.md for the approved player-facing rewrite, modest team rewards, powered-loadout milestone, preserved live progression, and validation results. Native regression harness: python scripts/protection_pixel/run_quest_tests.py (uses the existing isolated test server only). The current Echo Plate uses the exact user-selected PNG; older texture-export instructions are superseded.


## Consumable repair restrictions - 2026-09-20
User requested that Disposable Water Tanks and Flare Rods cannot have Mending or be repaired by the Reforger.
- Exact items: protection_pixel:watertank and protection_pixel:flarerod. Empty tanks, reactors, armor and ordinary tools are unaffected.
- Consumable item overrides reject Mending eligibility while preserving other enchantment eligibility.
- ItemStack component writes filter only Mending from these items, covering enchant()/EnchantmentHelper and machine/component application. Enchantment reads and server inventory ticks clean legacy copies. Stored items elsewhere are cleaned on access, not by scanning world storage.
- A narrow Reforger curioTick redirect skips only these two items in its damaged-item selection. It does not alter general repair functions or item damage semantics.
- Existing durability, names and other enchantments are retained. Fuel and water wear are unchanged. Unbreaking remains allowed under existing rules.
- Compatibility targets: Protection Pixel 2.2.1, NeoForge 21.1.248, Nameless Trinkets 1.2.4. Reforger is optional via @Pseudo; its exact call site requires revalidation on mod upgrades.
- Source: ConsumableRepairRules.java and the three ConsumableItem/ConsumableEnchantments/Reforger mixins. Native coverage: ConsumableRepairTest.java, exercised by the isolated test server selftest.

## Recipe review scope - 2026-09-20
User requested detailed proposals for review points 3, 4, 5, 6, 7 and 9 first. See [PROTECTION_PIXEL_RECIPE_PROPOSALS.md](PROTECTION_PIXEL_RECIPE_PROPOSALS.md) for exact candidates, assumptions and potential problems. All nine recipes in that document were subsequently approved and installed on 2026-09-20. The generator preserves the changes; the original fiber recipe remains alongside the hemp alternative. Defer points 1, 2, 8, 10, 11 and 12 for later detailed review.

## Native Echo plate tier - 2026-09-21
User approved replacing the attachment mechanic and explicitly waived migrations. Legacy boolean markers are ignored; no conversion or refunds are implemented. The existing Echo recipe and texture remain unchanged.

The Echo item is unstackable, damageable and tagged as a native plate. Installation hooks refresh its stats from the Alloy configuration on the server before native assembly, independent of client tooltips. Native slot IDs and saved damage drive resonance; armor wear is allocated across occupied plates in order, matching native Fix1..Fix5. Native plates saturate at 99 damage: those exhausted Echo plates no longer qualify, even though the item can remain recoverable. At exhaustion, recovery uses Alloy's 15% destruction chance, rather than the default 25%. The existing plate repair kit repairs recovered Echo plates.

Adding missing native fitting/protection tags enables ordinary plating for Float Shield and Link Plate armor, not only Echo. Native capacities are 2 head, 5 chest, 4 legs, 2 feet. This changes the physical-defense options of these formerly excluded pieces; their combined combat balance remains a follow-up. No new custom menu is required.

The earlier statement that boots lacked a native fitting route was incorrect: Spreadgui routes the foot tag to Headgui, and Connecthead has a separate FEET branch. Source inspection and native tests supersede that assumption.

Player text now describes ordinary plate slots and worn-plate repair. Deferred recipe review points remain deferred. Native UI rendering and real multi-client play are not claimed by server-only tests.

Native Echo validation: 445 static assertions, 452 native FTB quest assertions, and 236 addon assertions passed on 2026-09-21. Tests call native connection, wear-recovery and repair procedures for ordinary armor, boots, Float Shield and Link Plate; check sequential Alloy/Echo wear, effect requirements, and the old-marker rejection. Updated addon/tag/translation Packwiz hashes verified. No push or server twin update.

## Addon version 1.1 — 2026-09-23
Build output, runtime metadata, test harness and index refresh use version 1.1. Obsolete 1.0.0 runtime JARs removed.
