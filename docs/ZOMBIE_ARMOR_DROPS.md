# Zombie armor drops: steel cap

The local balance change allows zombies to keep wearing all existing progression armor, but removes armor above the recoverable steel tier from their death drops. Steel, iron, chainmail, gold, leather, cardboard, Faraday, copper-diving pieces and turtle helmets remain eligible at their existing chances. Diamond, Netherite, Echo and spacesuit armor cannot drop. Unlisted armor is withheld by default; extend the allowlist only after reviewing its progression tier.

Implementation: `kubejs/server_scripts/apocalypse_zombie_armor_drops.js` registers the same drop filter for `minecraft:zombie` and `minecraft:drowned` and removes only disallowed armor from the resulting drop list. Armor is recognized by ArmorItem or the four vanilla armor-slot tags. Weapons, normal loot, armor worn during combat, spawn frequency, scaling and phase configuration are unchanged. The cap applies in every dimension and to already-existing zombies, including converted drowned and armor they picked up. Other entity types and player drops are unaffected. This follows the user's strict steel cap rather than the attachment's optional tiny-drop alternative or progression-only marking proposal.

Compatibility: installed InControl 1.21-10.2.6 armor actions call setItemSlot without setting drop chances. Its loot modifier processes loot-table output, so it is not used to filter equipment drops. Installed KubeJS 2101.7.2-build.368 exposes this drops event, its mutable getDrops list, and applies the modified list back to NeoForge LivingDropsEvent. Verified from installed class bytecode. No mod additions or startup scripts are needed.

Validation: `node scripts/test_zombie_armor_drops.cjs` checks every one of the 115 armor selections across all eight equipped progression phases for both zombies and drowned (230 cases), plus mixed normal loot, steel, Netherite, unknown armor and tagged non-ArmorItem equipment. Script syntax passes `node --check`. These are mocked API checks, not Minecraft runtime tests. All four InControl JSON files remain unchanged.

Activation: reload server scripts with `/reload`, or restart/reopen the world. In a disposable test world, summon zombies with Diamond/Echo/steel equipment and guaranteed ArmorDropChances, kill them, and check that only the steel armor remains eligible. Repeat for normal loot and a skeleton with guaranteed Diamond armor drops, and inspect kubejs/logs/server.log for errors. No in-game reload or kill test has been performed by the agent.

Packaging: only the new server-script entry and the index checksum are updated in Packwiz metadata; existing unrelated local changes are retained. Nothing has been pushed, and the Chunkserve twin has not been modified.

## Drowned conversion loophole fixed locally (2026-09-20)

The same steel-cap handler now covers drowned, including already-converted entities. No spawn-time marker is needed. Tridents, fishing rods, copper ingots, nautilus shells and rotten flesh remain unchanged.

Inspected installed NeoForge 21.1.248 Minecraft bytecode: Zombie.convertToZombieType calls convertTo with equipment retention enabled. Mob.convertTo uses copyAndClear/setItemSlot to transfer equipment and copies its drop chance; this path does not emit armor item drops. Filtering the resulting drowned death drops therefore covers the standard conversion path while retaining worn armor.

Regression checks model transferred high-tier equipment reaching the drowned drop handler, and verify steel, normal drowned loot and unrelated entities. This is not an in-game conversion test. Runtime verification remains: convert an armored zombie underwater, check its armor remains equipped, kill the drowned with guaranteed equipment drop chances, and check steel is eligible while Diamond/Echo/Netherite/spacesuit armor is blocked. Check logs after reload/restart.

A revised standalone server hotfix packet accompanies this fix. The earlier 2026-09-14 packet remains unchanged and does not contain drowned support. No official release push or server-twin synchronization was performed.
