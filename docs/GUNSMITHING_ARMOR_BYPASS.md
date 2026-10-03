# Piercing-round armor bypass — 2026-10-03

Release v1.1.6 includes this addon. The following installation/test notes describe the earlier local preparation. Publication and server activation have separate deployment records.

Implemented and installed in local WIP: Gunsmithing Enchantments **1.1.0**.

Only `cgs:round_revolver_piercing` and `cgs:round_gatling_piercing` ignore **65% of armor points**. Armor input becomes `armor * 0.35`; toughness is unchanged. The current 12 base damage, entity penetration, attachments, critical hits/headshots and all four enchantments retain their existing pipelines. Resistance, Protection and absorption remain in Minecraft's normal subsequent defense stages. Ordinary rounds retain their normal armor calculation.

The hook identifies the projectile's stored ammo item, not the shooter's currently loaded ammo. It marks the existing damage-source type and keeps its causing/direct entities. There is no temporary target-attribute mutation, global damage-type tag change or shared per-hit state.

## Verified results

Against exactly 20 armor and 8 toughness, body hits without damage attachments, headshots, critical hits or additional defenses:

| Round | Incoming damage | Health damage | Hearts |
|---|---:|---:|---:|
| Normal round | 8 | 2.24 | 1.12 |
| Piercing, unenchanted | 12 | 10.08 | 5.04 |
| Piercing, Sharpshooter IV | 24 | 22.656 | 11.328 |

120 HP / 22.656 requires six hits; the native six-hit test passed for both piercing ammo types. Vanilla zombies also have two natural armor points: a full-diamond zombie normally totals 22 armor. Its Sharpshooter-IV-equivalent hit took 22.368 damage in the native test; it still takes six hits to kill 120 HP. The prior chat examples explicitly used only the diamond set's 20 armor and 8 toughness.

## Verification

- User confirmed the previous 1.0.0 enchantment addon works in game. That confirmation predates this change.
- Built 1.1.0 against pinned actual NTGL 3.1.8 and Gunsmithing 1.4.9, Java 21, Minecraft 1.21.1 / NeoForge 21.1.248.
- **589 native dedicated-server assertions passed**, including transformed ProjectileEntity ammo identification and CombatRules armor modification. Covered both piercing and ordinary rounds, 7 armor values, 3 toughness values, 6 incoming damage values, real equipped-zombie health loss, Protection IV, Resistance I, vanilla natural zombie armor, unchanged zombie armor durability behavior, and the six-hit sequence. Server started and shut down cleanly.
- **91 isolated enchantment regression assertions passed** on the final JAR: 49 actual Minecraft lookup checks and 42 compiled-effect checks, including Sharpshooter IV's 2x multiplier and CEI above-max levels.
- The native six-shot test feeds 24 damage into the real hit pipeline; Sharpshooter's production multiplier was separately checked by the existing compiled-hook regression. Player armor durability, interactive client shooting, CEI machine operation and full-pack startup have not been retested for 1.1.0.
- Test attempt 1 required equipment-attribute refresh in the fixture; attempt 2 used an incorrect assumption that zombie armor wears like player armor. Both were corrected in the fixture. Attempt 3 passed. Logs are preserved separately.

## Artifact and source

Installed: `mods/apocalypse-gunsmithing-enchantments-1.1.0+mc1.21.1.jar`.
SHA-256: `295fc223b628fb8a7dfdab9ec49eba7a0f68372534832394bfce226fcc83da73`.

Rebuildable source and native tests: `scripts/gunsmithing_enchantments/`.
Build and test evidence: `Q:/GPT General/gunsmithing-armor-bypass-build-2026-10-03/`.
Passing native log: `regression-attempt3.log`; isolated log: `registry-results.txt`.

The obsolete 1.0.0 runtime JAR was backed up outside the pack and removed from mods, avoiding duplicate mod IDs. The targeted Packwiz entry and root index checksum were updated. This is not a full audit of the WIP manifest or a release-version change.

## Activation and rollback

Restart the WIP client to load 1.1.0. Dedicated servers must also install the addon and restart to enforce the behavior there. No running client/server was interrupted, no twin/Chunkserve deployment or publication was performed, and no existing saves/schematics were accessed.

Rollback: while stopped, remove only the 1.1.0 addon and restore the 1.0.0 JAR from `Q:/GPT General/gunsmithing-armor-bypass-build-2026-10-03/local-installation/before/mods/`. Restore only this addon's manifest/ignore entries and refresh the root index checksum; do not overwrite later unrelated metadata edits. Before/after hashes are recorded in `local-installation/record.json`.
