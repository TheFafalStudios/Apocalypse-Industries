# Treated wood physics

Local WIP change, 2026-10-13. Not published or deployed.

Extend Sable's existing mass tags through KubeJS data, preserving upstream tag contents and crafting tags.

- Treated planks (horizontal, vertical and packaged), posts, scaffolding and wooden barrels: Light, 0.5 kpg.
- Treated catwalks, catwalk stairs, wall mounts and windows: Super Light, 0.25 kpg, matching smaller wooden fittings.
- Existing treated slabs, stairs, fences, gates, doors and trapdoors retain their upstream Super Light behavior. Double slabs retain their existing 0.5 kpg override. Signs retain existing collision behavior.
- Metal scaffolding with wooden tops, machinery, recipes, buoyancy and unrelated wood types are unchanged.

Tag entries are optional. Static verification checks installed block IDs, merged tag membership and mass-rule conflicts. Runtime tooltip and ship physics checks remain pending a fresh client launch. No publication, server deployment, restart or reload is performed.
