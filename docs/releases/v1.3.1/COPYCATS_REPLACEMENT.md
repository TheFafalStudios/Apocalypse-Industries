# Copycats replacement — v1.3.1

- Replace Create: Copyfoxes 2.0.0 with Create: Copycats Plus Additions 1.5.0 on client and dedicated server.
- Keep Copycats+ 3.0.4, Create 6.0.10, Sable 2.0.3 and NeoForge 21.1.248. The replacement's declared dependency ranges accept these versions.
- Add inner/outer corner slopes, layered corner slopes and wall-mountable slope layers. The addon supplies Sable physics data and moving-sublevel rendering compatibility.
- The mods use different block IDs; existing Copyfoxes blocks/items are not automatically converted. No world or schematic files are included or modified by deployment.
- Distribution and deployed hashes are verified separately from gameplay. Server activation requires a user-managed restart; combined gameplay remains untested.


Published baseline: b28abdfe33636f4aa6da3e3d71a07f6502d44261 (v1.3).

Approved payload: delete `mods/copyfoxies-2.0.0.jar`; add `mods/copycats-plus-additions-1.5.0-neo.jar`. Packwiz removes the old managed download by removing its metafile. Unrelated local WIP changes and server config/quest overrides are preserved.

New JAR SHA-256: `eaf0d3b5e0cc614207e7719780b8a18599b476094e6442ab6866bb9fc537f663`.
