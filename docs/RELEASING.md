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

## Reconciled testing workflow (2026-09-20)

Local VERSION and pack.toml use v1-dev until an official release label is selected. This is an unpublished development build based on GitHub v1. Preserve the pending zombie-spawning decision in AGENTS.md before publishing.

Fetch origin/main before building. Run `scripts/Build-PackwizIndex.ps1 -OutputRoot <new directory outside the instance>`; it selects Python 3.11+ and accepts -PythonPath when needed. The builder compares deletions against origin/main (override with -BaselineRef), not the partially refreshed live index. It writes fresh pack/index hashes into the output repository only. Do not copy portable generated options.txt over live player preferences or run validation on the live folder as if it were a filtered release.

Build/test folders are excluded from source-tool copies. The local instance icon and CI workflow are retained. Generated validation reports never inherit runtime-test claims from an older release. Validate and build the ATLauncher ZIP in the generated repository before publication. No automatic push occurs.
