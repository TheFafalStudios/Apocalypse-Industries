# Echo Plate texture — 2026-09-18

## Source analysis and design
All inspected textures are native 16x16 RGBA. Protection Pixel Iron, Brass and Alloy plates share a diagonal stepped silhouette, broad metal face and dark rim. Create Deep Dark Echo armor/ingots use blue-green near-black shadows, dark teal surfaces and sparse cyan highlights. The new plate follows that shape family, with a restrained cyan resonance inlay; no floating particles, bloom or extra armor iconography.

References were extracted from Protection Pixel 2.2.1 and Create Deep Dark 3.0.2. The comparison preview includes native-size icons below the enlarged sprites.

## Assets and verification
- Game texture: `scripts/protection_pixel/resources/assets/apocalypse_pp/textures/item/echo_plate.png`
- Model: `scripts/protection_pixel/resources/assets/apocalypse_pp/models/item/echo_plate.json`
- Preview/comparison: `docs/assets/echo_plate_preview.png`, `docs/assets/echo_plate_comparison.png`
- Original generated image: `docs/assets/echo_plate_generated.png`
- Deterministic size/palette export: `scripts/protection_pixel/export_texture.ps1`

Validated 16x16 RGBA, nine opaque colors, binary transparency, transparent margin. Rebuilt the live addon and verified that its only changed entries are the new PNG and the model texture reference. All gameplay classes are byte-identical to the previous JAR. Refreshed only the addon hash and pack-to-index hash. Visual inspection used enlarged/native-size previews; the live game was not launched for this asset change.

## Generation provenance
Mode: built-in image-generation tool, with the displayed reference sheet as style reference. The direct local-path reference failed because of the image tool's filesystem helper; the displayed sheet was used successfully. The generated image was mechanically exported to 16x16, fitted to the normal plate footprint, mapped to the Echo palette and given binary pixel transparency. Original generated alpha/source was retained in the full-resolution source image.

Final generation prompt:

> Generate ONE new Echo Armor Plate inventory sprite using the reference sheet displayed in the preceding conversation as a style reference only. Match the diagonal, stepped oblong silhouette of the sheet's alloyarmorplate (upper left) and ironarmorplate (lower row): thick metal plate tilted lower-left to upper-right with broad face, dark folded bottom/right rim and tiny fastener highlights. Use the Echo items' palette: near-black blue-green #0D1117 #111A21 #041A1D, dark teal #052832 #033D50 #0A4B60, mid teal #0D626C #008A95, and only two or three bright cyan #29D4EB pixels. Add a restrained small broken cyan resonance stripe inlaid across the plate face. No floating particles, glow, bloom, crystal shards, weapons, shield symbols, text, labels, shadows on background, or comparison sheet. EXACTLY 16x16 logical pixel art, enlarged with square uniform pixels to the output size (1024x1024 -> 64x64 blocks on the canvas grid). Hard edges, no anti-aliasing, no gradients, 8-10 solid colors. Item contained in logical columns and rows 1..14 with a transparent margin. True transparent alpha background. One centered square sprite. This will become a native 16x16 Minecraft texture, so keep stock low-resolution readability.


## Revision 2: Echo Shard surface
User approved the outline and corner bolts but requested dark elements and sparkles instead of the central stripe. Built-in image generation created the revised surface; the first output copied the shard silhouette and was discarded. The accepted generated face was exported with the previous native alpha, border, and 3x3 corner-bolt regions preserved byte-for-byte. Small generated cyan highlights were retained during downsampling. Only the texture entry changed in the rebuilt addon.

Current export: `scripts/protection_pixel/export_texture_v2.ps1`. Current preview: `docs/assets/echo_plate_preview_v2.png`. Source: `docs/assets/echo_plate_generated_v2.png`. Original native texture retained as `docs/assets/echo_plate_v1.png`.

Revision generation prompt: Correct the previous wrong edit. We are editing the BROAD ARMOR PLATE from the earlier generated image, NOT either of the two long narrow Echo Shards. Target is the broad squat oblong plate leaning / (bottom LEFT to top RIGHT), with FOUR cyan corner bolts, wide planar face and black metal rim. Preserve this plate shape and four bolts. The narrow shards leaning backslash are only palette/material references and must NOT determine the shape. Produce a broad armor plate leaning /, occupying roughly a 14x13 logical-pixel bounding box within a 16x16 transparent canvas. Keep chunky stepped corners, broad center and four corner rivets. Remove the stripe from the center: instead dark blue-black irregular patches with three scattered small cyan sparkle clusters on dark teal. Sparkles are separated, not joined by a stripe, not aligned. No floating pieces. The face should look like Echo Shard MATERIAL fitted into this plate, not an ingot edge. Uniform crisp 16x16 logical pixel-art grid, transparent background, no gradients, blur or glow. Output one plate only. Most important: broad PLATE silhouette leaning /, never the long thin SHARD silhouette leaning backslash.


## User-selected image - 2026-09-20
The user selected exec-311aa82e-5bfa-47c8-a157-9e0fa2b7713a.png. The item texture now uses that PNG byte-for-byte (1280x1280), not either reduced 16x16 export. Original SHA256: E70AB0AC0D63E8A1A1B8BF821501C824351F5AF2762224F5E26897A3FD5BBFFD. The older export scripts are historical utilities and must not be rerun as part of an ordinary addon build. A smaller faithful grid export can be considered later; do not regenerate or redesign the selected image.


## Native sprite scaling fix - 2026-09-20
Supersedes the instruction to install the large PNG unchanged. Native Iron/Brass/Alloy plate textures are 16x16 with bounds (1,1)-(15,15). The installed Echo image was 1254x1254; visible alpha>=128 bounds were (274,314)-(1020,980), only about 60% of the canvas width. Faint alpha outside the sprite also expanded generated item geometry.
The normalized texture is 16x16 with a one-pixel transparent margin and binary alpha. The built-in imagegen edit preserved the diagonal plate, cyan corner bolts and face details; export_scaled_texture.ps1 crops visible alpha and samples the result into the native grid, retaining small cyan details crossing sample cells. Source: docs/assets/echo_plate_scaled_source.png. Final texture: scripts/protection_pixel/resources/assets/apocalypse_pp/textures/item/echo_plate.png. Preview: docs/assets/echo_plate_scaled_preview.png.
Only this PNG changed inside the addon archive; class files, Mending/Reforger restrictions, models and gameplay data are byte-for-byte unchanged. Existing item/generated model is retained. Pack hashes updated. In-game appearance still needs a client resource reload.
Prompt (built-in imagegen): Normalize the Echo Plate in the first reference to a native Minecraft item sprite, matching the 16x16 resolution and occupied area of the tiny Brass Plate in the second reference. Preserve the first image's diagonal plate shape, dark teal face, dark navy rim, four bright cyan corner bolts and small diagonal cyan details. This is a scaling fix, not a redesign. Exactly 16x16 logical pixels, transparent background, object within x=1..14 and y=1..14 with one transparent pixel margin. Keep all four bolts clearly distinct. All pixels flat solid square colors; no soft shading, texture noise, antialiasing, glow, extra marks or changes to the design. Output true transparent PNG. If larger output resolution is necessary, render this 16x16 logical sprite at integer scale across the square canvas (each logical pixel one large solid square) so it can be exported to16x16 with nearest-neighbor sampling.


## Final approved texture integrated
The approved palette-only recolor of the original Alloy Plate is now the Echo Plate. Source: docs/assets/original_protection_pixel_plates/alloyarmorplate.png. Installed asset: scripts/protection_pixel/resources/assets/apocalypse_pp/textures/item/echo_plate.png. Exact 16x16 grid and original alpha are preserved; no center details were added. Both the build resource and addon archive contain the approved PNG byte-for-byte. Only the PNG changed inside the addon; all code and other resources were preserved. Pack hashes were refreshed. Previous generated/scaled textures and export scripts are superseded and must not be used for this asset.
Approved PNG SHA256: 409941c401baf6de6838bbf7dda7d6f1fbee60e3021be34bcf5abf838a284dae
