# Apocalypse Atmosphere port to Euphoria Patches

Date: 2026-10-08
Base: Complementary Unbound r5.8.1
Euphoria: 1.9.3 (official matching NeoForge patcher)
Source: ComplementaryUnbound_r5.8.1_APOCALYPSE_ATMOSPHERE_v1.1_PARSEFIX_TESTED.zip
Output: ComplementaryUnbound_r5.8.1_EuphoriaPatches_1.9.3_APOCALYPSE_ATMOSPHERE_v1.1.zip

## Tracked original changes

The source ZIP contains edit notes and static test reports. Its normalized diff against local stock confirms eight changed source files. Line-ending-only changes are excluded from the patch. The source atmosphere edit incorporates the earlier OPPRESSIVE_SUN edit.

- Procedural disc size thresholds: style 2 0.9975 -> 0.9944, with scale 400 -> 178.571; style 3 0.9983 -> 0.996175, with scale 588.235 -> 261.438. These are shared sun/moon calculations in the existing source; the port preserves that behavior.
- Sun disc color: (0.9, 0.5, 0.3) -> (1.0, 0.38, 0.10); brightness 25 -> 28.
- Glare multiplier: 0.7 -> 0.95; warmer glare tint and reduced noon whitening.
- Low-altitude smog, fading to zero at world Y=190; distance contribution begins after 24 blocks. Rain and dry-biome contributions preserved.
- Ochre clear-weather haze, ash-grey storm haze, desaturated sky/night palette and copper sunsets.
- Ash-heavy, darker storm clouds.
- Star amount default 2 -> 1; remaining stars multiplied by 0.58.
- Rainbows default 1 -> 0.
- Unbound cloud amount 1.00 -> 1.12; rain cloud addition 0.40 -> 0.70.
- Minecraft 1.21.1 Pale Garden compatibility fallback.
- Global color calculations remain declaration initializers, preserving the earlier Iris parser repair.

## Euphoria merge decisions

Generated Euphoria using the binary patch embedded in the official SHA1-verified patcher JAR. The deterministic stock TAR SHA256 matches the expected value embedded in that exact JAR. Restored the Unbound style after official patch normalization.

Defaults moved into Euphoria's shaderSettings files instead of duplicating defines in common.glsl. Preserved Euphoria's atmospheric fog multiplier, cloud lighting controls, custom sky color handling, optional blood-moon/glare controls, star color and twinkling controls, and tone-mapping branch. Applied the orange disc in both tone-mapping branches. Retained Euphoria's Pale Garden version guard and supplied an explicit zero fallback below 1.21.4.

Euphoria block/item/entity mappings, DH terrain/water programs and unrelated programs/assets are byte-identical to the generated Euphoria baseline. Ten Euphoria source files changed, reflecting the relocated settings.

Copied the existing atmosphere shader's sidecar settings to the new shader. Added the matching client-only Euphoria patcher JAR to mods. Existing shader ZIPs and settings remain intact. Iris selection and disabled state are unchanged. No server changes, publication or release version changes were made.

## Validation

Passed: official patcher SHA1; official deterministic base TAR SHA256; ZIP CRC; no merge markers; balanced GLSL delimiters and preprocessor blocks; direct include resolution for changed GLSLs; regression check against free-standing global assignments in the color includes; exact scope and unchanged material/DH assets.

Not performed: Iris compilation, GPU render test or gameplay validation. Do not treat this output as gameplay-tested. Test moving Sable/Aeronautics structures, Create machinery, Weather2 storms, gun sights, DH transitions and modded dimensions before release.

## Records and rollback

manifest.json contains SHA256 hashes, source identity and changed paths. apocalypse-vs-stock.patch tracks the original customization; apocalypse-vs-euphoria.patch tracks this port. BuildMatchedEuphoria.java and port_apocalypse.py record the generation and merge logic; staging inputs remain under .local/shader-port-2026-10-08. The original shader remains available for rollback. Removing the newly added shader ZIP, its sidecar and Euphoria patcher JAR reverts these additions; no original files were overwritten.
