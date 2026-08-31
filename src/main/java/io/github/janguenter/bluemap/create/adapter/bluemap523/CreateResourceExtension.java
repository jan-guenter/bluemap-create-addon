/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.create.model.CreateObjParser;
import io.github.janguenter.bluemap.create.profile.CreateFamilyArtifacts;
import io.github.janguenter.bluemap.create.profile.CreateFamilyArtifacts.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Exact-artifact resource gate and focused renderer installation. */
final class CreateResourceExtension implements ResourcePackExtension {

    private static final Set<Key> BELT_TEXTURES = Set.of(
            Key.parse("create:block/belt"),
            Key.parse("create:block/belt_offset"),
            Key.parse("create:block/belt_diagonal")
    );
    private static final Set<Key> TANK_TEXTURES = Set.of(
            Key.parse("create:block/fluid_tank"),
            Key.parse("create:block/fluid_tank_connected"),
            Key.parse("create:block/fluid_tank_top"),
            Key.parse("create:block/fluid_tank_top_connected"),
            Key.parse("create:block/fluid_tank_inner"),
            Key.parse("create:block/fluid_tank_inner_connected"),
            Key.parse("create:block/fluid_tank_window"),
            Key.parse("create:block/fluid_tank_window_single"),
            Key.parse("create:block/creative_fluid_tank"),
            Key.parse("create:block/creative_fluid_tank_connected"),
            Key.parse("create:block/creative_casing"),
            Key.parse("create:block/creative_casing_connected"),
            Key.parse("create:block/creative_fluid_tank_window"),
            Key.parse("create:block/creative_fluid_tank_window_single")
    );
    private static final Set<Key> CRAFTER_TEXTURES = Set.of(
            Key.parse("create:block/brass_casing"),
            Key.parse("create:block/brass_casing_connected"),
            Key.parse("create:block/crafter_side"),
            Key.parse("create:block/crafter_side_connected")
    );
    private static final Set<Key> VAULT_TEXTURES = Set.of(
            Key.parse("create:block/vault/vault_top_small"),
            Key.parse("create:block/vault/vault_top_medium"),
            Key.parse("create:block/vault/vault_top_large"),
            Key.parse("create:block/vault/vault_bottom_small"),
            Key.parse("create:block/vault/vault_bottom_medium"),
            Key.parse("create:block/vault/vault_bottom_large"),
            Key.parse("create:block/vault/vault_front_small"),
            Key.parse("create:block/vault/vault_front_medium"),
            Key.parse("create:block/vault/vault_front_large"),
            Key.parse("create:block/vault/vault_side_small"),
            Key.parse("create:block/vault/vault_side_medium"),
            Key.parse("create:block/vault/vault_side_large")
    );
    private static final List<String> BELT_MODELS = List.of(
            "create:block/belt/start", "create:block/belt/start_bottom",
            "create:block/belt/middle", "create:block/belt/middle_bottom",
            "create:block/belt/end", "create:block/belt/end_bottom",
            "create:block/belt/diagonal_start",
            "create:block/belt/diagonal_middle",
            "create:block/belt/diagonal_end",
            "create:block/belt_pulley"
    );
    private static final List<String> STABLE_PARTIAL_MODELS = List.of(
            "create:block/fluid_pipe/casing",
            "create:block/mechanical_crafter/block",
            "create:block/mechanical_pump/cog",
            "create:block/cogwheel_shaftless",
            "create:block/mechanical_crafter/lid",
            "create:block/mechanical_crafter/arrow",
            "create:block/mechanical_crafter/belt",
            "create:block/mechanical_crafter/belt_animated",
            "create:block/steam_engine/gauge",
            "create:block/steam_engine/gauge_dial",
            "create:block/steam_engine/piston",
            "create:block/steam_engine/linkage",
            "create:block/steam_engine/shaft_connector",
            "create:block/powered_shaft",
            "create:block/blaze_burner/blaze/inert",
            "create:block/blaze_burner/blaze/idle",
            "create:block/blaze_burner/blaze/active",
            "create:block/blaze_burner/blaze/super",
            "create:block/blaze_burner/blaze/super_active",
            "create:block/factory_gauge/panel",
            "create:block/factory_gauge/panel_restocker"
    );
    private static final List<String> CORE_COMPLETION_MODELS = List.of(
            "create:block/mechanical_saw/blade_horizontal_inactive",
            "create:block/mechanical_saw/blade_vertical_inactive",
            "create:block/shaft_half",
            "create:block/shaft",
            "create:block/deployer/pole",
            "create:block/deployer/hand_pointing",
            "create:block/mechanical_arm/cog",
            "create:block/mechanical_arm/base",
            "create:block/mechanical_arm/lower_body",
            "create:block/mechanical_arm/upper_body",
            "create:block/mechanical_arm/claw_base",
            "create:block/mechanical_arm/claw_base_goggles",
            "create:block/mechanical_arm/lower_claw_grip",
            "create:block/mechanical_arm/upper_claw_grip",
            "create:block/millstone/inner",
            "create:block/portable_storage_interface/block_middle",
            "create:block/portable_storage_interface/block_top",
            "create:block/portable_fluid_interface/block_middle",
            "create:block/portable_fluid_interface/block_top",
            "create:block/metal_girder/segment_middle",
            "create:block/metal_girder/segment_top",
            "create:block/metal_girder/segment_bottom"
    );
    private static final List<String> AQUATIC_MODELS = List.of(
            "create_aquatic_ambitions:block/conduit_eye",
            "create_aquatic_ambitions:block/inactive_conduit",
            "create_aquatic_ambitions:block/conduit_cage"
    );
    private static final List<String> CAA_MODELS = List.of(
            "create:block/shaft_half",
            "create:block/shaft",
            "createaddition:block/portable_energy_interface/block_middle",
            "createaddition:block/portable_energy_interface/block_top",
            "createaddition:block/modular_accumulator/guage",
            "createaddition:block/modular_accumulator/dial",
            "createaddition:entity/liquid_hat"
    );
    private static final List<String> HYPERTUBE_MODELS = List.of(
            "create_hypertube:block/hypertube_entrance/cogwheel_hole",
            "create_hypertube:block/redstone_detector_tube_attachment_no_cog",
            "create_hypertube:block/tube_scanner_attachment_no_cog"
    );
    private static final List<String> CEI_MODELS = List.of(
            "create_enchantment_industry:block/printer/nozzle_top",
            "create_enchantment_industry:block/printer/nozzle_bottom",
            "create_enchantment_industry:block/printer/piston",
            "create_enchantment_industry:block/mechanical_grindstone",
            "create_enchantment_industry:block/affix_augmentor/plate",
            "create_enchantment_industry:block/affix_augmentor/plate_powered",
            "create_enchantment_industry:block/affix_augmentor/big_column",
            "create_enchantment_industry:block/affix_augmentor/small_column",
            "create_enchantment_industry:block/affix_augmentor/needle",
            "create_enchantment_industry:block/gem_cutter/crystal_needle",
            "create_enchantment_industry:block/gem_cutter/crystal_needle_powered",
            "create_enchantment_industry:block/gem_cutter/vertical_aligned",
            "create_enchantment_industry:block/gem_cutter/vertical_aligned_powered",
            "create_enchantment_industry:block/gem_cutter/vertical",
            "create_enchantment_industry:block/gem_cutter/vertical_powered",
            "create_enchantment_industry:block/gem_cutter/horizontal",
            "create_enchantment_industry:block/gem_cutter/horizontal_powered",
            "create_enchantment_industry:block/belt_casing/special",
            "create_enchantment_industry:block/belt_casing/special_with_shaft",
            "create_enchantment_industry:block/belt_casing/special_top_only",
            "create_enchantment_industry:block/infuser/eterna_needle",
            "create_enchantment_industry:block/infuser/arcana_needle",
            "create_enchantment_industry:block/infuser/quanta_needle",
            "create_enchantment_industry:block/blaze/enchanter_hat",
            "create_enchantment_industry:block/blaze/enchanter_hat_small",
            "create_enchantment_industry:block/blaze/forger_hat",
            "create_enchantment_industry:block/blaze/forger_hat_small",
            "create_enchantment_industry:block/blaze/composer_hat",
            "create_enchantment_industry:block/blaze/composer_hat_small",
            "create_enchantment_industry:block/ender_woven_bag/light_off",
            "create_enchantment_industry:block/ender_woven_bag/open_pocket"
    );

    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final CreateRuntime runtime;
    private Set<Key> usedTextures = Set.of();
    private Map<String, CompiledCreateObj> objects = Map.of();
    private Set<Profile> enabledProfiles = Set.of();
    private CreateFamilyArtifacts artifacts;

    CreateResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            CreateRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<java.nio.file.Path> roots) {
        if (Boolean.getBoolean("bluemap.create.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        artifacts = CreateFamilyArtifacts.detect(roots);
        enabledProfiles = Set.of();
        usedTextures = Set.of();
        objects = Map.of();
        Path core = artifacts.path(Profile.CREATE).orElse(null);
        if (core == null) {
            runtime.inactive("exact-create-artifact-not-found");
            return;
        }
        try {
            LinkedHashSet<Key> textures = new LinkedHashSet<>(BELT_TEXTURES);
            textures.add(Key.parse("minecraft:block/chain"));
            textures.add(Key.parse("minecraft:block/white_concrete"));
            textures.addAll(TANK_TEXTURES);
            textures.addAll(CRAFTER_TEXTURES);
            textures.addAll(VAULT_TEXTURES);
            for (String beltModel : BELT_MODELS) {
                collectModelTextures(beltModel, textures);
            }
            for (String partial : STABLE_PARTIAL_MODELS) {
                collectModelTextures(partial, textures);
            }
            for (String partial : CORE_COMPLETION_MODELS) {
                collectModelTextures(partial, textures);
            }
            EnumSet<Profile> enabled = EnumSet.of(Profile.CREATE);
            collectOptionalProfile(
                    Profile.AQUATIC_AMBITIONS, textures, enabled,
                    profileTextures -> collectModels(AQUATIC_MODELS, profileTextures)
            );
            collectOptionalProfile(
                    Profile.CRAFTS_AND_ADDITIONS, textures, enabled,
                    profileTextures -> {
                        profileTextures.add(Key.parse(
                                "createaddition:block/modular_accumulator/block_connected"
                        ));
                        profileTextures.add(Key.parse(
                                "createaddition:block/modular_accumulator/block_top_connected"
                        ));
                        collectModels(CAA_MODELS, profileTextures);
                    }
            );
            collectOptionalProfile(
                    Profile.HYPERTUBE, textures, enabled,
                    profileTextures -> {
                        profileTextures.add(Key.parse(
                                "create_hypertube:block/tube_base_glass"
                        ));
                        profileTextures.add(Key.parse(
                                "create_hypertube:block/tube_base_glass_2"
                        ));
                        collectModels(HYPERTUBE_MODELS, profileTextures);
                    }
            );
            collectOptionalProfile(
                    Profile.ENCHANTMENT_INDUSTRY, textures, enabled,
                    profileTextures -> {
                        profileTextures.add(Key.parse(
                                "create_enchantment_industry:block/blaze_enchanter_book"
                        ));
                        collectModels(CEI_MODELS, profileTextures);
                    }
            );
            for (String casing : CreateCatalog.CONNECTED_CASINGS) {
                String path = casing.substring(casing.indexOf(':') + 1);
                textures.add(Key.parse("create:block/" + path));
                textures.add(Key.parse("create:block/" + path + "_connected"));
            }
            for (var partials : CreateCatalog.PARTIALS.values()) {
                for (String partial : partials) {
                    collectModelTextures(partial, textures);
                }
            }
            for (String component : List.of("connection", "rim_connector", "rim", "drain")) {
                for (String direction : List.of(
                        "down", "up", "north", "south", "west", "east"
                )) {
                    collectModelTextures(
                            "create:block/fluid_pipe/" + component + '/' + direction,
                            textures
                    );
                }
            }
            LinkedHashMap<String, CompiledCreateObj> compiled = new LinkedHashMap<>();
            try (ZipFile zip = new ZipFile(core.toFile())) {
                CreateCatalog.OBJECTS.forEach((name, route) -> {
                    try {
                        CompiledCreateObj object = compileObject(zip, route, textures);
                        compiled.put(name, object);
                    } catch (IOException | RuntimeException exception) {
                        runtime.report("obj-skip-" + name + '-'
                                + exception.getClass().getSimpleName());
                    }
                });
            }
            objects = Map.copyOf(compiled);
            usedTextures = Set.copyOf(textures);
            enabledProfiles = Set.copyOf(enabled);
            runtime.activate();
        } catch (IOException | RuntimeException exception) {
            objects = Map.of();
            usedTextures = Set.of();
            enabledProfiles = Set.of();
            runtime.inactive("resource-compile-" + exception.getClass().getSimpleName());
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return usedTextures;
    }

    @Override
    public void bake() {
        if (!runtime.active()) {
            return;
        }
        Set<Profile> profiles = enabledProfiles;
        VariantRendererCatalog catalog = VariantRendererCatalog.wrap(
                resourcePack, renderer, CreateCatalog.blocks(profiles)
        );
        runtime.catalog(resourcePack, catalog);
        runtime.objects(resourcePack, objects);
        runtime.profiles(resourcePack, profiles);
        System.out.println("BlueMap Create add-on active: wrapped " + catalog.size()
                + " variants for copycats, belts, pipes, casing CT and "
                + objects.size() + " frozen OBJ models; exact profiles "
                + profiles + '.');
    }

    private void collectOptionalProfile(
            Profile profile,
            Set<Key> target,
            Set<Profile> enabled,
            Consumer<Set<Key>> collector
    ) {
        if (!artifacts.has(profile)) {
            return;
        }
        LinkedHashSet<Key> profileTextures = new LinkedHashSet<>();
        try {
            collector.accept(profileTextures);
            target.addAll(profileTextures);
            enabled.add(profile);
        } catch (RuntimeException exception) {
            runtime.report("profile-skip-" + profile.name().toLowerCase(Locale.ROOT)
                    + '-' + exception.getClass().getSimpleName());
        }
    }

    private void collectModels(List<String> models, Set<Key> target) {
        for (String model : models) {
            collectModelTextures(model, target);
        }
    }

    private void collectModelTextures(String modelId, Set<Key> target) {
        Model model = resourcePack.getModels().get(new ResourcePath<>(modelId));
        if (model == null) {
            throw new IllegalStateException("missing installed model " + modelId);
        }
        model.applyParent(resourcePack.getModels());
        model.getTextures().values().forEach(variable -> {
            var path = variable.getTexturePath(model.getTextures()::get);
            if (path != null) {
                target.add(path);
            }
        });
    }

    private CompiledCreateObj compileObject(
            ZipFile zip,
            CreateCatalog.ObjRoute route,
            Set<Key> used
    ) throws IOException {
        var geometry = CreateObjParser.parse(read(zip, route.objPath()));
        Map<String, String> variables = CreateObjParser.parseMaterials(
                read(zip, route.mtlPath())
        );
        Model textureModel = resourcePack.getModels().get(
                new ResourcePath<>(route.textureModel())
        );
        if (textureModel == null) {
            throw new IOException("missing OBJ texture model " + route.textureModel());
        }
        textureModel.applyParent(resourcePack.getModels());
        LinkedHashMap<String, Key> materials = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            var variable = textureModel.getTextures().get(entry.getValue());
            var path = variable == null
                    ? null : variable.getTexturePath(textureModel.getTextures()::get);
            // Resource extensions run before BlueMap has populated the texture map.
            // Register the resolved path here; CreateObjEmitter validates the loaded
            // texture when the model is baked.
            if (path == null) {
                throw new IOException("missing OBJ material texture " + entry.getValue());
            }
            materials.put(entry.getKey(), path);
            used.add(path);
        }
        if (geometry.triangles().stream().anyMatch(
                triangle -> !materials.containsKey(triangle.material())
        )) {
            throw new IOException("OBJ face references unmapped material");
        }
        return new CompiledCreateObj(geometry, materials);
    }

    private static byte[] read(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory()) {
            throw new IOException("missing installed resource " + path);
        }
        try (InputStream input = zip.getInputStream(entry)) {
            return input.readAllBytes();
        }
    }
}
