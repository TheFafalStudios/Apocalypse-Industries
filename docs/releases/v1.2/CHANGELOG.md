# Apocalypse Industries â€” Pending v1.2

Cumulative release notes: includes the v1.1.1â€“v1.1.15 updates already released, plus the local changes queued for v1.2. Minecraft remains 1.21.1 and NeoForge remains 21.1.248. This is a draft changelog, not publication or deployment confirmation.

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

### Queued local changes for v1.2

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

