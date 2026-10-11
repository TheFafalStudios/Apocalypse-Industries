# Mod compatibility backlog

## Studious Keybinds

- Status: deferred; the mod has not been released yet.
- Revisit after a public build is available.
- Required target: Minecraft 1.21.1 on NeoForge 21.1.248.
- Before adding it, verify client/server requirements, dependencies, and compatibility with the pack's existing Controlling and Searchables keybind stack.
- Do not replace or remove Controlling/Searchables without a separate compatibility decision.

## Mace downswing and hit window (2026-10-11)

- Status: optional future idea, deferred at the user's request; no implementation authorized now.
- Possible change: slow the vanilla mace's visible downswing and check for targets throughout that motion. First valid contact triggers one normal attack, then closes the window before recovery; no repeated damage per swing.
- Requires a mace-specific Better Combat code change and animation retiming, rather than a simple configuration adjustment. Review falling smash, enchantments, cooldown behavior and multiplayer compatibility before any implementation.
- Reason for deferral: implementation and rebalancing effort for an existing vanilla mechanic. The subsequent confirmed decision is to keep vanilla mace combat and exclude maces from Better Combat, because falling hits felt unwieldy. Preserve that exclusion and the custom mace visuals; revisit this optional Better Combat idea only on a new user request.
