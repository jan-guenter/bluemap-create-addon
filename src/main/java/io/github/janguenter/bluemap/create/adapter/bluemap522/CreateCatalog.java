/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import io.github.janguenter.bluemap.create.profile.CreateFamilyArtifacts.Profile;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Closed first-tranche route roster for exact Create 6.0.10. */
final class CreateCatalog {

    static final String COPYCAT_PANEL = "create:copycat_panel";
    static final String COPYCAT_STEP = "create:copycat_step";
    static final String BELT = "create:belt";
    static final String MECHANICAL_PUMP = "create:mechanical_pump";
    static final String MECHANICAL_CRAFTER = "create:mechanical_crafter";
    static final String STEAM_ENGINE = "create:steam_engine";
    static final String POWERED_SHAFT = "create:powered_shaft";
    static final String FACTORY_GAUGE = "create:factory_gauge";
    static final String ITEM_VAULT = "create:item_vault";
    static final String CHAIN_CONVEYOR = "create:chain_conveyor";
    static final String TRACK = "create:track";
    static final String MECHANICAL_SAW = "create:mechanical_saw";
    static final String DEPLOYER = "create:deployer";
    static final String MECHANICAL_ARM = "create:mechanical_arm";
    static final String MILLSTONE = "create:millstone";
    static final String PORTABLE_STORAGE_INTERFACE =
            "create:portable_storage_interface";
    static final String PORTABLE_FLUID_INTERFACE =
            "create:portable_fluid_interface";

    static final String AQUATIC_CONDUIT =
            "create_aquatic_ambitions:mechanical_conduit";

    static final Set<String> PIPE_BLOCKS = Set.of(
            "create:fluid_pipe",
            "create:encased_fluid_pipe",
            "create:glass_fluid_pipe",
            "create:smart_fluid_pipe",
            "create:fluid_valve",
            MECHANICAL_PUMP
    );

    static final Set<String> TANK_BLOCKS = Set.of(
            "create:fluid_tank", "create:creative_fluid_tank"
    );

    static final Set<String> CONNECTED_CASINGS = Set.of(
            "create:andesite_casing",
            "create:brass_casing",
            "create:copper_casing",
            "create:shadow_steel_casing",
            "create:refined_radiance_casing"
    );

    static final Set<String> BRACKET_KINETICS = Set.of(
            "create:shaft", "create:cogwheel", "create:large_cogwheel"
    );

    static final Map<String, ObjRoute> OBJECTS = Map.ofEntries(
            Map.entry("flywheel", new ObjRoute(
                    "create:flywheel", "create:block/flywheel/block",
                    "assets/create/models/block/flywheel/flywheel_shaftless.obj",
                    "assets/create/models/block/flywheel/flywheel.mtl", false
            )),
            Map.entry("crushing_wheel", new ObjRoute(
                    "create:crushing_wheel", "create:block/crushing_wheel/block",
                    "assets/create/models/block/crushing_wheel/crushing_wheel.obj",
                    "assets/create/models/block/crushing_wheel/crushing_wheel.mtl", false
            )),
            Map.entry("large_water_wheel", new ObjRoute(
                    "create:large_water_wheel", "create:block/large_water_wheel/textures",
                    "assets/create/models/block/large_water_wheel/waterwheel_large.obj",
                    "assets/create/models/block/large_water_wheel/waterwheel_large.mtl", false
            )),
            Map.entry("large_water_wheel_extension", new ObjRoute(
                    "create:large_water_wheel", "create:block/large_water_wheel/textures",
                    "assets/create/models/block/large_water_wheel/waterwheel_large_extension.obj",
                    "assets/create/models/block/large_water_wheel/waterwheel_large.mtl", false
            )),
            Map.entry("water_wheel", new ObjRoute(
                    "create:water_wheel", "create:block/water_wheel/textures",
                    "assets/create/models/block/water_wheel/water_wheel.obj",
                    "assets/create/models/block/water_wheel/water_wheel.mtl", true
            )),
            Map.entry("blaze_burner", new ObjRoute(
                    "create:blaze_burner", "create:block/blaze_burner/block",
                    "assets/create/models/block/blaze_burner/blaze_cage.obj",
                    "assets/create/models/block/blaze_burner/blaze_cage.mtl", false
            )),
            Map.entry("blaze_rods_small", blazeRods(
                    "create:block/blaze_burner/rods_small",
                    "assets/create/models/block/blaze_burner/blaze_rods_small.obj"
            )),
            Map.entry("blaze_rods_large", blazeRods(
                    "create:block/blaze_burner/rods_large",
                    "assets/create/models/block/blaze_burner/blaze_rods_large.obj"
            )),
            Map.entry("blaze_superheated_rods_small", blazeRods(
                    "create:block/blaze_burner/superheated_rods_small",
                    "assets/create/models/block/blaze_burner/blaze_rods_small.obj"
            )),
            Map.entry("blaze_superheated_rods_large", blazeRods(
                    "create:block/blaze_burner/superheated_rods_large",
                    "assets/create/models/block/blaze_burner/blaze_rods_large.obj"
            )),
            Map.entry("chain_conveyor_wheel", new ObjRoute(
                    CHAIN_CONVEYOR, "create:block/chain_conveyor/textures",
                    "assets/create/models/block/chain_conveyor/conveyor_wheel.obj",
                    "assets/create/models/block/chain_conveyor/conveyor_wheel.mtl", true
            )),
            Map.entry("chain_conveyor_guard", new ObjRoute(
                    CHAIN_CONVEYOR, "create:block/chain_conveyor/textures",
                    "assets/create/models/block/chain_conveyor/conveyor_ports.obj",
                    "assets/create/models/block/chain_conveyor/conveyor_ports.mtl", true
            )),
            Map.entry("chain_conveyor_shaft", new ObjRoute(
                    CHAIN_CONVEYOR, "create:block/chain_conveyor/textures",
                    "assets/create/models/block/chain_conveyor/conveyor_shaft.obj",
                    "assets/create/models/block/chain_conveyor/conveyor_shaft.mtl", true
            )),
            Map.entry("track_tie", new ObjRoute(
                    TRACK, "create:block/track/obj_track",
                    "assets/create/models/block/track/tie.obj",
                    "assets/create/models/block/track/track.mtl", false
            )),
            Map.entry("track_left", new ObjRoute(
                    TRACK, "create:block/track/obj_track",
                    "assets/create/models/block/track/segment_left.obj",
                    "assets/create/models/block/track/track.mtl", false
            )),
            Map.entry("track_right", new ObjRoute(
                    TRACK, "create:block/track/obj_track",
                    "assets/create/models/block/track/segment_right.obj",
                    "assets/create/models/block/track/track.mtl", false
            ))
    );

    static final Map<String, List<String>> PARTIALS = Map.of(
            "create:mechanical_press", List.of("create:block/mechanical_press/head"),
            "create:mechanical_mixer", List.of(
                    "create:block/cogwheel_shaftless",
                    "create:block/mechanical_mixer/pole",
                    "create:block/mechanical_mixer/head"
            ),
            "create:encased_fan", List.of(
                    "create:block/shaft_half",
                    "create:block/encased_fan/propeller"
            ),
            "create:mechanical_drill", List.of("create:block/mechanical_drill/head")
    );

    static final Set<String> CORE_BLOCKS;
    static final Map<Profile, Set<String>> PROFILE_BLOCKS;
    static final Set<String> CUSTOM_BLOCKS;

    static {
        LinkedHashSet<String> blocks = new LinkedHashSet<>();
        blocks.add(COPYCAT_PANEL);
        blocks.add(COPYCAT_STEP);
        blocks.add(BELT);
        blocks.add(MECHANICAL_CRAFTER);
        blocks.add(STEAM_ENGINE);
        blocks.add(POWERED_SHAFT);
        blocks.add(FACTORY_GAUGE);
        blocks.add(ITEM_VAULT);
        blocks.add(CHAIN_CONVEYOR);
        blocks.add(TRACK);
        blocks.add(MECHANICAL_SAW);
        blocks.add(DEPLOYER);
        blocks.add(MECHANICAL_ARM);
        blocks.add(MILLSTONE);
        blocks.add(PORTABLE_STORAGE_INTERFACE);
        blocks.add(PORTABLE_FLUID_INTERFACE);
        blocks.addAll(PARTIALS.keySet());
        blocks.addAll(PIPE_BLOCKS);
        blocks.addAll(TANK_BLOCKS);
        blocks.addAll(CONNECTED_CASINGS);
        blocks.addAll(BRACKET_KINETICS);
        OBJECTS.values().forEach(route -> blocks.add(route.blockId()));
        CORE_BLOCKS = Set.copyOf(blocks);

        EnumMap<Profile, Set<String>> profiles = new EnumMap<>(Profile.class);
        profiles.put(Profile.CREATE, CORE_BLOCKS);
        profiles.put(Profile.AQUATIC_AMBITIONS, Set.of(AQUATIC_CONDUIT));
        profiles.put(Profile.CRAFTS_AND_ADDITIONS, Set.of(
                "createaddition:connector",
                "createaddition:large_connector",
                "createaddition:small_light_connector",
                "createaddition:redstone_relay",
                "createaddition:modular_accumulator",
                "createaddition:alternator",
                "createaddition:electric_motor",
                "createaddition:rolling_mill",
                "createaddition:portable_energy_interface",
                "createaddition:liquid_blaze_burner"
        ));
        profiles.put(Profile.HYPERTUBE, Set.of(
                "create_hypertube:hypertube",
                "create_hypertube:hypertube_accelerator",
                "create_hypertube:hypertube_entrance",
                "create_hypertube:hypertube_junction"
        ));
        profiles.put(Profile.ENCHANTMENT_INDUSTRY, Set.of(
                "create_enchantment_industry:printer",
                "create_enchantment_industry:grindstone_drain",
                "create_enchantment_industry:blaze_enchanter",
                "create_enchantment_industry:classic_blaze_enchanter",
                "create_enchantment_industry:blaze_forger",
                "create_enchantment_industry:blaze_composer",
                "create_enchantment_industry:affix_augmentor",
                "create_enchantment_industry:gem_cutter",
                "create_enchantment_industry:infuser",
                "create_enchantment_industry:brass_bookshelf",
                "create_enchantment_industry:ender_woven_bag"
        ));
        PROFILE_BLOCKS = Map.copyOf(profiles);
        CUSTOM_BLOCKS = blocks(Set.of(Profile.values()));
    }

    static Set<String> blocks(Set<Profile> activeProfiles) {
        LinkedHashSet<String> blocks = new LinkedHashSet<>();
        for (Profile profile : Profile.values()) {
            if (activeProfiles.contains(profile)) {
                blocks.addAll(PROFILE_BLOCKS.getOrDefault(profile, Set.of()));
            }
        }
        return Set.copyOf(blocks);
    }

    record ObjRoute(
            String blockId,
            String textureModel,
            String objPath,
            String mtlPath,
            boolean appendStock
    ) {
    }

    private static ObjRoute blazeRods(String textureModel, String objPath) {
        return new ObjRoute(
                "create:blaze_burner", textureModel, objPath,
                "assets/create/models/block/blaze_burner/blaze_rods.mtl", false
        );
    }

    private CreateCatalog() {
    }
}
