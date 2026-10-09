# Apocalypse Positional Wind prototype

Makes Weather Physics 1.0.3 use Weather2's existing height-aware wind system for ship forces. No replacement atmospheric model or adjustable multiplier is introduced.

## Implementation

One required Mixin redirect replaces the single `WindManager.getWindSpeed()` invocation inside `WindPhysics.processGlobalWind` with `getWindSpeed(BlockPos.containing(ship.logicalPose().position()))`. This is the same world-space origin already used by Weather Physics for wind direction, not Sable's storage coordinates. Vanilla BlockPos flooring handles negative coordinates correctly.

The existing wind direction, obstruction checks, sail calculations, mass handling, force multiplier and tornado-force path remain upstream. Local storm influence and caps now come from Weather2's positional API. No timers, packets, per-sail sampling, global wind mutation or additional cache are introduced. Injection requires exactly one call site; incompatible changes fail at load rather than silently doing nothing.

Pinned dependencies: Minecraft 1.21.1, Weather2 1.21.0-2.8.7, Weather Physics 1.0.3, Sable 2.0.3; built against NeoForge 21.1.248 and Java 21. This is common/server logic, including single-player's integrated server. Dedicated-server loading was tested; integrated-server play and remote-client compatibility were not.

## Research and balance implications

Installed JAR bytecode and decompiled WindManager were inspected, rather than assuming the global and positional APIs were interchangeable. The bridge intentionally follows the stock positional implementation, including its quirks:

- Height is relative to a cached regional surface average, not sea level or the block directly below the ship. Weather2 samples 25 surface heights, spaced three chunks apart, across roughly 192 blocks.
- Above that average, its amplifier rises linearly toward the dimension ceiling. It does not reduce speed below the regional surface average or model shelter; Weather Physics still performs its own obstruction check.
- Once the amplifier exceeds 1.3, Weather2 adds an extra `amplifier - 1` to speed. This creates a discontinuity and can produce wind aloft even when global wind is zero. This bridge preserves that behavior.
- Normal positional speed caps at 1; locally storm-dominated wind caps at 2. Consequently this can both increase speed aloft and reduce a strong positionless gust. These are Weather2 units, not metres per second.
- Nearby high-wind storms are sampled within 512 blocks. The upstream event cache is horizontal-chunk based, so ships at different heights in the same chunk share the event sample until refresh.
- The client audio prototype uses Weather2's client cached wind at the player. This bridge samples ship position on the server. They share the stock system, but are not guaranteed numerically identical due to position and cache timing.

For illustration, stock global wind 0.2 over average terrain Y64 produces approximately 0.2 at Y64, 0.25 at Y128, 0.561 at Y141, 0.8 at Y192 and 1 at Y256 (no local storm or gust). This is stock game behavior, not a physical wind-profile claim.

## Performance

One BlockPos allocation and positional query per existing ship-wind evaluation. The expensive regional-height and storm work reuses Weather2's chunk caches: terrain normally refreshes after 6000 game ticks, storm samples after 100. A cache miss/refresh performs up to 25 heightmap queries, which can request unloaded chunks. The upstream cache is not bounded, and its time-zero sentinel can cause repeated initial sampling at game time zero. Flying into fresh areas therefore deserves profiling. This is a minimal integration, not a claim of zero cost or a measured performance improvement.

## Validation and status

- Compiled against the installed, hash-checked Weather2 and Weather Physics builds.
- Fresh disposable dedicated server loaded Create, Sable, CoroUtil, Weather2, Weather Physics and the prototype successfully, then shut down cleanly.
- Reflection confirmed the injected handler; exported transformed bytecode confirmed `processGlobalWind` invokes it and its only wind query takes a BlockPos.
- The actual Weather2 API returned global 0.2, ground 0.2 at Y-63 and high-altitude 1.0 at Y250 on a one-layer flat test world.
- Startup emitted client-class distribution diagnostics from the dependency stack but continued successfully. The test does not establish a warning-free full pack.
- Ship motion, storms, obstruction in flight, full-pack interactions and performance under many moving ships still require gameplay testing.

Initial prototype validation did not install or deploy the addon. Pack v1.1.11 publishes this prototype for client and server use; deployment and activation evidence lives in the separate release record. Production worlds were not accessed. The original prototype is packaged under `Q:/GPT General/wind-height-prototype-0.1.0/`.

## Rebuild and test

Run `build.py --output <new directory outside the instance>` using Python 3.12; the build locates this instance's Java and libraries. `native_test.py --output <build output>` creates a fresh isolated test server under that directory, links only dependency libraries, copies an explicit mod allowlist and generates a disposable world. Never point it at an existing game/server directory. Reruns use a new build directory.

For manual evaluation, use a disposable copy of the pack with this JAR, compare identical ships at several altitudes under fixed wind, and examine cold-cache movement before deciding on deployment. Removing this addon restores Weather Physics's original sampling; it registers no blocks, items or persistent world data.
