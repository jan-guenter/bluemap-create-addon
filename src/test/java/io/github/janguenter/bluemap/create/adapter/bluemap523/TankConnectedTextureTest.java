/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TankConnectedTextureTest {

    private static final float EPSILON = 1E-6F;

    @Test
    void rectangleSelectorProducesAllSixteenExactCells() {
        HashSet<Integer> indices = new HashSet<>();
        for (int mask = 0; mask < 16; mask++) {
            TankConnectedTexture.Context context = new TankConnectedTexture.Context(
                    (mask & 1) != 0,
                    (mask & 2) != 0,
                    (mask & 4) != 0,
                    (mask & 8) != 0
            );
            indices.add(TankConnectedTexture.index(context));
        }
        assertEquals(16, indices.size());
        assertEquals(6, TankConnectedTexture.index(
                new TankConnectedTexture.Context(true, true, true, true)
        ));
        assertEquals(12, TankConnectedTexture.index(
                new TankConnectedTexture.Context(false, false, false, false)
        ));
        assertEquals(11, TankConnectedTexture.index(
                new TankConnectedTexture.Context(true, false, true, false)
        ));
    }

    @Test
    void framesMatchExactFaceOrientationIncludingTopAndBottom() {
        assertFrame(CreateDirection.NORTH, CreateDirection.WEST, CreateDirection.UP);
        assertFrame(CreateDirection.SOUTH, CreateDirection.EAST, CreateDirection.UP);
        assertFrame(CreateDirection.WEST, CreateDirection.SOUTH, CreateDirection.UP);
        assertFrame(CreateDirection.EAST, CreateDirection.NORTH, CreateDirection.UP);
        assertFrame(CreateDirection.UP, CreateDirection.EAST, CreateDirection.NORTH);
        assertFrame(CreateDirection.DOWN, CreateDirection.EAST, CreateDirection.SOUTH);
    }

    @Test
    void twoByTwoTopCornersAndThreeByThreeCenterUseExactCells() {
        assertEquals(1, TankConnectedTexture.index(
                new TankConnectedTexture.Context(false, true, false, true)
        ));
        assertEquals(3, TankConnectedTexture.index(
                new TankConnectedTexture.Context(false, true, true, false)
        ));
        assertEquals(9, TankConnectedTexture.index(
                new TankConnectedTexture.Context(true, false, false, true)
        ));
        assertEquals(11, TankConnectedTexture.index(
                new TankConnectedTexture.Context(true, false, true, false)
        ));
        assertEquals(6, TankConnectedTexture.index(
                new TankConnectedTexture.Context(true, true, true, true)
        ));
    }

    @Test
    void standardAndCreativeMaterialsIncludeTopAndInnerRoutes() {
        assertEquals("create:block/fluid_tank_connected",
                material("create:fluid_tank", "create:block/fluid_tank"));
        assertEquals("create:block/fluid_tank_top_connected",
                material("create:fluid_tank", "create:block/fluid_tank_top"));
        assertEquals("create:block/fluid_tank_inner_connected",
                material("create:fluid_tank", "create:block/fluid_tank_inner"));
        assertEquals("create:block/creative_fluid_tank_connected",
                material("create:creative_fluid_tank", "create:block/creative_fluid_tank"));
        assertEquals("create:block/creative_casing_connected",
                material("create:creative_fluid_tank", "create:block/creative_casing"));
        assertFalse(TankConnectedTexture.material(
                "create:fluid_tank", "create:block/fluid_tank_window"
        ).isPresent());
    }

    @Test
    void uvProjectionSelectsOneQuarterSheetCell() {
        TankConnectedTexture.Uv uv = TankConnectedTexture.connectedUv(11, 0.25F, 0.75F);
        assertEquals((3F + 0.25F) / 4F, uv.u(), EPSILON);
        assertEquals((2F + 0.75F) / 4F, uv.v(), EPSILON);
    }

    @Test
    void connectivityRequiresSameBlockAndControllerIdentity() {
        TankConnectedTexture.Position controller = new TankConnectedTexture.Position(1, 2, 3);
        TankConnectedTexture.GroupKey first = new TankConnectedTexture.GroupKey(
                "create:fluid_tank", controller
        );
        TankConnectedTexture.GroupKey same = new TankConnectedTexture.GroupKey(
                "create:fluid_tank", new TankConnectedTexture.Position(1, 2, 3)
        );
        TankConnectedTexture.GroupKey creative = new TankConnectedTexture.GroupKey(
                "create:creative_fluid_tank", controller
        );
        assertTrue(TankConnectedTexture.sameGroup(first, same));
        assertFalse(TankConnectedTexture.sameGroup(first, creative));
        assertFalse(TankConnectedTexture.sameGroup(first, null));
    }

    private static void assertFrame(
            CreateDirection face,
            CreateDirection right,
            CreateDirection up
    ) {
        TankConnectedTexture.Frame frame = TankConnectedTexture.frame(face);
        assertEquals(right, frame.right());
        assertEquals(up, frame.up());
    }

    private static String material(String block, String source) {
        return TankConnectedTexture.material(block, source)
                .orElseThrow().connectedTexture();
    }
}
