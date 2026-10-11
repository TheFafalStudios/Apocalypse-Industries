# Apocalypse Industries workspace instructions

## Standing restart instruction (2026-10-09)

The user manages all server restarts and reloads. Do not perform them or ask about restart/reload timing or permission. After a deployment, state when a restart or reload is required and leave activation to the user. This supersedes conflicting restart/reload confirmation instructions below; deployment still requires task authorization.

## Approved release and server workflow (2026-09-26)

This workflow supersedes the mandatory post-push confirmation, mandatory update ZIPs, and blanket prohibition on world backups/local world testing. Follow `docs/SERVER_RELEASES.md` for the operational procedure.

### Authorization and version alignment

- Client and server should use the same pack release version. When updating only one, ask for confirmation to update the other before publication/deployment. A request explicitly naming both already authorizes both; do not ask again after the GitHub push.
- If one side must remain behind, obtain explicit agreement and record the temporary mismatch. Never change version markers merely to hide a mismatch.
- A release may contain client-only or server-only changes; both sides still reference the shared release version after verifying their applicable payload.
- Apply only the requested changes. Full synchronization requires an explicit request. Preserve unrelated work, intentional server overrides and server-only mods.
- Confirm reload/restart timing unless already authorized. Upload authorization alone does not authorize interruption or activation.

### Live-world safety, backups and local testing

- Never include worlds/saves, dimensions, schematic/blueprint storage or generated Distant Horizons LOD data in pack releases, update ZIPs, deletion lists or pack manifests. Exclude these paths BEFORE traversal and guard against links/junctions.
- Never overwrite, delete, replace or synchronize the live Chunkserve world as a side effect of a pack update. Never mirror an entire client instance onto a server or run parent-directory cleanup that could affect protected storage.
- World backups in the local twin are allowed and encouraged. Keep them separate from deployable pack files, timestamp them and record the associated pack version. Prefer a stopped server or supported snapshot/save-flush procedure for consistency; coordinate operational timing.
- Local testing with a world copy is allowed. Preserve an untouched backup first. Keep test copies and backups out of distribution paths.
- Restoring a backup onto Chunkserve requires explicit approval identifying the backup and destination.
- Schematic/blueprint storage remains protected from incidental pack operations. Do not read, copy, modify or package it as part of a pack update; separate explicit authorization is required for schematic work.
- Read server.properties when needed to identify custom level-name and protect related save/dimension directories. Confirm additional storage locations before any broad inventory or backup operation.

### Remote changes and deployment evidence

- Compare the published release, client development instance, twin and relevant Chunkserve files before preparing updates.
- Catalogue Chunkserve quest and config differences as potential pack changes, recording paths, baseline, differences and likely effects. Present candidates for review: promote to pack, keep server-specific, or discard with approval. Exclude credentials and sensitive operational values from reports.
- Do not silently overwrite remote changes or automatically promote them. Unrelated review candidates do not block a narrow hotfix unless they conflict with it.
- Preserve rollback copies of affected unprotected pack files before changes. Verify deployed hashes and report success/failure separately for each target.
- Full compatibility markers may advance only after applicable payload validation. A targeted hotfix can advance a verified baseline; an uncertain baseline remains explicitly unverified. Track intentional overrides separately.
- Successful upload proves file deployment, not active recipe registration or gameplay compatibility. Distinguish static validation, gameplay testing and remote file verification.

### Paths and deployment records

Twin: `Q:\Other Games\ATLauncher\servers\Chunkserve - Apocalypse Industries`

Records: `Q:\Other Games\ATLauncher\servers\Apocalypse Industries versions\<version>`

- Maintain deployment notes, exact changes and before/after hashes, rollback files, compatibility metadata, checksums and a remote-change review catalogue for each deployment.
- ZIPs and deletion lists are optional: generate them on request or when needed for manual deployment. Complete eligible manifests are for full synchronization; targeted hotfixes record affected files only.
- Keep completed records immutable; retries/corrections use separate attempt identifiers. Keep world backups outside these release records/packets.
- `scripts/Build-ServerUpdatePacket.ps1` now prepares records by default and creates ZIPs only with `-CreateZip`. It does not deploy, reload, restart or advance compatibility markers. See `docs/SERVER_RELEASES.md`.

## Persistent Chunkserve access

For user-requested Chunkserve work, follow `docs/CHUNKSERVE_ACCESS.md`. Reuse `.local/chunkserve/connection.json` and run `scripts/Test-ChunkserveAccess.ps1`; do not request saved connection details again. Routine connection checks and relevant read-only access for the task are authorized. Keep remote changes within the requested scope, use strict host-key verification and never expose private keys. A successful login is not server validation.

## Confirmed release decisions (2026-09-23)

Keep the surface-only additional zombie spawning test. All 27 additional zombie rules must require sky visibility; verification remains in progress. Preserve natural cave spawning, progression scaling, the Spore lock, and unrelated rules. Do not ask the keep/discard question again.

The retired ocean-wave mod and its dedicated compatibility addon have been removed at the user's request. Do not reintroduce them without a new user decision. Preserve core Sable/Aeronautics and unrelated compatibility fixes.

Published v1 is confirmed tested, working and playable; this does not claim that later candidates have been tested. Client/server alignment and deployment authorization follow the approved workflow below, superseding the old timing waiver and post-push confirmation.

Protection Pixel integration now uses public addon version 1.1.

## Nuclear progression decision (2026-10-03)

Future Create Nuclear / Nuclear Radiation integration must gate nuclear reactor construction behind aluminum (aluminium), making reactors Space Age technology. Integrate nuclear progression with the planned endgame expansion. This is a confirmed design constraint, not an instruction to install mods or implement recipes now. Exact recipes, costs, radiation balance and endgame dependencies remain undecided; preserve the user's control over those details.
