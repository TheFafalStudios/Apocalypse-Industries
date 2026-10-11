# Echo Plate recipe - 2026-09-24

User-approved replacement for the old mechanical-crafting recipe. No changes to plate stats, resonance, repair, or texture.

Input: one undamaged Protection Pixel Alloy Armor Plate. Damage must equal zero; native stat metadata is allowed. Repaired-to-full plates qualify, because the native item does not track whether it was previously used.

Repeat this sequence exactly eight times:
1. Fill with 250 mB lava.
2. Press.
3. Fill with 50 mB create_deep_dark:molten_echo.
4. Deploy one blaze powder.
5. Press.
6. Fill with 250 mB lava.
7. Deploy one diamond.
8. Press.

The 64 operations produce one Inert Echo Plate, guaranteed. The assembly transition is a distinct registered item with Create's progress bar; it is not accepted by the finishing recipe.

Finish: heated basin compacting of one Inert Echo Plate, one minecraft:echo_shard, and one blaze powder produces one Echo Plate. Normal heated requirement, not superheated.

Total direct inputs: one undamaged Alloy Plate; 4,000 mB lava; 400 mB molten Echo; eight diamonds; nine blaze powder; one Echo Shard. No upgrade templates. Output one, no random failures. This excludes materials/fuel used to produce the Alloy Plate and molten Echo.

The old recipe ID apocalypse_pp:echo_plate now holds the final compacting recipe. apocalypse_pp:echo_plate_assembly holds the eight-loop sequence. Generation maintains both definitions and native recipe checks. Existing plate and approved texture are retained; intermediate models reference existing textures without editing their pixels.

No GitHub push or server-twin update is part of this change.

Validation: 451 static assertions, 443 addon assertions and 452 native FTB quest checks passed. Native Create tests reject damaged plates, accept undamaged plates with native stat metadata, advance all 64 operations in order, reject premature basin input, and confirm heated finishing. All generated recipe/tag hashes remain stable on regeneration. Tests do not claim a manually operated in-world production line.
