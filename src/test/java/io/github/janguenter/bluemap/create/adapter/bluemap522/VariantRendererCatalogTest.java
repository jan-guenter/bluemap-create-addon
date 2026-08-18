/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockStateModelRenderer;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockStateCondition;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Multipart;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantRendererCatalogTest {

    private static final Key FLUID_PIPE = Key.parse("create:fluid_pipe");
    private static final Key AIR_MODEL = Key.parse("minecraft:block/air");
    private static final BlockRendererType WRAPPER = new BlockRendererType.Impl(
            Key.parse("test:create_wrapper"),
            (pack, gallery, settings) -> null
    );

    @Test
    void exactPipeMultipartGetsOneCarrierForEveryPortMask() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        pack.getModels().put(AIR_MODEL, new Model(new Element[0]));
        BlockState original = exactPipeMultipartShape();
        pack.getBlockStates().put(FLUID_PIPE, original);

        Map<Integer, Integer> originalMatches = new HashMap<>();
        for (int mask = 0; mask < 64; mask++) {
            var state = pipeState(mask);
            originalMatches.put(mask, selected(original, state).size());
        }

        VariantRendererCatalog catalog = VariantRendererCatalog.wrap(pack, WRAPPER);
        BlockState wrapped = pack.getBlockStates().get(FLUID_PIPE);
        assertNotNull(wrapped);
        assertNotNull(wrapped.getMultipart());
        assertEquals(31, wrapped.getMultipart().getParts().length);

        List<Variant> all = new ArrayList<>();
        wrapped.forEach(all::add);
        assertEquals(30, all.stream()
                .filter(variant -> variant.getRenderer() == BlockRendererType.DEFAULT)
                .count());
        List<Variant> carriers = all.stream()
                .filter(variant -> variant.getRenderer() == WRAPPER)
                .toList();
        assertEquals(1, carriers.size());
        Variant carrier = carriers.getFirst();
        assertEquals(AIR_MODEL, carrier.getModel());
        assertSame(BlockRendererType.DEFAULT, catalog.original(carrier));
        assertEquals(1, catalog.size());
        Model carrierModel = carrier.getModel().getResource(pack.getModels()::get);
        assertNotNull(carrierModel);
        assertTrue(carrierModel.getElements() == null
                || carrierModel.getElements().length == 0);

        for (int mask = 0; mask < 64; mask++) {
            var state = pipeState(mask);
            BlockState throughCache = pack.getBlockState(state);
            assertSame(wrapped, throughCache);
            List<Variant> variants = selected(throughCache, state);
            long custom = variants.stream()
                    .filter(variant -> variant.getRenderer() == WRAPPER)
                    .count();
            long stock = variants.stream()
                    .filter(variant -> variant.getRenderer() == BlockRendererType.DEFAULT)
                    .count();
            assertEquals(1, custom, "wrapper count for mask " + mask);
            assertEquals(
                    originalMatches.get(mask).longValue(),
                    stock,
                    "stock count for mask " + mask
            );
            assertEquals(
                    originalMatches.get(mask) + 1,
                    variants.size(),
                    "total count for mask " + mask
            );
        }

        VariantRendererCatalog second = VariantRendererCatalog.wrap(pack, WRAPPER);
        assertEquals(31, pack.getBlockStates().get(FLUID_PIPE)
                .getMultipart().getParts().length);
        assertEquals(1, second.size());
    }

    @Test
    void missingEmptyCarrierFallsBackWithoutMutatingMultipart() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        BlockState original = exactPipeMultipartShape();
        pack.getBlockStates().put(FLUID_PIPE, original);

        VariantRendererCatalog catalog = VariantRendererCatalog.wrap(pack, WRAPPER);

        assertSame(original, pack.getBlockStates().get(FLUID_PIPE));
        assertEquals(30, original.getMultipart().getParts().length);
        List<Variant> variants = new ArrayList<>();
        original.forEach(variants::add);
        assertEquals(30, variants.stream()
                .filter(variant -> variant.getRenderer() == WRAPPER)
                .count());
        assertEquals(30, catalog.size());
    }

    @Test
    void blueMapDispatcherInvokesCarrierExactlyOnceForEveryPortMask() {
        AtomicInteger callbacks = new AtomicInteger();
        BlockRendererType countingWrapper = new BlockRendererType.Impl(
                Key.parse("test:counting_create_wrapper"),
                (pack, gallery, settings) -> (block, variant, target, color) ->
                        callbacks.incrementAndGet()
        );
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        pack.getModels().put(AIR_MODEL, new Model(new Element[0]));
        pack.getBlockStates().put(FLUID_PIPE, exactPipeMultipartShape());
        VariantRendererCatalog.wrap(pack, countingWrapper);
        BlockStateModelRenderer renderer = new BlockStateModelRenderer(
                pack, new TextureGallery(), SETTINGS
        );

        for (int mask = 0; mask < 64; mask++) {
            var state = pipeState(mask);
            BlockNeighborhood neighborhood = new BlockNeighborhood(
                    new SingleBlockAccess(state),
                    pack,
                    SETTINGS,
                    DimensionType.OVERWORLD
            );
            neighborhood.set(11, 22, 33);
            callbacks.set(0);
            renderer.render(
                    neighborhood,
                    new TileModelView(new ArrayTileModel(256)),
                    new Color()
            );
            assertEquals(1, callbacks.get(), "callback count for mask " + mask);
        }
    }

    @Test
    void everyHypertubeMultipartUsesOneCarrierWithoutWrappingAuthoredParts() {
        for (String id : List.of(
                "create_hypertube:hypertube",
                "create_hypertube:hypertube_accelerator",
                "create_hypertube:hypertube_entrance",
                "create_hypertube:hypertube_junction"
        )) {
            ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
            pack.getModels().put(AIR_MODEL, new Model(new Element[0]));
            Variant first = new Variant(new ResourcePath<Model>("test:block/first"));
            Variant second = new Variant(new ResourcePath<Model>("test:block/second"));
            Key key = Key.parse(id);
            pack.getBlockStates().put(key, new BlockState(new Multipart(
                    new VariantSet[]{new VariantSet(first), new VariantSet(second)}
            )));

            VariantRendererCatalog catalog = VariantRendererCatalog.wrap(
                    pack, WRAPPER, Set.of(id)
            );
            List<Variant> variants = new ArrayList<>();
            pack.getBlockStates().get(key).forEach(variants::add);
            assertEquals(3, variants.size(), id);
            assertEquals(2, variants.stream()
                    .filter(variant -> variant.getRenderer() == BlockRendererType.DEFAULT)
                    .count(), id);
            assertEquals(1, variants.stream()
                    .filter(variant -> variant.getRenderer() == WRAPPER)
                    .count(), id);
            assertEquals(1, catalog.size(), id);
        }
    }

    @Test
    void connectorCarrierDispatchesOnceForNormalAndGirderMultipartStates() {
        for (String id : List.of(
                "createaddition:connector",
                "createaddition:large_connector",
                "createaddition:small_light_connector"
        )) {
            AtomicInteger callbacks = new AtomicInteger();
            BlockRendererType countingWrapper = new BlockRendererType.Impl(
                    Key.parse("test:" + id.substring(id.indexOf(':') + 1)),
                    (pack, gallery, settings) -> (block, variant, target, color) ->
                            callbacks.incrementAndGet()
            );
            ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
            pack.getModels().put(AIR_MODEL, new Model(new Element[0]));
            Key bodyModel = Key.parse("test:block/connector_body");
            Key girderModel = Key.parse("test:block/connector_girder");
            pack.getModels().put(bodyModel, new Model(new Element[0]));
            pack.getModels().put(girderModel, new Model(new Element[0]));
            pack.getBlockStates().put(Key.parse(id), new BlockState(new Multipart(
                    new VariantSet[]{
                            new VariantSet(BlockStateCondition.and(
                                    BlockStateCondition.property("facing", "north"),
                                    BlockStateCondition.property("mode", "none")
                            ), new Variant(new ResourcePath<Model>(bodyModel))),
                            new VariantSet(BlockStateCondition.and(
                                    BlockStateCondition.property("facing", "north"),
                                    BlockStateCondition.property("variant", "girder")
                            ), new Variant(new ResourcePath<Model>(girderModel)))
                    }
            )));
            VariantRendererCatalog.wrap(pack, countingWrapper, Set.of(id));
            BlockStateModelRenderer renderer = new BlockStateModelRenderer(
                    pack, new TextureGallery(), SETTINGS
            );

            for (String connectorVariant : List.of("normal", "girder")) {
                var state = de.bluecolored.bluemap.core.world.BlockState.fromString(
                        id + "[facing=north,mode=none,variant="
                                + connectorVariant + "]"
                );
                List<Variant> selected = selected(pack.getBlockState(state), state);
                assertEquals("girder".equals(connectorVariant) ? 3 : 2,
                        selected.size(), id + ' ' + connectorVariant);
                BlockNeighborhood neighborhood = new BlockNeighborhood(
                        new SingleBlockAccess(state), pack, SETTINGS,
                        DimensionType.OVERWORLD
                );
                neighborhood.set(11, 22, 33);
                callbacks.set(0);
                renderer.render(
                        neighborhood,
                        new TileModelView(new ArrayTileModel(64)),
                        new Color()
                );
                assertEquals(1, callbacks.get(), id + ' ' + connectorVariant);
            }
        }
    }

    @Test
    void encasedPipeKeepsSixAuthoredFacesAndDispatchesDynamicGeometryOnce() {
        AtomicInteger callbacks = new AtomicInteger();
        BlockRendererType countingWrapper = new BlockRendererType.Impl(
                Key.parse("test:encased_pipe_wrapper"),
                (pack, gallery, settings) -> (block, variant, target, color) ->
                        callbacks.incrementAndGet()
        );
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        pack.getModels().put(AIR_MODEL, new Model(new Element[0]));
        VariantSet[] faces = new VariantSet[6];
        for (int index = 0; index < faces.length; index++) {
            Key model = Key.parse("test:block/encased_face_" + index);
            pack.getModels().put(model, new Model(new Element[0]));
            faces[index] = new VariantSet(new Variant(new ResourcePath<Model>(model)));
        }
        String id = "create:encased_fluid_pipe";
        pack.getBlockStates().put(Key.parse(id),
                new BlockState(new Multipart(faces)));
        VariantRendererCatalog.wrap(pack, countingWrapper, Set.of(id));
        var state = de.bluecolored.bluemap.core.world.BlockState.fromString(
                id + "[axis=x,waterlogged=false]"
        );
        List<Variant> variants = selected(pack.getBlockState(state), state);
        assertEquals(7, variants.size());
        assertEquals(6, variants.stream().filter(variant ->
                variant.getRenderer() == BlockRendererType.DEFAULT).count());
        assertEquals(1, variants.stream().filter(variant ->
                variant.getRenderer() == countingWrapper).count());

        BlockStateModelRenderer renderer = new BlockStateModelRenderer(
                pack, new TextureGallery(), SETTINGS
        );
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new SingleBlockAccess(state), pack, SETTINGS, DimensionType.OVERWORLD
        );
        neighborhood.set(11, 22, 33);
        renderer.render(
                neighborhood, new TileModelView(new ArrayTileModel(64)), new Color()
        );
        assertEquals(1, callbacks.get());
    }

    /**
     * Create's exact JSON has one part for every one- or two-port mask in each
     * plane perpendicular to X, Y and Z: 3 * (C(4,1) + C(4,2)) = 30.
     */
    private static BlockState exactPipeMultipartShape() {
        List<VariantSet> parts = new ArrayList<>();
        int model = 0;
        for (CreateDirection.Axis axis : CreateDirection.Axis.values()) {
            List<CreateDirection> perpendicular = List.of(CreateDirection.values())
                    .stream()
                    .filter(direction -> direction.axis() != axis)
                    .toList();
            for (int mask = 0; mask < 16; mask++) {
                int ports = Integer.bitCount(mask);
                if (ports != 1 && ports != 2) {
                    continue;
                }
                List<BlockStateCondition> conditions = new ArrayList<>();
                for (int index = 0; index < perpendicular.size(); index++) {
                    conditions.add(BlockStateCondition.property(
                            name(perpendicular.get(index)),
                            Boolean.toString((mask & 1 << index) != 0)
                    ));
                }
                Variant variant = new Variant(new ResourcePath<Model>(
                        "test:block/fluid_pipe_part_" + model++
                ));
                parts.add(new VariantSet(
                        BlockStateCondition.and(
                                conditions.toArray(BlockStateCondition[]::new)
                        ),
                        variant
                ));
            }
        }
        assertEquals(30, parts.size());
        return new BlockState(new Multipart(parts.toArray(VariantSet[]::new)));
    }

    private static List<Variant> selected(
            BlockState resource,
            de.bluecolored.bluemap.core.world.BlockState state
    ) {
        List<Variant> variants = new ArrayList<>();
        resource.forEach(state, 11, 22, 33, variants::add);
        return variants;
    }

    private static de.bluecolored.bluemap.core.world.BlockState pipeState(int mask) {
        StringBuilder state = new StringBuilder("create:fluid_pipe[");
        CreateDirection[] directions = CreateDirection.values();
        for (int index = 0; index < directions.length; index++) {
            if (index != 0) {
                state.append(',');
            }
            state.append(name(directions[index]))
                    .append('=')
                    .append((mask & 1 << index) != 0);
        }
        return de.bluecolored.bluemap.core.world.BlockState.fromString(
                state.append(",waterlogged=false]").toString()
        );
    }

    private static String name(CreateDirection direction) {
        return direction.name().toLowerCase(Locale.ROOT);
    }

    private static final RenderSettings SETTINGS = new RenderSettings() {
        @Override
        public int getRemoveCavesBelowY() {
            return Integer.MIN_VALUE;
        }

        @Override
        public int getCaveDetectionOceanFloor() {
            return 0;
        }

        @Override
        public boolean isCaveDetectionUsesBlockLight() {
            return false;
        }

        @Override
        public float getAmbientLight() {
            return 0F;
        }

        @Override
        public Mask getRenderMask() {
            return Mask.ALL;
        }

        @Override
        public boolean isSaveHiresLayer() {
            return false;
        }

        @Override
        public boolean isRenderTopOnly() {
            return false;
        }
    };

    private static final class SingleBlockAccess implements BlockAccess {

        private final de.bluecolored.bluemap.core.world.BlockState center;
        private int x;
        private int y;
        private int z;

        private SingleBlockAccess(de.bluecolored.bluemap.core.world.BlockState center) {
            this.center = center;
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            return new SingleBlockAccess(center);
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public de.bluecolored.bluemap.core.world.BlockState getBlockState() {
            return x == 11 && y == 22 && z == 33
                    ? center : de.bluecolored.bluemap.core.world.BlockState.AIR;
        }

        @Override
        public LightData getLightData() {
            return new LightData(15, 0);
        }

        @Override
        public Biome getBiome() {
            return Biome.DEFAULT;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return null;
        }

        @Override
        public boolean hasOceanFloorY() {
            return false;
        }

        @Override
        public int getOceanFloorY() {
            return 0;
        }
    }
}
