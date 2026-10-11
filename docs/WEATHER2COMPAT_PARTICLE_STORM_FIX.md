# Weather2Compat particle-storm crash fix — 2026-10-11

Local development fix only, based on pack v1.1.15. No shared release version change, publication, twin or Chunkserve deployment. Existing game processes retain old loaded code; next client launch activates the replacement.

## Cause and change

Weather2Compat 1.6.0 casts every WeatherObject from getStormObjects() to StormObject before checking rain predicates or distance. WeatherObjectParticleStorm instead extends WeatherObject directly. Both isStormAbove and getPrecipitationStrength consequently throw ClassCastException when the weather list contains particle storms. The latter also supplies sky brightness. Reports at 01:53, 01:58 and 10:25 on October 11 show these two paths.

Standalone addon 1.0.1 replaces only Weather2CompatSafety.safeIterator and addon metadata; existing class replacement plugin, mixin and Weather2Utils patch bytes are preserved. The helper still snapshots the list, then selects instanceof StormObject entries. Normal storms, subclasses, ordering and the original dead-storm checks are preserved. It never modifies the weather manager list or deletes particle storms. The original upstream Weather2Compat and Weather2 JARs remain untouched.

## Verification

Java 21 compilation passed. Actual compiled addon helper passed mixed normal/particle storms, particle-only and empty lists, normal storms, subclass/dead-state preservation, unchanged source list, and source mutation after snapshot. Tests use minimal binary-compatible storm fixtures and -Xverify:all; they are not Minecraft gameplay tests. The old addon fails the identical regression with ClassCastException. Bytecode audit confirms both affected loops call the corrected helper. Archive comparison confirms only helper and metadata changed among existing entries; source is added. Installed addon bytes and SHA-256 match the tested artifact. In-game verification remains pending.

## Files and rollback

Installed: mods/apocalypse-weather2compat-threadsafe-fix-1.0.1.jar
SHA-256: ef4404a0d2d2588dcddec9e6b1e7bde92b7644a2f0f774c17acdcc158e02c833
Old addon SHA-256: e83466504f8517d17937b532c790298be21d19d80ab023185b807c3decae9d1c

Build, regression and installation reports: Q:/GPT General/weather2compat-particle-fix-20261011-attempt1/
Rollback: Q:/GPT General/weather2compat-particle-fix-20261011-attempt1/rollback/apocalypse-weather2compat-threadsafe-fix-1.0.0.jar

To roll back with the client closed, move the 1.0.1 addon out of mods and restore the saved 1.0.0 addon. Never leave both active. Rollback restores the particle-storm bug as well.

Rebuild with scripts/weather2compat_fix/build.py --output <new external directory> --baseline <saved original addon>, then scripts/weather2compat_fix/test.py --build <that directory>. Builders do not install or launch Minecraft. Live packwiz manifests were not regenerated; the release builder must regenerate them if this fix is published later.

Gameplay check after next launch: enter the affected test world and exercise sand/snow particle storms, normal rain and thunder. Verify client sky brightness and server-side rain checks remain stable while particle effects continue.
