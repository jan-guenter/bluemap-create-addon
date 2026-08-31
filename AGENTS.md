# Create add-on agent guide

This repository is a speed-first BlueMap 5.23 feature-backport add-on for the exact All the
Mons 1.2.0 Create-family artifacts. The core gate is Create `6.0.10`, exact
runtime JAR size `19,123,767` bytes and SHA-256
`ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a`.

Preserve stock fallback and never bundle Create models, textures, classes,
source, worlds, or private fixtures. Read only operator-installed resources.
The owner cares about stable exterior shape, connected/multiblock structure,
multipart geometry and Copycat materials. Ignore motion phase, contents,
fluids, displays, gauges, LEDs and other fast-changing state.

The only BlueMap target is version
`5.22-feature.backport-5.23-stateless-java-web-server-46` at commit
`7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`, with API commit
`285c9a60eff3ac2b0cab308ce1058d1565be0971`. Compile the four Adapter API
`0.1.0-alpha.2` sources from commit
`e81f08bc4bfbf02d810ec8949a019130e2e61634`; never bundle its standalone JAR.
