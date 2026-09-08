# Release workflow

The live ATLauncher instance is the development source of truth.

1. Make and test changes in the live instance.
2. Run `scripts/Generate-PackwizModMetadata.ps1` to refresh external mod references from `instance.json`.
3. Run `scripts/Build-PackwizIndex.ps1 -PackwizPath <path-to-packwiz.exe>` to rebuild the manifest in a clean staging directory.
4. Run `scripts/Build-ATLauncherBootstrap.ps1` if the update URL or loader version changed.
5. Review Git status and the changelog. Credentials, saves, logs, databases, and player histories must remain excluded.
6. Commit tested work to `dev`, merge it to `main`, and tag the stable version.

Automatic Packwiz updates use:

`https://raw.githubusercontent.com/TheFafalStudios/Apocalypse-Industries/main/pack.toml`

Friends import `dist/Apocalypse-Industries-ATLauncher.zip` once. Packwiz checks this manifest before every launch.

Normal third-party mods use CurseForge or Modrinth references. The three live patched JARs and the live resource/data packs are stored directly in this repository so the updater reproduces the working instance.
