# Pending v1.2 — Weapon model library

Status: prepared locally for integration into v1.2; not published or deployed. This component captures the work in the weapon-library chat. Other WIP contributions are preserved and must be integrated separately into the final release.

The client gains the consolidated Nongko/Prominence/Majestica weapon model library, 262 assigned RP Renames entries and 17 retained unassigned designs. Removed families and rejected duplicates remain archived outside the active pack. The self-contained catalogue provides textured 3D previews, categories, material restrictions and approved exceptions.

Names now accept words before and after the full model name and styled text (font/color/bold/italic), including legacy section-sign formatting between letters. Longer overlapping names win. Historical aliases distinguished only by capitalization retain that distinction. Unnamed items keep normal models; weapon/material eligibility remains as approved.

The client rendering bridge 1.3.2 preserves named trident models and textures in flight, with normalized held/throwing poses and scale. Heavenly Trident was removed. It also fixes the spear mods overriding named inventory and RP Renames preview models. Existing mace assets in the shared pack are preserved.

Runtime requirements: Minecraft 1.21.1, NeoForge 21.1.248, existing Polytone 1.21-4.1.0, RP Renames 0.9.2, Connector 2.0.0-beta.17 and Forgified Fabric API 0.116.15+2.3.5+1.21.1. The four new JAR manifests are client-only. Polytone's published both-side reference remains unchanged. No server payload, recipes, combat balance, worlds or schematic storage are part of this component.

Validation: 9,721 Java name-pattern checks, unnamed-item guard regression, ZIP/JSON checks and byte-identical source rebuild of the installed runtime classes. The user confirmed “all seems well” after the final spear GUI fix on 2026-10-11. Fresh-install/update validation of the combined v1.2 release is still required.

## Release integration

Merge the prepared overlay into a clean checkout of published v1.1.15 (c6ddd03f254e1d2578967993d0d2bbfebb003c53), alongside other approved v1.2 contributions. Include all four client JARs and their .pw.toml files. Their SHA-256 hashes pin the working binaries; download URLs use the future v1.2 tag and cannot be tested until publication. Include only the latest rendering addon, 1.3.2; remove older copies when upgrading a development installation. No addon existed in the published v1.1.15 manifest.

Add file/Apocalypse_Weapon_Library.zip to portable fresh-install resource-pack defaults. Keep options.txt preserve=true; existing players can enable the library in Resource Packs without resetting their preferences. Do not copy this computer's full options.txt or absolute paths into distribution.

Set VERSION and pack.toml to v1.2 in the final clean release checkout, rebuild the full Packwiz index and bootstrap, validate the combined client/server release and assign the shared release version only after each applicable payload has been checked. This component fragment is not a complete pack manifest. Existing server baseline remains unverified; do not relabel it to claim full alignment. Publication, deployment and activation are pending; server restarts/reloads remain user-managed.

## Source and evidence

Addon Java source and build resources: scripts/weapon_library. Build with Python 3.11+ and Java 21 using build_addon.py --output <new external directory>. The build extracts MixinExtras from the pinned NeoForge runtime and leaves the installed JAR untouched. Catalogue preview builder and template are included. Catalogue JSON documents all approved targets and names.

Historical rollback/model archives remain in Q:/GPT General/Weapon Model Library. Release evidence and exact payload checksums are stored under the v1.2 weapon-library preparation attempt. No historical backup packs or excluded models should be copied into the release.
