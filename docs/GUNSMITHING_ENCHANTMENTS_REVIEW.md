# Gunsmithing enchantments implementation review — 2026-10-03

Historical review of 1.0.0: user confirmed working in-game on 2026-10-03. Superseded locally by 1.1.0; see GUNSMITHING_ARMOR_BYPASS.md.

Result: accepted for local WIP installation; no blocking implementation issue found in the performed checks.

Installed `mods/apocalypse-gunsmithing-enchantments-1.0.0+mc1.21.1.jar` unchanged from the user attachment.
SHA-256: `8c0a3989066ef89c6bb8f4074b1cabeb3c48614ffa954372a117109f046208f7`.

## Verification

- 27 static/build checks passed: rebuilt all six classes with Java 21, compared disassembled compiled methods/signatures/constants against the supplied JAR, compared every source resource byte-for-byte, parsed JSON, checked all ten required injection targets against the installed dependency JARs. No stub classes are bundled and no existing addon ID or package collision was found.
- Exact runtime dependencies present: Minecraft 1.21.1, NeoForge 21.1.248, Create: Gunsmithing 1.4.9, NTGL 3.1.8, Create: Enchantment Industry 2.5.3b.
- 49 lookup checks passed against the actual mapped Minecraft classes using named enchantment holders and actual ItemStack/ItemEnchantments components, including empty, absent and above-max-level lookups.
- 42 supplied-JAR effect checks passed through direct isolated invocation with actual NTGL WeaponData and Mixin CallbackInfoReturnable classes. Reload start/main/end, firing cooldown, ADS speed and damage were checked at levels 0, 1, 2, 3, 4, 5 and 10. The fixture bypassed WeaponData construction; this verifies hook calculations, not mixin transformation or gameplay.
- Required target signatures, NTGL ADS speed consumer and firing cooldown helper were inspected. The existing NTGL ADS transition addon targets different classes; no direct injection-target overlap was identified.
- CEI data-map path matches the installed mod, its level-extension codec accepts the supplied keys, and additive rules preserve existing entries. In-game Blaze Enchanter/Forger behavior remains untested.

## Intended behavior

| Enchantment | Normal maximum | Normal effect | CEI +1 effect |
|---|---:|---|---|
| Quick Hands | II | 70% / 40% reload phase time | III: 10% |
| Trigger Finger | II | 75% / 50% firing cooldown | III: 25% |
| Lightweight | I | 1.5x ADS transition speed | II: 2x |
| Sharpshooter | IV | 1.25x / 1.5x / 1.75x / 2x final helper projectile damage | V: 2.25x |

Ticks round to whole ticks and positive durations have a one-tick minimum. Seven guns are tagged; the hammer and frag grenade are excluded. These balance values are the supplied implementation, not newly selected balance changes.

## Limits and follow-up

Full pack startup, actual mixin application, enchanting table/anvil behavior, CEI processing, client/server firing synchronization, attachment interactions and gameplay balance were not tested. Restart the WIP client to load the addon, then check each enchantment normally and at CEI's extra level. Gunsmithing currently returns false from its own isFoil method, so the addon does not guarantee visible enchantment glint.

EnchantmentLevels publishes its first cached reflection Method before the other cached methods. Concurrent first calls could briefly return level zero and log an error; the completed initialization is usable on later calls. This is a minor hardening opportunity, not a blocker observed by the checks.

## Scope and rollback

Only the addon JAR, its Git ignore exception, the unreleased changelog entry and this report were added to the WIP workspace. Existing mods, launcher metadata, release version markers and distribution manifests were preserved. No publication, server deployment, reload/restart, save or schematic operation occurred. Distribution generation already includes standalone JARs without external download references, so the next filtered release build will include this addon.

Rollback: remove this specific addon JAR while the client is stopped and remove its corresponding changelog/ignore entries. External review evidence and original attachments are preserved at `Q:/GPT General/gunsmithing-enchantments-review-2026-10-03`.
