# Euphoria sky-history experiment v1

Candidate: `ComplementaryUnbound_r5.8.1_EuphoriaPatches_1.9.3_APOCALYPSE_ATMOSPHERE_v1.1_SKY_HISTORY_FIX_v1_TEST.zip`

Select the candidate in Iris Shader Packs to test. It is installed but not automatically selected. Temporal Filtering must remain ON. The original pack and its options remain available for rollback; select the original to revert.

This independently implemented experiment records categorical coverage (sky, geometry, particles/translucents) in the unused alpha channel of normal Euphoria TAA history. It rejects history if any texel in its sampling footprint has incompatible coverage, and excludes foreground neighbours from sky colour clamping (and vice versa). Stable cloud regions retain the original accumulation strength and original cloud raymarching. Distant Horizons and Voxy opaque depth prevent LOD terrain from being classified as sky. The experiment is disabled when Long Exposure or Renko Cut is enabled.

Only `shaders/lib/antialiasing/taa.glsl` and `shaders/program/composite6.glsl` change. The candidate receives an exact copy of the original saved options, including Low temporal smoothing and jitter off. Cloud settings are unchanged.

Validation: ZIP CRC and complete entry comparison passed. Nine CPU reference/source/archive tests passed. These do not execute GLSL: GPU compilation, frame-time cost and visual gameplay results remain unverified.

Test the same moving blocks and particles against clear sky, clouds, and terrain, with the camera stationary and then moving. Check that cloud interiors stay smooth, that trails shrink, and that outlines do not acquire unacceptable shimmer. Compare frame times against the original at identical settings. Newly revealed regions can briefly show noise as history rebuilds.

Limitations: this is categorical coverage rejection, not full previous-depth reconstruction or object motion vectors. It conservatively rejects a narrow region around mixed coverage, adds texture fetches, and may produce edge shimmer. Transparent particles that do not provide depth or material coverage can still trail. No separate cloud-history pass has been added.

Build: `scripts/build_euphoria_sky_history_fix.py` (refuses to overwrite an existing candidate).
Validation: `scripts/test_euphoria_sky_history_fix.py`.
Exact source/candidate and changed-entry hashes: `docs/EUPHORIA_SKY_HISTORY_FIX_v1.json`.
