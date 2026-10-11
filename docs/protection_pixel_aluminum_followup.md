# Protection Pixel follow-up â€” 2026-09-29

User decision: do not alter native mod balance or mechanics without explicit user approval. Work in small, reviewable steps.

Replaced the single Moon Regolith input with one immersiveengineering:plate_aluminum in Float Shield, Evasion Wing and Lancer-AS. R&D now depends on the existing aluminum milestone instead of Reach the Moon, retains its existing Electric prerequisite, and detects one aluminum plate. Shifted the Space PP branch beneath aluminum. Generator and recipe audit updated; no addon/mechanics changes or deployment.

452 static assertions pass; quest generation is idempotent. In-game reload/crafting not tested this turn.

Wheat-icon investigation: PP advancement tasks use powered_loadout and resonance. These hidden advancements have no display icon, so FTB uses its wheat fallback. Addon code awards them after checking powered equipped armor / four powered Echo-equipped armor pieces. They are not text-only tasks. Walking Industrial Accident is a manual checkmark gated by the three equipment possession quests. No changes to these tasks in this pass.

## Revised integration (supersedes the token substitution above)

Following the user's correction, mirror the pack's material-replacement approach (AE2 iron-to-aluminum, IE components copper-to-aluminum) rather than appending progression tokens.

- Float Shield: replace both copper sheet positions with aluminum plates; preserve seven sturdy sheets, electrum, battery and other native ingredients. Remove appended token row; native 5x5 pattern restored.
- Evasion Wing: replace all seven iron sheet positions with aluminum plates; retain the two constant-speed motors and remaining ingredients. Remove appended token row; native 5x4 pattern restored.
- Lancer-AS: replace the existing sturdy-sheet deployment with one aluminum plate. Remove appended token deployment; native six-step, one-loop sequence restored.
- Aluminum plates have a native Create pressing recipe from aluminum ingots; no new material item or production recipe added.
- Branch remains attached to aluminum milestone. No equipment balance/mechanics changes.

Validation: 452 static assertions pass; native pattern and sequence length comparison passes. Current recipe audit and generator updated together. Historical recipe proposals remain historical; use the current audit for these superseding changes.

Runtime verification passed on the isolated test server: all three loaded recipes match intended ingredient quantities, native patterns, outputs and assembly operations after KubeJS and Almost Unified processing. Test server stopped normally. This verifies registration/serialization, not a physical crafting-machine playthrough. Evidence: docs/protection_pixel_space_recipes_runtime.txt.
