# Apocalypse Atmosphere

An industrial, polluted atmosphere customization for **Complementary Unbound r5.8.1 with Euphoria Patches 1.9.3**. This folder distributes a patch and settings, rather than a generated shader ZIP.

Original shader: [Complementary Shaders by EminGT](https://www.complementary.dev/).
Mod support and additional features: [Euphoria Patches by SpacEagle17](https://www.euphoriapatches.com/).

## Build locally

1. Install [Euphoria Patcher 1.9.3-r5.8.1 for NeoForge](https://modrinth.com/mod/euphoria-patches/version/9Q9EA4jQ) and the corresponding stock Complementary r5.8.1 through their official distribution channels. Use the [official Euphoria installation instructions](https://www.euphoriapatches.com/how-to-install/). The Minecraft instance uses NeoForge 1.21.1.
2. Start Minecraft to let the official patcher generate the unmodified Euphoria shader. Close Minecraft before editing its shader files.
3. With Python 3.10 or newer, run the following command. `--input` accepts either the generated shader directory or a ZIP with `shaders/` at its root. Replace the example paths with your actual instance paths.

```powershell
python build_shader.py --input "Q:\path\to\instance\shaderpacks\ComplementaryUnbound_r5.8.1 + EuphoriaPatches_1.9.3" --output-dir "Q:\path\to\instance\shaderpacks"
```

4. Select `ApocalypseAtmosphere_r5.8.1_EP1.9.3_v1.1.zip` in Iris. The generated sidecar applies the recorded ApocalypseIndustriesGitHubd settings. The builder does not alter Iris selection or activation.

The input must be unmodified Euphoria 1.9.3 for Complementary r5.8.1. Per-file hashes reject different versions or edits. The builder accepts either Unbound or Reimagined input and explicitly selects Unbound for this output. Existing output files are never overwritten.

## Preserved customization

- Larger orange procedural sun and warmer, stronger glare. The original size edits affect shared sun/moon disc calculations; this behavior is retained.
- Persistent low-altitude smog that fades out at Y=190, with stronger rain and dry-biome contributions.
- Ochre haze, copper sunsets, desaturated nights and ash-grey storm clouds.
- Reduced star amount and 58% brightness multiplier; rainbows disabled by default.
- Unbound cloud amount 1.12 and additional rain clouds 0.70.
- Minecraft 1.21.1 Pale Garden fallback and the existing global-initializer parser repair.

Euphoria cloud-lighting controls, custom sky handling, tone-mapping branches and optional star/glare effects are retained. Its material mappings and Distant Horizons terrain/water programs are unchanged. Ten source files are patched; settings moved to Euphoria's separate settings files where appropriate.

## Player settings

`BLOCK_REFLECT_QUALITY=1`, `ENTITY_SHADOW=-1`, `FXAA_STRENGTH=55`, `LIGHTSHAFT_QUALI_DEFINE=1`, `SHADOW_QUALITY=1`, `TAA_JITTER=0`, `TAA_SMOOTHING=2`, `shadowDistance=128.0`.

## Validation and release status

The original local build verified the official patcher SHA1 and its expected deterministic stock TAR SHA256. This portable builder verifies each changed input/output source hash, upstream license identity and output ZIP integrity. Its output was compared against the original local port: all shader and asset member contents match after line-ending normalization, and copied setting values match exactly.

**Iris compilation and in-game rendering remain untested.** Check moving Sable/Aeronautics structures, Create machinery, Weather2 storms, gun sights, DH transitions and modded dimensions before a pack release. The modpack author is responsible for integration and support of this customization.

This branch is an optional shader customization, not a new pack release. It does not change the distributed Packwiz manifest, pack version, automatic update source or server payload.

## Distribution

Euphoria Patches License Agreement v1.1 section 2.3 requires modified packs to be distributed exclusively through a patching mechanism. It also requires name differentiation and credit to Complementary and Euphoria Patches. This bundle contains the customization instructions and a local patch builder; it does not include a generated upstream shader pack. The builder retains both original license files unchanged. No additional license is imposed on the resulting modified shader. Share this patch bundle rather than the generated ZIP.
