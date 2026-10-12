# Axiom integration — v1.3

Current distribution: Axiom, Connector and Forgified Fabric API apply to client and server. Dedicated-server editing is disabled by default by the command addon; see [server control](AXIOM_SERVER_CONTROL.md). The local installation evidence below describes the original client trial.

# Axiom integration — local WIP, 2026-10-12

Installed official Axiom 6.1.3 for Minecraft 1.21.1, Modrinth version `vHcXJQqi`. The pack remains Minecraft 1.21.1 / NeoForge 21.1.248. Local development only: no publication, twin/Chunkserve changes or release/compatibility marker advancement.

## Requirements

Axiom is a Fabric mod. The JAR declares Minecraft >=1.21.0 <1.21.2, Java >=17, Fabric Loader >=0.14.21 and Fabric API. This pack uses Java 21 and the existing Sinytra Connector 2.0.0-beta.17+1.21.1 / Forgified Fabric API 0.116.15+2.3.5+1.21.1. Neither dependency was changed. Do not add ordinary Fabric API or a separate Fabric loader to this NeoForge pack.

Upstream [Connector issue 1974](https://github.com/Sinytra/Connector/issues/1974) records an Axiom loader failure addressed by the beta.11 development merge [2000](https://github.com/Sinytra/Connector/pull/2000). Installed beta.17 was tested directly with this Axiom build.

Axiom declares conflicts with Sodium <=0.5.0, OptiFabric, Immersive Portals <=6.0.6, ClientSpoofer and OpSec. This pack's Sodium is NeoForge 0.8.12-beta.2. The isolated startup retained the current Sodium, Iris and Distant Horizons JARs. The embedded Axiom Client API, Lattice, MixinConstraints and MixinExtras are supplied by upstream; no standalone copies or Connector Extras were needed for startup.

## Distribution and multiplayer

`mods/axiom.pw.toml` pins the official Modrinth CDN URL and SHA-512, with `side = "client"`. The local JAR remains excluded from Git; it is not rehosted or added as a direct repository payload. Updated only the new metadata's index entry and pack index hash, plus WIP changelog/documentation. All 236 existing mod JARs remained byte-identical.

Client distribution scope does not establish multiplayer editing support. Axiom's [official project page](https://modrinth.com/mod/axiom) describes non-commercial singleplayer/localhost use, private multiplayer licensing/whitelisting and public-server whitelist requests. See its [EULA](https://axiom.moulberry.com/eula). No license purchase, whitelist request, server-side installation or permission change was performed. Confirmed user decision (2026-10-12): Axiom editing must be prohibited on the server. During later server configuration/deployment, enforce and verify this restriction server-side; do not rely solely on client controls or lack of licensing/whitelisting. Do not enable editing or grant exceptions without a new explicit user decision. No server configuration is authorized or changed by this reminder. Local singleplayer use remains available.

## Validation

- Official download matched upstream SHA-1 and SHA-512; archive CRC validation passed.
- Isolated offline client in `Q:\GPT General\axiom-20261012\startup-check` used all 236 pre-existing top-level mod JARs plus Axiom with fresh configs. No existing worlds, saves, schematics or credentials were copied or accessed.
- Connector remapped Axiom and embedded libraries; the loader recognized Axiom 6.1.3, its mixin plugin loaded, its client initialized, and its Dear ImGui config initialized. Texture atlases and sound initialized, with no observed Axiom loader/mixin crash. The test client was then stopped; the user's launcher was left running.
- This is startup evidence only. Editing/undo, modded blocks, in-world shader rendering, the existing WIP config combination and multiplayer remain untested. Network requests failed under the sandbox, and mod/resource warnings plus OpenGL errors occurred. No comparison run was performed to attribute those messages, and a clean-log or gameplay result is not claimed.
- New metadata download hash and index/hash chain verified; existing dependency JARs unchanged.

## Activation and controls

Restart the WIP client to load Axiom. Creative builder menu: hold Left Alt. Editor: press Right Shift. Existing bindings overlap: Right Shift with Protection Pixel armor-table activation/Xaero quick-confirm; Left Alt with Create, Copycats, gun controls and other mods. Existing preferences were preserved. Adjust Axiom controls if they interfere, then check editor opening, placement, selection and undo in a disposable new world before using existing builds. Schematics were outside this task's scope.

## Evidence and rollback

`Q:\GPT General\axiom-20261012` holds official version metadata, pristine download, startup script/command/logs, before-copies and `installation.json` with exact before/after hashes. Tests created no world.

After closing the client, remove only `mods/Axiom-6.1.3-for-MC1.21.1.jar` and `mods/axiom.pw.toml`, remove only the Axiom index entry, and recompute the index hash in `pack.toml`. Remove the WIP Axiom changelog entry/document if desired. Use before-copies for comparison, without overwriting later unrelated metadata changes. Retain Connector/Forgified Fabric API: the existing weapon library already uses them.
