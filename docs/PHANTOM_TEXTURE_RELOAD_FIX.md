# Phantom-track texture reload guard — local WIP, 2026-10-12

## Reported failure and cause

`crash-2026-10-12_13.05.05-client.txt` reports `ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1` in SpriteContents.upload, reached through Steam 'n' Rails PhantomSpriteManager.renderTick and its forced animated-frame upload. The initial resource reload was unfinished. Studious Keybinds was not in the failing call chain.

Steam 'n' Rails 0.2.1 registers phantom sprites during construction, while resource workers may still generate their mip chains. Its manual renderTick uploads can run during loading. SpriteContents.upload receives a captured image array but uses the sprite's current mip-chain length as its loop bound; replacement of a one-level array by a completed chain can make those lengths differ. The timing can vary, so a successful startup alone does not rule it out.

The earlier Axiom/Studious tests used fresh rendering defaults, and their passing results did not cover this WIP resource combination or the reload race. Axiom and Studious are retained. Attribution of the timing change to either added mod is not established.

## Fix

Apocalypse Phantom Texture Reload Guard 1.0.0 injects at the start of PhantomSpriteManager.renderTick and defers its upload while Minecraft's LoadingOverlay is active. It cancels before the upstream hasChanged flag is cleared, so pending visibility updates run when loading finishes. It does not suppress errors generally, alter vanilla mipmap generation, change phantom-track visibility rules, patch upstream JARs or reduce graphics settings.

Client mixin only. Pinned to the inspected Railways 0.2.1+neoforge-mc1.21.1 JAR, SHA-256 `b7636c8b1b0352ed1a130dfe67f8bb574e2fc08803ed1cda4d3ea00505193914`, Minecraft 1.21.1 and NeoForge >=21.1.248 <21.2. Review/rebuild before updating Railways.

## Verification

External isolated client used all current WIP mod JARs, the exact selected portable resource packs, mipmap settings and relevant Iris/Railways preferences. No existing world, schematic, player credentials or server was accessed.

A test-only mixin deterministically models mipmap completion after capturing a one-level upload array. The unguarded baseline reproduced the same ArrayIndexOutOfBoundsException. With the guard, the fixture is deferred and the pending update preserved; actual loaded phantom textures upload afterward, four visible/hidden frame selections execute, and a full resource reload completes with stable subsequent client ticks. Tests are installed only in the external isolated directory; no harness or race injection is installed in the WIP.

This verifies the guarded upload/reload path in the isolated rendering environment. In-world phantom-track visuals, shaders enabled in a world, world loading and general gameplay remain untested. Network/resource warnings occurred; no clean-log result is claimed. A first harness attempt had a mixin-package error that was corrected before the baseline/fixed regression runs.

## Installation, evidence and rollback

Local WIP only. Restart the WIP client to load the fix. No publication, shared version advancement, twin/Chunkserve change or server activation. Axiom editing remains prohibited on the server under the standing instruction.

Sources/build script: scripts/railways_texture_fix/. Build with Python 3.11+ and `build.py --output NEW_EXTERNAL_DIRECTORY`; outputs must stay outside the instance. Before-copies, exact installation hashes, pristine addon build and baseline/fixed test logs are under `Q:\GPT General\wip-texture-crash-20261012`. Existing mod JARs and options remain unchanged.

Client Packwiz metadata uses the repository's main URL and pins this authored addon by hash. The raw URL is a future publication reference; it is not deployed by this local installation. Any future release must publish the exact addon JAR as well as its client metadata and revalidate the download hash.

Rollback after closing the client: remove only the guard JAR and its metadata, remove only its index entry, recompute the pack index hash, and undo only its changelog/ignore additions. Before-copies are for comparison; do not overwrite later unrelated changes. No existing mod needs to be removed.
