# Release workflow

The live ATLauncher instance is the development source of truth.

1. Make and test changes in the live instance.
2. Regenerate mod reference metadata from `instance.json` and the active files in `mods/`.
3. Run `scripts/Build-PackwizIndex.ps1 -PackwizPath <path-to-packwiz.exe>` so `index.toml` and the hash in `pack.toml` match the approved live distributable files. The script builds in a clean temporary directory because installed JARs coexist with their metadata in the live instance.
4. Review `git status` and the changelog. No JAR, save, log, credential, database, or player-history file may be staged.
5. Commit the tested snapshot and tag the release version.
6. Export an installer snapshot and test it in a separate ATLauncher instance before sharing it.

Automatic Packwiz updates require `pack.toml` and the indexed files to be available over HTTPS. A private GitHub repository cannot provide anonymous raw-file access to friends' launchers, so final distribution requires either making the metadata repository public or hosting release files on another public/static endpoint. Repository visibility must be chosen before enabling automatic updates.

## Versioning

- Patch: fixes and tuning that should preserve existing worlds.
- Minor: new mods, mechanics, progression, or meaningful content changes.
- Major: stable milestone or changes that require a new world/migration.
- Development snapshots use `-dev.N` and are not sent to the stable channel.

## Binary policy

Third-party mods are represented by CurseForge or Modrinth project/file references whenever possible. A modified or otherwise unmatched JAR must be reviewed for its license and replaced with a legal download reference, a separate original-code compatibility mod, or a patch before release.
