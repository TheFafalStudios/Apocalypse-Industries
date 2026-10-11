# Chunkserve access workflow

## Reconnect without asking for connection details

The user requested persistent access on 2026-09-26. For a user-requested server task, reuse the saved connection. Do not ask again for its address, username, password, or authorization merely to run the read-only connection check.

1. Read AGENTS.md and this document.
2. Read `.local/chunkserve/connection.json` for the endpoint, account, key path, pinned host-key file, twin location, and confirmed protected roots. This local profile is excluded from Git and Packwiz.
3. Run `./scripts/Test-ChunkserveAccess.ps1`. It uses Windows OpenSSH and the loaded Windows SSH agent. It only requests the working directory and disconnects.
4. Use the same profile for task-specific SFTP operations. Preserve strict host verification, public-key-only authentication, and noninteractive batch mode. Use the native Windows OpenSSH executables; the installed WinSCP version may not share the Windows SSH agent.
5. Report only checks actually completed. A successful login does not validate server health or modpack compatibility.

No permanent connection or background monitoring is required. Reconnect for each task. This workflow does not depend on a temporary Python installation or the previous one-off synchronization scripts.

## Scope and protected data

Routine connection checks and relevant read-only inspection are authorized when needed for a requested Chunkserve task. Remote edits must be within the user's requested task; login access alone does not authorize deployment, deletion, restart, or changes to the twin. Do not ask again for actions already authorized in that task. When both client and server updates are requested, no post-push reconfirmation is needed. If only one is requested, confirm updating the other to maintain the shared version.

The user confirmed on 2026-09-25 that all world/save and schematic storage was under `/world` and `/schematics`, with no other custom locations on the twin. Exclude these roots BEFORE traversal. Also protect all names and paths covered by AGENTS.md, including any subsequently identified custom level-name, dimensions, schematic storage, links/junctions, and Distant Horizons generated data. Read server.properties only when necessary to identify the protected level-name. An access check does not require launching a world. World backups and local testing copies are permitted under the approved AGENTS.md safeguards; exclude them from pack operations.

For file changes, select explicit permitted paths, keep rollback copies of permitted files, and verify transferred bytes. Never perform a blanket recursive root download, mirror, or cleanup. Live logs and temporary profiling files can change during transfer; report snapshot exceptions honestly. Follow docs/SERVER_RELEASES.md and AGENTS.md for release packets and compatibility markers.

## Credentials and recovery

The private key stays in the user's Windows `.ssh` directory, outside the repository. The profile contains only its path and public fingerprint, not the key or passphrase. The dedicated known_hosts file pins the server key accepted during the successful 2026-09-25 connection; this was first-use trust, not independent provider verification.

- If access works, no user input is needed.
- If the key is not loaded, ask the user to run `ssh-add "$env:USERPROFILE\.ssh\chunkserve_codex"` in their own PowerShell and enter the passphrase locally. Never ask them to paste the passphrase or private key into chat.
- If the SSH agent is stopped, diagnose its service state. It was verified Running with Automatic startup on 2026-09-26. Do not recreate keys as a troubleshooting shortcut.
- If host verification fails, stop and verify the new fingerprint with the provider/user. Never use StrictHostKeyChecking=no, accept-new, or replace the pin automatically.
- If authentication fails while the correct key is loaded, check whether the account key or permissions were revoked. Do not fall back to rotating panel passwords.
- If the local profile is missing on another computer, the endpoint and account must be restored locally and the key securely provisioned by the user. This setup persists on this computer; a Git clone alone does not carry private credentials or the local profile.
- If the terminal sandbox cannot initialize or denies network access, request a narrowly scoped tool escalation. That is separate from asking for connection details. Never work around an approval rejection.

## Local evidence

The 2026-09-25 twin snapshot report and rollback copies are in `.local/chunkserve-sync-20260925/`. Those are historical records, not proof of current remote contents. The old compatibility marker was preserved without revalidation.