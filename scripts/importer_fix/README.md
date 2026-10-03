# Importer Safety Fix 1.0.0

Standalone compatibility addon for Jack's Economy (Forked) 1.2.2-1.6.2,
Create 6.0.10, Minecraft 1.21.1 and NeoForge 21.1.248.

The two mixins replace only the item importers' tick and purchase methods with
the tested source fix. They preserve valid manifest selections and reject
invalid transactions. The original Jack's Economy JAR remains unchanged.
Fluid importers and exporters are outside this fix.

Build from these checked-in sources with Python and the installed Java 21 runtime:

    python scripts/importer_fix/build.py --output "Q:/GPT General/NEW-importer-fix-build"

Builds are deterministic and verify pinned mod hashes. They do not install,
publish, restart, or change pack versions. Install the resulting addon alongside
the original mod; never alongside a modified replacement JAR.

Source attribution: the importer method bodies derive from
SCsupercraft/JacksEconomyForked, 1.21.1 commit
55553657482a8cbdd4e0cede5ec074df6568a5ef. The pinned upstream snapshot identifies its license as All-Rights-Reserved; its original notice is preserved. The addon's mod metadata separately declares AGPL-3.0-only.
The preserved upstream notice and addon Java sources are included in the JAR.
The upstream source diff is saved as upstream-source.patch.
The regression harness and usage are in tests/.

See docs/IMPORTER_SAFETY_FIX.md for verification and rollback.
