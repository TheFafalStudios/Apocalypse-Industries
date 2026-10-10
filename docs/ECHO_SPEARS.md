# Spears and Echo Spear integration

Local development, Minecraft 1.21.1 / NeoForge 21.1.248. Updated 2026-10-10.

## Current Echo Spear (addon 1.0.6)

`apocalypse_echo_spears:echo_spear` is a copy of Backported Spears' Netherite Spear, with its own Echo Spear name and the approved smithing recipe. The only stat difference is **0.1 fewer attacks per second**. Damaging melee hits also apply the Echo Sword status effects.

The model shape, held transforms, damage, reach, charge conditions, piercing, animations, sounds, durability, fire resistance, tier, enchantability and repair material follow the upstream Netherite Spear. Inventory and held textures now use the chestplate-derived Echo palette. Repairs use Netherite Ingots. Weakness II and Darkness II last five seconds, matching the Echo Sword. Both drop to level I when Create: Deep Dark's strong_sword setting is false. No custom durability/repair/enchantment bonuses are added. The previously created box model is replaced by the upstream Netherite model structure, with references redirected to the recolored Echo inventory and held textures.

Smithing remains unchanged: **Echo Upgrade Smithing Template + Netherite Spear + Echo Ingot**. Uses Create: Deep Dark's existing template and ingot. Standard smithing preserves the base item's custom name, enchantments and existing damage. Listed in the Combat creative tab with the spear and applicable weapon enchantment tags, including Lunge.

## Upstream and compatibility

- [Backported Spears](https://modrinth.com/mod/backported-spears): NeoForge 1.8.0, Modrinth version FROwtXRs. Official SHA-1: `944b22a7fa34c1e87e70f4558c9164d950646dd6`. No additional dependencies.
- [Create: Deep Dark](https://www.curseforge.com/minecraft/mc-mods/create-deep-dark): installed/latest listed release 3.0.2 contains no spear. No compatible Echo Spear integration was found in the checked upstream pages and searches.
- Seven upstream optional integration recipes have item-existence conditions, and two optional item tags mark their unchanged values optional. Missing Copper Age/Oreganized/Dragon Loot/Enderite content is skipped. The upstream JAR is unchanged.
- The inventory rendering fix from 1.0.1 remains byte-for-byte unchanged; the user confirmed it fixed the base icons. It selects the inventory model before transforms in GUI/ground/fixed contexts, preserving held rendering.

## Build, validation and activation

Source: `scripts/echo_spears/src/dev/apocalypse/spears/EchoSpears.java`.
Build: `python scripts/echo_spears/build.py`. Uses installed Java 21 and pinned client/server library classes. Build outputs, isolated test world and rollback JARs remain outside the pack under `Q:\GPT General\echo-spears-20261010`.

Remake evidence: `runtime-test-remake.log`, `verify_remake.py`, `remake-tests/RuntimeTest.java` and `remake-installation.json` in that external directory. **47 native dedicated-server assertions passed.** Attack speed is 0.8695652485 -> 0.7695652485 attacks per second (displayed approximately 0.87 -> 0.77). Native tests compare every unchanged item component and tier, verify the exact speed delta and unchanged other attributes, repairs, smithing matching/output/name/damage retention, tags, hit durability and absence of the removed status effects. Static validation verifies an exact model copy, byte-identical recipe and icon fix, and absence of stale/custom-tier or test classes in the production JAR.

Client visuals for the remade Echo Spear, full-pack gameplay and multiplayer balance remain pending. The earlier 29-check evidence applies to the superseded initial design and must not be interpreted as the current specification.

Installed locally as `mods/apocalypse-echo-spears-1.0.8.jar`, alongside Backported Spears. No pack release label change, publication, twin update or remote deployment. Restart the client to load the new item defaults. Both new mods are required on client/server for a future release; server activation remains user-managed.

## Right-click correction (1.0.3)

The Netherite Spear subclass implements use and getUseDuration in addition to the methods copied in the remake. Their omission caused Echo's right-click to return the ordinary item's PASS result and duration zero. Both now delegate to the upstream Netherite Spear, starting item use and providing its sustained-use duration (72,000 ticks when charge attacks are enabled). This also follows upstream behavior when charge attacks are disabled.

67 isolated native server assertions passed: the 47 remake checks plus right-click use through ItemStack.use in both main hand and offhand, consumed result, same stack, correct hand, active-use state and remaining duration, a real item-use tick and stopping use. Byte comparison confirms only EchoSpear's two item-use methods and the addon version changed; recipe, model, icon fix and stats are unchanged. Client input and gameplay verification remain pending the user's next launch.

Evidence: runtime-test-right-click.log, right-click-tests/RuntimeTest.java, verify_right_click.py and right-click-installation.json under the external build directory. Previous local addon is preserved in right-click-rollback. Restart the client to activate; no server deployment was performed.

## Restored effects and description (1.0.4)

Restored the on-hit Weakness/Darkness effects at the user's request after they confirmed right-click charging works. Effects are applied on positive direct melee damage, including an actively used Echo Spear in either hand; rejected hits and projectiles do not apply them. Duration is 100 ticks, and levels follow Create: Deep Dark's strong_sword configuration. Repeated hits refresh duration without accumulating levels.

The tooltip matches the Echo Sword's three description lines, their ordering immediately after the item name, gray text and red effect names: "On hit, inflicts to target:", "- Weakness II (5 sec.)", "- Darkness II (5 sec.)". The descriptions also follow the sword's lower-level config variant.

**97 native dedicated-server assertions passed.** Runtime evidence is runtime-test-effects-attempt2.log and effects-tests/RuntimeTest.java under the external build directory. Native tests compare all three formatted tooltip lines directly against the installed Echo Sword tooltip procedure in both config modes, exercise main/offhand effects and duration, and retain the right-click regression checks. The model, recipe, tags and icon fix remain byte-identical to 1.0.3. Actual mounted charge gameplay and client tooltip appearance remain pending in-game confirmation.

Prior addon preserved in effects-rollback. Local development update only; restart the client to load it. No twin/remote deployment or publication.

The first test attempt assumed the upstream client-only tooltip subscriber was registered on a dedicated server. The harness was corrected to invoke the upstream tooltip procedure directly for comparison; no production code changes were needed. Both attempt logs are preserved.

## Chestplate-derived texture conversion (1.0.5)

At the user's request, compare vanilla Netherite chestplate and Create: Deep Dark Echo chestplate at the same 16x16 pixel coordinates. The source armor is not a one-to-one palette swap: some source colors have multiple target colors, and two opaque armor pixels become transparent. Preserve the spear shape rather than transferring those armor-specific geometry changes.

Use the most frequent opaque target for each source color, resolving equal counts with the darker target. Spear colors absent from the chestplate use the nearest Netherite chestplate RGB shade, with squared RGB distance and darker-source tie breaking. This supplies a deterministic, auditable conversion while staying within the observed Echo palette. All alpha values and fully transparent RGB pixels are unchanged. Both the 16x16 inventory and 32x32 held texture use the same conversion per source color.

The complete coordinate trace, ambiguous alternatives, chosen palette mapping and source/output hashes are in texture-conversion/palette-conversion.json under Q:\GPT General\echo-spears-20261010. The spear-color table is palette-conversion.csv in that directory; preview.png shows the references and both before/after spear textures. Generator: scripts/echo_spears/recolor.py, requiring Python with Pillow. Run it before build.py to regenerate texture assets.

Static validation confirms the installed texture sizes, original alpha and shape, original model structure/transforms, and byte-identical gameplay classes, recipes, tags, tooltip/effects code and inventory Mixin. No runtime tests were repeated for this asset-only change; the preceding gameplay checks remain applicable to unchanged bytecode. Actual client texture appearance remains pending confirmation. The user has confirmed the status effects, description and charging from 1.0.4 work.

Installed locally; prior JAR and builder preserved in texture-rollback. Restart the client to load addon 1.0.5. No twin/remote deployment or publication.

## Preserve light brown shaft (1.0.6)

The user corrected the recolor: light brown wood must keep its original color. Exclude original shaft shades #603432 (96,52,50) and #734543 (115,69,67) from the Echo palette conversion. Restored exactly six inventory pixels and seventeen held-texture pixels to their upstream RGBA values. Every other pixel is unchanged from 1.0.5, including the Echo spearhead and dark shades. Gameplay, models, tooltip and icon fix are byte-identical.

Generator now preserves these wood colors on regeneration. Revised trace and preview are in texture-conversion-handle-fix under the external build directory; the original conversion reports remain intact. Evidence: verify_handle.py and handle-installation.json. Prior JAR/textures/generator preserved in handle-rollback. Installed locally; restart the client to load 1.0.6. No server deployment or publication.

## Restore spearhead bevel shading (1.0.7)

The nearest-color conversion merged the purple outer edge (#4a2940) with its neighboring bevel (#4b3f46), and the intermediate highlight (#584e56) with the brighter highlight (#5d565d). Restore those two distinctions in both textures: the outer edge uses the observed Echo shade #062d31; the intermediate highlight uses #0b555e, interpolated between the traced chestplate anchors #4d494d -> #0a4950 and #5d565d -> #0d626c. The brighter highlight remains #0d626c.

Exactly fifteen inventory pixels and seventeen held pixels changed. Every other pixel, original brown wood, alpha, geometry, gameplay class, model, recipe, effect and tooltip remains unchanged. Pixel assertions and JAR byte comparisons passed; enlarged previews inspected. Evidence: verify_shading.py, shading-validation.json and texture-conversion-shading-fix under the external build directory. Prior files preserved in shading-rollback. Addon 1.0.7 installed locally and its SHA256 verified. Restart the client to load it; in-game appearance awaits user confirmation. No server deployment or publication.

## Original shaft outline (1.0.8)

Restore the original outline around the light brown shaft using a coordinate mask rather than a global palette change. The shaft's dark #231012/#2f2122 border and held texture's #4a2940 butt border retain upstream RGBA values. Pixels sharing those colors on the Echo spearhead keep their recoloring and restored bevel shading. Decorative shaft accents retain their existing Echo conversion.

Seventeen inventory pixels and fifty held pixels restored. All other pixels and all alpha values are unchanged. Exact changed-coordinate comparison, original-color checks, brown wood preservation, and byte comparison of all gameplay/model/recipe resources passed. Preview inspected. Evidence: verify_shaft_outline.py, shaft-outline-validation.json and texture-conversion-shaft-outline-fix in the external build directory. Prior JAR, textures and generators preserved in shaft-outline-rollback. Addon 1.0.8 installed locally with verified hash; restart the client to load it. In-game appearance awaits user confirmation. No publication or server deployment.
