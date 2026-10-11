# Better Combat scope — local WIP, updated 2026-10-11

Minecraft 1.21.1 / NeoForge 21.1.248. Better Combat 2.4.0+1.21.1, official Modrinth version VhIOvcXP, SHA-1 bacbc8c86671663e9998b141c5732250c1f4d4fb. Uses the existing Cloth Config 15.0.140 and Player Animator 2.0.4.

## Approved scope and balance

SwordItem, AxeItem and TridentItem weapons use Better Combat. Vanilla mace eligibility was rolled back for testing on 2026-10-11. Preserve upstream profiles and every upstream balance setting, including movement_speed_while_attacking=0.5 and dual_wielding_attack_speed_multiplier=1.2. No server.json5 or client config overrides are installed. Automatic profile assignment retains the applicable original sword/axe/trident presets and excludes gun and bow profiles.

Backported Spears 1.8.0, Echo Spear 1.0.8, maces, pickaxes, empty hands and all cgs/ntgl items (including the pneumatic hammer) remain outside Better Combat. Items carrying the spear piercing/kinetic components are also excluded. Custom swords and axes must extend the corresponding Minecraft item class to participate; other custom weapon classes intentionally remain native until reviewed.

## Compatibility implementation

Apocalypse Combat Scope 1.0.1 adds required mixins on both sides:

- WeaponRegistry.register rejects excluded items. WeaponRegistry.getAttributes(ItemStack) also returns null for excluded items, protecting against explicit data profiles, synchronized profiles and bettercombat:preset_id components. Accepted sword/axe/trident profiles pass through unchanged.
- SpearsNeoForge constructor return sets Spears.hasBetterCombat=false. Upstream ClientPlayerEntityMixin.triggerOnSwingEffects checks this flag before sending its extra TriggerStabEffectsC2SPacket. Since excluded spears already use their native stab packet, that compatibility bridge would duplicate on-swing effects such as Lunge. Native spear packets, charging, animations, item components and stats are retained. config/spears-common.toml remains unchanged, with stabbing and charge attacks enabled.

Upstream JARs remain unmodified. Source and pinned build: scripts/better_combat/. Rebuild with build.py --output NEW_EXTERNAL_DIRECTORY --better-combat OFFICIAL_JAR. The addon pins Better Combat 2.4.0+1.21.1 and Spears 1.8.0; review before updating either.

## Validation

The restored, byte-identical 1.0.1 addon previously passed 667 assertions on an isolated NeoForge dedicated server with actual Better Combat, both spear mods, Create, Create: Deep Dark, Gunsmithing, NTGL, GeckoLib, Cloth Config and Player Animator. Both addon mixins applied. All public upstream server config fields match a fresh ServerConfig instance. All six vanilla sword and axe materials have real profiles. 143 excluded items include 82 cgs items and seven spears; neither direct registration nor an explicit sword-preset component bypasses the guard. Netherite and Echo spear right-click charge starts correctly in both hands, with native use duration. Tridents use the unchanged upstream one-handed stab profile (trident category, +0.25 range bonus, not two-handed); custom names preserve that profile. Native trident charging in both hands and projectile release passed. The vanilla mace is excluded, including explicit registration and preset-component bypass attempts. No test harness is installed in the pack.

Evidence for the restored addon and isolated generated test world: Q:\GPT General\better-combat-tridents-20261011. See runtime-attempt1.log, build-attempt1/build.json and installation.json. Earlier 1.0.0 evidence remains in Q:\GPT General\better-combat-20261010. Existing worlds were not accessed. This proves native registration, mixin application, default balance and server-side item use, not client rendering or full-pack gameplay compatibility.

## Installation and activation

Local WIP only; no publication, twin changes or Chunkserve deployment. Version and compatibility markers are not advanced. Restart the WIP client to load the added mods. A future multiplayer deployment requires Better Combat and the scope addon on both client and server, plus the same fallback config; server activation remains user-managed.

In-game checks still needed:

1. First- and third-person sword sweeps and axe attacks with Apocalypse Weapon Library models; mining with axes. Trident melee animations, named models, throwing, Loyalty and Riptide require client gameplay verification. Test vanilla mace attacks, falling smash damage, Density, Breach and Wind Burst with Better Combat excluded and the new models retained.
2. Base and Echo spear stabbing, right-click charging, piercing and Lunge IV (one activation per click), then switching to a sword/axe and back.
3. Gun firing (semi/automatic), aiming, reload, attachments and rapid switches between gun, sword/axe and spear. Check main-hand guns with an offhand melee weapon too.
4. Echo Sword effects, criticals, shields and dual wielding with upstream balance intact.

Rollback: after closing the client, remove only mods/bettercombat-neoforge-2.4.0+1.21.1.jar and mods/apocalypse-combat-scope-1.0.1.jar. Remove the newly added fallback config and Better Combat metadata. Revert only this integration's index and ignore additions; do not restore whole metadata files over subsequent unrelated edits. Exact additions and before/after hashes are recorded in installation.json; pre-install metadata copies are in local-installation/before. Existing spear and gun JARs/configs are unchanged.

## Trident enablement rollback (2026-10-11)

The 1.0.0 addon and before-copies of affected files are retained under Q:\GPT General\better-combat-tridents-20261011\before. To revert only trident enablement, close the client, remove the 1.0.1 scope addon and restore the 1.0.0 addon; restore the previous fallback config. Revert the corresponding index entry/hash and ignore-path replacement without overwriting subsequent unrelated edits. The upstream Better Combat JAR, native spears, guns, balance configs and named-trident rendering addon are unchanged. No server deployment or release marker advancement.

## Vanilla mace enablement rollback (2026-10-11)

The previous 1.0.1 addon and affected files are backed up under Q:\GPT General\better-combat-mace-20261011\before. To revert only mace enablement, close the client, replace scope 1.0.2 with 1.0.1, and revert only its index entry/hash and ignore path. The fallback config is unchanged; only minecraft:mace is newly eligible. Original Better Combat balancing, trident support, spear and gun exclusions remain intact. Local WIP only; no release or server deployment.

## Mace combat eligibility reverted (2026-10-11)

Restored the exact previously tested scope 1.0.1 JAR (SHA-256 583d5c3d06df625ddd8c55253800ff38b45644099d707461f415bdde5206a87b) and its source/build/tests. No fresh runtime run is claimed for this reversion. Swords, axes and tridents retain Better Combat; maces use native combat. Improved Mace Hammer, Breach/Wind Burst visuals, The Zephyr rename, all other mods and configs are unchanged. Restart the client to activate. Local WIP only; no server changes or release advancement. Rollback record: Q:\GPT General\better-combat-mace-20261011\rollback-011610.

## Confirmed mace decision (2026-10-11)

Keep vanilla mace combat mechanics. The user found Better Combat's mace handling unwieldy and difficult to connect with during falling attacks. Maces remain excluded from Better Combat; preserve Improved Mace Hammer, its Breach/Wind Burst model variants and the mace-only The Zephyr rename. Do not re-enable Better Combat for maces without a new user request.
