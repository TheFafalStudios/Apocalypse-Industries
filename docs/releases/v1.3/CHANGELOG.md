## v1.3 — 2026-10-12

- Add Axiom 6.1.3 through the existing Connector/Forgified Fabric API stack. Dedicated servers start with Axiom disabled; permission-level-4 operators can enable or disable it with `/axiomserver enable|disable` and inspect `/axiomserver status`. Enabling preserves native permissions and licensing; restart resets to disabled.
- Add Studious Keybinds 1.0.8-beta.1, retaining NeoForge 21.1.248 and existing keybind preferences.
- Fix the Steam n Rails phantom-track texture reload race that crashed WIP startup.
- Fix Studious/Connector shared-key dispatch that crashed after entering a world, preserving its visual editor and native NeoForge input behavior.
- Client crash fixes and Studious remain client-only. The server receives Axiom, its existing runtime dependencies and the server control addon. Existing server overrides are preserved; activation requires a user-managed restart.

Static distribution checks, isolated client crash tests and dedicated Axiom policy tests are distinct from full multiplayer gameplay validation. See `docs/AXIOM_SERVER_CONTROL.md`, `docs/PHANTOM_TEXTURE_RELOAD_FIX.md` and `docs/STUDIOUS_INPUT_FIX.md`.

