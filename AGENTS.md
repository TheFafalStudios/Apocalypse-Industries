# Apocalypse Industries workspace instructions

## Local dedicated server twin

Chunkserve cannot be accessed by the agent. Do not attempt SFTP access or claim direct validation of the remote server.

The theoretical local twin of Chunkserve is:

`Q:\Other Games\ATLauncher\servers\Chunkserve - Apocalypse Industries`

Use it to prepare and validate server updates. It must exclude Minecraft worlds/saves and all Distant Horizons LOD/generated data. Read `level-name` from `server.properties` and protect that directory plus related dimension/save directories.

Server release artifacts belong in:

`Q:\Other Games\ATLauncher\servers\Apocalypse Industries versions\<pack-version>`

Each official GitHub pack release must have its own version folder containing a clearly written server changelog, a ready-to-drop delta update ZIP, a deletion list, a complete server file manifest, deployment instructions, compatibility metadata, and checksums. Generate these with `scripts/Build-ServerUpdatePacket.ps1` after updating and validating the local twin.

The twin must contain `SERVER_PACK_COMPATIBILITY.toml`. Update it only after the twin is synchronized with the official pack release and compatibility checks pass. The compatibility marker is also included in the deployment packet so the remote server version can be compared manually with the twin.

After every successful GitHub push that changes the official distributed modpack, stop before modifying the twin or creating its deployment packet and ask exactly this prominent confirmation, filling in the version and commit:

# **SERVER UPDATE CONFIRMATION REQUIRED**

## **The GitHub modpack was updated to `<version>` (`<commit>`). Should I update the local Chunkserve twin and build its versioned server update packet now?**

Wait for an explicit answer. A GitHub push does not authorize the server update. Workflow-only repository changes that do not change the distributed Packwiz manifest are not official pack updates and do not trigger this question.