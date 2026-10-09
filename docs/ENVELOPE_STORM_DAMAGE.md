# Local envelope storm damage

Release v1.1.13 adds Apocalypse Local Envelope Damage 1.0.0 for Minecraft 1.21.1, NeoForge 21.1.248, Weather Physics 1.0.3, Weather2 1.21.0-2.8.7 and Sable 2.0.3. Install the addon on both clients and the server; restart to load it. Server restarts/reloads are managed by the user.

## Behavior

Weather Physics previously selected random ship blocks twice per second anywhere within half the storm's size, using horizontal distance only. Damage had no distance falloff or clearance requirement. Native Weather2 block grabbing is a separate system; Chunkserve's disabled native grabbing override remains preserved.

The addon replaces envelope eligibility in Weather Physics's existing random surface-ray selection. Non-envelope candidates, wind, tornado forces, storm spawning and native terrain grabbing retain their upstream behavior. The existing configured destruction whitelist, destruction mode OFF, attempt count and outer storm-stage/range checks remain authoritative. Ordinary thunderstorms and F0/forming storms do not qualify.

A selected envelope is checked at its actual world position using Sable's full pose transform. Eligible envelopes must have adjacent air, be at least 30 blocks above local terrain, and belong to a ship whose lower world bounding box is at least 5 blocks above terrain sampled at its center and four corners. Terrain uses MOTION_BLOCKING_NO_LEAVES in already loaded chunks. Unknown/unloaded terrain prevents damage; the check does not load chunks. These samples are a conservative clearance heuristic, not a physical landing-contact detector.

Damage must be close to the actual server funnel layers at the block's Y. Centers and radii interpolate between adjacent layers; an 8-block margin allows close passes, including a vertical tolerance at the ends. Horizontal damage radius, including margin, is capped at 32 blocks. No initialized funnel geometry means no puncture.

Each ship gets at most one eligible opportunity per 100 ticks (5 seconds at 20 TPS), shared across all candidates and qualifying storms. Probability falls quadratically from 10% at the funnel axis to zero at the edge. Overlapping storms take the highest probability, not a sum. Upstream random candidate misses can make events less frequent. A successful puncture removes exactly one envelope block and starts a 600-tick (30-second) ship-wide cooldown. Failed block removal does not count as success. DROP_BLOCKS remains respected.

Envelopes are punctured individually even in upstream SABLE cluster mode; the ship is never marked as crumble by the envelope path. Native block removal is responsible for Aeronautics containment updates. The addon does not implement separate pressure or explosion mechanics. Cooldowns use weak ship references and are runtime-only; unloading/reloading a ship or restarting may reset them.

## Configuration

config/apocalypse-envelope-damage-common.toml contains enabled, minimumEnvelopeClearance, minimumHullClearance, funnelMargin, maximumDamageRadius, maximumChance, opportunityIntervalTicks and successfulPunctureCooldownTicks. The server's configuration controls server damage. Disabling the addon restores upstream envelope destruction.

## Build and verification

scripts/envelope_damage/build.py compiles an isolated reproducible JAR against the exact hashes in pins.json. It requires the installed pinned dependency JARs and the local Java/runtime libraries. It never installs or deploys.

scripts/envelope_damage/native_test.py boots a disposable local server with the pinned Create, Weather2, Weather Physics, Sable and Aeronautics dependencies and the addon. It verifies real Mixin application and local-variable captures, actual Aeronautics envelope tags, real assembled Sable ships, terrain/height/radius protection, quadratic falloff, opportunity/cooldown sharing, overlapping storms, single punctures in VANILLA and SABLE modes, explicit block IDs, disabled behavior and unchanged oak-plank destruction. Test probability is temporarily set to one or zero for deterministic behavioral checks; production defaults remain 10%.

Automated validation is separate from full-pack client startup, flight feel and balloon pressure/lift gameplay testing. Those remain pending. Test worlds are new and isolated outside the game instance and release records; no existing worlds or schematics are read or copied.

Native validation passed 878 assertions on 2026-10-09. Production JAR SHA-256: c56b944b5f7abc062c03a31f6d194ea5d6ca40dae43a3584f782a6c2f885ed86.
