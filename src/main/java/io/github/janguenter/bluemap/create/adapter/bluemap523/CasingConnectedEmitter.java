/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Exact 8x8 Create omnidirectional CT selection for plain casing cubes. */
final class CasingConnectedEmitter {

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    CasingConnectedEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        String name = blockId.substring(blockId.indexOf(':') + 1);
        Key baseKey = Key.parse("create:block/" + name);
        Key sheetKey = Key.parse("create:block/" + name + "_connected");
        Texture base = resourcePack.getTextures().get(baseKey);
        Texture sheet = resourcePack.getTextures().get(sheetKey);
        if (base == null || sheet == null) {
            return false;
        }
        int material = textures.get(sheetKey);
        for (Direction face : Direction.values()) {
            var normal = face.toVector();
            var adjacent = block.getNeighborBlock(
                    normal.getX(), normal.getY(), normal.getZ()
            );
            if (adjacent.getProperties().isCulling()) {
                continue;
            }
            Context context = context(blockId, block, face);
            emitFace(face, index(context), material, block, target);
        }
        mapColor.add(new Color().set(base.getColorPremultiplied()));
        mapColor.flatten().straight();
        return true;
    }

    private static Context context(
            String blockId,
            BlockNeighborhood block,
            Direction face
    ) {
        Direction horizontal = switch (face) {
            case WEST, EAST -> Direction.SOUTH;
            default -> Direction.WEST;
        };
        Direction vertical = switch (face) {
            case WEST, EAST, NORTH, SOUTH -> Direction.UP;
            default -> Direction.NORTH;
        };
        if (face == Direction.UP || face == Direction.SOUTH || face == Direction.EAST) {
            horizontal = horizontal.getOpposite();
        }
        if (face == Direction.DOWN) {
            horizontal = horizontal.getOpposite();
            vertical = vertical.getOpposite();
        }
        Direction leftDirection = horizontal.getOpposite();
        Direction downDirection = vertical.getOpposite();
        boolean up = same(blockId, block, vertical);
        boolean down = same(blockId, block, downDirection);
        boolean left = same(blockId, block, leftDirection);
        boolean right = same(blockId, block, horizontal);
        return new Context(
                up, down, left, right,
                up && left && same(blockId, block, vertical, leftDirection),
                up && right && same(blockId, block, vertical, horizontal),
                down && left && same(blockId, block, downDirection, leftDirection),
                down && right && same(blockId, block, downDirection, horizontal)
        );
    }

    private static boolean same(
            String blockId,
            BlockNeighborhood block,
            Direction first
    ) {
        return same(blockId, block, first, null);
    }

    private static boolean same(
            String blockId,
            BlockNeighborhood block,
            Direction first,
            Direction second
    ) {
        var a = first.toVector();
        int x = a.getX();
        int y = a.getY();
        int z = a.getZ();
        if (second != null) {
            var b = second.toVector();
            x += b.getX();
            y += b.getY();
            z += b.getZ();
        }
        return blockId.equals(block.getNeighborBlock(x, y, z)
                .getBlockState().getId().getFormatted());
    }

    static int index(Context c) {
        int tileX = 0;
        int tileY = 0;
        int borders = (c.up() ? 0 : 1) + (c.down() ? 0 : 1)
                + (c.left() ? 0 : 1) + (c.right() ? 0 : 1);
        if (c.up()) {
            tileX++;
        }
        if (c.down()) {
            tileX += 2;
        }
        if (c.left()) {
            tileY++;
        }
        if (c.right()) {
            tileY += 2;
        }
        if (borders == 0) {
            if (c.topRight()) {
                tileX++;
            }
            if (c.topLeft()) {
                tileX += 2;
            }
            if (c.bottomRight()) {
                tileY += 2;
            }
            if (c.bottomLeft()) {
                tileY++;
            }
        }
        if (borders == 1) {
            if (!c.right() && (c.topLeft() || c.bottomLeft())) {
                tileY = 4;
                tileX = -1 + (c.bottomLeft() ? 1 : 0) + (c.topLeft() ? 2 : 0);
            }
            if (!c.left() && (c.topRight() || c.bottomRight())) {
                tileY = 5;
                tileX = -1 + (c.bottomRight() ? 1 : 0) + (c.topRight() ? 2 : 0);
            }
            if (!c.down() && (c.topLeft() || c.topRight())) {
                tileY = 6;
                tileX = -1 + (c.topLeft() ? 1 : 0) + (c.topRight() ? 2 : 0);
            }
            if (!c.up() && (c.bottomLeft() || c.bottomRight())) {
                tileY = 7;
                tileX = -1 + (c.bottomLeft() ? 1 : 0) + (c.bottomRight() ? 2 : 0);
            }
        }
        if (borders == 2 && ((c.up() && c.left() && c.topLeft())
                || (c.down() && c.left() && c.bottomLeft())
                || (c.up() && c.right() && c.topRight())
                || (c.down() && c.right() && c.bottomRight()))) {
            tileX += 3;
        }
        return tileX + 8 * tileY;
    }

    private static void emitFace(
            Direction direction,
            int cell,
            int material,
            BlockNeighborhood block,
            TileModelView target
    ) {
        float[][] p = switch (direction) {
            case DOWN -> new float[][]{{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}};
            case UP -> new float[][]{{0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}};
            case NORTH -> new float[][]{{1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0}};
            case SOUTH -> new float[][]{{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}};
            case WEST -> new float[][]{{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}};
            case EAST -> new float[][]{{1, 0, 1}, {1, 0, 0}, {1, 1, 0}, {1, 1, 1}};
        };
        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        positions(mesh, start, p[0], p[1], p[2]);
        positions(mesh, start + 1, p[0], p[2], p[3]);
        int column = cell % 8;
        int row = cell / 8;
        float u0 = column / 8F;
        float u1 = (column + 1F) / 8F;
        float v0 = row / 8F;
        float v1 = (row + 1F) / 8F;
        mesh.setUvs(start, u0, v1, u1, v1, u1, v0);
        mesh.setUvs(start + 1, u0, v1, u1, v0, u0, v0);
        var normal = direction.toVector();
        LightData own = block.getLightData();
        LightData outside = block.getNeighborBlock(
                normal.getX(), normal.getY(), normal.getZ()
        ).getLightData();
        int sunlight = Math.max(own.getSkyLight(), outside.getSkyLight());
        int blocklight = Math.max(own.getBlockLight(), outside.getBlockLight());
        for (int triangle = start; triangle < start + 2; triangle++) {
            mesh.setMaterialIndex(triangle, material);
            mesh.setColor(triangle, 1F, 1F, 1F);
            mesh.setAOs(triangle, 1F, 1F, 1F);
            mesh.setSunlight(triangle, sunlight);
            mesh.setBlocklight(triangle, blocklight);
        }
    }

    private static void positions(
            TileModel model,
            int triangle,
            float[] a,
            float[] b,
            float[] c
    ) {
        model.setPositions(
                triangle,
                a[0], a[1], a[2],
                b[0], b[1], b[2],
                c[0], c[1], c[2]
        );
    }

    record Context(
            boolean up,
            boolean down,
            boolean left,
            boolean right,
            boolean topLeft,
            boolean topRight,
            boolean bottomLeft,
            boolean bottomRight
    ) {
    }
}
