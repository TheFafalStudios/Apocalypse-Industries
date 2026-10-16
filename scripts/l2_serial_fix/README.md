# Building the shared L2 Serial fix

See docs/L2_SERIAL_FIX.md. Install addon 1.1.0 on clients AND dedicated servers.
It forces the existing NBT/name branch on both sides, independent of registry
initialization order. Remove addon 1.0.0 when upgrading.

Build: `python scripts/l2_serial_fix/build.py --instance "<ATLauncher-instance>" --output <new-directory>`

The builder does not install, restart or reload anything. The required common
mixin is pinned to L2Library 3.0.8 / L2 Serial 3.1.1 and NeoForge 21.1.248.
