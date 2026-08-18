/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.janguenter.bluemap.create.adapter.bluemap522.CrafterConnectedTexture.Material.BRASS_CASING;
import static io.github.janguenter.bluemap.create.adapter.bluemap522.CrafterConnectedTexture.Material.SIDE_HORIZONTAL;
import static io.github.janguenter.bluemap.create.adapter.bluemap522.CrafterConnectedTexture.Material.SIDE_VERTICAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrafterConnectedTextureTest {

    @Test
    void exactSpriteAndWorldFaceChooseTheShiftType() {
        assertEquals(BRASS_CASING, material(
                "north", CreateDirection.NORTH, "create:block/brass_casing"
        ));
        assertEquals(BRASS_CASING, material(
                "north", CreateDirection.SOUTH, "create:block/brass_casing"
        ));
        assertEquals(SIDE_HORIZONTAL, material(
                "north", CreateDirection.UP, "create:block/crafter_side"
        ));
        assertEquals(SIDE_VERTICAL, material(
                "east", CreateDirection.UP, "create:block/crafter_side"
        ));
        assertEquals(SIDE_VERTICAL, material(
                "north", CreateDirection.EAST, "create:block/crafter_side"
        ));
        assertTrue(CrafterConnectedTexture.material(
                "north", CreateDirection.SOUTH, "create:block/brass_block"
        ).isEmpty());
        assertTrue(CrafterConnectedTexture.material(
                "north", CreateDirection.EAST, "create:block/brass_casing"
        ).isEmpty());

        for (CreateDirection facing : List.of(
                CreateDirection.NORTH, CreateDirection.SOUTH,
                CreateDirection.WEST, CreateDirection.EAST
        )) {
            String facingName = facing.name().toLowerCase(java.util.Locale.ROOT);
            assertEquals(BRASS_CASING, material(
                    facingName, facing, "create:block/brass_casing"
            ));
            assertEquals(BRASS_CASING, material(
                    facingName, facing.opposite(), "create:block/brass_casing"
            ));
            for (CreateDirection face : CreateDirection.values()) {
                if (face.axis() == facing.axis()) {
                    assertTrue(CrafterConnectedTexture.material(
                            facingName, face, "create:block/crafter_side"
                    ).isEmpty());
                    continue;
                }
                CrafterConnectedTexture.Material expected =
                        face.axis() == CreateDirection.Axis.Y
                                && facing.axis() == CreateDirection.Axis.Z
                                ? SIDE_HORIZONTAL : SIDE_VERTICAL;
                assertEquals(expected, material(
                        facingName, face, "create:block/crafter_side"
                ));
            }
        }
    }

    @Test
    void rearAndFrontFramesMirrorTheExactNorthFacingGroup() {
        assertEquals(
                new CrafterConnectedTexture.Frame(CreateDirection.UP, CreateDirection.EAST),
                CrafterConnectedTexture.frame("north", CreateDirection.SOUTH).orElseThrow()
        );
        assertEquals(
                new CrafterConnectedTexture.Frame(CreateDirection.UP, CreateDirection.WEST),
                CrafterConnectedTexture.frame("north", CreateDirection.NORTH).orElseThrow()
        );
        assertEquals(
                new CrafterConnectedTexture.Frame(CreateDirection.SOUTH, CreateDirection.WEST),
                CrafterConnectedTexture.frame("north", CreateDirection.UP).orElseThrow()
        );
        assertEquals(
                new CrafterConnectedTexture.Frame(CreateDirection.NORTH, CreateDirection.WEST),
                CrafterConnectedTexture.frame("south", CreateDirection.DOWN).orElseThrow()
        );
        assertTrue(CrafterConnectedTexture.frame("up", CreateDirection.NORTH).isEmpty());
    }

    @Test
    void verticalFaceReverseTableCoversAllFourFacings() {
        assertFrame("north", CreateDirection.UP, CreateDirection.SOUTH, CreateDirection.WEST);
        assertFrame("north", CreateDirection.DOWN, CreateDirection.SOUTH, CreateDirection.EAST);
        assertFrame("south", CreateDirection.UP, CreateDirection.NORTH, CreateDirection.EAST);
        assertFrame("south", CreateDirection.DOWN, CreateDirection.NORTH, CreateDirection.WEST);
        assertFrame("west", CreateDirection.UP, CreateDirection.SOUTH, CreateDirection.WEST);
        assertFrame("west", CreateDirection.DOWN, CreateDirection.NORTH, CreateDirection.WEST);
        assertFrame("east", CreateDirection.UP, CreateDirection.NORTH, CreateDirection.EAST);
        assertFrame("east", CreateDirection.DOWN, CreateDirection.SOUTH, CreateDirection.EAST);
    }

    @Test
    void rearAndFrontOmniCellsMatchExactTwoByTwoBackplate() {
        Map<Cell, Boolean> group = Map.of(
                new Cell(0, 0), true,
                new Cell(1, 0), true,
                new Cell(0, 1), true,
                new Cell(1, 1), true
        );
        assertEquals(20, omni(group, new Cell(0, 0), CreateDirection.EAST));
        assertEquals(12, omni(group, new Cell(1, 0), CreateDirection.EAST));
        assertEquals(21, omni(group, new Cell(0, 1), CreateDirection.EAST));
        assertEquals(13, omni(group, new Cell(1, 1), CreateDirection.EAST));

        assertEquals(12, omni(group, new Cell(0, 0), CreateDirection.WEST));
        assertEquals(20, omni(group, new Cell(1, 0), CreateDirection.WEST));
        assertEquals(13, omni(group, new Cell(0, 1), CreateDirection.WEST));
        assertEquals(21, omni(group, new Cell(1, 1), CreateDirection.WEST));
    }

    @Test
    void twoByTwoSheetIndicesAndUvProjectionAreExact() {
        CrafterConnectedTexture.Context rightAndDown = context(
                false, true, false, true, false, false, false, false
        );
        assertEquals(1, CrafterConnectedTexture.index(
                SIDE_HORIZONTAL, rightAndDown
        ));
        assertEquals(2, CrafterConnectedTexture.index(
                SIDE_VERTICAL, rightAndDown
        ));
        CrafterConnectedTexture.Uv uv = CrafterConnectedTexture.connectedUv(
                SIDE_VERTICAL, 3, 0.25F, 0.75F
        );
        assertEquals(0.625F, uv.u(), 0.00001F);
        assertEquals(0.875F, uv.v(), 0.00001F);
    }

    @Test
    void horizontalVerticalAndOmniIndicesAreExhaustive() {
        for (int mask = 0; mask < 4; mask++) {
            boolean first = (mask & 1) != 0;
            boolean second = (mask & 2) != 0;
            CrafterConnectedTexture.Context horizontal = context(
                    false, false, second, first,
                    false, false, false, false
            );
            CrafterConnectedTexture.Context vertical = context(
                    first, second, false, false,
                    false, false, false, false
            );
            assertEquals(mask, CrafterConnectedTexture.index(
                    SIDE_HORIZONTAL, horizontal
            ));
            assertEquals(mask, CrafterConnectedTexture.index(
                    SIDE_VERTICAL, vertical
            ));
        }

        for (int mask = 0; mask < 256; mask++) {
            CrafterConnectedTexture.Context context = context(
                    bit(mask, 0), bit(mask, 1), bit(mask, 2), bit(mask, 3),
                    bit(mask, 4), bit(mask, 5), bit(mask, 6), bit(mask, 7)
            );
            assertEquals(CasingConnectedEmitter.index(
                    new CasingConnectedEmitter.Context(
                            context.up(), context.down(), context.left(), context.right(),
                            context.topLeft(), context.topRight(),
                            context.bottomLeft(), context.bottomRight()
                    )
            ), CrafterConnectedTexture.index(BRASS_CASING, context));
        }
    }

    @Test
    void connectedUvStaysInsideEveryExactSheetCell() {
        for (CrafterConnectedTexture.Material material
                : CrafterConnectedTexture.Material.values()) {
            int sheet = material.type().sheetSize();
            for (int cell = 0; cell < sheet * sheet; cell++) {
                for (float u : new float[]{0F, 0.25F, 1F}) {
                    for (float v : new float[]{0F, 0.75F, 1F}) {
                        CrafterConnectedTexture.Uv uv =
                                CrafterConnectedTexture.connectedUv(material, cell, u, v);
                        assertTrue(uv.u() >= 0F && uv.u() <= 1F);
                        assertTrue(uv.v() >= 0F && uv.v() <= 1F);
                    }
                }
            }
        }
    }

    @Test
    void groupIdentityUsesFacingAndEffectiveControllerOnly() {
        CrafterConnectedTexture.Position controller =
                new CrafterConnectedTexture.Position(10, 20, 30);
        CrafterConnectedTexture.GroupKey north =
                new CrafterConnectedTexture.GroupKey("north", controller);
        assertTrue(CrafterConnectedTexture.sameGroup(
                north, new CrafterConnectedTexture.GroupKey("north", controller)
        ));
        assertFalse(CrafterConnectedTexture.sameGroup(
                north, new CrafterConnectedTexture.GroupKey("south", controller)
        ));
        assertFalse(CrafterConnectedTexture.sameGroup(
                north, new CrafterConnectedTexture.GroupKey(
                        "north", new CrafterConnectedTexture.Position(11, 20, 30)
                )
        ));
        assertFalse(CrafterConnectedTexture.sameGroup(north, null));
    }

    @Test
    void localNormalsRotateToEveryHorizontalCrafterFacing() {
        assertEquals(CreateDirection.NORTH, CrafterConnectedTexture.rotateFromNorth(
                CreateDirection.NORTH, "north"
        ));
        assertEquals(CreateDirection.EAST, CrafterConnectedTexture.rotateFromNorth(
                CreateDirection.NORTH, "east"
        ));
        assertEquals(CreateDirection.SOUTH, CrafterConnectedTexture.rotateFromNorth(
                CreateDirection.NORTH, "south"
        ));
        assertEquals(CreateDirection.WEST, CrafterConnectedTexture.rotateFromNorth(
                CreateDirection.NORTH, "west"
        ));
        assertEquals(CreateDirection.UP, CrafterConnectedTexture.rotateFromNorth(
                CreateDirection.UP, "west"
        ));
    }

    private static CrafterConnectedTexture.Material material(
            String facing,
            CreateDirection face,
            String texture
    ) {
        return CrafterConnectedTexture.material(facing, face, texture).orElseThrow();
    }

    private static void assertFrame(
            String facing,
            CreateDirection face,
            CreateDirection up,
            CreateDirection right
    ) {
        assertEquals(
                new CrafterConnectedTexture.Frame(up, right),
                CrafterConnectedTexture.frame(facing, face).orElseThrow()
        );
    }

    private static boolean bit(int mask, int bit) {
        return (mask & (1 << bit)) != 0;
    }

    private static int omni(
            Map<Cell, Boolean> group,
            Cell own,
            CreateDirection right
    ) {
        Cell up = new Cell(0, 1);
        Cell rightCell = new Cell(right.x(), right.z());
        boolean hasUp = contains(group, own, up);
        boolean hasDown = contains(group, own, up.opposite());
        boolean hasRight = contains(group, own, rightCell);
        boolean hasLeft = contains(group, own, rightCell.opposite());
        return CrafterConnectedTexture.index(BRASS_CASING, context(
                hasUp,
                hasDown,
                hasLeft,
                hasRight,
                hasUp && hasLeft && contains(group, own, up.add(rightCell.opposite())),
                hasUp && hasRight && contains(group, own, up.add(rightCell)),
                hasDown && hasLeft && contains(
                        group, own, up.opposite().add(rightCell.opposite())
                ),
                hasDown && hasRight && contains(
                        group, own, up.opposite().add(rightCell)
                )
        ));
    }

    private static boolean contains(Map<Cell, Boolean> group, Cell own, Cell offset) {
        return group.containsKey(own.add(offset));
    }

    private static CrafterConnectedTexture.Context context(
            boolean up,
            boolean down,
            boolean left,
            boolean right,
            boolean topLeft,
            boolean topRight,
            boolean bottomLeft,
            boolean bottomRight
    ) {
        return new CrafterConnectedTexture.Context(
                up, down, left, right,
                topLeft, topRight, bottomLeft, bottomRight
        );
    }

    private record Cell(int x, int y) {
        Cell add(Cell other) {
            return new Cell(x + other.x, y + other.y);
        }

        Cell opposite() {
            return new Cell(-x, -y);
        }
    }
}
