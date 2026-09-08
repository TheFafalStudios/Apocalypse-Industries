# Apocalypse Industries

Version-controlled source for the live Apocalypse Industries Minecraft modpack.

- Minecraft: 1.21.1
- Loader: NeoForge 21.1.248
- Launcher: ATLauncher
- Baseline ancestry: CurseForge `Apocalypse Industries 1.0.2` (file `8380248`)

The live ATLauncher instance is the development source of truth. This repository stores Packwiz download references, the three required patched JARs, the complete live resource/data packs, configuration, and KubeJS scripts. Player worlds, logs, launcher state, databases, histories, and credentials remain excluded.

Automatic update manifest:

`https://raw.githubusercontent.com/TheFafalStudios/Apocalypse-Industries/main/pack.toml`

Friends install the pack once using `dist/Apocalypse-Industries-ATLauncher.zip`. Packwiz checks the manifest before every launch.

See [docs/RELEASING.md](docs/RELEASING.md) for the update and release workflow.
