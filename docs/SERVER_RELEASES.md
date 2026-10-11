# Server release and deployment workflow

## Standing restart instruction (2026-10-09)

The user manages all server restarts and reloads. Do not perform them or ask about restart/reload timing or permission. After a deployment, state when a restart or reload is required and leave activation to the user. This supersedes conflicting restart/reload confirmation instructions below; deployment still requires task authorization.

Approved 2026-09-26. This supersedes the old post-push confirmation and mandatory ZIP workflow.

## Authorization and shared versions

Client and server use the same pack version. If only one is requested, ask whether to update the other before publishing/deploying. If both are explicitly requested, continue through both without a second post-push confirmation. Explicitly agree and record any temporary mismatch. A side with no applicable file changes still references the same release after verification; never relabel an unverified server to hide drift.

Narrow fixes remain narrow. Full synchronization requires an explicit request. Preserve intentional server configuration, quest edits and server-only mods. Confirm activation/reload/restart timing unless already authorized.

## Preparation and remote review

1. Reuse the saved connection through docs/CHUNKSERVE_ACCESS.md. Inspect exact relevant paths; never mirror an instance root.
2. Compare the published GitHub baseline, client development files, twin and relevant remote files. Capture separate local before snapshots of the affected files for each target; preserve original bytes for rollback. A partial before snapshot is suitable only for explicit-path hotfixes, never full inventory/delta inference.
3. Catalogue Chunkserve config and quest differences. Identify the baseline, exact path, sanitized summary and likely effect. Review each as promote-to-pack, keep-server-specific, or discard-with-approval. Hash-only detection is not a completed semantic review. Do not publish secrets or operational values. Unrelated candidates need not block an otherwise independent hotfix.
4. Resolve conflicts affecting the approved change before writing. Prepare a candidate snapshot preserving intentional differences and applying only the approved changes. A deletion is explicit absence from the candidate for an approved path, not a license to clean other files.
5. Prepare the client release in a clean checkout; update shared version, changelog and Packwiz hashes. Validate applicable syntax, recipe format, dependencies and distribution hashes. Clearly distinguish static checks from gameplay tests and remote verification.

## World safety and backups

Read server.properties to identify custom world paths, including related dimensions. Keep save/world, schematic/blueprint and generated LOD storage out of pack inventories, release artifacts and deletion lists; prune paths before traversal and skip links/junctions. Confirm any unusual storage locations before broad operations.

World backups in the local twin are allowed and encouraged. Keep timestamped backups in a separate non-deployable BACKUPS/worlds area (or another explicitly recorded backup location), labelled with pack version. Use stopped-server or supported snapshot/save-flush procedures for consistency and coordinate timing. Testing local copies is allowed after preserving an untouched backup. Restoring onto Chunkserve requires explicit approval of the chosen backup and destination. Never overwrite or synchronize a live world as an incidental pack-update step. Schematic work requires separate explicit authorization.

## Record tooling

The existing command name is retained, but Build-ServerUpdatePacket.ps1 now prepares a deployment record by default. It does not connect, deploy, reload, restart, back up worlds or write compatibility markers. The previous -RecipeOnly/-TwinRoot invocation is replaced by explicit before/candidate snapshots and -Paths; an old invocation fails instead of silently using unsafe behavior.

Requirements: PowerShell and Python 3.11+. Specify -PythonPath if the default Python is older.

Example (replace placeholders with actual approved release details):

```powershell
./scripts/Build-ServerUpdatePacket.ps1 `
  -Version '<shared-version>' -ClientVersion '<shared-version>' `
  -Commit '<commit-sha>' -ChangelogPath '<notes.md>' `
  -BeforeRoot '<pre-change-server-snapshot>' -CandidateRoot '<approved-candidate>' `
  -Paths 'kubejs/server_scripts/custom_recipes.js' `
  -PythonPath '<python-3.11-or-later.exe>'
```

- Add -CreateZip for a manual deployment ZIP and deletion list; both are absent by default.
- Add -Full only for an explicitly authorized full comparison of complete eligible snapshots. It inventories known pack/runtime roots and launcher files, pruning protected storage first. Add new eligible roots deliberately only after reviewing what they store.
- Supply -BaselineRoot and -ReviewPaths for known Chunkserve quest/config candidates. BeforeRoot must contain the remote pre-change snapshot. The output never includes config values; enrich the catalogue with sanitized summaries/effects and user decisions.
- -BaselineVerified records evidence established independently; it does not certify candidate compatibility. -MismatchApproved records explicit user approval, never an inferred exception.
- Records default to `Q:\Other Games\ATLauncher\servers\Apocalypse Industries versions\<version>`. A retry/correction uses -AttemptId, creating `<version>/attempts/<id>`; existing attempt directories are never overwritten.
- Separate target snapshots must use separate attempts if their original bytes differ.

Records contain DEPLOYMENT.md, CHANGES.json, ROLLBACK/, COMPATIBILITY.toml, DEPLOYMENT_STATUS.json, REMOTE_CHANGE_CATALOGUE.json, SHA256SUMS.txt, and a full or targeted SERVER_FILE_MANIFEST.json. ZIPs contain only changed pack files; marker metadata is not a payload. World backups are never stored in records/ZIPs.

## Deploy, activate and complete

1. Recheck each destination against its captured before hash immediately before mutation; unexpected drift pauses that affected target. Keep rollback copies outside active script/config directories.
2. Apply approved file changes only; verify bytes read back from each target. Handle approved deletions individually, never through blanket synchronization.
3. Record client/twin/Chunkserve outcomes separately in DEPLOYMENT_STATUS.json, with verification evidence and activation status. Successful upload alone does not establish active recipe registration or gameplay compatibility.
4. Advance SERVER_PACK_COMPATIBILITY.toml only after applicable release payload/dependencies and intentional overrides pass checks. A targeted hotfix may advance a verified baseline; if the baseline is uncertain, record target version and unverified status rather than claiming alignment. Keep override descriptions separate from the shared release version.
5. Coordinate any necessary activation. Remote restarts or reloads are not implied by uploading files.
6. Finish the catalogue review or clearly leave candidates pending. Add verified results to DEPLOYMENT.md, update metadata, recompute checksums using the helper's checksums(record_path) function, and mark the record complete. After completion, the attempt is immutable. Corrections get a new attempt.
7. Report shared version/commit, each target's status, checks and activation, record/rollback/world-backup locations where applicable, and pending config/quest candidates.

The record tool checks payload safety and captures preparation artifacts; live deployment, semantic review, compatibility assessment and final evidence remain explicit operational steps. Do not mistake generated metadata for completed deployment.