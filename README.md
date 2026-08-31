# BlueMap Create Add-on

[![CI](https://github.com/jan-guenter/bluemap-create-addon/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jan-guenter/bluemap-create-addon/actions/workflows/ci.yml)

An exact-profile BlueMap 5.23 feature-backport add-on for stable Create-family world
appearance.

## Status and compatibility

Version `0.1.0-alpha.2` is an owner-accepted BlueMap 5.23 release candidate
for this exact environment. Version `0.1.0-alpha.1` remains the latest
published release.

- All the Mons `1.2.0`, Minecraft `1.21.1`, NeoForge `21.1.248`, Java `21`;
- BlueMap feature backport
  `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`, API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`;
- Create `6.0.10`;
- Create Aquatic Ambitions `2.0.4`;
- Create Crafts & Additions `1.6.0`;
- Create Hypertube `0.6.0`;
- Create: Enchantment Industry `2.5.0`.

The `0.1.0-alpha.2` production JAR was accepted on 2026-08-31. It is exactly
320,656 bytes with SHA-256
`0b401d72783f0285ce68fbc2dcd2a90f34a64c0079080807b8db3c7c09666d73`.
It differs from the visually accepted staging JAR only in the add-on version
inside `bluemap.addon.json`; all 165 class files and every other archive entry
are byte-identical.
Compatibility outside these exact inputs is not asserted. Core Create is
mandatory; each extension profile is detected and activated independently by
its exact installed JAR size and SHA-256. Unknown, changed, missing, or
malformed inputs retain BlueMap's stock result.

## Visual scope

The core Create profile supplies stable geometry and persisted structure that
BlueMap cannot derive from ordinary blockstates alone:

- Copycat panel and step materials;
- complete belt surfaces, pulleys, fluid-pipe ends, connections, drains,
  valves, pumps, and saved brackets;
- connected casings, tanks, item vaults, and mechanical crafters;
- stable boiler gauges, steam-engine parts, kinetic-machine partials, and
  installed-resource OBJ silhouettes;
- factory-gauge panels, chain-conveyor wheels and repeating chain links;
- persisted curved track rails, ties, and optional girders.

The independently gated extension profiles add the Aquatic Ambitions
mechanical conduit; Crafts & Additions wires, accumulator structure, gauges,
and stable machines; Hypertube straight and Bezier tubes, junctions, and
attachments; and Enchantment Industry's stable machines, books, blaze parts,
and powered structures.

Animation phase, transported or held items, fluid contents and levels,
changing screens, readouts, LEDs, sparks, and other fast-changing state are
intentionally frozen, normalized, or omitted. Create: Dragons Plus, Bells &
Whistles, Create Cobblemon Balls Overhaul, and both Sophisticated Create
integration artifacts were audited at their exact pack versions and need no
custom stable rendering. Rechiseled: Create owns a separate Fusion namespace
and is intentionally outside this add-on.

The add-on contains no Create-family code, models, textures, binaries, or
other third-party assets. BlueMap must be able to read the operator-installed
resource archives, either through normal mod resource scanning or by making
the exact JARs available in `config/bluemap/packs`.

## Build and verification

Clone with submodules so the exact reviewed build convention and Adapter API
sources are available:

```bash
git clone --recurse-submodules \
  https://github.com/jan-guenter/bluemap-create-addon.git
```

For an existing checkout, run `git submodule update --init --recursive`. The
build accepts only Adapter API commit
`e81f08bc4bfbf02d810ec8949a019130e2e61634` and toolkit commit
`6cd34a8368cc4ee8628fbe830a90ec5b14960629` and rejects an uninitialized,
dirty, or incorrectly pinned submodule. Install the corresponding
`v0.3.0-alpha.1` toolkit wheel from `requirements/toolkit.txt`, then verify the
repository contract:

```bash
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
bluemap-addon-toolkit conventions check .
```

The wheel is exactly 20,585 bytes with SHA-256
`82f1ec53603646849a7c2d4b58f3fb7000413fe83043a302bee88cc88daeb8f7`.
Then use Java 21, Gradle 9.6.1, and the exact sibling BlueMap checkout:

```bash
gradle --no-daemon clean check build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

`check` verifies the production and sources archive boundaries. The accepted
implementation passed 125/125 Java tests, activated 882
blockstate variants plus 16 OBJ resources in the exact five-profile runtime,
and passed all 115 delayed gallery checks before owner visual acceptance.

Tagged releases publish the production and source JARs, POM, Gradle module
metadata, and checksums on GitHub Releases and at Maven coordinates
`io.github.jan-guenter:bluemap-create-addon:<version>` on GitHub Packages. The
tag must equal `v<addon_version>`.

## Installation

Place the reviewed add-on JAR in `config/bluemap/packs` and restart the JVM.
It is not a NeoForge mod and does not belong in the server's `mods` directory.
It writes no world or player data.

## License and provenance

The add-on is released under the [MIT License](LICENSE). Third-party software
and resources are not bundled; see [NOTICE.md](NOTICE.md),
[THIRD_PARTY.md](THIRD_PARTY.md), and
[provenance/upstreams.json](provenance/upstreams.json).
