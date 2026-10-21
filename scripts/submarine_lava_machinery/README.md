# Lava ballast addon

Common-side compatibility for Deep Seas 3.4.4 and Sable 2.0.3. Scope is ballast tanks, vents, typed cluster plumbing, neighbour transfers and floaters. Propellers, water thrusters, surface-boat buoyancy, pressure ratings, fuel/oxygen generation and recipes are outside this patch.

Build into a new `.local` directory:

```powershell
python scripts/submarine_lava_machinery/build.py --output .local/lava-ballast-20261021/build-new
python scripts/submarine_lava_machinery/compile_harness.py --build .local/lava-ballast-20261021/build-new
python scripts/submarine_lava_machinery/native_test.py --build .local/lava-ballast-20261021/build-new --output .local/lava-ballast-20261021/native-new
```

The default native fixture loads only the relevant mod stack and exits at load completion. It tests actual tank entities, mixin gates, fluid conservation, simulate/execute semantics, rejected mixing, incorrect-fluid drains, ocean transitions and save/load. For an independent assembly/force fixture, add `--world-test`; it creates a fresh disposable world under the explicit output directory. `--baseline` disables only the new addon's mixin registration in that test copy, preserving its production classes. Never install this negative-control artifact or the test harness.

`compile_harness.py` changes only test classes/JAR and asserts that the production artifact hash stays fixed. It also includes test-only tracing for discarded Sable assemblies. Compare runs only when their captured harness hashes/method and inputs match. Setup failures are not movement evidence.

`validate.py --build <build> --native <successful-fixture>` checks the runtime evidence, production hash, distribution exclusions and absence of any propulsion/boat/client targets. `install.py --build <validated-build>` installs only the new addon and its index entries in WIP, with rollback records. It does not deploy, start/restart/reload the WIP client or any existing server.
