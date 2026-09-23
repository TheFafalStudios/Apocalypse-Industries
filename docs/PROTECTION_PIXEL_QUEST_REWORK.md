# Protection Pixel quest rework - 2026-09-20

Implements approved review points 1, 2, 3, 5, 6 and 7. Plate statistics, recipes, age gates and layout are unchanged.

## Changes

- Rewrote all 19 descriptions for players and added 19 subtitles.
- Added brief equipment-role examples, with detailed choices left to item tooltips.
- Added practical platform, reactor, Motor Interface, Exoskeleton and Echo service instructions.
- First armor quest retains its original crafting task and adds a server-awarded powered-use task: at least one PP armor item in its proper slot and a valid active reactor. Inventory-only, foreign armor, wrong-slot and stale-power states do not qualify.
- Echo crafting milestone requires the first plate, allowing sequential installation. Four-Part Harmony still requires four worn Echo sockets and power.
- Four new nonrepeatable team rewards: setup = one lava bucket; reactor = one empty water tank; working loadout = two Iron Armor Plates; resonance = one empty water tank. No upgraded equipment is rewarded.
- Existing optional collection award and delivery contracts retain their requirements and cash rewards.
- Current live IDs, prerequisites, chapter placement, coordinates and unrelated content were preserved. The stale Brass quest map was synchronized from live files before editing.
- Existing quest progress is not reset. The added advancement task will be detected when a player next wears powered equipment; no historical equipment use is inferred.
- Installed the user-selected Echo Plate PNG unchanged, preserving its bolts and face details rather than the earlier reduced export. Source image hash: E70AB0AC0D63E8A1A1B8BF821501C824351F5AF2762224F5E26897A3FD5BBFFD.

## Assumptions and remaining checks

- Rewards are deliberately modest and once per team; ordinary FTB team progress sharing is retained.
- The use milestone verifies operational equipment, not sufficient armor protection or enjoyable combat balance.
- Instructions were checked against installed PP 2.2.1 item descriptions and matching decompiled procedures. Native UI operation and multiple real players remain untested.
- Existing saved completion/reward state is retained; behavior of newly added tasks on an already-completed quest needs a real existing-player session check.
- Current runtime addon filename/version remains 1.0.0, as in the existing local development workflow. Clients and server must use the rebuilt matching file; a future public release should version the addon.

## Quest text

The descriptions below replace the overlong first rewrite. Measurements are recorded in protection_pixel_description_lengths.json.

### Occupational Hazard

Power armor: start here

Management approved putting the machinery on the employee.

Pipe lava into the Armor Load Platform. Wear your armor, stand on it and press Armor Load Platform (Right Shift by default). Installing or removing ordinary plates costs 100 mB each.

The hanger stores armor and fits Exoskeletons. Continue with Steam Powered to prepare your reactor.

### Steam Powered

Prepare, install and refuel your reactor

Fill a Water Tank with 1,000 mB water. Right-click the reactor in your hand and insert the tank and a Flare Rod.

On the platform, open the menu and put the reactor in the Power Interface. Remove it there to replace spent supplies.

Extra rods increase carrying capacity. If abilities stop, check water and fuel; /ppresonance reports power status.

### Personal Protective Equipment

Choose a specialist and power it up

Choose any Brass specialist: Pioneer helps mining, Night Demon helps exploration, and Lancer rewards moving melee attacks. Other choices are described in their tooltips; a matching suit is unnecessary.

Wear a Protection Pixel piece with a working reactor to complete the powered-use task. Add armor plates before difficult combat.

### Bolt-On Protection

Fit protection without overloading yourself

Iron plates are light; Brass offers more protection but weighs more. Alloy later reduces that weight.

Wear your armor and open its connection in the lava-fed platform menu to fit plates. If movement becomes sluggish, reduce weight or increase reactor capacity.

Float Shield has no ordinary chestplate plate slots.

### Light Industrial Frame

Optional alternative: a lighter frame

Link-Plate trades armor protection for a lighter, tough frame.

Unlike most powered chestplates, it supports the Steam Exoskeleton introduced in Electric Age. Your leggings must also be compatible.

### Heavy Industry, Personal Scale

Electric workshop: lighter plates and upgrades

Power Grid magnets unlock Alloy plates and heat-overlocking mechanisms. Alloy protects as well as Brass at lower weight.

Choose armor upgrades, external equipment or an Exoskeleton. Check the seven-loop mechanism recipe before setting up production.

### Reinforced Chassis

Upgrade the piece you actually use

Upgrade the specialist you actually use through its Create assembly recipe.

Remove Echo Plates first. Do not assume crafting preserves installed plates, enchantments or other additions; check the output before upgrading valuable armor.

Lancer-AS belongs to the separate lunar branch.

### External Systems

Choose one device for your Motor Interface

Choose one device: wings or thrusters for travel, Steam Booster for carrying capacity, oxygen supply for diving, or a launcher for combat. Check tooltips for controls and ammunition.

The platform menu has one Motor Interface slot. Remove the previous device before installing another, and keep the reactor fueled.

### Powered Exoskeleton

Choose upper- or lower-body assistance

Right-click the Exoskeleton in your hand to choose upper-body mining/combat support or lower-body movement support. Both need reactor power.

Place it on an empty hanger, then sneak-right-click empty-handed to equip. Right-click an empty hanger empty-handed to remove it; click again to retrieve it.

Most powered chestplates and leggings are incompatible; Link-Plate and Float Shield are exceptions.

### Echo Resonance Plates

Craft and install one plate in each armor slot

Craft one Echo Plate to begin; four are needed for resonance. No finished Echo armor is consumed.

At the platform, hold a plate in your main hand and one armor piece in your offhand, then right-click. Each piece has one separate Echo socket, including boots and Float Shield.

To recover it, sneak-right-click with armor offhand and main hand empty. Remove plates before upgrading armor.

### Four-Part Harmony

Activate all four Echo sockets

Wear an Echo-plated piece in all four armor slots and power the reactor to gain Resistance II and Strength II.

Completion is automatic. Use /ppresonance to check missing sockets or power. Losing either requirement ends the supplied buffs within one second.

### R&D Department

Space workshop: three optional experiments

Moon Regolith unlocks three optional projects: Float Shield, Evasion Wing and Lancer-AS.

Choose what suits your loadout. Building all three is only necessary for the separate collection award.

### Active Protection System

Rechargeable protection between attacks

Float Shield stores 25 shield points and restores roughly one every 1.25 seconds while powered. Repeated hits can overwhelm it.

Its base armor is weak and it has no ordinary plate slots, but it accepts an Echo Plate. Use cover to recharge.

### Automatic Evasion

A charged escape from direct attacks

Evasion Wing spends a charge to avoid a direct attack, then recharges. Upgrades shorten the wait.

Install it in the Motor Interface and keep the reactor powered. It replaces your other device and does not provide ordinary flight.

### Experimental Kinetics

Lunar specialist: stronger moving attacks

Lancer-AS raises the movement-based attack bonus from 6 to 9 damage while powered.

Choose it for mobile melee combat. Recover any Echo Plate before upgrading, then install it on the new helmet.

### Walking Industrial Accident

Optional collection award: all three experiments

An optional collection award for obtaining Float Shield, Evasion Wing and Lancer-AS. They do not need to be equipped together.

Management offers $1,000 per team. Storage space for the prototypes remains your problem.

### REQUEST: Workplace Safety Initiative

Delivery contract: basic replacement supplies

&lRequest&r

"Employee retention has been reclassified as a hardware problem. Send replacement plates and tanks."

- Personnel Department

Submitted supplies are consumed. One $1,000 payment per team.

### REQUEST: Executive Protection

Delivery contract: management gets the good equipment

&lRequest&r

"Management has approved a separate protection budget for management. Send Alloy plates, tanks and reactors."

- Executive Office

Empty reactors before submitting. Supplies are consumed; one $8,000 payment per team.

## Validation results
- 449 static checks passed; repeat generation makes no changes.
- Native FTB load/save/reload passed, including exact descriptions, subtitles, task labels and all 55 stable object IDs.
- All 19 native-saved task/reward definitions and nonrepeatable settings matched.
- 145 addon assertions passed, including seven new powered-loadout positive/negative checks.
- Original quest IDs, prerequisites, coordinates, chapter settings and unrelated translations were checked against the pre-edit backup.
- The final addon has exactly the tested class files; only the selected texture changed after the test copy. Its PNG matches the supplied image byte-for-byte.
- No live client UI or real multiplayer session was used. Restart Minecraft to load the rebuilt addon.


## Concision correction
The original rewrite was too long: median 98 words and a 340-word equipment quest. Existing descriptions in Brass/Electric/Space have medians of 37/24/16 words. All 19 PP descriptions were shortened to 27-64 words (median 40), with the longer entries reserved for essential installation instructions. Only description text changed; titles, subtitles, objectives, rewards, prerequisites and layout are unchanged. Exact before/after counts are in protection_pixel_description_lengths.json.

Concision validation: 449 static checks, 476 native FTB load/save/reload checks, and 145 addon assertions passed.


## Lancer quest removal
Removed the dedicated Brass quest Kinetic Doctrine at the user's request. There are now 18 PP quests. Removed its active translations and generator entries; no other quest depended on it. The helmet and its recipes are unchanged.
