# Pending v1.2 — Combat and mace visuals

Prepared locally for v1.2 integration; publication and deployment are pending. This component complements WEAPON_LIBRARY.md and preserves all other queued contributions.

Better Combat 2.4.0+1.21.1 and Apocalypse Combat Scope 1.0.1 enable sword sweeps, axe attacks and trident melee profiles. Retain every upstream balance setting, including the 0.5 movement multiplier and 1.2 dual-wield attack-speed multiplier. Include the approved fallback configuration. Do not ship generated client/server/trail settings as deliberate balance overrides.

Spears, guns, the pneumatic hammer, empty hands and maces remain outside Better Combat. Preserve native spear charging/Lunge and trident charging/projectile release. The user explicitly chose vanilla mace combat because Better Combat made falling hits unwieldy. The proposed mace downswing/hit-window redesign remains deferred.

Improved Mace Hammer is the default vanilla mace appearance. Breach selects the thorns model; Wind Burst selects the animated wind model and wins when both enchantments are present. Renaming only a vanilla mace to `The Zephyr` selects the alternative Zephyr model through the existing Polytone/CIT system. Apocalypse Mace Visuals 1.0.2 retains the enchantment selector and keeps both animated wind sprites active through Sodium's public API.

The third wind band uses the same-side working UV strips with a separate one-frame phase offset: frames [1,2,3,4,5,6,0], 2 ticks per frame. The PNG bytes remain original; geometry and animation speed are unchanged. The user confirmed "all is well now" after this final repair on 2026-10-11.

## Payload and integration

Both client and server: official Better Combat JAR/Packwiz metadata, Combat Scope 1.0.1 and config/bettercombat/fallback_compatibility.json. Existing Cloth Config, Player Animator and Backported Spears dependencies remain required. Mace Visuals 1.0.2 and Apocalypse_Weapon_Library.zip are client-only. Do not install the mace visuals addon on the server.

The component overlay includes exact working binaries, component source/build/tests and documentation. New custom-addon Packwiz metadata pins SHA-256 and uses the future v1.2 tag. Those URLs remain unverified until publication. Replace older development copies of the scope and mace addons individually; the published v1.1.15 baseline has neither addon. Avoid duplicate mod IDs.

The shared weapon-library ZIP in this overlay includes the latest mace repair and existing library content. It supersedes the earlier weapon-library preparation snapshot for that one path. Compare its hash and preserve any subsequent library edits when integrating; do not blindly overwrite newer work. Enable the library in portable fresh-install defaults and preserve existing player preferences.

Integrate alongside the other approved v1.2 changes in a clean release checkout, set the shared release version there, rebuild the complete Packwiz index and bootstrap, then validate the applicable client and server payloads. This component manifest is targeted and cannot establish full server alignment. No live version markers or server files changed during this preparation.

## Validation and publication prerequisites

The exact restored Combat Scope 1.0.1 previously passed 667 isolated native-server assertions, covering profiles, exclusions, native spear/trident use and unchanged upstream balance. Mace selector tests passed separately; the final client event code compiled and asset/ZIP checks passed. The user confirmed the final Wind Burst appearance. Full-pack sword/axe/trident/gunplay checks, all mace enchantment/mechanic combinations, fresh-install/update and multiplayer release validation remain pending.

Improved Mace Hammer by GugerOS is All Rights Reserved. Local adaptation is retained, but permission to redistribute its merged model/texture assets has not been established. Resolve that before publishing the shared ZIP; this preparation does not grant permission. Zephyr Warhammer by jonnyloaf uses CC-BY-SA-4.0; preserve attribution and the applicable derived-asset license. See docs/MACE_VISUALS.md for exact original versions and source links.

The user manages all activation/restarts/reloads. A future client installation requires restart for the addons; a future server combat installation requires user-managed activation.
