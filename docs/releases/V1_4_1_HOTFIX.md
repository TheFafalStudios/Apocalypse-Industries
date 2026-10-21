# Apocalypse Industries v1.4.1 — Lava ballast hotfix

- Ballast tanks, vents and floaters work in lava while preserving their existing force, speed and pressure limits.
- Vents and clusters retain actual water/lava identity, reject mixing and conserve fluid during transfers. Empty water-filled tanks before taking in lava.
- Propulsion remains unchanged: this hotfix does not enable propellers or water thrusters in lava.

Built on published v1.4 with no other gameplay changes. The exact shipped addon passed 304 runtime assertions and matched assembled water/lava tests; the original water response is preserved. Separate manual testing was waived by the user. A piloted full-size submarine and multiplayer operation remain unverified.

Update clients and server together. Client relaunch and a user-managed server restart are required. Server pressure mode stays HYBRID. Worlds, schematics and generated LOD data are excluded.
