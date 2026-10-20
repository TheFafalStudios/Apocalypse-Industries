# Test workflow improvement record

Standing user instruction, 2026-10-19: in each testing session, the assistant independently reviews and tries to improve one useful part of the workflow. The trigger is recorded in the root AGENTS.md so it applies to future project chats without reminders from the user.

## How to record a review

For each distinct session, append a short entry with the test/evidence reference, observed weakness, improvement implemented (or reason none was justified), verification, limitations and any effect on comparability. Keep completed entries intact; record corrections separately. One session may contain many test commands; do not manufacture one change per assertion.

## 2026-10-20 — Release inventory v2

- Weakness: historical release traversal could include diagnostic artifacts or timestamped backups, use an obsolete shader filename, and mistake legitimate worldgen, AdLods and blueprint recipe folders for protected storage.
- Improvement: introduced a pruned, link-aware inventory shared by release preparation, selected the active shader from its configuration, excluded local diagnostics and the unfinished endgame quest prototype, and corrected deployment-record protection rules. Read-only SFTP snapshots preserve target configs and support explicit affected-file retrieval without touching worlds or trust pins.
- Validation: four release-safety and six deployment-record regression tests passed. They verify storage pruning before traversal, link rejection, private-file exclusions, retention of worldgen/ore/blueprint recipes, rollback hashes and prepared-only status. Final v1.4 candidate manifests and target comparisons are under the separate server release `_preparation/v1.4-20261020` directory.
- Comparability: final assembly uses release-inventory-v2. Preliminary client-01 was superseded after discovering overly broad exclusions; it is not the release candidate. No gameplay or performance improvement is inferred. Remote snapshots are read-only observations, not atomic snapshots or post-deployment verification; affected hashes must be checked again before deployment.

## 2026-10-19 — Persistent review trigger

- Weakness: workflow improvements depended on conversational reminders and could be forgotten between chats.
- Improvement: added a standing project instruction and this evidence ledger. Reviews must be owned by the assistant and preserve the method within compared runs.
- Verification: confirmed the instruction was appended while preserving the existing AGENTS.md content. This establishes the review procedure; it does not demonstrate a game-performance improvement.
- Comparability: no game code, recording method, settings or existing capture data changed.

## 2026-10-20 — Submarine balance and next-update preparation

- Weakness: formula/bytecode checks alone cannot detect failed runtime mixin application, registry classification or loss of a fitting's saved upgrade. Preparing from historical notes can also downgrade newer compatible artifacts or accidentally include generated storage.
- Improvement: added `scripts/submarine_balance/native_test.py` and a separate test-only mod that exits at load completion before any world starts. It checks actual pressure-hook application and registry caps; the expanded method also exercises real block-entity pressure-jacket save/load. Added explicit-path `scripts/Prepare-SubmarineNetherUpdate.py`, which freezes current payloads by side, rejects protected/link paths before traversal, records source drift against the published commit/twin and verifies copied hashes.
- Validation: `.local/submarine-balance-20261020/native-03/balance-native-validation.json` passed runtime hooks/ratings. Expanded `native-04` passed recognized viewport caps and jacket persistence with the same production artifact. Policy/bytecode checks passed in `build-03/validation.json`. The preparation tool also includes a negative protected-path check without opening protected storage; its candidate manifest/checksums record snapshot verification.
- Comparability: native-01 was rejected for a split test-package setup; native-02 stopped at the EULA gate. Neither is gameplay evidence. Native-03 and native-04 use the same production JAR; native-04 adds coverage and uses the revised WIP JSON, so it is not an identical-input performance comparison. No performance gain is claimed.
- Limits: a reduced loader stack is not full-pack gameplay; assembly, moving ships, fluid transitions, instruments and fleet performance remain pending. The live comparison failed strict host-key verification; no remote trust or files changed.


## 2026-10-20 — Require representative shader gameplay

- Evidence: `.local/performance/capture-20261020-141837-9908f5f4/`; quality review in `.local/performance/reviews/capture-20261020-141837-9908f5f4-20261020-142916/`.
- Observed weakness: the 120-second wall-clock recording completed after only 17.73 seconds of eligible gameplay. Shaders were off in that gameplay section, then enabled while the game was paused. Whole-capture averages could obscure the mismatch. Scene GPU recording worked (5,256 samples); no shadow samples were recorded.
- Improvement: analyzer v2 separates sampled shader/dimension contexts, warns about absent shader gameplay, insufficient play, flagged-frame dominance and missing shadows. Reviews use a new output directory and preserve original artifacts. It also documents that row N's Pre-to-Pre interval precedes row N's CPU/GPU work, preventing invalid direct correlation.
- Recorder 1.0.1 now waits for focused, unpaused gameplay with shaders active. Warm-up and the recording budget count only eligible intervals, excluding state transitions. Raw menu/shader-off frames remain flagged; GPU timing is restricted to eligible gameplay. A 10-minute wall limit and existing capacity cap bound recordings. One improved automatic capture is armed.
- Verification: 24 Java storage/clock/settings/query/report checks passed, including paused-clock behavior; real GTX1060 query correctness passed. Analyzer assertions reproduce 663 eligible frames / 17.7340887 seconds and all four quality warnings; original capture file hashes were unchanged. New recorder lifecycle still requires live validation.
- Comparability: method is explicitly `eligible-gameplay-v2`; this is a fresh shader-gameplay baseline, not a controlled comparison with the earlier shader-off/menu-heavy recording. No game rendering settings or existing fog/lava fixes changed.
- Installation/rollback: `.local/performance/eligibility-fix-20261020-143125/installation.json`. Next normal client restart loads 1.0.1; no restart or reload was performed.

## 2026-10-20 — Freeze and audit the shader release payload

- Evidence: `Q:/GPT General/shader-release-prep-20261020/attempt-03/manifest.json`, `release-validation.json`, `shader-entry-changes.json` and `shader-source-changes.patch`. Attempts 01 and 02 are superseded preparation iterations; `CURRENT.md` identifies the reviewed candidate.
- Observed weakness: local Git HEAD and the broad distribution builder lag GitHub main, while WIP contains unrelated development and temporary performance recording code. Validating the live index or copying the entire instance could produce a valid-looking release with the wrong scope. A shader ZIP hash alone also obscures its source changes.
- Improvement: `scripts/prepare_shader_release.py` prepares from a pinned GitHub v1.3.2 archive, verifies the baseline index, screens protected/runtime paths before extraction and restricts live copies to named inputs. It rejects changes outside an explicit allowlist, verifies changed dependency download hashes, emits per-entry shader hashes/source differences, and records before/after hashes and exact rollback bytes. Client rendering addons install through client-only metafiles without duplicate direct-JAR index entries. Existing shader preferences and published hull-strength settings are preserved.
- Validation: the final candidate passed the distribution validator (1,747 entries), overlay/rollback content and hash checks, client-side metadata checks, published hull-config byte equality, and bootstrap integrity/loader/URL checks. Addon 1.1.2 rebuilt successfully; every ZIP entry matches the shipped installed artifact. A fresh run passed 31 fluid/source checks, four Iris/parser/GPU variants, 114 Vortex/GPU/state checks and 16 compatibility signatures. Archive timestamps explain the rebuilt whole-JAR hash difference; the installed artifact is shipped.
- Remaining limits: no fresh/update gameplay test, controlled FPS improvement, dedicated-server dependency validation, publication or deployment. The lower-sky fallback still assumes lava Y32; WIP's newer Y100 Nether generation is outside this shader-only payload. Client/server release alignment is a publication gate. The prepared bootstrap tracks live main and cannot preview an unpublished candidate.
- Comparability: method `pinned-baseline-allowlist-v1` freezes the release payload and tested shader hash. This packaging/regression session changes no measurement method, game rendering configuration, runtime JAR, world or server. WIP version remains v1.3.2; proposed v1.3.3 exists only in external staging.

## 2026-10-20 — Identify the actual gameplay instance

- Evidence: `Q:/GPT General/performance-github-instance-20261020/installation.json`, `baseline-before.json` and `baseline-installed.json`. User explicitly selected the local GitHub-fed `ApocalypseIndustriesGitHubd` instance, not WIP or the public distribution.
- Observed weakness: the baseline collector assumed WIP's root and shader filename. Reusing it for the released local instance would attach the wrong shader/configuration evidence to otherwise useful gameplay captures.
- Improvement: added `--instance`, explicit instance identity and baseline schema 2; the collector follows that instance's selected shader and records its hash/options. It checks links and accepts only a simple shader filename. Named input collection remains separate from protected saves, schematics and DH generated data. The installed post-change baseline is copied into each automatic capture by the existing recorder.
- Validation: validated recorder 1.0.1 against its tested artifact hash and the target instance's actual Iris ShadowRenderer class hash. Installed only the recorder JAR, armed one-shot config and local baseline; verified hashes. The baseline names ApocalypseIndustriesGitHubd and its older selected Complementary shader, matches the actual shader hash, and preserves all pre-install measured input hashes and client option values. Existing 24 recorder tests and GPU correctness evidence apply to the unchanged recorder artifact.
- Limitations: client restart required; none performed. Runtime hook application, capture completion and instrumentation overhead require live evidence. No uploads, GitHub edits, pack version/manifest changes, server changes or background monitoring. Capture output is local to the selected instance under `.local/performance/`.
- Comparability: recorder method remains `eligible-gameplay-v2`. This is a separate instance/baseline from WIP and must not be presented as a controlled shader performance comparison. The recorder waits for 60 eligible shader-play seconds and records 120 eligible seconds once; existing stop/capacity limits remain unchanged.


## 2026-10-20 — Cross-chat release integration and sea-level regression

- Observed weakness: individually validated preparations diverged at integration. The broad v1.4 candidate omitted the shader option/preserve flags, indexed the rendering addon as a common direct JAR, imported unrelated WIP configuration, and retained a Y32 fallback against a Y100 datapack.
- Improvement: `consolidate_v14_release.py` combines frozen preparations with an explicit runtime allowlist; `validate_v14_consolidation.py` checks both effective client/server installation plans, source-hosted artifact hashes, target-specific packet/rollback checksums, unchanged unrelated baseline entries, and the actual shader sea-plane calculation. Method: `cross-preparation-install-plan-and-sea-level-v1`.
- Validation: consolidated candidate has 1,798 verified index entries and 79 logical indexed changes; both narrowed server packets have 64 changes. All packet/rollback hashes and HYBRID values passed. Existing shader suite passed 31 fluid/source checks, four installed Iris/GPU variants, 114 Vortex checks and 16 signatures. A new 14-check GPU fixture covers above/below/at-sea, upward/horizontal/diagonal, underwater and horizon-clamped cases and compiles the full Nether deferred fragment. The same fixture rejects the original Y32 helper, establishing that it detects the observed integration bug.
- Evidence: `Q:/GPT General/apocalypse-v1.4-consolidated-20261020/attempt-01/FINAL_VALIDATION.json`, `SHADER_INTEGRATION.json`, `addon-validation/validation.json`, `addon-validation/sea-gpu.log`. Earlier preparations remain intact; this candidate supersedes their assembly instructions.
- Limits: these are packaging/offline GPU results. Moving submarines, fluid transitions, combined multiplayer, fresh/update gameplay, visual acceptance and performance remain unverified. Target snapshots require a drift check before any future deployment. No worlds, live settings, server markers or activation changed.


## 2026-10-20 — Validate the committed export before publication

The initial v1.4 Actions run 39429984826 found that config/almostunified/debug.json was indexed and present locally but excluded by its nested .gitignore. The corrected commit explicitly tracks its existing approved bytes and adds scripts/validate_release_export.py to CI. The new check validates a temporary Git archive, reproduces the original failure, and requires all indexed exported bytes/hashes to match. Indexed runtime contents and the staged server payload are unchanged. Export correctness is distinct from gameplay validation and user-managed activation.
