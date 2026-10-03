# Survival Attachment Removal 1.0.0

Minecraft 1.21.1, NeoForge 21.1.248, pinned NTGL 3.1.8. Tested with Create: Gunsmithing 1.4.9 and Apocalypse Gunsmithing Enchantments 1.1.0.

Allows Survival and Adventure players to take attachments out using the normal attachment menu (Z in the current pack). Supports ordinary pickup and shift-click. Creative remains allowed and Spectator remains blocked for occupied attachment slots. No new keybinds, recipes or configurations.

The addon redirects only the creative permission check inside NTGL AttachmentSlot.mayPickup. The vanilla base-slot permission and NTGL attachment/state handling remain in place. A second redirect in checkAmmoCount splits excess ammo refunds into valid item stacks before inventory insertion or dropping. Native testing found that NTGL's original full-inventory fallback could drop 200 rounds as one stack, which failed Minecraft serialization and could disappear after a server save/restart. The addon fixes that path without changing ammo capacity or refund quantities.

Install the final JAR on both clients and the server, then restart them. The addon is included in pack release v1.1.8. Deployment hashes and restart status are recorded separately; native testing does not certify full-pack gameplay compatibility.

Build with Python 3.11+ and the installed Java 21 runtime:

    python scripts/attachment_removal/build.py --output "Q:/GPT General/NEW-attachment-build"

Run native tests in a new disposable server:

    python scripts/attachment_removal/tests/run.py --build "Q:/GPT General/NEW-attachment-build" --attempt native

Tests use only named dependency JARs and runtime libraries, a newly generated disposable world, and localhost port 25590. Existing world and schematic data are never read or copied. Production JARs include their source, with fixed ZIP entry timestamps. Build output and generated test worlds remain outside the instance and release packets.

Verified final run: 1,501 assertions across 72 valid gun/attachment and pickup/shift-click combinations. Covers attachment installation/removal, no duplication on repeated empty clicks, closing/reopening, item save/parse roundtrips, Survival/Adventure/Creative permissions and occupied-slot Spectator denial, incompatible swaps, full inventories, unchanged ordinary inventory pickup, loaded magazines, round conservation, and valid dropped-stack serialization. A 300-round Gatling drum returns 200 excess rounds and retains the base 100 rounds. Both redirects applied in a native dedicated server with the existing enchantment addon loaded; clean exit code 0.

Limitations: automated tests use server-side simulated players. Real client UI/network interactions, actual player reconnects, full-pack gameplay and every other third-party addon remain untested. Save/parse tests verify the underlying persisted item data rather than claiming a live reconnect test.

Final artifact: Q:/GPT General/attachment-removal-build-2026-10-04-verified/apocalypse-attachment-removal-1.0.0+mc1.21.1.jar
SHA-256: 3b08f3d3ddadf899f122ed070030197406ef6d81be936de9221bbfd41898223d

Earlier experimental builds remain outside distribution and must not be installed; only the verified artifact above includes the safe ammo refund fix. Rollback after a future installation: stop the target and remove only this addon JAR. Original NTGL and gun JARs are not modified.
