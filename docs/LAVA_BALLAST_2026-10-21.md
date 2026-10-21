# Lava ballast — installed WIP candidate, 2026-10-21

User authorization: fix the lava-only gaps identified in ballast, then explicitly **leave propulsion out**. Installed `mods/apocalypse-submarine-lava-ballast-1.0.0.jar` alongside the pressure-balance and lava-visuals addons. This common-side addon pins Deep Seas 3.4.4 and Sable 2.0.3. No upstream JAR, recipes, pressure settings, user worlds, client/server version markers or live server files were changed.

## Behavior

- Ballast tanks recognize water and lava at their transformed world position and below it; the existing surface scan, speed target, force limits, righting and keel behavior are retained.
- Vents recognize submerged open faces in either liquid. Their ocean capability supplies the actual surrounding source fluid, including source normalization for flowing liquid. Dry or fully closed vents cannot supply liquid. Existing infinite-ocean capability semantics and normal pipe/vent rates are retained.
- Cluster capabilities report and drain the stored fluid instead of manufacturing a water stack. Incoming water cannot mix with stored lava, and vice versa. **Empty water-filled tanks before taking in lava.** Existing stored water still acts as ballast while the vessel is in lava; entering another liquid never silently converts tank contents.
- Existing mixed clusters remain recoverable: one real fluid is reported/drained at a time, preserving the other tanks' contents. Unsupported old contents are not deleted or overwritten.
- Neighbour equalization simulates acceptance before transfer and refunds any unexpected remainder. A mismatched or full neighbour does not destroy the source fluid.
- Ordinary floaters use the same supported-liquid checks, and their surface cache resets on medium changes. Their existing lift and pressure/burst limits remain; this patch does not make weak floaters suitable for the deepest dives.
- Pressure, hull ratings, oxygen/electrolyzers, fire safety, propellers, water thrusters and surface-boat behavior are unchanged. A vent's lava supply does not become water or oxygen. Propulsion remains excluded at the user's request.

The ballast tooltip now says “Ballast fluid”. Water bubble effects remain on the water path; lava intake does not display water bubbles. No client renderer or shader dependency was added.

## Validation

The actual reduced NeoForge/Create/Aeronautics/Sable/Deep Seas/Design n' Decor stack, including the existing pressure-balance addon, applied the new mixins successfully. **304 runtime assertions** covered capacity boundaries, simulate/execute behavior, actual fluid identity, rejected mixing, wrong-fluid requests, dry vents, cross-medium discharge, legacy mixed clusters, native tank save/load, pipe-rate preservation and water/lava/air force predicates. Native floaters' guards were exercised; the assembled movement fixture below directly tests tanks and vents.

Fresh disposable worlds then assembled two small, real Sable structures with a tank, vent and solid pressure-block support. They were evaluated in matching water and lava pools. A matched negative control used identical production classes with only the new addon's mixin registration disabled. The harness hash, all effective server-property lines and both Deep Seas config files matched. Minecraft's timestamp comment was the only server.properties difference; both raw hashes are retained.

| Measurement | Original water | Patched water | Original lava | Patched lava |
|---|---:|---:|---:|---:|
| Initial vertical velocity with empty ballast | +2.0 | +2.0 | −2.1245 | +2.0 |
| Full-tank commanded vertical force | −40.0 | −40.0 | 0.0 | −40.0 |
| Wet vent's supplied fluid | Water | Water | None | Lava |

The empty patched fixture had already reached the configured ascent speed, so its additional empty-tank force was zero. Filling it produced the opposite downward command. The patched assembled vent/cluster path also passed actual-fluid intake and drain conservation checks. This proves the intended controller/capability response in these fixtures, not a complete piloted vessel, floor expedition or long-duration performance result.

Production artifact SHA-256: `ac2090061afde70e0132987c2380740289f03a0d83930f7b038a31423ab33f1e`.

Evidence under `.local/lava-ballast-20261021/`:

- `build-02/validation.json`: runtime and packaging checks; no propulsion, boat or client-only targets/references; original Deep Seas hash unchanged.
- `world-candidate-10/`: final enabled-patch fixture and 304 assertions.
- `world-baseline-01/`: matched disabled-patch negative control.
- `comparison-01.json`: exact hashes, input normalization, results and limitations.
- `install-204224-391546/installation.json`: WIP installation hashes and rollback copies of index/pack metadata.

Earlier world-candidate probes were rejected while developing the fixture. Resetting Sable's mass tracker after assembly invalidated its structures; removing that reset and waiting for registration fixed the setup. The final expectation also accounts for a controller already at ascent speed. These were test changes, not production fixes or evidence of gameplay failure. Only the matched final runs support the table above.

## Activation and release preparation

A full **WIP client relaunch** is required. None was performed. The addon belongs on both sides of the eventual release; no twin/Chunkserve deployment or restart/reload was performed. Keep the required server HYBRID setting and verify it separately during deployment.

Sources and reproducible build/native-test tools: `scripts/submarine_lava_machinery/`. The refreshed next-update snapshot is `.local/next-big-update-20261021/prepared-01/`; the earlier October 20 snapshot is preserved. Full-pack client visuals, piloted multi-tank assemblies and larger fleets remain gameplay checks. Existing worlds, schematic storage and generated LOD data are excluded from preparation.
