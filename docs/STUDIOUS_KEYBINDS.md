# Studious Keybinds — local WIP, 2026-10-12

Installed official `studious-keybinds-fabric-1.21.1-1.0.8-beta.1.jar`, [CurseForge file 9106887](https://www.curseforge.com/minecraft/mc-mods/studious-keybinds/files/9106887), project 1731572. Uses the existing Sinytra Connector 2.0.0-beta.17+1.21.1. Client only; Minecraft 1.21.1 and NeoForge 21.1.248 remain unchanged. No publication, twin/Chunkserve deployment, release marker advancement or Axiom server-permission changes.

## Selection and dependencies

The native NeoForge build (file 9106885) was inspected outside the instance and rejected: its declared minimum is NeoForge 21.1.256. Do not install it alongside the Fabric version. The author labels the files as releases, but the filenames and internal version are `1.0.8-beta.1`.

The Fabric JAR declares client environment, Minecraft 1.21.1, Java >=21 and Fabric Loader >=0.19.5. Connector's bundled Fabric Loader is 0.19.3 and its compatibility path accepts the mod; this is not a native Fabric installation satisfying that declared version floor. The exact Connector integration was tested directly, including the Controls-screen injection and keyboard editor rendering. No dependency declaration was patched or bypassed manually. Keep this integration pinned and revalidate when updating either mod. No additional library is declared; existing Connector/Forgified Fabric API are retained.

## Validation and distribution

Downloaded from the official CurseForge CDN. SHA-256: `1662657baf9c5670b37f1cbab61340627164a1b833a1c2d30363490d47589fef`. Archive CRC passed. This hash was calculated from the official download, not compared to an independently available upstream hash.

An isolated offline client used all 237 existing top-level mod JARs, including Axiom, Controlling and Searchables, plus Studious and a temporary test-only harness. Fresh configs; no existing worlds, schematics, player preferences or credentials copied. The harness opened vanilla Controls, found Studious's Keyboard button, pressed it, confirmed KeyboardScreen initialization and let it render for 40 client ticks. A PASS marker is recorded in the console log; the test then stopped its own client. The harness is not installed in the WIP pack.

This checks UI opening/rendering with the current JAR combination. Rebinding, keyboard layout selection, search, persistence and the WIP's existing config combination still need interactive verification. Offline network/resource warnings mean a clean-log result is not claimed.

`mods/studious-keybinds.pw.toml` pins the official CDN URL, calculated hash, CurseForge project/file and `side = "client"`. Updated its index entry and the pack index hash. The JAR is locally installed and Git-ignored, with no rehosting. All pre-existing JARs and `options.txt` remain byte-identical.

## Use and rollback

Restart the WIP client, then open Options → Controls → Keyboard. Existing keybind assignments were preserved. Studious visualizes shared keys; it does not automatically resolve conflicts or enforce server editing permissions.

Evidence, inspected candidate JARs, startup scripts/logs, UI test source, before-copies and exact installation hashes: `Q:\GPT General\studious-keybinds-20261012`. After closing the client, rollback removes only the Studious Fabric JAR/reference, its index entry and changelog entry; recompute the pack index hash. Use before-copies for comparison without restoring over unrelated later work. Retain existing Connector, Forgified Fabric API, Controlling, Searchables and Axiom.


## Required input compatibility (2026-10-12)

The Fabric shared-key callbacks crash through Connector during mouse/keyboard input on this NeoForge pack. Apocalypse Studious Native Key Compatibility 1.0.0 is now required with the pinned Studious/Connector versions. It preserves the visual editor and uses native NeoForge shared-key dispatch. See STUDIOUS_INPUT_FIX.md for reproduced failure, fix, runtime validation and rollback. Earlier editor-opening validation did not cover this input path.
