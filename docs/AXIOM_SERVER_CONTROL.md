# Axiom server control — v1.3

The dedicated server starts with Axiom disabled. Permission-level-4 operators and the console can use:

- `/axiomserver status` — report current state.
- `/axiomserver enable` — enable Axiom for the current server session.
- `/axiomserver disable` — block Axiom immediately.

Every restart resets the state to disabled. No enabled state is written to disk. Enabling preserves Axiom's normal operator/Fabric permissions and licensing/whitelist requirements; this addon grants no native permissions or license exemption. Singleplayer authorization is unaffected.

The server uses official Axiom 6.1.3, Connector 2.0.0-beta.17 and Forgified Fabric API 0.116.15+2.3.5+1.21.1. Axiom's `axiom.json` is client-only in this build; its `ServerConfig` packet is not a persisted disable-editing configuration. This native NeoForge addon is pinned to Axiom 6.1.3 and Minecraft 1.21.1. Required mixins fail loading if their targets disappear, rather than silently dropping the policy.

When disabled, the addon rejects every incoming `axiom` custom payload before protocol dispatch, including tunneled edits and operator requests. Native Axiom `hasPermission`, both `canUseAxiom` overloads and handshake acceptance also deny access. Axiom's normal tick sends its client disable packet and clears pending authorization. On enable, the native denial cache clears so connected eligible players can handshake again. Requests under other namespaces pass through unchanged.

Build source and runtime assertions are in `scripts/axiom_control`. Compile with Java 21 against the existing NeoForge server libraries and Connector-mapped official Axiom JAR, using `build.py --instance <development-instance> --output <new-external-output>`. The source is included in the addon JAR. Test-only observer/harness classes are excluded from production.

Validation: isolated dedicated server with the real Axiom backend passed 267 assertions over 40 official protocol IDs, non-operators/operators, command authorization, native permissions, enable/disable and unrelated traffic. Enabled payload dispatch was observed before test cancellation; this is not a licensed multiplayer editing or undo test. The complete 200-mod server snapshot plus four production additions also reached startup and passed the same 267 checks in a second process, again starting disabled after the previous process ended enabled. Existing warning/error messages were present; no clean-log or full gameplay claim is made. Network/licensing verification and real-client reconnect remain user gameplay checks.

Live activation requires a user-managed server restart after deployment. Before that restart the newly uploaded addon is not active. Existing worlds and schematic/blueprint storage were excluded from release/deployment operations.
