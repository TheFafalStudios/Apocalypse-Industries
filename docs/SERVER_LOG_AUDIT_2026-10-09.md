# Server log audit — 2026-10-09

## Deployment follow-up — v1.1.12

The first 518-table loot fix is now published to GitHub main/tag v1.1.12 (87025ab85c18dfce54cb1aa065de4d2920b026a8) and deployed to the client, twin and Chunkserve with matching SHA-256 for every affected file. Versioned records: `Q:/Other Games/ATLauncher/servers/Apocalypse Industries versions/v1.1.12/attempts/lootguards-20261009-<target>/`. The concurrent v1.1.11 positional-wind release is preserved. Baseline remains unverified; no restart or reload was performed. Restart is required and is user-managed. Measured error reduction/gameplay checks remain pending. The initial read-only audit and prepared candidate details below describe the pre-deployment evidence.


## Result and first fix candidate

Reviewed 23 main server log files: 22 October archives plus latest.log, downloaded read-only from Chunkserve. Also inspected KubeJS startup/server logs and downloaded JadeErrorOutput.txt. Current snapshot: 2026-10-09, startup at 21:09 server-log time; log timezone was not independently verified. No active client, twin or remote files were changed. No publication, reload, restart, world access or compatibility-marker advance occurred.

The current snapshot contains 1122 ERROR/FATAL headers and 536 WARN headers. Repeated traceback lines are not counted as separate errors. These counts describe log messages, not unique bugs or measured performance costs. Main-log archive/current snapshots are counted once; KubeJS diagnostic copies are not added to the main-log totals. Active latest.log is a downloaded snapshot and can grow; its local checksum does not prove a frozen remote source.

First candidate targets **1,036 messages (about 92.3% of current ERROR/FATAL headers)**: 256 Design n' Decor tables, 246 Steam 'n' Rails tables and 16 Create Connected tables. Each table failed twice during startup/resource loading. Their single item entries refer to unregistered optional content. Design n' Decor failures span its extended palette; Create Connected failures reference Dye Depot variants; Railways failures reference optional track integrations.

Prepared **518 JSON overrides** under the private audit candidate directory. Each preserves the original table's type, pools, rolls, explosion condition, item entry and random sequence, adding only a root `neoforge:conditions` item-existence check. A present item retains the original table; an absent item produces an empty table through NeoForge's supported conditional loading. This guards broken optional resources; it does not install missing mods or enable their blocks. The candidate intentionally covers only observed tables, not all loot resources.

Static checks passed for all 518 files: valid JSON, exact semantic preservation of the original body, exactly one matching item-existence condition, matching missing-item evidence, and no existing same-path client/twin file. SFTP reported the three remote target loot directories absent. Gameplay/resource-loader verification remains pending; the predicted reduction has not been observed after activation.

## Baseline and rollback evidence

Published tag v1.1.10 was verified through the remote tag listing. Source mod payloads match that tag (Packwiz SHA-1 for Design n' Decor/Create Connected; published binary SHA-256 for Railways). Client, twin and downloaded remote jars match SHA-256 byte for byte:

| Source jar | SHA-256 |
| --- | --- |
| Design-n-Decor-1.21.1-2.2b.jar | `af0264e8ba44b77744d9a6173b79fcc39758c5b975fbb377d4000e85bfcd2c8f` |
| railways-0.2.1+neoforge-mc1.21.1.jar | `b7636c8b1b0352ed1a130dfe67f8bb574e2fc08803ed1cda4d3ea00505193914` |
| create_connected-1.3.2-mc1.21.1.jar | `90bdceac63cb0ebd5e51351e22136182b996512edef937d0635d2857b0a3f24b` |

The published kubejs/data directory has no affected namespaces. Relevant remote target namespaces had no loot override directories; no conflicting quest/config changes were overwritten or promoted. This audit is not a full quest/config drift catalogue or full server compatibility validation. Unrelated working changes are preserved.

The candidate is additive: active target files are currently absent. Exact proposed paths and hashes are in candidate-manifest.json; original packaged resources are preserved in original-resources. A rollout must recheck target absence immediately before writing and record rollback as removal of precisely those newly added files. It must not delete namespace trees or unrelated resources.

## Frequency queue

Sorted by message count in the latest snapshot. October totals combine all reviewed main-log snapshots, including repeated startups; they must not be interpreted as continuous runtime rates. Related symptoms can appear in multiple rows (e.g. a client-class ERROR and its paired mixin WARN).

| Group | Latest snapshot | October reviewed total |
| --- | ---: | ---: |
| Loot tables / dndecor | 512 | 7680 |
| Loot tables / railways | 492 | 7380 |
| KubeJS recipe fallback warnings | 246 | 3690 |
| mixin/ WARN | 198 | 2954 |
| Loot tables / create_connected | 32 | 480 |
| Propulsion missing optional fuel overrides | 32 | 480 |
| Curios API/ ERROR | 24 | 360 |
| Recipes / parse failure | 20 | 300 |
| KubeJS crafting category warnings | 18 | 270 |
| Client classes requested on dedicated server | 13 | 195 |
| Broken tags | 8 | 120 |
| Recipes / missing optional recipe type | 8 | 120 |
| Loot tables / tracks | 6 | 90 |
| net.minecraft.core.MappedRegistry/ WARN | 5 | 75 |
| Biome Replacer rules / malformed | 5 | 75 |
| Loot tables / golemdungeons | 4 | 60 |
| Recipes / invalid JSON | 4 | 60 |
| Biome Replacer rules / missing biome | 3 | 45 |
| net.minecraft.server.packs.VanillaPackResourcesBuilder/ WARN | 2 | 30 |
| Loot tables / modulargolems | 2 | 30 |
| net.minecraft.advancements.AdvancementTree/ ERROR | 2 | 30 |
| net.neoforged.neoforge.common.loot.LootModifierManager/ WARN | 2 | 30 |
| net.neoforged.neoforge.registries.DataMapLoader/ ERROR | 2 | 30 |
| ModernFix/ WARN | 2 | 30 |
| net.neoforged.neoforge.server.ServerLifecycleHooks/ WARN | 2 | 30 |
| DistantHorizons-DistantHorizons-com.seibel.distanthorizons.core.Initializer/ WARN | 2 | 30 |
| net.minecraft.server.packs.PathPackResources/ WARN | 2 | 30 |
| net.neoforged.jarjar.selection.JarSelector/ WARN | 1 | 15 |
| KubeJS/ WARN | 1 | 15 |
| org.patryk3211.powergrid.PowerGrid/ ERROR | 1 | 15 |
| net.neoforged.fml.VersionChecker/ WARN | 1 | 17 |
| net.minecraft.world.level.biome.MobSpawnSettings/ ERROR | 1 | 15 |
| BiomeControlEngine/ WARN | 1 | 15 |
| net.minecraft.server.MinecraftServer/ WARN | 1 | 14 |
| DistantHorizons-DistantHorizons-com.seibel.distanthorizons.core.network.session.NetworkSession/ ERROR | 1 | 4 |
| net.minecraft.util.thread.BlockableEventLoop/FATAL ERROR | 1 | 4 |
| net.neoforged.neoforge.network.registration.NetworkRegistry/ ERROR | 1 | 4 |

## Next investigations

1. **KubeJS fallbacks: 246 recipe warnings plus 18 crafting-category warnings.** The log explicitly falls back to the vanilla recipe loader. KubeJS's final diagnostic reports 16/16 server scripts loaded with no script errors/warnings, and 44 recipes added, 170 removed, 23 modified, zero failed custom recipes. Many fallback warnings reflect nested output items or fluid formats that the bundled KubeJS Create schemas do not accept. Others contain empty optional ingredient tags. Do not delete these recipes wholesale. Review each affected source recipe against its actual loader and the pack's replacement/removal scripts. Separate final RecipeManager parse failures from successful fallbacks.
2. **Mixin warnings: 198.** Includes missing reference maps, optional class targets, client-class access and overwrite conflicts. Missing reference maps alone do not prove a broken mod. Identify owners and whether hooks actually fail before replacing mods or disabling optimizations. Thirteen RuntimeDistCleaner ERROR messages also occur separately.
3. **Propulsion fuel overrides: 32.** Optional unregistered fluid overrides are ignored. Inspect relevant config versus intentional pack fuels; preserve valid fuels and progression. No config change was prepared.
4. **Curios slot errors: 24.** Twelve missing slot names reported twice. Local packaged source is Modular Golems' golem_curios.json, which lists optional integration slots together with existing slots. Confirm remote source bytes and installed slot providers before preparing a conditional compatibility fix. Do not create arbitrary player slots as a log-suppression workaround.
5. **RecipeManager parse errors: 20 headers.** Some resources are reported twice under different parse messages and twice during reload. Examples: Create Deco placard ingredient uses `id` where `item` is expected; Aerocables recipes reference absent optional cable items; CBC AT mould output uses an incompatible result format; Create Cafe blood tea references a tag as an item. Identify legitimate recipes versus absent-mod compat and preserve progression before fixing.
6. **Remaining loot/tag/config issues.** Tracks loot uses uppercase `Tracks` resource namespaces (6 messages); Tracks block tags fail (6 of 8 tag messages). The apocalypse create_rns deposits tag accounts for the other 2 tag messages. Golem Dungeons references absent Twilight Forest items (4 loot messages); Modular Golems guide references absent Patchouli (2). Biome Replacer reports five malformed lines and three missing biome rules. Review exact config files against published/client/twin/remote versions before editing.

## Operation and performance findings

- **Distant Horizons 3.2.0-b: four failed-message sequences** across reviewed October logs (two October 8, two October 9). Each sequence includes a network message failure, a server task exception and a failed synchronized payload; these are linked symptoms of four events, not twelve independent failures. Exception text references `IncompatibleMessageInternalEvent{}` and a Log4j placeholder-count failure inside DhLogger. Log severity alone does not establish a full server crash. Verify joining clients' exact DH versions and the packet mismatch, then review the logging bug/upstream fix. The existing DH 3.3.3 review notes a config schema reset and shader/multiplayer testing requirements; this audit does not authorize that upgrade or prove it fixes these events.
- **Power Grid 0.5.5.1: native solver unavailable.** The server log directly reports `GLIBC_2.38` missing in the host libc while loading its native library. Power Grid disables its accelerated solver. This is a concrete possible performance issue despite low message count. A provider-compatible native build or hosting image change may be required; no host/library changes or mod removals were made. The performance impact is unmeasured.
- **14 “Can't keep up” warnings** occur across the reviewed files. They warrant correlation with loading, joins, generation and profiling; they do not identify a culprit by themselves. No profiler was started and no load was added to the live server.

## Rollout gate

First batch is prepared, not deployed. AGENTS.md and docs/SERVER_RELEASES.md require confirming the other side when only server work was requested, and confirming reload/restart timing. Before publication/deployment, obtain the client's inclusion or explicit temporary mismatch agreement. Prepare the shared release and per-target deployment records with exact-path rollback, verify each deployed hash, and coordinate activation. Then compare a fresh startup/resource-loading log against the 518 table IDs and verify representative ordinary block drops remain intact. Static JSON checks and successful upload must not be called gameplay validation.

## Evidence locations

Private audit root: `.local/server-log-audit-20261009-2113/`.

- `candidate/`: proposed pack payload only (518 loot JSON overrides).
- `candidate-manifest.json`: exact affected paths, source/candidate hashes, target-before states and validation status.
- `original-resources/`, `remote-mods/`, `published-baseline/`: narrowly scoped source/rollback evidence.
- `published-baseline-evidence.json`: v1.1.10 mod and namespace comparison.
- `ranked-summary.json`, `frequency.json`, `snapshot-sha256.json`: frequency evidence and downloaded snapshot checksums.
- Main logs and KubeJS logs remain private; do not publish raw player/network details.

NeoForge conditional-loader specification: https://docs.neoforged.net/docs/1.21.1/resources/server/conditions/ . It confirms root conditions are evaluated before decoding, false loot conditions produce empty loot tables, and item_exists tests registry presence.
