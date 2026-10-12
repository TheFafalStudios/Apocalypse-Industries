# Changelog

## v1.3 — 2026-10-12

- Add Axiom 6.1.3 through the existing Connector/Forgified Fabric API stack. Dedicated servers start with Axiom disabled; permission-level-4 operators can enable or disable it with `/axiomserver enable|disable` and inspect `/axiomserver status`. Enabling preserves native permissions and licensing; restart resets to disabled.
- Add Studious Keybinds 1.0.8-beta.1, retaining NeoForge 21.1.248 and existing keybind preferences.
- Fix the Steam n Rails phantom-track texture reload race that crashed WIP startup.
- Fix Studious/Connector shared-key dispatch that crashed after entering a world, preserving its visual editor and native NeoForge input behavior.
- Client crash fixes and Studious remain client-only. The server receives Axiom, its existing runtime dependencies and the server control addon. Existing server overrides are preserved; activation requires a user-managed restart.

Static distribution checks, isolated client crash tests and dedicated Axiom policy tests are distinct from full multiplayer gameplay validation. See `docs/AXIOM_SERVER_CONTROL.md`, `docs/PHANTOM_TEXTURE_RELOAD_FIX.md` and `docs/STUDIOUS_INPUT_FIX.md`.

## v1.2 - 2026-10-11

### Client connection hotfix

- Add the client-only L2 Serial compatibility addon. It matches the running server's name-based registry encoding and fixes Golem Dungeons custom-payload decoding disconnects. Reconnection was confirmed in the running client; fresh-launch addon activation remains untested.


Cumulative release notes: includes the v1.1.1â€“v1.1.15 updates already released, plus the local changes queued for v1.2. Minecraft remains 1.21.1 and NeoForge remains 21.1.248. Publication/deployment file checks are recorded separately; activation and combined gameplay validation remain user-managed.

### Already released in v1.1.x

- **Spears and Echo Spear:** add Backported Spears and the custom Echo Spear, with native charging/Lunge controls, Echo smithing, reduced attack speed, Weakness/Darkness effects and corrected inventory icons. Spear gameplay and Super Forging/Lunge IV were confirmed by the user. (v1.1.15)
- **Gun enchantments:** add Quick Hands, Trigger Finger, Lightweight and Sharpshooter, including Create: Enchantment Industry's extra enchantment levels. Piercing revolver and Gatling rounds bypass 65% of armor points while preserving toughness and the normal subsequent defenses. (v1.1.6)
- **Survival attachment removal:** allow ordinary pickup and shift-click removal of gun attachments in Survival/Adventure; split large ammunition refunds into valid stacks to prevent oversized, unsaveable stacks. (v1.1.8)
- **Displays and decoration:** add Dead's Displays, Items Displayed and Armor Poser. (v1.1.7)
- **Create and ship utilities:** add Create: Copyfoxes and Sable CleanUp. (v1.1.9)
- **Weather-driven ship wind:** update Weather Physics to 1.0.3, fixing ordinary wind slowing wheels/vehicle propulsion; introduce the positional-wind prototype that samples Weather2 at each ship's world position, including its existing altitude and local-storm behavior. Flight balance/performance still need gameplay verification. (v1.1.10â€“v1.1.11)
- **Envelope storm damage:** restrict punctures to close airborne funnel encounters, with terrain-clearance checks, distance falloff, a capped chance and shared ship cooldowns. Preserve the configured destruction controls and other storm physics. (v1.1.13)
- **Toxic-rain shelter:** recognize standing/riding on assembled Sable sublevels as shelter and retain normal sheltered exposure recovery. The temporary full-armor Poison immunity was rolled back; armor does not grant that immunity in the final behavior. (v1.1.4â€“v1.1.5)
- **Crafting and mining fixes:** restore axe effectiveness for BOP dead logs/wood and stripped variants; add unheated Create mixing for eight IE concrete blocks and correct its output to blocks rather than fluid; restore nine-ingot copper-block unpacking for Sophisticated Storage compression. (v1.1.1â€“v1.1.3, v1.1.14)
- **Stability and server loading:** fix normal/Mechanical Importer hangs on invalid or non-progressing purchases; guard 518 optional block loot tables against missing items, reducing the targeted startup parsing errors. (v1.1.6, v1.1.12)

### Added in v1.2

- **Weapon model library:** consolidate the approved weapon models into one active pack, with 262 assigned RP Renames entries, material/weapon restrictions, approved lower-tier exceptions and 17 retained unassigned designs. Archive rejected families and duplicates outside Minecraft. Include a categorized, textured 3D browser catalogue.
- **Flexible model names and rendering:** allow words before/after model names and font/color formatting; resolve overlapping names without changing unnamed items. Normalize trident held/throwing poses and scale, preserve named models in flight, and fix spear inventory/RP Renames previews. Client bridge 1.3.2 and pinned RP Renames/Connector/Forgified Fabric API dependencies accompany the library. The user confirmed the final weapon-library setup works.
- **Better Combat:** introduce the scoped sword, axe and trident trial with upstream movement/dual-wield balance settings. Keep spears, guns, the pneumatic hammer and vanilla mace outside its scope; preserve native spear/trident charging and projectile release. Full-pack client animation and gunplay validation remain pending.
- **Mace visuals:** use Improved Mace Hammer for the vanilla mace, with Breach thorns, animated Wind Burst variants and the mace-only The Zephyr appearance. Repair wind-sprite activity/UVs and add the third band's offset animation phase. Mace combat remains vanilla. The user confirmed the final Wind Burst animation works after the phase-offset repair. Broader mace gameplay and combined-release checks remain pending.
- **Echo Plate production:** replace mechanical crafting with eight Create assembly loops from an undamaged Alloy Plate, yielding an Inert Echo Plate, then finish with heated basin compacting. No random failures or upgrade templates; native plate/resonance mechanics remain unchanged. Isolated recipe registration and sequence tests passed.
- **Protection Pixel aluminum progression:** replace relevant existing sheet/deployment ingredients with aluminum plates for Float Shield, Evasion Wing and Lancer-AS; move the Space equipment branch beneath the aluminum milestone while preserving its Electric prerequisite. Preserve native recipe patterns and equipment mechanics.
- **Protection Pixel discovery and quest clarity:** expose Echo Resonance Plate in JEI and the Protection Pixel creative tab through integration 1.1.1. Add explicit reactor/Echo quest icons, put reactor onboarding before powered equipment, correct Float Shield plating guidance and possession-task labels, and clarify Space equipment titles and movement-dependent Lancer bonuses. Preserve existing quest IDs/progress, rewards and completion checks.
- **Weather2Compat crash fix:** update the standalone safety addon to skip sand/snow particle-storm objects in rain and precipitation/sky-brightness checks, preventing the save-loading ClassCastException while preserving its earlier thread-safety fix and Weather2 particle events.

### Experiments and remaining checks

- The Euphoria sky-history shader candidate, Aeronautics/Deep Seas/Distant Horizons upgrade research and Endgame visual quest prototype remain experiments or design work; they are not advertised as completed v1.2 features. The proposed nuclear progression remains a design constraint, not an installed integration.
- Historical static/isolated tests and published file verification do not establish complete multiplayer compatibility. Combined v1.2 fresh-install/update, client rendering, flight balance and applicable server-payload validation remain pending. Preserve intentional server overrides; no restart/reload is performed by changelog preparation.

See [the v1.2 changelog evidence](CHANGELOG_EVIDENCE.json) and [weapon-library integration notes](WEAPON_LIBRARY.md). See [combat and mace integration notes](COMBAT_AND_MACE.md) for the exact scope, validation and asset redistribution prerequisite. Detailed historical release notes remain in the root CHANGELOG.md.


## Pending v1.2 — Major changes since v1.1 (2026-10-11)

Cumulative release notes: includes the v1.1.1–v1.1.15 updates already released, plus the local changes queued for v1.2. Minecraft remains 1.21.1 and NeoForge remains 21.1.248. This is a draft changelog, not publication or deployment confirmation.

### Already released in v1.1.x

- **Spears and Echo Spear:** add Backported Spears and the custom Echo Spear, with native charging/Lunge controls, Echo smithing, reduced attack speed, Weakness/Darkness effects and corrected inventory icons. Spear gameplay and Super Forging/Lunge IV were confirmed by the user. (v1.1.15)
- **Gun enchantments:** add Quick Hands, Trigger Finger, Lightweight and Sharpshooter, including Create: Enchantment Industry's extra enchantment levels. Piercing revolver and Gatling rounds bypass 65% of armor points while preserving toughness and the normal subsequent defenses. (v1.1.6)
- **Survival attachment removal:** allow ordinary pickup and shift-click removal of gun attachments in Survival/Adventure; split large ammunition refunds into valid stacks to prevent oversized, unsaveable stacks. (v1.1.8)
- **Displays and decoration:** add Dead's Displays, Items Displayed and Armor Poser. (v1.1.7)
- **Create and ship utilities:** add Create: Copyfoxes and Sable CleanUp. (v1.1.9)
- **Weather-driven ship wind:** update Weather Physics to 1.0.3, fixing ordinary wind slowing wheels/vehicle propulsion; introduce the positional-wind prototype that samples Weather2 at each ship's world position, including its existing altitude and local-storm behavior. Flight balance/performance still need gameplay verification. (v1.1.10–v1.1.11)
- **Envelope storm damage:** restrict punctures to close airborne funnel encounters, with terrain-clearance checks, distance falloff, a capped chance and shared ship cooldowns. Preserve the configured destruction controls and other storm physics. (v1.1.13)
- **Toxic-rain shelter:** recognize standing/riding on assembled Sable sublevels as shelter and retain normal sheltered exposure recovery. The temporary full-armor Poison immunity was rolled back; armor does not grant that immunity in the final behavior. (v1.1.4–v1.1.5)
- **Crafting and mining fixes:** restore axe effectiveness for BOP dead logs/wood and stripped variants; add unheated Create mixing for eight IE concrete blocks and correct its output to blocks rather than fluid; restore nine-ingot copper-block unpacking for Sophisticated Storage compression. (v1.1.1–v1.1.3, v1.1.14)
- **Stability and server loading:** fix normal/Mechanical Importer hangs on invalid or non-progressing purchases; guard 518 optional block loot tables against missing items, reducing the targeted startup parsing errors. (v1.1.6, v1.1.12)

### Queued local changes for v1.2

- **Weapon model library:** consolidate the approved weapon models into one active pack, with 262 assigned RP Renames entries, material/weapon restrictions, approved lower-tier exceptions and 17 retained unassigned designs. Archive rejected families and duplicates outside Minecraft. Include a categorized, textured 3D browser catalogue.
- **Flexible model names and rendering:** allow words before/after model names and font/color formatting; resolve overlapping names without changing unnamed items. Normalize trident held/throwing poses and scale, preserve named models in flight, and fix spear inventory/RP Renames previews. Client bridge 1.3.2 and pinned RP Renames/Connector/Forgified Fabric API dependencies accompany the library. The user confirmed the final weapon-library setup works.
- **Better Combat:** introduce the scoped sword, axe and trident trial with upstream movement/dual-wield balance settings. Keep spears, guns, the pneumatic hammer and vanilla mace outside its scope; preserve native spear/trident charging and projectile release. Full-pack client animation and gunplay validation remain pending.
- **Mace visuals:** use Improved Mace Hammer for the vanilla mace, with Breach thorns, animated Wind Burst variants and the mace-only The Zephyr appearance. Repair wind-sprite activity/UVs and add the third band's offset animation phase. Mace combat remains vanilla. The latest visual follow-up still needs gameplay confirmation.
- **Echo Plate production:** replace mechanical crafting with eight Create assembly loops from an undamaged Alloy Plate, yielding an Inert Echo Plate, then finish with heated basin compacting. No random failures or upgrade templates; native plate/resonance mechanics remain unchanged. Isolated recipe registration and sequence tests passed.
- **Protection Pixel aluminum progression:** replace relevant existing sheet/deployment ingredients with aluminum plates for Float Shield, Evasion Wing and Lancer-AS; move the Space equipment branch beneath the aluminum milestone while preserving its Electric prerequisite. Preserve native recipe patterns and equipment mechanics.
- **Protection Pixel discovery and quest clarity:** expose Echo Resonance Plate in JEI and the Protection Pixel creative tab through integration 1.1.1. Add explicit reactor/Echo quest icons, put reactor onboarding before powered equipment, correct Float Shield plating guidance and possession-task labels, and clarify Space equipment titles and movement-dependent Lancer bonuses. Preserve existing quest IDs/progress, rewards and completion checks.
- **Weather2Compat crash fix:** update the standalone safety addon to skip sand/snow particle-storm objects in rain and precipitation/sky-brightness checks, preventing the save-loading ClassCastException while preserving its earlier thread-safety fix and Weather2 particle events.

### Experiments and remaining checks

- The Euphoria sky-history shader candidate, Aeronautics/Deep Seas/Distant Horizons upgrade research and Endgame visual quest prototype remain experiments or design work; they are not advertised as completed v1.2 features. The proposed nuclear progression remains a design constraint, not an installed integration.
- Historical static/isolated tests and published file verification do not establish complete multiplayer compatibility. Combined v1.2 fresh-install/update, client rendering, flight balance and applicable server-payload validation remain pending. Preserve intentional server overrides; no restart/reload is performed by changelog preparation.

See [the v1.2 changelog evidence](docs/releases/v1.2/CHANGELOG_EVIDENCE.json) and [weapon-library integration notes](docs/releases/v1.2/WEAPON_LIBRARY.md). Detailed historical release notes follow below.

## Unreleased WIP - Weather2Compat particle-storm fix (2026-10-11)

- Upgrade the standalone Weather2Compat safety addon to 1.0.1. Skip non-rain/thunderstorm objects in both rain detection and precipitation/sky brightness checks, preventing sand/snow particle storms from causing ClassCastException. Preserve the original snapshot/thread-safety fix and Weather2 particle events.
- Regression tests passed with the actual addon helper and minimal storm fixtures; the old addon fails the same test. Both packaged call sites audited. Minecraft gameplay validation remains pending. Local testing instance only; next client launch loads the fix. No publication or server deployment.
- See docs/WEATHER2COMPAT_PARTICLE_STORM_FIX.md for rollback and evidence.

## Unreleased WIP - Better Combat trial (2026-10-10)

- Add Better Combat 2.4.0 for swords, axes and tridents; retain all original balance settings, including movement slowdown and dual-wield speed bonus.
- Add Apocalypse Combat Scope 1.0.1 to exclude spears, guns, the pneumatic hammer and other weapon classes, including explicit/synchronized preset assignments. Disable the spear-only duplicate Better Combat swing bridge; preserve native spear controls and animations.
- The restored scope 1.0.1 previously passed 667 isolated native-server assertions, including original trident melee profile, named-trident eligibility, charging in both hands and native projectile release. Client animation, Lunge and full-pack gunplay verification remain pending. Local WIP only; restart the client to activate. No publication or server deployment.
- Enable tridents on 2026-10-11 with the upstream one-handed stab profile; preserve throwing and all Better Combat balance settings.
- Revert Better Combat mace eligibility on 2026-10-11 for vanilla testing, restoring the previously verified scope 1.0.1. Retain Improved Mace Hammer, enchantment models and the mace-only The Zephyr rename.
- Add Improved Mace Hammer as the default vanilla mace appearance, with original Breach thorns and Wind Burst models. Rename only a vanilla mace to The Zephyr for the alternative model through the existing Polytone/CIT library. Client-only model selector; 700 isolated runtime checks passed, with rendering still pending client verification.
- See docs/BETTER_COMBAT.md for evidence, test checklist and rollback.


## v1.1.15 - 2026-10-10

- Add Backported Spears 1.8.0 and Apocalypse Echo Spears 1.0.8 for Minecraft 1.21.1 NeoForge.
- Echo Spear copies Netherite Spear behavior, durability, repairs and enchantability, with attack speed reduced by 0.1 and the Echo-template/Netherite-spear/Echo-ingot smithing recipe.
- Include working right-click charging, five-second Weakness/Darkness effects and the Echo Sword description/config behavior.
- Use chestplate-derived Echo spearhead colors, preserve bevel shading and the original brown shaft and outline. Fix base spear inventory icons.
- User confirmed gameplay, visuals and Super Forging/Lunge IV work. Prior isolated runtime checks passed 97 assertions; release manifests and deployed file hashes verified separately.
- Narrow client/server release; preserve unrelated configs and quests. Restart the client and server to load the new mods. Server restart remains user-managed.

## v1.1.14 - 2026-10-09

- Restore the vanilla copper block to nine copper ingots crafting recipe, allowing Sophisticated Storage compression to recognize the reversible ingot/block conversion.
- Remove only minecraft:copper_ingot from the recipe-removal list; preserve all other removals and copper-block blasting salvage.
- JavaScript syntax, recipe-removal behavior and Packwiz distribution hashes validated. In-game compression verification remains pending user activation.
- Narrow client/server hotfix; reload server recipes or restart to activate. Server activation remains user-managed.

## v1.1.13 - 2026-10-09

- Add Local Envelope Damage 1.0.0. Weather Physics envelope punctures now require a close airborne funnel encounter: 30-block envelope clearance, 5-block lower-ship clearance, and a maximum 32-block radius around actual funnel layers.
- Limit eligible opportunities to once per ship per 5 seconds, with up to 10% chance and quadratic distance falloff. A successful single-block puncture starts a shared 30-second cooldown; overlapping storms cannot multiply damage.
- Preserve upstream wind/tornado forces, non-envelope destruction and intentional server overrides. Use normal Aeronautics block removal for containment updates; no separate pressure/explosion mechanic is added.
- Passed 878 native dedicated-server assertions with actual Aeronautics envelopes and Sable ships, including Mixin application, parked/distant/vertical protection, overlap/cooldown checks, explicit block IDs and SABLE single punctures. Full-pack client and flight/pressure gameplay testing remain pending.
- Install on client and server; restart to activate. Server restart remains user-managed.



## v1.1.12 - 2026-10-09

- Guard 518 optional block loot tables with NeoForge item-existence conditions: 256 Design n' Decor, 246 Steam 'n' Rails and 16 Create Connected tables. Preserve each original table whenever its item is registered; skip missing optional items before parsing.
- Targets 1,036 repeated loot parsing messages in the reviewed server startup. No mods, recipes, gameplay costs, world data or server overrides are changed.
- Validate JSON, original loot-body preservation, published/source mod hashes and all Packwiz distribution hashes. Runtime error reduction and gameplay verification remain pending user activation.
- Client and server share this release. Upload only: the user manages restarts/reloads; report when activation is required without asking or interrupting the server.

## v1.1.11 - 2026-10-09

- Add the positional-wind prototype: Weather Physics 1.0.3 now samples Weather2 at each ship world position, using existing altitude scaling, local storm influence and caches.
- Prototype passed isolated dedicated-server loading and positional API checks; flight balance and full-pack gameplay remain unverified. Weather2 altitude boost and speed caps are preserved.
- Narrow upload only; server restart remains pending. Existing server configuration and quest overrides are preserved.

## v1.1.10 - 2026-10-08

- Update Weather Physics (Wind, Sails & Tornadoes) from 1.0.2 to 1.0.3 on client and server, pinned to CurseForge file 8468173.
- Fix ordinary wind slowing wheels and other vehicle propulsion. Upstream changes are limited to WindPhysics and the version metadata; tornado forces, lifetimes and all pack configuration remain unchanged.
- Verify JAR integrity, unchanged dependency requirements, matching installed Sable/Weather2/Aeronautics dependency hashes and Packwiz distribution hashes. Full-pack startup and gameplay testing of 1.0.3 remain pending.
- Server upload only: restart and activation remain pending at the pack author's request. Preserve existing server overrides and unrelated development work.

## v1.1.9 - 2026-10-06

- Add Create: Copyfoxes 2.0.0 (copyfoxies-2.0.0.jar) and Sable CleanUp 0.3.0 for Minecraft 1.21.1 / NeoForge on both client and server.
- Pin publisher downloads and SHA-256 hashes. Existing NeoForge 21.1.248, Create 6.0.10, Copycats+ 3.0.4 and Sable 2.0.3 satisfy their declared requirements; no dependency upgrades are required.
- Verify dedicated-server loading, Copyfoxies block/item registration and Sable CleanUp command permissions in an isolated disposable test. Full-pack gameplay and client GUI testing remain pending.
- Preserve unrelated client development work, server configuration/quest changes and server-only mods. Activation requires client/server restarts; server restart remains user-managed.


## v1.1.8 - 2026-10-04

- Add Survival Attachment Removal 1.0.0 for pinned NTGL 3.1.8. Survival and Adventure players can remove gun attachments by ordinary pickup or shift-click; Creative remains allowed and occupied-slot Spectator removal remains blocked.
- Split excess ammunition refunds into valid item stacks, fixing NTGL's full-inventory fallback that could create unsaveable oversized drops after removing a larger magazine. Ammo quantities, normal attachment storage and existing gun enchantments are preserved.
- Passed 1,501 native dedicated-server checks across 72 removal combinations, including save/parse roundtrips, repeated removal, incompatible swaps, full inventories and a 300-to-100-round Gatling drum refund. Tested alongside the existing Gunsmithing Enchantments addon. Full-pack/client gameplay testing remains pending.
- Required on both client and server; restart both to activate. Server restart remains user-managed. Unrelated configuration, quests and development work are preserved.


## v1.1.7 - 2026-10-03

- Add Dead's Displays 1.1.0, Items Displayed 2.0.10, and Armor Poser 6.2.4 for Minecraft 1.21.1 / NeoForge.
- All three are distributed to both clients and dedicated servers. No additional required libraries or custom configuration changes.
- Verified downloaded hashes, JAR integrity, embedded loader/dependency requirements, distinct mod IDs, and Packwiz manifests. Full-pack gameplay testing remains pending.
- Restart clients and the server to activate. Server restart is managed by the pack author. Existing configurations, quests and unrelated development work are preserved.


## v1.1.6 - 2026-10-03

- Add Gunsmithing Enchantments 1.1.0: Quick Hands, Trigger Finger, Lightweight and Sharpshooter, with Create: Enchantment Industry extra-level support. Piercing revolver and Gatling rounds ignore 65% of armor points while preserving toughness, their existing damage boost and penetration. Passed 589 native dedicated-server checks and 91 isolated enchantment checks; the prior 1.0.0 enchantment implementation was confirmed working by the pack author.
- Add Importer Safety Fix 1.0.0: prevent normal and Mechanical Importers from hanging the server when item manifests change; preserve valid selections and reject invalid or zero-progress purchases. Passed 56 native regression checks. The original Jack's Economy mod remains installed.
- Both final addons passed their 645 native assertions together in a disposable server and shut down cleanly. Full-pack gameplay testing remains pending.
- Both client and dedicated server require a restart. This release adds only these two runtime JARs and their source/docs; existing quests, recipes, configurations and unrelated WIP work are preserved.


- Published to GitHub main and v1.1.6 at 8868178f5dc2ad99954271e11fc4be56de87b6ca. Both addon JARs were uploaded to Chunkserve and downloaded back with matching hashes. Activation is pending the user-managed restart.


## Unreleased â€” queued after v1.1





- Protection Pixel integration 1.1.1: add Echo Resonance Plate to the Protection Pixel creative tab, making it discoverable by JEI with its normal hidden-item setting. Item ID, crafting recipe, texture and native plate behavior are unchanged. Requires a client restart. Pending the next GitHub distribution; not published.

## Apocalypse Industries v1.1 â€” 2026-09-23

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
