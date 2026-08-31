/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;

import java.util.List;

/** BlueMap 5.23 feature-backport registration boundary. */
public final class BlueMap523Adapter {

    private static final CreateRuntime RUNTIME = CreateRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_create:prototype"), BlueMap523Adapter::createRenderer
    );
    private static final ResourcePack.Extension<CreateResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    Key.parse("bluemap_create:prototype"),
                    pack -> new CreateResourceExtension(pack, RENDERER, RUNTIME)
            );
    private static final List<BlockEntityType> BLOCK_ENTITIES = List.of(
            new BlockEntityType.Impl(Key.parse("create:copycat"), CopycatBlockEntityData.class),
            new BlockEntityType.Impl(Key.parse("create:fluid_pipe"), PipeBlockEntityData.class),
            new BlockEntityType.Impl(
                    Key.parse("create:encased_fluid_pipe"), PipeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:glass_fluid_pipe"), PipeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:smart_fluid_pipe"), PipeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:fluid_valve"), PipeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:fluid_tank"), TankBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:creative_fluid_tank"), TankBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:mechanical_crafter"), CrafterBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:factory_panel"), FactoryGaugeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:item_vault"), VaultBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:chain_conveyor"),
                    ChainConveyorBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:track"), TrackBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:mechanical_arm"), ArmBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("createaddition:connector"), CaaWireBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("createaddition:large_connector"),
                    CaaWireBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("createaddition:small_light_connector"),
                    CaaWireBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("createaddition:redstone_relay"),
                    CaaWireBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("createaddition:modular_accumulator"),
                    CaaAccumulatorBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create_hypertube:hypertube_entity"),
                    HypertubeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create_hypertube:hypertube_entrance_entity"),
                    HypertubeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create_hypertube:hyper_accelerator_entity"),
                    HypertubeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create_hypertube:hyper_junction_entity"),
                    HypertubeBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create_enchantment_industry:affix_augmentor"),
                    CeiPoweredBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create_enchantment_industry:gem_cutter"),
                    CeiPoweredBlockEntityData.class
            ),
            new BlockEntityType.Impl(
                    Key.parse("create:simple_kinetic"), PipeBlockEntityData.class
            )
    );

    private BlueMap523Adapter() {
    }

    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.inactive("registry-collision");
            return false;
        }
        for (BlockEntityType type : BLOCK_ENTITIES) {
            if (!RegistryGuard.canRegister(BlockEntityType.REGISTRY, type)) {
                RUNTIME.inactive("block-entity-registry-collision");
                return false;
            }
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.inactive("registry-collision");
            return false;
        }
        for (BlockEntityType type : BLOCK_ENTITIES) {
            if (!RegistryGuard.register(BlockEntityType.REGISTRY, type)) {
                RUNTIME.inactive("block-entity-registry-collision");
                return false;
            }
        }
        return true;
    }

    private static BlockRenderer createRenderer(
            ResourcePack pack,
            TextureGallery gallery,
            RenderSettings settings
    ) {
        try {
            return new CreateRenderer(
                    pack, gallery, settings, RUNTIME, RUNTIME.catalog(pack),
                    RUNTIME.objects(pack)
            );
        } catch (Error error) {
            CreateRuntime.throwIfFatal(error);
            RUNTIME.inactive("renderer-construction-" + error.getClass().getSimpleName());
            return BlockRendererType.DEFAULT.create(pack, gallery, settings);
        } catch (RuntimeException exception) {
            RUNTIME.inactive("renderer-construction-" + exception.getClass().getSimpleName());
            return BlockRendererType.DEFAULT.create(pack, gallery, settings);
        }
    }

}
