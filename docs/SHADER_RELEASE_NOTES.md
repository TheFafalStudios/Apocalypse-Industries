> Historical shader-only preparation. Superseded for release assembly by [v1.4 consolidated notes](releases/V1_4_CONSOLIDATED.md). The Y32 mismatch is corrected in the v1.4 candidate; validation limits still apply.

Updates Apocalypse Atmosphere with animated lava shared between normal terrain and Distant Horizons, a smoother transition at the render-distance boundary, and Nether fog centered around 1,000 blocks. Biome fog/depth handling and a lava-colored lower-sky fallback are included; the fallback still assumes a Y32 lava plane and needs adjustment for the newer Y100 sea.

Adds hull-aware water/lava fog through Deep Seas submarine windows and fixes sampler binding that could apply submarine fog to ordinary Overworld views. Adds the Aeronautics Iris rendering fix for balloon-envelope artifacts.

Includes an experimental Vortex lighting/mist bridge, **disabled on every startup**. `/apovortex enable` opts in for the current session; `/apovortex disable` turns it off. The submarine fog fixes work independently of that switch.

Uses Apocalypse Atmosphere NetherDH v1.2 and submarine visuals addon 1.1.2 with Deep Seas 3.4.4, Aeronautics bundle 1.3.2, Distant Horizons 3.3.3, Aeronautics Iris Render Fix 2.1.0 and the existing Iris 1.8.14 Beta 1. Removes Euphoria Patcher, whose official-pack verification rejects this customized Complementary pack.

Fresh-install shader defaults use reduced water reflections, light shafts and shadows, with a 128-block shadow distance. Separate DH clouds are disabled. Existing Iris selection and shader option preferences are preserved; select **ApocalypseAtmosphere_NetherDH_v1.2.zip** once if updating from the old shader filename.

A full client restart is required for the addon changes. Offline shader/GPU regression checks pass; fresh-install gameplay, moving submarine visuals and a controlled performance gain remain unverified. This candidate makes no promise of restored 50+ FPS.
