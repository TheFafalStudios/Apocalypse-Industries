# Queued for next distribution: Echo Plate JEI visibility

Status: implemented in local WIP instance; not published to GitHub or deployed to the server.

Addon: Apocalypse Protection Pixel Integration 1.1.1
Item: apocalypse_pp:echo_plate (Echo Resonance Plate)

Cause: JEI 19.39.0.368 enumerates creative-tab contents by default. The addon registered the item without adding it to a tab; showHiddenIngredients=false excluded it.

Fix: register a BuildCreativeModeTabContentsEvent listener on the mod event bus and accept the Echo Plate into ProtectionPixelModTabs.PP. Standard accept exposes it to both the tab and search. No global JEI setting change is required.

Validation: compiled against pinned Minecraft 1.21.1 / NeoForge 21.1.248 / Protection Pixel 2.2.1; 445 existing static assertions passed; compiled event handler inspected. Binary comparison against released 1.1 shows only ApocalypsePP.class and addon version metadata changed. Local Packwiz entry/hash and pack index hash verified; exactly one active addon JAR. Client UI verification remains pending.

Next release: distribute mods/apocalypse-protection-pixel-integration-1.1.1.jar and remove the obsolete 1.1 JAR from managed installations. Rebuild the filtered release snapshot using the normal workflow. Restart the WIP client and search JEI for Echo Resonance Plate with showHiddenIngredients=false; confirm it also appears in the Protection Pixel creative tab. Retain this fix in the next server packet through the existing server-update confirmation workflow.

SHA-256: 968572cf05dc4ada20ef4046c37955be6914b21d4dd9eeae849068d8b89dee4a
