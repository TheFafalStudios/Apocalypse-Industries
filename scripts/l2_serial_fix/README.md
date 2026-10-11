# Building the client L2 Serial fix

See docs/L2_SERIAL_FIX.md for scope, evidence and rollback. The addon matches the existing server's built-in registry name/NBT encoding and is client-only. Its mixin contains one required redirect, pinned to L2Library 3.0.8 / bundled L2 Serial 3.1.1.

Build: `python scripts/l2_serial_fix/build.py --instance "<ATLauncher-instance>" --output <new-directory>`

No install or restart is performed by the builder. Build output is kept separate from source and from deployable pack files.
