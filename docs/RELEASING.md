# Release workflow

The live ATLauncher instance is the development source. Build outputs and research belong outside it, under Q:\GPT General.

1. Build a filtered snapshot using Python 3.11 or later: `python scripts/prepare_distribution.py --instance "<live instance>" --output "<new external directory>"`.
2. Review validation.json, direct-jars.json, working-mods.json, exclusions and deletion list. The builder verifies each external download hash against the live JAR and refuses duplicate mod IDs or standalone-fix package collisions. Do not regenerate references blindly from stale launcher metadata.
3. Update VERSION and pack.toml to the same release label, and prepare CHANGELOG.md. Preserve current release documentation and CI scripts when overlaying generated payloads into a separate Git checkout.
4. Run `python scripts/validate_distribution.py` from the release checkout. Build the launcher ZIP with scripts/Build-ATLauncherBootstrap.ps1.
5. Test fresh installations and existing-install updates as practical, then publish the reviewed commit and version tag. The bootstrap tracks main, so each published main manifest becomes the client update source.
6. After a successful push changing the distributed Packwiz manifest, ask the exact prominent confirmation from AGENTS.md before updating the local server twin or making its deployment packet.

The generated options.txt contains only portable resource-pack defaults and is marked preserve=true. Existing players keep their preferences. Never distribute worlds, Distant Horizons generated data, credentials or player histories.

Only original top-level mod JARs plus named compatibility mods should be active. Do not reintroduce the old patched Moogs Paths/Weather2/Weather2Compat JARs alongside standalone fixes.
