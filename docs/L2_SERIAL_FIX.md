# L2 Serial client compatibility fix

The client selected numeric built-in registry IDs while the live dedicated server selected NBT names. L2 Serial 3.1.1 chooses these encodings in Handlers.enableVanilla according to registry sync state during static initialization. Golem Dungeons' spawn config was decoded with the wrong format, generating an invalid ResourceLocation and a secondary null-pointer disconnect.

`apocalypse-l2-serial-fix-1.0.0.jar` is a client addon pinned to Minecraft 1.21.1, NeoForge 21.1.248 and L2Library 3.0.8. Its required client mixin redirects the one registry-sync test in enableVanilla to select the existing name/NBT encoding for Item, Block, Potion, MobEffect, Attribute and EntityType. It preserves registry synchronization flags, registered values and holder codecs. Install on clients only; no live-server change or restart is needed for the current server payload.

Sources and reproducible builder: scripts/l2_serial_fix. Build with `python scripts/l2_serial_fix/build.py --instance "<ATLauncher-instance>" --output <new-directory>`. The builder verifies the original L2Library SHA256 before compiling. Do not replace upstream library jars.

Installed into the WIP development client and ApocalypseIndustriesGitHubd on 2026-10-11. Addon SHA256: 89139bb363e4ef3427ae0b7500e452b86945cbb5cd29e5af829f937e0203347f. Upstream jar hashes and the server remain unchanged. This client-only hotfix is included in the GitHub main manifest for v1.2. The existing server already emits the required name/NBT encoding and needs no payload change. Pack/version/compatibility markers were not advanced.

Validation: captured packet replay with the old numeric codec reproduces the exact malformed ResourceLocation from the log; NBT reads the equipment and faction correctly. Applied equivalent handler selection to the running client without retaining any transformer or altered class bytecode. User reconnected and confirmed "loaded in". The corresponding client log continues past the earlier failure. Both installed addon copies are hash-verified. Compilation, metadata and injection target were statically checked. The persistent mixin's fresh-start activation is not yet gameplay-tested; it takes effect on the next normal client launch.

Rollback: remove the added addon from the client mods directories. No upstream files were overwritten. The current session's handler fix persists until Minecraft exits. Original pre-fix codec selections and packet evidence are preserved under .local/l2-diagnosis-20261011, excluded from distribution.

Future compatibility: this fix targets the current server's NBT/name payload. Re-evaluate when changing L2Library, NeoForge, Connector or registry-initialization behavior on the server; the pinned dependency fails startup rather than silently accepting an incompatible L2Library update. The Packwiz reference declares side="client" and excludes the addon from server installs.
