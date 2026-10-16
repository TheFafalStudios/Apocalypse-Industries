# L2 Serial format agreement (2026-10-16)

Addon 1.1.0 must be installed on BOTH clients and dedicated servers. It forces
the existing name/NBT encoding for Item, Block, Potion, MobEffect, Attribute and
EntityType in L2 Serial 3.1.1. It does not change registry synchronization flags,
registered values, holder codecs, or upstream JARs.

The previous 1.0.0 addon forced clients only. On 2026-10-11 the server sent NBT
names and a temporary client correction allowed a successful join. On
2026-10-16 the installed client addon correctly selected NBT, but the server
sent numeric registry IDs. Captured bytes at offset 124 were bc 02 (VarInt 316),
which the NBT decoder rejected as Invalid tag id: -68. The old server packet
used 08 00 19 followed by modulargolems:metal_golem at the same position.
The mismatch recurred because server selection depended on initialization
state. Equal library hashes alone did not prevent different format choices.

1.1.0 moves the required redirect to the common mixin list and removes the
client-only mod restriction. Both sides now explicitly choose the same branch.
The required single-target redirect and pinned Minecraft 1.21.1, NeoForge
21.1.248 and L2Library 3.0.8 dependencies remain. The Packwiz entry uses
side="both". Remove 1.0.0 when installing 1.1.0; never load both copies.

Build: python scripts/l2_serial_fix/build.py --instance "<instance>" --output <new-directory>
Sources and reproducible builder: scripts/l2_serial_fix.

Validation: captured old NBT and current numeric packets identify the wire
format disagreement; current client runtime inspection confirms NBT selection.
Build reproducibility, archive/metadata/common-side registration, injection
target and Packwiz hashes are checked separately. Dedicated-server activation
and a multiplayer join with the new server addon remain pending until the
user restarts the server. Static checks do not certify gameplay compatibility.

Activation: server restart required, managed by the user. The already-running
client's 1.0.0 redirect selects the same NBT format as 1.1.0, so it can join once
the server activates 1.1.0. A normal client relaunch loads the updated addon.
No restart, reload, or automatic client reconnection is performed by tooling.

Rollback: restore each target's exact prior files from the deployment record.
Rolling back only one side may reintroduce the mismatch. Server rollback also
requires a user-managed restart. No worlds or schematics are included.
