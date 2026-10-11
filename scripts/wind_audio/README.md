# Apocalypse Wind Audio — 0.1.0 prototype

Client-only addon for Minecraft 1.21.1, NeoForge 21.1.248 and Weather2 1.21.0-2.8.7. No server installation or server restart is needed for this addon. No networking, mixins, physics changes or world writes.

## Behavior

Samples Weather2 wind at the player's eye position four times per second. This uses Weather2's client wind data and local-height calculation; it does not simulate independent wind. Audio fades every client tick. It works away from trees, including over open water, and does not require a storm object nearby.

Loops Weather2's existing `weather2:env.wind_calm` sound, following Minecraft's Weather and Master volume sliders. No Weather2 audio files are redistributed. Resource packs replacing that sound also affect this addon. Storm/tornado sounds remain active, so the layers can overlap.

Wind below 0.08 is silent; volume follows a smooth curve reaching 0.65 at wind 1.2. These are Weather2 relative units, not m/s or km/h. Rise time constant is 1 second, fall time constant 2 seconds (about 63% of the change per time constant). A sky-obstructing roof reduces volume to 20%; being underwater reduces it to 5%. Settings are in `config/apocalypse_wind_audio-client.toml`, generated on the first launch.

Stops on leaving a world; resets on dimension/world change and sound-engine reload. Pausing follows Minecraft's sound pause behavior. Retries a dropped/muted sound channel at most once every two seconds; owns at most one loop.

## Try it locally

1. Close the client normally.
2. Copy `apocalypse-wind-audio-0.1.0-prototype.jar` into the WIP client's `mods` directory. This prototype was delivered separately and has NOT been installed automatically.
3. Start the client and check the mod list for Apocalypse Wind Audio (Prototype).
4. Listen outdoors during calm and strong wind, over open water, beneath a solid roof, and underwater. Compare with an outdoor anemometer.
5. Check pause/resume, disconnect/reconnect, Weather volume zero/back up, and a resource reload. Confirm no duplicate or stuck loop.
6. Remove that one JAR to uninstall. No pack version change, Git push or server deployment has been performed.

For an isolated test world only, Weather2 commands can change weather, but this addon exposes no commands and never changes wind. Do not use weather-changing commands on the live server just to test this prototype.

## Prototype limits

- Compiled and checked against real mapped client APIs; wind response and sound-instance lifecycle primitives have automated checks. No interactive NeoForge client startup, live listening or full-pack gameplay test has been performed.
- Existing calm-wind audio is a placeholder. Seamlessness and loudness need listening; it may be too gentle for extreme winds. Volume saturates rather than indicating unlimited extremes.
- Shelter is a simple vanilla sky-access test, not acoustic ray tracing. It can muffle sound under trees/open-sided roofs and does not explicitly transform moving Sable ship roofs. Ship cabins need a follow-up integration test.
- This is an indicator of local Weather2 wind, not a calibrated vehicle-force meter. Weather Physics 1.0.3 samples the global wind speed for ordinary vehicle force, whereas this addon includes Weather2's positional/height effect; vehicle mass, sails and tornado forces are separate.
- Weather2 client/server synchronization accuracy is inherited. Wind-effects-disabled dimensions remain silent.

## Build and checks

Source: `src/dev/apocalypse/windaudio/`; loader metadata: `resources/META-INF/neoforge.mods.toml`.

From the WIP workspace, using Python 3.12 and the installed Java 21 runtime:

```powershell
python scripts/wind_audio/build.py --output "Q:/GPT General/wind-audio-new-build"
python scripts/wind_audio/test.py --build "Q:/GPT General/wind-audio-new-build"
```

Use an unused external output directory. The builder pins the audited Weather2 SHA-256, compiles against locally installed libraries and never installs the result. Test fixture uses real Minecraft sound classes with a synthetic constant-volume asset descriptor; it does not play audio or boot a world.
