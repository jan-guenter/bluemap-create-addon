/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackEndpointLookupTest {

    private static final int OWNER_X = 216;
    private static final int OWNER_Y = 118;
    private static final int OWNER_Z = 162;
    private static final int REMOTE_X = 224;
    private static final int REMOTE_Y = 118;
    private static final int REMOTE_Z = 170;
    private static final BlockState TRACK = new BlockState(Key.parse("create:track"));

    @Test
    void independentAbsoluteLookupBypassesPlusEightRingAlias() throws IOException {
        TrackBlockEntityData primary = primary();
        TrackBlockEntityData secondary = secondary();
        BlockNeighborhood neighborhood = neighborhood(Map.of(
                new Position(OWNER_X, OWNER_Y, OWNER_Z),
                new SavedBlock(TRACK, primary),
                new Position(REMOTE_X, REMOTE_Y, REMOTE_Z),
                new SavedBlock(TRACK, secondary)
        ));

        assertSame(neighborhood, neighborhood.getNeighborBlock(8, 0, 8));
        assertTrue(CreateRenderer.validTrackReciprocal(
                neighborhood, primary.connections().getFirst()
        ));
    }

    @Test
    void missingAndMalformedRemoteEndpointsFailClosed() throws IOException {
        TrackBlockEntityData primary = primary();
        TrackBlockEntityData.Connection connection = primary.connections().getFirst();

        BlockNeighborhood missing = neighborhood(Map.of(
                new Position(OWNER_X, OWNER_Y, OWNER_Z),
                new SavedBlock(TRACK, primary)
        ));
        assertFalse(CreateRenderer.validTrackReciprocal(missing, connection));

        BlockNeighborhood malformed = neighborhood(Map.of(
                new Position(OWNER_X, OWNER_Y, OWNER_Z),
                new SavedBlock(TRACK, primary),
                new Position(REMOTE_X, REMOTE_Y, REMOTE_Z),
                new SavedBlock(TRACK, primary)
        ));
        assertFalse(CreateRenderer.validTrackReciprocal(malformed, connection));
    }

    @Test
    void absoluteWorldOriginEndpointDoesNotReuseOwnerBackingBlock()
            throws IOException {
        Position owner = new Position(9, 8, 8);
        Position remote = new Position(0, 0, 0);
        TrackBlockEntityData primary = connection(
                true,
                new int[]{-9, -8, -8},
                new double[][]{{1, 0, .5}, {-8.5, -8, -8}},
                new double[][]{{1, 0, 0}, {0, 0, -1}}
        );
        TrackBlockEntityData secondary = connection(
                false,
                new int[]{9, 8, 8},
                new double[][]{{.5, 0, 0}, {10, 8, 8.5}},
                new double[][]{{0, 0, -1}, {1, 0, 0}}
        );
        BlockNeighborhood neighborhood = neighborhood(Map.of(
                owner, new SavedBlock(TRACK, primary),
                remote, new SavedBlock(TRACK, secondary)
        ), owner);

        var remoteView = neighborhood.copy();
        remoteView.set(owner.x(), owner.y(), owner.z());
        remoteView.set(remote.x(), remote.y(), remote.z());
        assertEquals(TRACK, remoteView.getBlockState());
        assertSame(secondary, remoteView.getBlockEntity());
        assertTrue(primary.connections().getFirst().reciprocal(
                secondary.connections().getFirst()
        ));

        assertTrue(CreateRenderer.validTrackReciprocal(
                neighborhood, primary.connections().getFirst()
        ));
    }

    @Test
    void endpointDistanceCapRemainsFailClosed() throws IOException {
        TrackBlockEntityData primary = connection(
                true,
                new int[]{257, 0, 0},
                new double[][]{{1, 0, .5}, {257.5, 0, 0}},
                new double[][]{{1, 0, 0}, {1, 0, 0}}
        );
        BlockNeighborhood neighborhood = neighborhood(Map.of(
                new Position(OWNER_X, OWNER_Y, OWNER_Z),
                new SavedBlock(TRACK, primary)
        ));

        assertFalse(CreateRenderer.validTrackReciprocal(
                neighborhood, primary.connections().getFirst()
        ));
    }

    private static BlockNeighborhood neighborhood(Map<Position, SavedBlock> blocks) {
        return neighborhood(
                blocks, new Position(OWNER_X, OWNER_Y, OWNER_Z)
        );
    }

    private static BlockNeighborhood neighborhood(
            Map<Position, SavedBlock> blocks,
            Position owner
    ) {
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new SavedBlockAccess(blocks),
                new ResourcePack(new PackVersion(34, 0)),
                SETTINGS,
                DimensionType.OVERWORLD
        );
        neighborhood.set(owner.x(), owner.y(), owner.z());
        return neighborhood;
    }

    private static TrackBlockEntityData primary() throws IOException {
        return connection(
                true,
                new int[]{8, 0, 8},
                new double[][]{{1, 0, .5}, {8.5, 0, 8}},
                new double[][]{{1, 0, 0}, {0, 0, -1}}
        );
    }

    private static TrackBlockEntityData secondary() throws IOException {
        return connection(
                false,
                new int[]{-8, 0, -8},
                new double[][]{{.5, 0, 0}, {-7, 0, -7.5}},
                new double[][]{{0, 0, -1}, {1, 0, 0}}
        );
    }

    private static TrackBlockEntityData connection(
            boolean primary,
            int[] secondPosition,
            double[][] starts,
            double[][] axes
    ) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("Connections").beginList(1, TagType.COMPOUND);
            writer.beginCompound();
            writer.name("Primary").value((byte) (primary ? 1 : 0));
            writer.name("Girder").value((byte) 0);
            writer.name("Material").value("create:andesite");
            positions(writer, new int[]{0, 0, 0}, secondPosition);
            vectors(writer, "Starts", starts);
            vectors(writer, "Axes", axes);
            vectors(writer, "Normals", new double[][]{{0, 1, 0}, {0, 1, 0}});
            writer.endCompound();
            writer.endList();
            writer.endCompound();
        }
        return new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                TrackBlockEntityData.class
        );
    }

    private static void positions(
            NBTWriter writer,
            int[] first,
            int[] second
    ) throws IOException {
        writer.name("Positions").beginList(2, TagType.COMPOUND);
        for (int[] position : new int[][]{first, second}) {
            writer.beginCompound();
            writer.name("Pos").value(position);
            writer.endCompound();
        }
        writer.endList();
    }

    private static void vectors(
            NBTWriter writer,
            String name,
            double[][] vectors
    ) throws IOException {
        writer.name(name).beginList(vectors.length, TagType.COMPOUND);
        for (double[] vector : vectors) {
            writer.beginCompound();
            writer.name("V").beginList(vector.length, TagType.DOUBLE);
            for (double value : vector) {
                writer.value(value);
            }
            writer.endList();
            writer.endCompound();
        }
        writer.endList();
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

    private record Position(int x, int y, int z) {
    }

    private record SavedBlock(BlockState state, BlockEntity blockEntity) {
    }

    private static final class SavedBlockAccess implements BlockAccess {

        private final Map<Position, SavedBlock> blocks;
        private int x;
        private int y;
        private int z;

        private SavedBlockAccess(Map<Position, SavedBlock> blocks) {
            this.blocks = blocks;
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            SavedBlockAccess copy = new SavedBlockAccess(blocks);
            copy.set(x, y, z);
            return copy;
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
        public BlockState getBlockState() {
            SavedBlock block = blocks.get(new Position(x, y, z));
            return block == null ? BlockState.AIR : block.state();
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
            SavedBlock block = blocks.get(new Position(x, y, z));
            return block == null ? null : block.blockEntity();
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
