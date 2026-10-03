# Importer Safety Fix 1.0.0 — 2026-10-03

Release v1.1.6 includes this addon. The following installation/test notes describe the earlier local preparation. Publication and server activation have separate deployment records.

Status: installed in the local WIP instance, pending Minecraft restart.
No pack release/version marker was advanced. Chunkserve and the server twin are unchanged.
No pull request was opened. PR work was cancelled at the user's request after the
source branch had already been pushed to TheFafalStudios/JacksEconomyForked,
branch codex/fix-importer-ticket-switch-hang.

## Cause and behavior

Changing an andesite manifest to diorite could retain the old selection, leaving
an empty item stack and null description. The purchase loop then advanced by zero
items indefinitely on the server thread.

Both normal and Mechanical Importers now:
- Clear selections when the manifest has no items and replace selections absent
  from a replacement manifest, preserving selections still available.
- Reset progress and stop on an empty selected stack, missing description, or
  nonpositive/nonfinite price.
- Reject invalid direct purchases, break on an empty generated purchase stack,
  and capture the purchased count before insertion.
- Avoid ticket damage and purchase recording when no items were bought.

Fluid importers/exporters, recipes, prices, and progression are not modified.

## Installed artifact

mods/apocalypse-importer-safety-fix-1.0.0.jar
SHA-256: e462669c88fbb75703f737fa88a1f4ab9fa912894a5fae766f0ffef2a4a707ca

Requires Minecraft 1.21.1, NeoForge 21.1.248 or newer, Jack's Economy
1.2.2-1.6.2 and Create 6.0.10. Keep the original Jack's Economy JAR.
The addon contains only its own namespace, two version-pinned mixins, source,
metadata, and the preserved upstream copyright notice. It introduces no blocks, items, or saves.

Rebuildable source, pinned input hashes, source patch and regression harness:
scripts/importer_fix/.

## Verification

- Baseline unmodified 1.2.2-1.6.2: fresh diorite and first andesite purchase
  succeeded, then andesite -> diorite hung; the disposable process was terminated.
- Standalone addon: 56 assertions passed on both importer types; clean shutdown.
- Exact upstream source patch compiled independently and substituted only in a
  disposable test JAR: the same 56 assertions passed; clean shutdown.
- Portable regression runner: 56 assertions passed.
- Final addon: identical class bytes to the tested addon; 56 assertions passed
  with the final dependency metadata and clean shutdown.
- Cases cover ticket switching both ways, preserved multi-item selection, empty
  and AIR manifests, empty direct-purchase inputs, invalid prices (zero, negative,
  NaN and infinities), no money, full output, zero process count, 130-item output,
  exact charges, and ticket use.
- Tested using native inventory/item/price/purchase code on a minimal dedicated
  server. Tests override energy/speed and update notifications. Interactive
  client testing and full-pack gameplay remain pending.
- The disposable test configurations emitted a flat-world generator-settings
  warning; all test worlds started and the final checks completed successfully.
- Local installed addon hash and targeted Packwiz entry/root hash verified.
  This is not a full-pack manifest audit.

Evidence directory:
Q:/GPT General/jacks-economy-importer-fix-build-2026-10-03/
Logs: baseline.log, patched.log, source.log, final.log,
portable-harness-verification/regression.log.

## Activation

Restart the local WIP Minecraft client to activate the addon for singleplayer.
For a dedicated server, the addon must also be installed on the server and that
server restarted; a client-only installation cannot fix Chunkserve's server thread.
No restart, remote upload, server deployment, or pack publication was performed.

## Rollback

With the affected game/server stopped, remove only
mods/apocalypse-importer-safety-fix-1.0.0.jar to restore original behavior.
No world migration is required. The original mod is unchanged.
Local metadata backups and before/after hashes are under:
Q:/GPT General/jacks-economy-importer-fix-final-2026-10-03/local-installation/
Restore metadata backups only if there have been no later edits; otherwise remove
only this addon's index/changelog/ignore entry and refresh the root index hash.
