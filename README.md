# BlueMap Create Add-on

[![CI](https://github.com/jan-guenter/bluemap-create-addon/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jan-guenter/bluemap-create-addon/actions/workflows/ci.yml)

An exact-profile BlueMap 5.22 add-on for stable Create-family world
appearance.

## Status and compatibility

Version `0.1.0-alpha.1` is the owner-accepted prerelease for this exact
environment:

- All the Mons `1.2.0`, Minecraft `1.21.1`, NeoForge `21.1.248`, Java `21`;
- BlueMap backport `5.22-agent.backport-5.22-mc1.21.1-2`, commit
  `9be321df995a1103808621d529eb72773e719d4d`;
- Create `6.0.10`;
- Create Aquatic Ambitions `2.0.4`;
- Create Crafts & Additions `1.6.0`;
- Create Hypertube `0.6.0`;
- Create: Enchantment Industry `2.5.0`.

The production JAR was accepted on 2026-08-18. It is exactly 312,744 bytes
with SHA-256
`e9e860ff0a3cc3398090d03f36441a9df863ec96c0c5e6da408815a1f9c1cd05`.
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

Use Java 21, Gradle 9.6.1, and the exact sibling BlueMap checkout:

```bash
gradle --no-daemon clean check build \
  generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication
```

`check` rejects a production JAR that differs from the accepted size or
SHA-256. The accepted implementation passed 125/125 Java tests, activated 882
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
