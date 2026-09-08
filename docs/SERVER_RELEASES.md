# Server release workflow

The agent cannot access Chunkserve directly. The folder `Q:\Other Games\ATLauncher\servers\Chunkserve - Apocalypse Industries` is the theoretical local twin used to prepare and validate server updates. The folder `Q:\Other Games\ATLauncher\servers\Apocalypse Industries versions` stores immutable, versioned deployment artifacts.

## Official pack update sequence

1. Test the client pack and push its official Packwiz update to GitHub.
2. Ask the required prominent server-update confirmation from `AGENTS.md`.
3. After approval, snapshot the current twin and update it for the new pack release without importing worlds, saves, logs, crash reports, backups, or Distant Horizons LOD data. Preserve live operator/access lists, bans, server properties, EULA state, user caches, and the Chunkserve `start.sh` symlink.
4. Validate server mod compatibility, configuration, startup requirements, and the pack version.
5. Write a server-specific changelog describing added, changed, removed, migrated, and operator-action items.
6. Run `scripts/Build-ServerUpdatePacket.ps1` with the pack version, Git commit, and changelog file.
7. Review the generated ZIP, deletion list, manifest, instructions, and checksums in the version folder.
8. Upload/extract the ZIP into Chunkserve, then apply `DELETE_THESE_FILES.txt`. The user performs this remote deployment.
9. Compare Chunkserve's deployed `SERVER_PACK_COMPATIBILITY.toml` with the local twin marker to confirm both represent the same release.

The packet contains changed and new files relative to the previous twin manifest. Removed files are listed separately because extracting a ZIP cannot delete obsolete remote files.