# Deep Seas Overworld and lava-sea balance proposal — 2026-10-19

Implementation follow-up, 2026-10-20: the user authorized installing this proposal and including it in the next update. See `SUBMARINE_PRESSURE_BALANCE_2026-10-20.md` for the implemented profile, costly opaque fitting jackets, validation and remaining gameplay checks. The analysis below is retained as the original design evidence; its "not installed" statements describe that original investigation.

## Recommendation

Use distinct water and lava pressure ratings. Restore a sensible material progression in water, and make the lowest approximately 20 blocks of the lava ocean an expensive engineering challenge. Keep HYBRID, lava density 3, shape cap 2 and the 120-block deep-failure threshold. Do not make the bottom a hard forbidden depth or rely on random cracking to create difficulty.

Confirmed navigation constraint added after user review: the best/deepest submarines must have opaque pressure hulls and rely on sonar/radar. Pressure glass is a weaker viewport material, not a superior hull or a future transparent endgame tier. The updated calculations below supersede the earlier proposal to retain 700-rated water glass.

This is a completed analysis and proposed balance profile, not an installed change. Active mods, hull JSON, TOML settings, recipes, pack index and worlds were not modified. The mathematical pressure capsule is not a validated working submarine. The proposed fluid-specific caps require compatibility code; stock `submarine_hull.json` has one global rating per block, not separate water/lava ratings.

## How Deep Seas is intended to work

The author's [3.0 pressure progression](https://modrinth.com/mod/create-deep-seas/version/3.0.0) puts normal glass around 20–30 blocks, wood around 40, stone around 60–80, iron around 100, obsidian around 500, and pressurized glass around 700. The 700 glass rating was explicitly described as suitable for the Abyss. Those are material reference ratings, not guaranteed depths for every 3.4 HYBRID hull. The [author's project README](https://github.com/MaxCreateMC/Create-Deep-Seas) describes the Abyss as a separate future/development biome/dimension; ordinary Overworld sailing is not inherently a 700-block dive.

The [3.4.1 changelog](https://www.curseforge.com/minecraft/mc-mods/create-deep-seas/files/9231952) also discusses an approximately 200 m iron hull under REALISTIC. That does not supersede the HYBRID formula used here: the pressure modes differ. Old public GitHub source is not authoritative for installed 3.4.4; installed bytecode/configs were used for numerical conclusions.

In the active HYBRID model:

`block depth limit = water-equivalent material rating × shape factor × girder factor / fluid density`

Shape is clamped to 0.5..2.0. Small/thick walls can reach 2 without a complicated rounded hull. Girder bonuses depend on distance to the supported hull endpoints, not merely proximity to any girder: `G = 1 + max[0.6 × (1 − distance/5)]` within four blocks. Bonuses do not stack. A recognized support is a straight contiguous girder run between two nonpermeable, nongirder blocks. Its endpoints are those supporting hull blocks. The theoretical maximum is 1.6, but most skin blocks get less. A collapsed support can weaken the nearby hull.

Pressure damage checks block positions individually. A strong hull with weak exterior fittings can still fail. The 120 setting controls the dangerous deep-failure regime; it is not a universal maximum operating depth. At the lava floor, failure can cause destructive implosion. Warning/strain behavior is a useful margin indication, not an exact probability of surviving a dive.

Current WIP enables Hadal Depths/Abyssal Ocean 2.1.7 as a separate Overworld ocean overhaul. Its floor is configured six blocks above the dimension minimum, with relief; its pressure rules explicitly recognize sealed Deep Seas shelter. Deep Seas' own `enableDeeperOceans` is false. With standard Overworld Y−64 minimum and sea level 63, a floor around Y−58..−54 implies roughly 117–121 blocks, not 700. This is a configuration-derived example, not a measurement of any saved world's dimension or floor. No world was opened to verify existing terrain.

## Why the present balance is too strong

Our lava surface is Y100 (highest lava block Y99); the basalt seabed top is Y−61..−59. The lowest full hull block immediately above it sits around Y−60..−58. Damage therefore sees approximately 157–159 blocks below Y99. Use 160 as a conservative design target and approximately 165 for useful recovery clearance, rather than treating all coordinate conventions as exactly the same depth.

At density 3 the bottom requires approximately 480 water-equivalent strength. Current base ratings are 700 for decorative metal, 1,000 for steel and 1,500 for netherite families. Even before bonuses, they support approximately 233, 333 and 500 blocks of lava. With the actual factor-2 small-shell geometry, these become approximately 467, 667 and 1,000. Thus our 160-block sea is already shallower than their ordinary limits. The 400 global automatic-rating cap does not clamp explicit version-3 JSON overrides.

Changing only those three families is insufficient. Stock pressurized glass remains 700, wet fittings can be 400, and other materials can become substitute hulls. A reliable balance needs a lava-wide fallback plus explicit material exceptions. Conversely, lowering the global multiplier or global glass rating also weakens Overworld submarines and any intended Abyss progression.

## Cost loophole: decorative netherite is not a solid netherite block

Installed Design n’ Decor recipes stonecut one netherite ingot into two sheet-metal blocks, two floors, or two decorative boilers. A solid Minecraft netherite block costs nine ingots. Giving them identical strength makes decorative sheet metal 18 times cheaper per block at the same pressure rating. No explicit overrides for these recipe IDs/materials were found in the scoped KubeJS search; the quoted conversion is the installed jar recipe, not a runtime recipe dump.

Installed Create Big Cannons also alloys one netherite scrap with four steel ingots into eight Nethersteel ingots; an alternate recipe uses eight cast-iron ingots. A Nethersteel block is therefore not economically equivalent to a netherite block. Keep its name/flavor in the high tier, but do not make it the cheapest bottom-proof pressure shell.

## Proposed lava material progression

These are water-equivalent *lava caps*, applied before the existing density/shape/girder calculation. They are proposed pack values, not upstream defaults. Preserve already-weaker block ratings. The fallback covers otherwise-unclassified strong blocks; copied materials must resolve to their actual material rather than the copycat frame.

| Material family | Proposed lava cap | Shape 1, no bracing | Shape 2, no bracing | Shape 2, girder 1.4 | Absolute optimistic ceiling (shape 2, girder 1.6) |
|---|---:|---:|---:|---:|---:|
| Ordinary decorative metal / fallback | 80 | 26.7 | 53.3 | 74.7 | 85.3 |
| Steel construction | 120 | 40.0 | 80.0 | 112.0 | 128.0 |
| Pressurized glass / external viewports | 40 | 13.3 | 26.7 | 37.3 | 42.7 |
| Ordinary wet fittings | 120 | 40.0 | 80.0 | 112.0 | 128.0 |
| Netherite sheet/plating and Nethersteel | 145 | 48.3 | 96.7 | 135.3 | 154.7 |
| Solid netherite pressure shell | 170 | 56.7 | 113.3 | 158.7 | 181.3 |

Metal tiers create useful operating bands. Cheap netherite plating can approach the bottom but its absolute ceiling stays below the 157–159-block floor. Only the costly solid-netherite tier has enough headroom for a genuine floor attempt. Windows are intentionally restricted to shallow lava exploration; an all-glass hull is inferior to every metal pressure tier.

## Window calculations and instrument navigation

Proposed pressure-glass rating is **60 in water and 40 water-equivalent in lava**, preserving any existing weaker glass value. Even at the algebraic maximum shape/girder product 3.2, this allows only 192 blocks of water or 42.7 blocks of lava. Under ordinary shape 1 without braces it allows 60 water or 13.3 lava. The lower lava cap represents a pack balance/thermal derating policy, not a claim about the physical strength of real pressure glass.

In the floor-going 5×5×5 capsule, a window placed at the very top is only four blocks above its lowest hull block. At the conservative 160 bottom-depth target, that window is still 156 blocks deep. It would require a lava rating of at least `156 × 3 / 3.2 = 146.25` even with impossible-to-improve bonuses; the proposed rating is 40. The top-of-hull placement does not save it. Its best-case depth is less than one-third of the required depth. The opaque 170-rated reference shell retains its calculated 162.1 limit; a transparent replacement invalidates it.

Use the computer's sonar, depth and floor-clearance readouts, plus appropriate radar/contact instruments, for blind navigation. Deep Seas sonar is the existing underwater/floor-mapping tool; this proposal does not add a radar capability or claim a particular radar mod already tracks terrain through lava. Instrument functionality in lava remains a gameplay check. No higher-rated transparent hull is proposed.

All transparent airtight boundary materials need the same policy: normal, stained, tinted, framed and other modded windows, including copied glass materials. The generated profile includes name-based viewport candidates for review; names alone do not establish transparency or airtightness. Interior glass behind an opaque exterior shell does not give an outside view and need not be treated as a loaded viewport.

Local pressure permits a technical exception: at bottom-depth 160, an optical window would need to sit at least 117.3 blocks above the lowest hull block even under maximum bonuses. A roughly 118-block tower/periscope is not ruled out by pressure mathematics. This is an explicit limitation of material ratings. If the intended rule is absolutely no optical window anywhere on a bottom-going connected vessel, apply a separate compartment/ship maximum-depth rule to external viewports and mirror it in diagnostics. Such a rule would intentionally differ from local hydrostatic pressure and remains unimplemented. Conventional-size bottom-going subs already fail the window test by a wide margin.

The 181.3 figure is an algebraic ceiling, not an advertised operating depth. At depth 160 a 170-rated block needs `shape × girder >= 480/170 = 2.8235`. Even at shape 2 this needs girder factor at least 1.4118, meaning a valid support endpoint within approximately 1.57 blocks. Many real panels will miss that condition. Without bracing, even a maximally compact shell stops at approximately 113.

No flat random damage percentage is recommended. Below every actual block limit the pressure system does not need to gamble with the ship; overload already brings failure. Make reaching the limit expensive and difficult to build correctly. A surviving full-pressure hull will still be safe at a stationary depth within its calculated limits—pressure alone cannot make every minute at the bottom inherently lethal without an additional wear/heat mechanic.

## A constructive check that the bottom is still possible

The isolated Java harness calls **installed 3.4.4** `SolverCore` and `HullShapeFactor.compute` for closed rectangular shells. It mirrors the installed `GirderSupport.scan` endpoint rules and distance formula for a specific bracing layout. It does not instantiate a Minecraft level or exercise live scanning/caches.

The test pressure capsule is a 5×5×5 outer cube, one-block shell, 3×3×3 gross internal space:

- 98 full netherite hull blocks.
- Four three-block vertical girder posts at inner X/Z corners (1 or 3), Y1..3.
- A horizontal interior perimeter girder ring at Y2, totaling 16 girder blocks including the posts.
- Eight valid straight support runs, anchored in the hull.
- Actual computed minimum shape factor: 2.0.
- Geometric minimum girder factor: 1.430294373.
- Conservative pressure depth: `170 × 2 × 1.430294373 / 3 = 162.1000`.

The same unbraced capsule holds only to 113.3. This demonstrates a pressure-feasible window of approximately 3–5 blocks beyond the actual floor depth; tilting, lower protrusions, invalidated supports or changing a loaded hull block can consume that margin. It leaves a cramped central standing column rather than the useful space of a normal 3×3 cabin.

The shell alone costs 882 netherite ingots using ordinary full blocks, before equipment. The decorative-sheet equivalent would cost only 49, which is why that cheaper material must stop below the floor. An eventual expensive pressure-plate recipe could offer a more reasonable endgame construction option, but its cost is a separate design choice; none was changed or invented as an installed recipe here.

This is a pressure test piece, **not proof of a working floor submarine**. Oxygen supply, propulsion, power, ballast, control, access and recovery still need a physical layout and gameplay test. A cable-lowered bathyscaphe is a possible direction for the capsule; it too remains untested.

## Fittings and the route to an operational bottom vessel

Ordinary wet fittings at cap 120 cannot simply be placed on the lowest pressure boundary. Glass at cap 40 is weaker still and is excluded from the floor vessel. An all-netherite shell with an ordinary exposed vent will lose its floor rating. Closing/burying the weak fitting inside a real sealed boundary may work where the mechanic allows it; otherwise add an explicitly expensive opaque netherite-reinforced port/hatch variant in the 170 tier. Simply promoting the existing cheap vent to 170 would let players build the whole hull from vents and bypass material progression. Transparent deep-rated ports are not part of this design.

For this reason the proposed cap file is not ready to install in isolation as a complete playable solution. The implementation must include a verified protected-fitting strategy or a gated deep-rated fitting before claiming a practical bottom-capable powered submarine. The small pressure capsule proves the mathematical limit is not a hard prohibition; it does not resolve that engineering work.

Surface boats and early shallow dives should remain useful. Debris origins are Y−44..64, with the best yields near the deep end; the deepest possible casing is Y−49. That corresponds to roughly 143 blocks to the deepest origins and about 148 to their casing, before clearance. Steel therefore supports modest shallow/medium exploration; well-engineered netherite sheet submarines can access deep deposits, while the floor stays beyond their limit. Netheritic reinforcement is rewarded before the exceptional full-block floor vessel. Deposit positions and yields are not changed by this proposal.

## Overworld companion balance

I recommend separately bringing our global decorative metal back toward a 100 rating, steel around 150, solid netherite around 225 and cheap netherite decorative panels around 150, with **pressure glass/viewports capped at 60 in water**, preserving already-weaker glass. These are proposed pack tiers, not canonical upstream steel/netherite values. Glass is below metal, and the best water submarines likewise sacrifice external viewports for instruments. The previously retained upstream 700 water-glass value is withdrawn from this proposal. Preserve intentional lower values and unrelated materials instead of globally regenerating the hull table. Obsidian's current WIP value is 100 despite an installed upstream default of 500; do not silently replace this existing configuration decision as part of the proposal.

A fluid-specific layer lets the same vessel behave appropriately in water and lava. It also avoids pretending that lava is 25–30 times denser than water merely to counteract our present high ratings. Such a density-only change can squeeze the best current materials toward the floor, but leaves decorative metal at 700 in the Overworld and squashes ordinary lava exploration; it does not fix the underlying material/cost progression.

## Implementation and validation boundaries

The proposal file is `.local/lava-balance-20261019/proposed-lava-material-caps.json`. It is a design artifact, **not a Deep Seas config file**. Numerical results and input hashes are in `analysis.json`; shape/support outputs in `geometry.csv`; reproducible harness source is `BalanceHullMath.java`.

A future implementation must apply the same fluid-aware rating to live damage, material caches, effective-depth diagnostics, computer/autopilot limits, alarms and client display. Cache changes must follow fluid transitions and edits. Keep the global pressure model HYBRID. Do not alter the dimension, ocean depth, world data or save files to force the difficulty. Keep sealed-cabin and outside-cabin damage rules distinct; no blanket lava/fire immunity.

Minimum gameplay evaluation: each tier reaches its intended band; glass and cheap decorative metal cannot substitute for full netherite at the floor; reference reinforcement is recognized in game; exposed and protected fittings behave correctly; a complete vessel has controlled descent and recovery; losing a support reduces the rating; water regression checks preserve intended behavior. Check glass at the top as well as the bottom of the hull, Copycats materials and modded transparent panels, plus instrument-only floor navigation. Finite geometry tests cannot establish an exploit-free balance or a statistical player success rate. Successful players can eventually standardize a good blueprint; keeping the floor perpetually rare would require additional progression, heat/wear, or terrain variability beyond static pressure ratings.

No current settings were changed. No restart, reload, server deployment, world/schematic read or gameplay test was performed.
