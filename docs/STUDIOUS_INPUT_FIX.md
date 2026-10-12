# Studious shared-key input compatibility — local WIP, 2026-10-12

## Reported failure

`crash-2026-10-12_13.24.27-client.txt` reports a mouse-input ClassCastException: CopyOnWriteArrayList cannot be cast to ArrayList. Studious's injected setShared handler calls Connector's emulated vanilla KeyMapping.MAP.get; its adapter assumes ArrayList while NeoForge's native KeyMappingLookup stores CopyOnWriteArrayList. This input failure is separate from the phantom texture reload race fixed earlier.

The previous Studious test proved Controls/editor opening and rendering, but did not exercise key/mouse dispatch. A direct mouse-press regression test in an isolated full-mod-set client reproduced the same exception without opening an existing world.

## Fix

Apocalypse Studious Native Key Compatibility 1.0.0 keeps the official Studious JAR unchanged. A client compatibility mixin runs after Studious and adapts only its two merged Fabric shared-key callbacks (clickShared/setShared) to no-ops, preserving NeoForge's original input dispatch. NeoForge already dispatches to every matching shared binding with conflict-context/modifier filtering. Studious's Fabric callback is redundant here, can dispatch extra clicks, and accesses the incompatible adapter.

The plugin checks the exact callback signatures and requires exactly two matches, failing visibly if the pinned integration changes. It does not disable KeyMapping.set/click or native key handling, catch/suppress input exceptions generally, change player key assignments or remove the editor. No upstream JAR is patched or republished.

Pinned source builds verify Studious 1.0.8-beta.1 SHA-256 `1662657baf9c5670b37f1cbab61340627164a1b833a1c2d30363490d47589fef` and Connector beta.17 SHA-256 `ca70b57df3a7a4e8d211fea7ff3ae77e768482af374f26bf953ff5fd755564f0`. Mod metadata requires these versions, Minecraft 1.21.1 and NeoForge >=21.1.248 <21.2. Revalidate/rebuild before updating either mod. This addon is required for this Studious Fabric/NeoForge combination.

## Verification

Isolated offline client uses all current WIP mod JARs, including Axiom, Studious, Controlling, Searchables and the phantom reload guard, with the selected portable resource packs and relevant rendering preferences. Existing worlds, schematics and credentials were not copied or accessed.

The baseline reproduced the actual CopyOnWriteArrayList/ArrayList mouse-input exception. The fixed run verified left mouse press/release, five repeated shared keyboard/mouse presses, exactly one click per matching action, inactive conflict contexts, release, rebinding and unbinding. It also opened Controls → Keyboard and kept the Studious editor rendering for 60 ticks. Test-only mappings and harness exist only in the isolated directory. The first fixed candidate failed a pre-merge signature check; the plugin was moved to post-merge. An expanded test of every mod binding encountered a Protection Pixel callback that requires a connected server, so that menu-only test was removed; this fix does not change Protection Pixel.

These are targeted input/editor runtime checks, not a full in-world gameplay or hardware-input test. The reported world-entry mouse-input path is reproduced through its actual KeyMapping entrypoint. In-world interactions, complete modifier combinations, guns, movement and Axiom tools still need ordinary gameplay verification. No clean-log result is claimed.

## Installation and rollback

Local WIP only. Restart the client to activate. Axiom, Studious and the earlier phantom reload guard remain installed; existing mod JARs and options are preserved. No publication, release marker advancement, twin/Chunkserve change or world modification. The standing prohibition on Axiom server editing remains.

Sources and build script: scripts/studious_input_fix/. Use Python 3.11+ and `build.py --output NEW_EXTERNAL_DIRECTORY`. Evidence, baseline/fixed logs, pristine addon build, before-copies and exact hashes: `Q:\GPT General\studious-input-fix-20261012`.

Client Packwiz metadata pins the authored addon to a future main-branch publication URL. That URL is not deployed by this local install; future release preparation must include the exact JAR and verify its download hash.

Rollback after closing the client removes only the compatibility addon/reference, its index entry, changelog and ignore additions, then recomputes the pack index hash. Compare before-copies without restoring over later changes. Removing the addon while retaining this Studious Fabric version restores the known input crash; disable Studious too if intentionally rolling the compatibility fix back.
