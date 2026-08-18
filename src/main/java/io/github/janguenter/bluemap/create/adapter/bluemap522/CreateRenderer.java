/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.map.hires.block.BlockStateModelRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.janguenter.bluemap.create.profile.CreateFamilyArtifacts.Profile;

/** Focused stable-appearance renderer for exact Create 6.0.10. */
final class CreateRenderer implements BlockRenderer {

    private static final ThreadLocal<Boolean> STOCK_FALLBACK =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final CreateRuntime runtime;
    private final VariantRendererCatalog catalog;
    private final BlockStateModelRenderer materialRenderer;
    private final ResourceModelRenderer partialRenderer;
    private final BeltEmitter belts;
    private final PipeAttachmentEmitter pipes;
    private final CasingConnectedEmitter casings;
    private final TankConnectedEmitter tanks;
    private final CrafterConnectedEmitter crafters;
    private final VaultConnectedEmitter vaults;
    private final ChainRibbonEmitter chains;
    private final CaaWireEmitter wires;
    private final CaaAccumulatorEmitter accumulators;
    private final HypertubeEmitter hypertubes;
    private final CeiBookEmitter ceiBooks;
    private final Set<Profile> profiles;
    private final Map<String, CompiledCreateObj> objects;
    private final CreateObjEmitter objectEmitter;
    private final Map<BlockRendererType, BlockRenderer> hosts = new IdentityHashMap<>();

    CreateRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            CreateRuntime runtime,
            VariantRendererCatalog catalog,
            Map<String, CompiledCreateObj> objects
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
        this.runtime = runtime;
        this.catalog = catalog;
        this.objects = Map.copyOf(objects);
        this.materialRenderer = new BlockStateModelRenderer(resourcePack, textures, settings);
        this.partialRenderer = new ResourceModelRenderer(resourcePack, textures, settings);
        this.belts = new BeltEmitter(resourcePack, partialRenderer);
        this.pipes = new PipeAttachmentEmitter(
                resourcePack, partialRenderer, materialRenderer
        );
        this.casings = new CasingConnectedEmitter(resourcePack, textures);
        this.tanks = new TankConnectedEmitter(resourcePack, textures);
        this.crafters = new CrafterConnectedEmitter(resourcePack, textures);
        this.vaults = new VaultConnectedEmitter(resourcePack, textures);
        this.chains = new ChainRibbonEmitter(resourcePack, textures);
        this.wires = new CaaWireEmitter(resourcePack, textures);
        this.accumulators = new CaaAccumulatorEmitter(resourcePack, textures);
        this.hypertubes = new HypertubeEmitter(resourcePack, textures);
        this.ceiBooks = new CeiBookEmitter(resourcePack, textures);
        this.profiles = runtime.profiles(resourcePack);
        this.objectEmitter = new CreateObjEmitter(resourcePack, textures, settings);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        try {
            if (!renderCreate(block, variant, target, mapColor)) {
                stock(block, variant, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (Error error) {
            CreateRuntime.throwIfFatal(error);
            reset(target, start);
            runtime.inactive("renderer-" + error.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start);
        } catch (RuntimeException exception) {
            reset(target, start);
            runtime.report("renderer-" + exception.getClass().getSimpleName());
            stockSafely(block, variant, target, mapColor, start);
        }
    }

    private boolean renderCreate(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (!runtime.active() || catalog == null) {
            return false;
        }
        String blockId = block.getBlockState().getId().getFormatted();
        if (CreateCatalog.COPYCAT_PANEL.equals(blockId)
                || CreateCatalog.COPYCAT_STEP.equals(blockId)) {
            return renderCopycat(blockId, block, target, mapColor);
        }
        if (CreateCatalog.BELT.equals(blockId)) {
            boolean casing = "true".equals(
                    block.getBlockState().getProperties().get("casing")
            );
            if (casing) {
                stock(block, variant, target, mapColor);
            }
            boolean emitted = belts.emit(block, target, mapColor);
            return emitted || casing;
        }
        if (CreateCatalog.CONNECTED_CASINGS.contains(blockId)) {
            return casings.emit(blockId, block, target, mapColor);
        }
        if (CreateCatalog.TANK_BLOCKS.contains(blockId)) {
            return renderTank(blockId, block, variant, target, mapColor);
        }
        if (CreateCatalog.MECHANICAL_PUMP.equals(blockId)) {
            return renderPump(blockId, block, variant, target, mapColor);
        }
        if (CreateCatalog.MECHANICAL_CRAFTER.equals(blockId)) {
            return renderCrafter(block, variant, target, mapColor);
        }
        if (CreateCatalog.FACTORY_GAUGE.equals(blockId)) {
            return renderFactoryGauge(block, variant, target, mapColor);
        }
        if (CreateCatalog.ITEM_VAULT.equals(blockId)) {
            return vaults.emit(block, variant, target, mapColor);
        }
        if (CreateCatalog.CHAIN_CONVEYOR.equals(blockId)) {
            return renderChainConveyor(block, variant, target, mapColor);
        }
        if (CreateCatalog.TRACK.equals(blockId)) {
            return renderTrack(block, variant, target, mapColor);
        }
        if (CreateCatalog.MECHANICAL_SAW.equals(blockId)) {
            return renderStableParts(
                    StableCoreRenderPlan.saw(block.getBlockState().getProperties())
                            .orElse(null), block, variant, target, mapColor
            );
        }
        if (CreateCatalog.DEPLOYER.equals(blockId)) {
            return renderStableParts(
                    StableCoreRenderPlan.deployer(block.getBlockState().getProperties())
                            .orElse(null), block, variant, target, mapColor
            );
        }
        if (CreateCatalog.MECHANICAL_ARM.equals(blockId)) {
            boolean goggles = block.getBlockEntity() instanceof ArmBlockEntityData data
                    && data.goggles();
            return renderStableParts(
                    StableCoreRenderPlan.arm(
                            "true".equals(block.getBlockState().getProperties().get("ceiling")),
                            goggles
                    ), block, variant, target, mapColor
            );
        }
        if (CreateCatalog.MILLSTONE.equals(blockId)) {
            return renderStableParts(
                    StableCoreRenderPlan.millstone(), block, variant, target, mapColor
            );
        }
        if (CreateCatalog.PORTABLE_STORAGE_INTERFACE.equals(blockId)
                || CreateCatalog.PORTABLE_FLUID_INTERFACE.equals(blockId)) {
            return renderStableParts(
                    StableCoreRenderPlan.portable(
                            blockId, block.getBlockState().getProperties()
                    ).orElse(null), block, variant, target, mapColor
            );
        }
        if (CreateCatalog.AQUATIC_CONDUIT.equals(blockId)
                && profiles.contains(Profile.AQUATIC_AMBITIONS)) {
            return renderAquaticConduit(block, variant, target, mapColor);
        }
        if (profiles.contains(Profile.CRAFTS_AND_ADDITIONS)) {
            if ("createaddition:liquid_blaze_burner".equals(blockId)) {
                return renderCaaLiquidBurner(block, target, mapColor);
            }
            if ("createaddition:modular_accumulator".equals(blockId)) {
                return renderCaaAccumulator(block, variant, target, mapColor);
            }
            if (isCaaWireNode(blockId)) {
                stock(block, variant, target, mapColor);
                wires.emit(block, target, mapColor);
                return true;
            }
            CaaStableRenderPlan caa = CaaStableRenderPlan.select(
                    blockId, block.getBlockState().getProperties()
            ).orElse(null);
            if (caa != null) {
                return renderStableParts(
                        new StableCoreRenderPlan(caa.parts()),
                        block, variant, target, mapColor
                );
            }
        }
        if (profiles.contains(Profile.HYPERTUBE) && isHypertube(blockId)) {
            return renderHypertube(blockId, block, target, mapColor);
        }
        if (profiles.contains(Profile.ENCHANTMENT_INDUSTRY)) {
            if (isCeiBlaze(blockId)) {
                return renderCeiBlaze(blockId, block, target, mapColor);
            }
            if ("create_enchantment_industry:grindstone_drain".equals(blockId)) {
                return renderCeiGrindstone(block, variant, target, mapColor);
            }
            boolean powered = block.getBlockEntity() instanceof CeiPoweredBlockEntityData data
                    && data.powered();
            CeiStableRenderPlan cei = CeiStableRenderPlan.select(
                    blockId, block.getBlockState().getProperties(), powered
            ).orElse(null);
            if (cei != null) {
                boolean rendered = renderStableParts(
                        new StableCoreRenderPlan(cei.parts()),
                        block, variant, target, mapColor
                );
                if ("create_enchantment_industry:affix_augmentor".equals(blockId)
                        || "create_enchantment_industry:gem_cutter".equals(blockId)) {
                    renderCeiSupport(block, target);
                }
                return rendered;
            }
        }
        if (CreateCatalog.STEAM_ENGINE.equals(blockId)) {
            return renderSteamEngine(block, variant, target, mapColor);
        }
        if (CreateCatalog.POWERED_SHAFT.equals(blockId)) {
            return renderPoweredShaft(block, target);
        }
        if ("create:blaze_burner".equals(blockId)) {
            return renderBlazeBurner(block, variant, target, mapColor);
        }
        String objectName = objectName(blockId, block.getBlockState());
        if (objectName != null) {
            CreateCatalog.ObjRoute route = CreateCatalog.OBJECTS.get(objectName);
            boolean appendedStock = route != null && route.appendStock();
            if (appendedStock) {
                stock(block, variant, target, mapColor);
            }
            return objectEmitter.emit(
                    objects.get(objectName), variant, block, target, mapColor
            ) || appendedStock;
        }
        if (CreateCatalog.PIPE_BLOCKS.contains(blockId)) {
            stock(block, variant, target, mapColor);
            pipes.emit(blockId, block, target, mapColor);
            return true;
        }
        if (CreateCatalog.BRACKET_KINETICS.contains(blockId)) {
            stock(block, variant, target, mapColor);
            pipes.emit(blockId, block, target, mapColor);
            return true;
        }
        List<String> partials = CreateCatalog.PARTIALS.get(blockId);
        if (partials == null) {
            return false;
        }
        stock(block, variant, target, mapColor);
        Color ignored = new Color().set(0F, 0F, 0F, 0F, true);
        for (String model : partials) {
            partialRenderer.render(
                    block,
                    new Variant(
                            new ResourcePath<Model>(model),
                            variant.getX(), variant.getY(), variant.getZ()
                    ),
                    target.initialize(),
                    ignored
            );
        }
        return true;
    }

    private boolean renderStableParts(
            StableCoreRenderPlan plan,
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        if (plan == null) {
            return true;
        }
        int partialStart = target.getTileModel().size();
        for (StableCoreRenderPlan.Part part : plan.parts()) {
            if (!renderPartial(part.model(), part.transform(), block, target)) {
                reset(target, partialStart);
                break;
            }
        }
        return true;
    }

    private boolean renderAquaticConduit(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        AquaticConduitRenderPlan plan = AquaticConduitRenderPlan.select(
                block.getBlockState().getProperties()
        ).orElse(null);
        if (plan == null) {
            return false;
        }
        return renderStableParts(
                new StableCoreRenderPlan(plan.parts()),
                block, variant, target, mapColor
        );
    }

    private boolean renderChainConveyor(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        int dynamicStart = target.getTileModel().size();
        if (!renderObject("chain_conveyor_wheel", AffineTransform.identity(),
                block, target, mapColor)
                || !renderObject("chain_conveyor_shaft", AffineTransform.identity(),
                block, target, mapColor)) {
            reset(target, dynamicStart);
            return true;
        }
        if (!(block.getBlockEntity() instanceof ChainConveyorBlockEntityData data)) {
            return true;
        }
        ChainConveyorRenderPlan plan = ChainConveyorRenderPlan.select(
                data.connections(), data.speed()
        );
        for (ChainConveyorRenderPlan.Connection connection : plan.connections()) {
            int connectionStart = target.getTileModel().size();
            if (!renderObject("chain_conveyor_guard", connection.guard(),
                    block, target, mapColor)
                    || !chains.emit(connection, block, target, mapColor)) {
                reset(target, connectionStart);
            }
        }
        return true;
    }

    private boolean renderObject(
            String name,
            AffineTransform transform,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getTileModel().size();
        boolean emitted = objectEmitter.emit(
                objects.get(name),
                new Variant(new ResourcePath<Model>("minecraft:block/air")),
                block, target, mapColor
        );
        if (!emitted) {
            reset(target, start);
            return false;
        }
        BeltEmitter.apply(target.initialize(start), transform);
        return true;
    }

    private boolean renderTrack(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        if (!(block.getBlockEntity() instanceof TrackBlockEntityData data)) {
            return true;
        }
        for (TrackBlockEntityData.Connection connection : data.connections()) {
            if (!validTrackReciprocal(block, connection)) {
                continue;
            }
            TrackRenderPlan plan = TrackRenderPlan.select(connection).orElse(null);
            if (plan == null) {
                continue;
            }
            int curveStart = target.getTileModel().size();
            boolean complete = true;
            for (TrackRenderPlan.Piece piece : plan.pieces()) {
                boolean emitted = piece.kind() == TrackRenderPlan.Kind.JSON
                        ? renderPartial(piece.resource(), piece.transform(), block, target)
                        : renderObject(piece.resource(), piece.transform(),
                                block, target, mapColor);
                if (!emitted) {
                    complete = false;
                    break;
                }
            }
            if (!complete) {
                reset(target, curveStart);
            }
        }
        return true;
    }

    static boolean validTrackReciprocal(
            BlockNeighborhood block,
            TrackBlockEntityData.Connection connection
    ) {
        if (connection == null || !connection.localPrimary()) {
            return false;
        }
        TrackRenderPlan.IntPoint delta = connection.secondPosition();
        if (Math.abs((long) delta.x()) > 256L || Math.abs((long) delta.y()) > 256L
                || Math.abs((long) delta.z()) > 256L) {
            return false;
        }
        long remoteX = (long) block.getX() + delta.x();
        long remoteY = (long) block.getY() + delta.y();
        long remoteZ = (long) block.getZ() + delta.z();
        if (remoteX < Integer.MIN_VALUE || remoteX > Integer.MAX_VALUE
                || remoteY < Integer.MIN_VALUE || remoteY > Integer.MAX_VALUE
                || remoteZ < Integer.MIN_VALUE || remoteZ > Integer.MAX_VALUE) {
            return false;
        }

        // BlockNeighborhood is an eight-block ring cache. Long-range offsets
        // that are congruent modulo eight can alias the current block, so use
        // an independent absolute block view for persisted track endpoints.
        var remote = block.copy();
        // ExtendedBlock.copy() retains the backing access position but starts
        // its wrapper coordinates at the origin. Synchronize them first so a
        // legitimate endpoint at absolute (0, 0, 0) cannot early-return.
        remote.set(block.getX(), block.getY(), block.getZ());
        remote.set((int) remoteX, (int) remoteY, (int) remoteZ);
        return CreateCatalog.TRACK.equals(remote.getBlockState().getId().getFormatted())
                && remote.getBlockEntity() instanceof TrackBlockEntityData remoteData
                && remoteData.connections().stream().anyMatch(connection::reciprocal);
    }

    private static boolean isCaaWireNode(String blockId) {
        return switch (blockId) {
            case "createaddition:connector",
                    "createaddition:large_connector",
                    "createaddition:small_light_connector",
                    "createaddition:redstone_relay" -> true;
            default -> false;
        };
    }

    private static boolean isHypertube(String blockId) {
        return switch (blockId) {
            case "create_hypertube:hypertube",
                    "create_hypertube:hypertube_accelerator",
                    "create_hypertube:hypertube_entrance",
                    "create_hypertube:hypertube_junction" -> true;
            default -> false;
        };
    }

    private static boolean isCeiBlaze(String blockId) {
        return switch (blockId) {
            case "create_enchantment_industry:blaze_enchanter",
                    "create_enchantment_industry:classic_blaze_enchanter",
                    "create_enchantment_industry:blaze_forger",
                    "create_enchantment_industry:blaze_composer" -> true;
            default -> false;
        };
    }

    private boolean renderHypertube(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (!(block.getBlockEntity() instanceof HypertubeBlockEntityData data)) {
            return true;
        }
        HypertubeAttachmentPlan attachmentPlan = HypertubeAttachmentPlan.select(
                blockId, block.getBlockState().getProperties(), data.attachments()
        );
        int attachmentStart = target.getTileModel().size();
        for (StableCoreRenderPlan.Part part : attachmentPlan.parts()) {
            if (!renderPartial(part.model(), part.transform(), block, target)) {
                reset(target, attachmentStart);
                break;
            }
        }
        hypertubes.emit(data.curves(), block, target, mapColor);
        return true;
    }

    private boolean renderCeiGrindstone(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        CeiStableRenderPlan.Grindstone plan = CeiStableRenderPlan.grindstone(
                block.getBlockState().getProperties()
        ).orElse(null);
        if (plan == null) {
            return true;
        }
        partialRenderer.render(
                block,
                new Variant(
                        new ResourcePath<Model>(
                                "create_enchantment_industry:block/mechanical_grindstone"
                        ),
                        plan.xRotation(), plan.yRotation(), 0F
                ),
                target.initialize(),
                new Color().set(0F, 0F, 0F, 0F, true)
        );
        return true;
    }

    private boolean renderCeiBlaze(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (!renderObject(
                "blaze_burner", AffineTransform.identity(), block, target, mapColor
        )) {
            return false;
        }
        CeiBlazeRenderPlan plan = CeiBlazeRenderPlan.select(
                blockId, block.getBlockState().getProperties()
        ).orElse(null);
        if (plan == null) {
            return true;
        }
        if ("create_enchantment_industry:classic_blaze_enchanter".equals(blockId)) {
            ceiBooks.emit(
                    block.getBlockState().getProperties().get("facing"),
                    target, mapColor
            );
        }
        int partialStart = target.getTileModel().size();
        if (!renderPartial(
                plan.blaze().headModel(), plan.blaze().headTransform(), block, target
        )) {
            reset(target, partialStart);
            return true;
        }
        if (plan.hat() != null && !renderPartial(
                plan.hat().model(), plan.hat().transform(), block, target
        )) {
            reset(target, partialStart);
            return true;
        }
        for (BlazeBurnerRenderPlan.Part rods : plan.blaze().rods()) {
            if (!renderBlazeRods(rods, block, target)) {
                reset(target, partialStart);
                break;
            }
        }
        return true;
    }

    private void renderCeiSupport(
            BlockNeighborhood block,
            TileModelView target
    ) {
        var below = block.getNeighborBlock(0, -1, 0);
        StableCoreRenderPlan.Part support = CeiStableRenderPlan.support(
                below.getBlockState().getId().getFormatted(),
                below.getBlockState().getProperties()
        ).orElse(null);
        if (support != null) {
            renderPartial(support.model(), support.transform(), block, target);
        }
    }

    private boolean renderCaaAccumulator(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (!accumulators.emit(block, variant, target, mapColor)) {
            return false;
        }
        if (!(block.getBlockEntity() instanceof CaaAccumulatorBlockEntityData data)
                || !data.controller()) {
            return true;
        }
        CaaAccumulatorGaugePlan plan = CaaAccumulatorGaugePlan.select(
                data.size(), data.height()
        );
        int gaugeStart = target.getTileModel().size();
        for (CaaAccumulatorGaugePlan.Gauge gauge : plan.gauges()) {
            if (!renderPartial(
                    "createaddition:block/modular_accumulator/guage",
                    gauge.housing(), block, target
            ) || !renderPartial(
                    "createaddition:block/modular_accumulator/dial",
                    gauge.dial(), block, target
            )) {
                reset(target, gaugeStart);
                break;
            }
        }
        return true;
    }

    private boolean renderCaaLiquidBurner(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getTileModel().size();
        if (!renderObject(
                "blaze_burner", AffineTransform.identity(), block, target, mapColor
        )) {
            return false;
        }
        CaaLiquidBurnerRenderPlan plan = CaaLiquidBurnerRenderPlan.select(
                block.getBlockState().getProperties()
        ).orElse(null);
        if (plan == null) {
            return true;
        }
        if (!renderPartial(
                plan.blaze().headModel(), plan.blaze().headTransform(), block, target
        ) || !renderPartial(
                plan.hat().model(), plan.hat().transform(), block, target
        )) {
            reset(target, start);
            return false;
        }
        for (BlazeBurnerRenderPlan.Part rods : plan.blaze().rods()) {
            if (!renderBlazeRods(rods, block, target)) {
                reset(target, start);
                return false;
            }
        }
        return true;
    }

    private boolean renderFactoryGauge(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        if (!(block.getBlockEntity() instanceof FactoryGaugeBlockEntityData data)) {
            return true;
        }
        FactoryGaugeRenderPlan plan = FactoryGaugeRenderPlan.select(
                block.getBlockState().getProperties(),
                data.activeSlots(),
                data.restocker()
        ).orElse(null);
        if (plan == null) {
            return true;
        }
        int partialStart = target.getTileModel().size();
        for (FactoryGaugeRenderPlan.Panel panel : plan.panels()) {
            if (!renderPartial(panel.model(), panel.transform(), block, target)) {
                reset(target, partialStart);
                break;
            }
        }
        return true;
    }

    private boolean renderTank(
            String blockId,
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (!tanks.emit(blockId, block, variant, target, mapColor)) {
            return false;
        }
        if (!(block.getBlockEntity() instanceof TankBlockEntityData entity)
                || !entity.isController()) {
            return true;
        }
        List<BoilerGaugeRenderPlan.Side> gauges = BoilerGaugeRenderPlan.select(
                entity.size(), entity.activeBoiler()
        );
        int gaugeStart = target.getTileModel().size();
        for (BoilerGaugeRenderPlan.Side gauge : gauges) {
            if (!renderPartial(
                    "create:block/steam_engine/gauge",
                    gauge.housing(), block, target
            ) || !renderPartial(
                    "create:block/steam_engine/gauge_dial",
                    gauge.dial(), block, target
            )) {
                reset(target, gaugeStart);
                break;
            }
        }
        return true;
    }

    private boolean renderPump(
            String blockId,
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        DirectionalPartialTransforms.pump(
                block.getBlockState().getProperties().get("facing")
        ).ifPresent(transform -> renderPartial(
                "create:block/mechanical_pump/cog", transform, block, target
        ));
        pipes.emit(blockId, block, target, mapColor);
        return true;
    }

    private boolean renderCrafter(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (!crafters.emit(block, variant, target, mapColor)) {
            stock(block, variant, target, mapColor);
        }
        Map<String, String> properties = block.getBlockState().getProperties();
        CreateDirection targetDirection = DirectionalPartialTransforms.crafterTarget(
                properties.get("facing"), properties.get("pointing")
        ).orElse(null);
        if (targetDirection == null) {
            return true;
        }
        var neighbor = block.getNeighborBlock(
                targetDirection.x(), targetDirection.y(), targetDirection.z()
        );
        boolean covered = block.getBlockEntity() instanceof CrafterBlockEntityData data
                && data.covered();
        CrafterRenderPlan plan = CrafterRenderPlan.select(
                properties,
                neighbor.getBlockState().getId().getFormatted(),
                neighbor.getBlockState().getProperties(),
                covered
        ).orElse(null);
        if (plan == null) {
            return true;
        }
        int partialStart = target.getTileModel().size();
        if (!renderPartial(
                "create:block/cogwheel_shaftless", plan.cogTransform(), block, target
        )) {
            reset(target, partialStart);
            return true;
        }
        for (String model : plan.bodyModels()) {
            if (!renderPartial(model, plan.bodyTransform(), block, target)) {
                reset(target, partialStart);
                break;
            }
        }
        return true;
    }

    private boolean renderSteamEngine(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        stock(block, variant, target, mapColor);
        Map<String, String> properties = block.getBlockState().getProperties();
        CreateDirection outward = SteamEngineRenderPlan.outward(
                properties.get("face"), properties.get("facing")
        ).orElse(null);
        if (outward == null) {
            return true;
        }
        var shaft = block.getNeighborBlock(
                outward.x() * 2, outward.y() * 2, outward.z() * 2
        );
        if (!CreateCatalog.POWERED_SHAFT.equals(
                shaft.getBlockState().getId().getFormatted()
        )) {
            return true;
        }
        SteamEngineRenderPlan plan = SteamEngineRenderPlan.select(
                properties.get("face"),
                properties.get("facing"),
                shaft.getBlockState().getProperties().get("axis")
        ).orElse(null);
        if (plan == null) {
            return true;
        }
        int partialStart = target.getTileModel().size();
        if (!renderPartial(
                "create:block/steam_engine/piston", plan.piston(), block, target
        ) || !renderPartial(
                "create:block/steam_engine/linkage", plan.linkage(), block, target
        ) || !renderPartial(
                "create:block/steam_engine/shaft_connector", plan.connector(), block, target
        )) {
            reset(target, partialStart);
        }
        return true;
    }

    private boolean renderPoweredShaft(
            BlockNeighborhood block,
            TileModelView target
    ) {
        PoweredShaftRenderPlan plan = PoweredShaftRenderPlan.select(
                block.getBlockState().getProperties().get("axis")
        ).orElse(null);
        if (plan == null) {
            return false;
        }
        int start = target.getTileModel().size();
        partialRenderer.render(
                block,
                new Variant(
                        new ResourcePath<Model>("create:block/powered_shaft"),
                        plan.xRotation(), plan.yRotation(), 0F
                ),
                target.initialize(),
                new Color().set(0F, 0F, 0F, 0F, true)
        );
        return target.getTileModel().size() > start;
    }

    private boolean renderBlazeBurner(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        boolean cage = objectEmitter.emit(
                objects.get("blaze_burner"), variant, block, target, mapColor
        );
        if (!cage) {
            return false;
        }
        String above = block.getNeighborBlock(0, 1, 0)
                .getBlockState().getId().getFormatted();
        boolean validAbove = "create:basin".equals(above)
                || CreateCatalog.TANK_BLOCKS.contains(above);
        BlazeBurnerRenderPlan plan = BlazeBurnerRenderPlan.select(
                block.getBlockState().getProperties().get("blaze"),
                block.getBlockState().getProperties().get("facing"),
                validAbove
        ).orElse(null);
        if (plan == null) {
            return true;
        }
        int partialStart = target.getTileModel().size();
        if (!renderPartial(plan.headModel(), plan.headTransform(), block, target)) {
            reset(target, partialStart);
            return true;
        }
        for (BlazeBurnerRenderPlan.Part part : plan.rods()) {
            if (!renderBlazeRods(part, block, target)) {
                reset(target, partialStart);
                break;
            }
        }
        return true;
    }

    private boolean renderBlazeRods(
            BlazeBurnerRenderPlan.Part part,
            BlockNeighborhood block,
            TileModelView target
    ) {
        String object = switch (part.model()) {
            case "create:block/blaze_burner/rods_small" -> "blaze_rods_small";
            case "create:block/blaze_burner/rods_large" -> "blaze_rods_large";
            case "create:block/blaze_burner/superheated_rods_small" ->
                    "blaze_superheated_rods_small";
            case "create:block/blaze_burner/superheated_rods_large" ->
                    "blaze_superheated_rods_large";
            default -> null;
        };
        if (object == null) {
            return false;
        }
        int start = target.getTileModel().size();
        boolean emitted = objectEmitter.emit(
                objects.get(object),
                new Variant(new ResourcePath<Model>("minecraft:block/air")),
                block,
                target,
                new Color().set(0F, 0F, 0F, 0F, true)
        );
        if (!emitted) {
            reset(target, start);
            return false;
        }
        BeltEmitter.apply(target.initialize(start), part.transform());
        return true;
    }

    private boolean renderPartial(
            String model,
            AffineTransform transform,
            BlockNeighborhood block,
            TileModelView target
    ) {
        int start = target.getTileModel().size();
        partialRenderer.render(
                block,
                new Variant(new ResourcePath<Model>(model)),
                target.initialize(),
                new Color().set(0F, 0F, 0F, 0F, true)
        );
        if (target.getTileModel().size() == start) {
            return false;
        }
        BeltEmitter.apply(target.initialize(start), transform);
        return true;
    }

    private boolean renderCopycat(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (!(block.getBlockEntity() instanceof CopycatBlockEntityData entity)) {
            return false;
        }
        BlockState material = entity.material();
        if (material == null || material.isAir()
                || CreateCatalog.CUSTOM_BLOCKS.contains(material.getId().getFormatted())
                || resourcePack.getBlockState(material) == null) {
            return false;
        }
        int start = target.getTileModel().size();
        materialRenderer.render(block, material, target.initialize(), mapColor);
        if (target.getTileModel().size() == start) {
            return false;
        }
        if (CreateCatalog.COPYCAT_PANEL.equals(blockId)
                && material.getId().getValue().endsWith("_trapdoor")) {
            target.initialize(start);
            return true;
        }
        target.initialize(start);
        if (CreateCatalog.COPYCAT_STEP.equals(blockId)) {
            transformStep(block.getBlockState(), target);
        } else {
            transformPanel(block.getBlockState(), target);
        }
        return true;
    }

    private static void transformStep(BlockState state, TileModelView target) {
        boolean top = "top".equals(state.getProperties().get("half"));
        String facing = state.getProperties().getOrDefault("facing", "south");
        float sx = 1F;
        float sz = 1F;
        float tx = 0F;
        float tz = 0F;
        if ("east".equals(facing) || "west".equals(facing)) {
            sx = 0.5F;
            tx = "east".equals(facing) ? 0.5F : 0F;
        } else {
            sz = 0.5F;
            tz = "south".equals(facing) ? 0.5F : 0F;
        }
        target.scale(sx, 0.5F, sz).translate(tx, top ? 0.5F : 0F, tz);
    }

    private static String objectName(String blockId, BlockState state) {
        return switch (blockId) {
            case "create:flywheel" -> "flywheel";
            case "create:crushing_wheel" -> "crushing_wheel";
            case "create:large_water_wheel" -> "true".equals(
                    state.getProperties().get("extension")
            ) ? "large_water_wheel_extension" : "large_water_wheel";
            case "create:water_wheel" -> "water_wheel";
            case "create:blaze_burner" -> "blaze_burner";
            default -> null;
        };
    }

    private static void transformPanel(BlockState state, TileModelView target) {
        float thickness = 3F / 16F;
        float far = 1F - thickness;
        String facing = state.getProperties().getOrDefault("facing", "up");
        switch (facing) {
            case "down" -> target.scale(1F, thickness, 1F).translate(0F, far, 0F);
            case "north" -> target.scale(1F, 1F, thickness).translate(0F, 0F, far);
            case "south" -> target.scale(1F, 1F, thickness);
            case "west" -> target.scale(thickness, 1F, 1F).translate(far, 0F, 0F);
            case "east" -> target.scale(thickness, 1F, 1F);
            default -> target.scale(1F, thickness, 1F);
        }
    }

    private void stock(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor
    ) {
        if (STOCK_FALLBACK.get()) {
            return;
        }
        STOCK_FALLBACK.set(Boolean.TRUE);
        try {
            BlockRendererType type = catalog == null
                    ? BlockRendererType.DEFAULT : catalog.original(variant);
            hosts.computeIfAbsent(
                    type, found -> found.create(resourcePack, textures, settings)
            ).render(block, variant, target, mapColor);
        } finally {
            STOCK_FALLBACK.set(Boolean.FALSE);
        }
    }

    private void stockSafely(
            BlockNeighborhood block,
            Variant variant,
            TileModelView target,
            Color mapColor,
            int start
    ) {
        try {
            stock(block, variant, target, mapColor);
        } catch (Error error) {
            CreateRuntime.throwIfFatal(error);
            reset(target, start);
            runtime.report("stock-fallback-" + error.getClass().getSimpleName());
        } catch (RuntimeException exception) {
            reset(target, start);
            runtime.report("stock-fallback-" + exception.getClass().getSimpleName());
        }
    }

    private static void reset(TileModelView target, int start) {
        target.getTileModel().reset(start);
        target.initialize(start);
    }
}
