# Gunsmithing Enchantments 1.1.0

Original 1.0.0 source supplied by the pack author (MIT metadata), independently verified and confirmed working by the user. Version 1.1.0 adds 65% armor-point bypass to only `cgs:round_revolver_piercing` and `cgs:round_gatling_piercing`.

Build using the installed Java 21 runtime and pinned actual Minecraft/NTGL dependencies:

    python scripts/gunsmithing_enchantments/build.py --output "Q:/GPT General/NEW-gunsmithing-build"

Run native tests in a NEW disposable server below the build directory:

    python scripts/gunsmithing_enchantments/tests/run.py --build "Q:/GPT General/NEW-gunsmithing-build" --attempt regression

The runner reuses only runtime libraries from the local server twin, copies four named mods, generates a disposable world outside the instance and shuts it down after assertions. It never reads existing saves or schematics. Local Minecraft EULA acceptance is reused from the established workspace test workflow. The test port is bound only to localhost (25589).

Production JARs include their source and have reproducible ZIP entry timestamps. No compilation stubs are bundled. Build outputs, test worlds and logs must remain outside the live instance and release payloads. Installation and remote deployment are separate from these scripts.

The addon marks NTGL's existing per-hit damage source only when the projectile's stored ammo item matches either exact piercing-round ID. The armor hook multiplies armor by 0.35 before the existing damage/toughness formula. Damage type, attribution, toughness, Resistance, Protection, absorption, critical/headshot calculations, attachment damage and penetration remain in their normal pipelines.

See docs/GUNSMITHING_ARMOR_BYPASS.md for verification and rollback.
