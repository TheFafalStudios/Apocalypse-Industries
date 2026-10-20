# Submarine pressure balance — installed WIP candidate, 2026-10-20

The user authorized implementing the revised proposal and including it in the next big update. WIP now contains `mods/apocalypse-submarine-balance-1.0.0.jar` and 487 water-rating corrections in `config/submarine_hull.json`. This supersedes the experimental 700/1,000/1,500 decorative tiers. Cracking probabilities, unrelated hull entries, world generation and recipes remain unchanged. No server deployment or version bump occurred.

| Material | Water rating/cap | Lava water-equivalent cap |
|---|---:|---:|
| Decorative metal, including IE and Locometal families | 100 | 80 |
| Steel construction | 150 | 120 |
| Netherite decoration / Nethersteel | 150 | 145 |
| Full vanilla netherite block | 225 | 170 |
| Pressure glass / viewports | 60 | 40 |
| Ordinary Deep Seas fittings | Existing weaker rating preserved | 120 |
| Otherwise unclassified hull materials | Existing rating | 80 |

All caps preserve already weaker ratings. Ordinary glass remains 28 in the tested registry. These are material ratings, before HYBRID shape, girder support and liquid-density calculations. HYBRID, density 3, shape range 0.5–2 and the deep-failure threshold remain unchanged.

The proposal's reference braced solid-netherite capsule still computes approximately 162.1 blocks of lava depth against the approximately 157–159-block floor. Its unbraced limit is approximately 113.3. Decorative netherite's absolute optimistic ceiling is 154.7; steel's is 128; pressure glass's is 42.7. A window at the top of an ordinary floor-going capsule still fails. The hydrostatic exception for an approximately 118-block optical tower remains; no ship-wide optics prohibition was added.

## Pressure jackets for endgame fittings

Sneak and right-click an eligible **opaque Deep Seas fitting** with a **full netherite block**. In survival this consumes one nine-ingot block and gives that fitting the full-block pressure material (225 water / 170 lava). A message confirms installation; repeating the action does not consume another block. Windows cannot receive jackets. Breaking the fitting loses the jacket; no refund or cheap reinforced item recipe is added.

Eligible block entities: ballast tank/vent, barometer, onboard computer, decompression chamber, electrolyzer, industrial alarm, oxygen diffuser, pump controller, sonar, submarine propeller and water thruster. A listed block must actually have a block entity to accept the upgrade. The upgrade affects pressure material only: it does not alter the fitting's fluid behavior, collision, airtightness, propulsion, oxygen supply or fire resistance.

The full-block cost also applies if players use reinforced fittings as hull panels, closing the cheap-port loophole. Ordinary exposed fittings remain weaker. Protected fittings inside a separate sealed pressure boundary remain another engineering option.

## Implementation and checks

The common-side addon pins Deep Seas 3.4.4 and Sable 2.0.3 and requires the installed Design n' Decor material family. Water caps apply through `HullStrengthConfig.getFor`. Fluid-aware lookups cover live damage, the material array used to choose cracking candidates, effective depth and weakest-hull diagnostics. Existing computer/alarm/autopilot consumers retain the upstream server pressure path. No separate client-only pressure formula was introduced.

Lava is identified using the same rounded world-space origin and real-fluid lookup as the upstream liquid column, without generating chunks. Materials and weakest-hull caches are discarded at each existing one-second pressure cycle; block edits also invalidate weakest-hull diagnostics. This trades more frequent material scanning for immediate recognition at the next pressure cycle. Performance with large fleets remains unmeasured.

Actual copied materials retain upstream resolution. Mixed Copycats use the weakest lava-tier ordering, including glass. Full netherite used as a copied covering is conservatively treated as decorative netherite, not a complete nine-ingot pressure block. Transparent coverage uses registry-name rules, native glass/translucent classes and the extensible block tag `apocalypse_submarine_balance:viewports`; the lava fallback prevents unclassified strong blocks from reaching the floor. Unusual third-party transparent materials and moving multipart copies still need full-pack gameplay coverage.

Validation completed:

- 1,363 executable policy/boundary checks and nine pinned bytecode/package checks passed.
- An isolated NeoForge loader loaded the actual Create/Aeronautics/Sable/Deep Seas/Design n' Decor stack and applied all eight pressure/material hooks successfully. No world started.
- Runtime water-rating checks passed, including preserved weak glass and the 60/150/225 progression. The expanded registry audit checked 92 recognized viewport/translucent states, all at or below 60.
- A ballast-vent block entity retained its pressure jacket through the real NeoForge save/load path. Pressure glass was rejected as an eligible fitting.
- WIP read-back, config/index/pack hashes and unchanged cracking probabilities passed.

This does not establish a working powered floor submarine. Right-click interaction in play, moving assembly persistence/client diagnostics, fluid transitions, lost girders, sonar navigation through lava, oxygen/access/recovery and large-vessel performance still need gameplay evaluation. The existing water-oriented machinery was not generalized to lava by this balance patch.

Artifact SHA-256: `e13af8e91c994b6d421588e3651927777fa23bb3ea72d886b2c5d71514cd561f`.

Sources/tools: `scripts/submarine_balance/`. Policy/bytecode evidence: `.local/submarine-balance-20261020/build-03/validation.json`. Native application: `native-03/balance-native-validation.json`; expanded registry and jacket persistence: `native-04/balance-native-validation.json`, under the same evidence root. Exact rollback and installation hashes: `.local/submarine-balance-20261020/install-143917-403697/installation.json`.

A full client relaunch is required to load the installed addon. Future twin/Chunkserve deployment must explicitly retain HYBRID and verify its effective file hash. Server activation remains user-managed; none was performed.
