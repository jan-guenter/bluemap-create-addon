# Changelog

## 0.1.0-alpha.2 - 2026-08-31

- Target only the pinned BlueMap 5.23 feature backport and API commits.
- Move the internal adapter package from `bluemap522` to `bluemap523`.
- Compile the four pinned Adapter API `0.1.0-alpha.2` sources and remove the
  duplicate compatibility, extension-factory, and registry helpers.
- Preserve all accepted Create-family rendering and gallery behavior.

## 0.1.0-alpha.1 - 2026-08-18

- Render persisted Copycat materials, complete belts and fluid pipes, saved
  brackets, connected casings, tanks, vaults, and mechanical crafters.
- Render stable kinetic-machine partials, installed-resource OBJ silhouettes,
  boiler and steam-engine structures, factory gauges, and curved tracks.
- Render chain-conveyor wheels and guards with correctly repeating transparent
  chain links.
- Add independent exact-gated profiles for Create Aquatic Ambitions, Create
  Crafts & Additions, Create Hypertube, and Create: Enchantment Industry.
- Normalize motion, contents, fill levels, displays, gauges, LEDs, sparks, and
  other changing state to the documented stable scope.
- Pass 125/125 Java tests, all 115 delayed runtime gallery checks, and owner
  visual acceptance.
