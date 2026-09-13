# Changelog

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

- Use original Moogs Paths, Weather2, Weather2Compat and SableWaves JARs with four separately named Apocalypse compatibility fixes.
- Remove superseded mod versions and patched base JARs that conflicted with the standalone fixes during startup.
- Exclude the abandoned Lost Cities/OWZA files. Retain Big Lost City and its Apocalypse biome compatibility datapack.
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
