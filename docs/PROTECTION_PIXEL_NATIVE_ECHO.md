# Native Echo Plates

Implemented 2026-09-21. No migrations, as requested.

## Player workflow
Wear the armor and use the Armor Load Platform's normal armor selection and plate-fitting menus. Put Echo Plates into ordinary plate slots and connect the plates. Boots use the native two-slot screen. Recover plates through the same platform; repair recovered plates with the Armor Plate Repair Kit.

One serviceable Echo Plate in EACH equipped armor piece, plus an operating reactor, grants Resistance II and Strength II. Extra plates in one piece cannot replace a missing piece and do not raise effect levels. Buffs expire within one second when the requirements stop being met; unrelated stronger/longer effects are preserved.

Echo has Alloy's configured armor/toughness/weight (currently 1.5/1.0/1.5) and 100 durability. Native wear saturates at damage 99 (displayed as 1/100 remaining); at that point the plate no longer qualifies for resonance. Exhausted plates have the same 15% chance of destruction on recovery as Alloy. Recover and repair before exhaustion to avoid that risk.

Float Shield and Link Plate armor now have the missing native fitting/protection tags. This enables other ordinary plates too. Capacities remain native: helmet 2, chest 5, legs 4, boots 2. Combined combat balance, particularly Float Shield plating, remains a follow-up.

The recipe and approved texture are unchanged. The old main-hand/offhand attachment interaction is removed. Old attachment markers are not converted and no longer grant resonance. A full client/server restart is required for the updated addon.

## Validation
- 445 structural assertions passed.
- 452 FTB native quest checks passed; all 18 saved definitions were checked.
- 236 addon assertions passed, including native fitting, server-side Alloy stat initialization, wear allocation, recovery, kit repair, four-slot bonuses, power checks and consumable repair restrictions.
- Generator preserves all existing recipe JSON files.
- Pack metadata hashes verified for updated addon, tags and translations.

Native procedures were exercised on an isolated dedicated server. Manual client rendering, real multiple-player sessions and combat balance were not tested. No release/push or Chunkserve twin update was performed.
