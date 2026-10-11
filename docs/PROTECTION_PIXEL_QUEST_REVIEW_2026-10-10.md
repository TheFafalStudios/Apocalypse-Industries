# Protection Pixel quest review — 2026-10-10

Scope: quest usability and correctness. No native equipment balancing, recipes, rewards, prerequisites or completion conditions changed in this pass. No server restart/reload or deployment performed.

## Completed correction

The powered-loadout advancement task now explicitly uses the reactor icon. The Echo resonance advancement task and its quest node explicitly use the Echo Plate icon. Previously both hidden advancements lacked display icons, allowing FTB's wheat fallback. Existing task IDs and advancement requirements remain intact. The manifest and generator preserve these choices. 455 static checks pass, including explicit icon checks; regeneration makes no further changes. Rendering in an actual client has not been verified.

## Evidence and references

- Local source: config/ftbquests/quests/chapters/{brass_age,electric_age,space_age,special_requests}.snbt and lang/en_us.snbt, reconciled against docs/protection_pixel_quest_map.json.
- Installed FTB 2101.1.27 QuestObjectBase accepts an item-stack icon override before fallback; AdvancementTask checks actual advancement completion. No addon rebuild needed.
- [SteamPunk tutorial](https://github.com/LunaPixelStudios/SteamPunk/blob/main/config/ftbquests/quests/chapters/short_tutorial.snbt): named informational checkmarks and subject-specific icons. Useful separation of information from achievement, but its long introductory descriptions are not a style to copy here. This is the public SteamPunk repository, not a verified SteamPunk Lite release.
- [SteamPunk IE](https://github.com/LunaPixelStudios/SteamPunk/blob/main/config/ftbquests/quests/chapters/immersive_engineering.snbt): starts with its manual, then tools; screwdriver and wirecutter branch from the hammer. Borrow the concrete dependency relationships, not a quest for every PP item.
- [ATM10 armor](https://github.com/AllTheMods/ATM-10/blob/main/config/ftbquests/quests/chapters/basic_armor.snbt): separate optional armor-set acquisition tasks and supporting visual sections. Borrow visible distinctions between alternatives; do not import its full-set collecting requirement into PP's mix-and-match system.
- [FTB authoring documentation](https://docs.feed-the-beast.com/mod-docs/mods/suite/Quests/Developer/Quests/): tag-filter tasks support any-item choices; they are not empty custom goals.

PP's 18 descriptions have a median of 40 words, maximum 59. Other nonempty descriptions in the same three age chapters have a median of 25 words (63 quests). This is a rough word count including formatting tokens, not proof that every longer description is bad. Installation instructions can justify extra length; duplicated warnings and branch commentary cannot.

## Findings and proposed next refinements

| Quest(s) | Finding | Targeted refinement |
|---|---|---|
| Occupational Hazard / Steam Powered / Personal Protective Equipment | The powered-use goal depends on the platform quest, bypassing the reactor tutorial. Setup asks for both platform and hanger although ordinary reactor/plate work uses the platform. | First correct the powered-use dependency to follow Steam Powered. Consider separating hanger instruction from essential setup only if it reduces required work. |
| Bolt-On Protection / Active Protection System | Direct contradiction: the first says Float Shield has no ordinary chestplate plate slots; the second says to fit ordinary plates. Current pack tags include Float Shield as supported chest armor. | Remove the obsolete no-slots statement. This is a documentation correction, not a balance change. |
| Personal Protective Equipment / Reinforced Chassis / External Systems | Labels say Craft, but these are non-consuming item-possession/filter tasks. They do not distinguish crafting, trade, gifts or loot. Broad categories save clutter but poorly identify what players can choose. | Say Obtain; preserve the any-one-item checks. Add representative quest icons where the smart-filter icon is unhelpful, without making one specialist mandatory. |
| Light Industrial Frame / Powered Exoskeleton | Explains compatibility, but points between ages and contains lengthy fitting controls. | Keep the actual compatibility caveat and fitting directions. Put hanger-specific instructions at the point players need them, avoiding duplicate instructions. |
| Heavy Industry, Personal Scale | Combines magnet and Alloy Plate ownership into a gateway for upgrades, devices and exoskeletons; not every child actually requires that plate. | Review this gateway alone next. Prefer actual shared industrial access over an unrelated mandatory component. No recipe change needed. |
| Echo Resonance Plates / Four-Part Harmony | Acquisition followed by actual powered use is meaningful. The second goal really requires four worn, intact Echo plates and reactor power. | Preserve both milestones and real checks. Keep debugging commands secondary to normal player instructions. |
| R&D Department | Plate possession still adds another step after the aluminum milestone; primarily introduces three choices. | Consider a direct branch from aluminum or an informational introduction, rather than implying that a quest itself unlocks recipes. Keep the agreed Space placement. |
| Automatic Evasion / Experimental Kinetics | Evasion text does not quantify its wait; Lancer text presents caps as fixed bonuses. | Say up to 6/up to 9 for Lancer. Add a verified nominal recharge time only if it helps a loadout decision. Do not alter either mechanic. |
| Walking Industrial Accident | Manual checkmark after three actual possession quests. Not a fake equipment test, but it adds a claim step and pressures collection after promising player choice. | Consider removing the redundant checkmark/collection branch in a separate agreed pass. Do not substitute kill-count grind. |
| Both Special Requests | Genuine consuming delivery tasks, once per team. Their role is clearer than the collector's. | Preserve their explicit consumption warnings and reactor-emptying warning. Reward balance is outside this pass. |

## Recommended first refinement

Repair the onboarding sequence and factual wording before expanding the quest book: reactor tutorial before powered use; remove the contradictory Float Shield sentence; change ownership-task labels from Craft to Obtain. These changes improve clarity without changing equipment balance or introducing new grind. Other structural proposals remain pending rather than being applied in a large rewrite.

## First refinement completed — 2026-10-11

Personal Protective Equipment now follows Steam Powered. Removed the obsolete Float Shield no-plate-slots sentence. Changed the three possession/filter task labels from Craft to Obtain. Existing objective IDs, completion checks, rewards and explicit reactor/Echo icons are preserved. No equipment mechanics or recipes changed.

Validation: 455 static checks pass; quest generation is idempotent. Updated hashes for the changed Brass chapter and English text, plus the pack index hash. No deployment, restart/reload or in-game rendering test performed.

## Second refinement completed — 2026-10-11

Space equipment quest titles now name Float Shield, Evasion Wing and Lancer-AS; former titles remain as subtitles. Lancer wording identifies movement-dependent maximum bonuses rather than fixed damage. Evasion describes automatic charge use and its interface limitation without promising protection from every attack. Entry and collection descriptions are shorter. The five descriptions total 123 words, down from 173. Existing tasks, dependencies, rewards, coordinates, IDs, recipes and mechanics remain unchanged. 455 static checks pass; regeneration is idempotent. Only the English quest language file changed in the runtime payload. Updated its pack hash. No deployment/reload or in-game visual validation.
