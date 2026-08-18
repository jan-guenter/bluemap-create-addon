/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.Optional;

/** Dependency-free projection of Create's exact mechanical-crafter CT rules. */
final class CrafterConnectedTexture {

    private CrafterConnectedTexture() {
    }

    static Optional<Material> material(
            String facingName,
            CreateDirection worldFace,
            String sourceTexture
    ) {
        CreateDirection facing = CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal)
                .orElse(null);
        if (facing == null || worldFace == null || sourceTexture == null) {
            return Optional.empty();
        }
        if (worldFace.axis() == facing.axis()) {
            return "create:block/brass_casing".equals(sourceTexture)
                    ? Optional.of(Material.BRASS_CASING) : Optional.empty();
        }
        if (!"create:block/crafter_side".equals(sourceTexture)) {
            return Optional.empty();
        }
        return Optional.of(worldFace.axis() == CreateDirection.Axis.Y
                && facing.axis() != CreateDirection.Axis.X
                ? Material.SIDE_HORIZONTAL : Material.SIDE_VERTICAL);
    }

    static Optional<Frame> frame(
            String facingName,
            CreateDirection worldFace
    ) {
        CreateDirection facing = CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal)
                .orElse(null);
        if (facing == null || worldFace == null) {
            return Optional.empty();
        }

        CreateDirection horizontal = worldFace.axis() == CreateDirection.Axis.X
                ? CreateDirection.SOUTH : CreateDirection.WEST;
        CreateDirection vertical = worldFace.horizontal()
                ? CreateDirection.UP : CreateDirection.NORTH;
        if (worldFace.positive()) {
            horizontal = horizontal.opposite();
        }
        if (worldFace == CreateDirection.DOWN) {
            horizontal = horizontal.opposite();
            vertical = vertical.opposite();
        }

        boolean reverse = reverseUvs(facing, worldFace);
        if (reverse) {
            horizontal = horizontal.opposite();
            vertical = vertical.opposite();
        }
        return Optional.of(new Frame(vertical, horizontal));
    }

    static CreateDirection rotateFromNorth(
            CreateDirection localFace,
            String facingName
    ) {
        CreateDirection facing = CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal)
                .orElse(null);
        if (localFace == null || facing == null || !localFace.horizontal()) {
            return localFace;
        }
        int turns = switch (facing) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> throw new IllegalStateException("horizontal facing required");
        };
        CreateDirection rotated = localFace;
        for (int turn = 0; turn < turns; turn++) {
            rotated = rotated.clockwise();
        }
        return rotated;
    }

    static int index(Material material, Context context) {
        if (material == null || context == null) {
            return 0;
        }
        return switch (material.type()) {
            case HORIZONTAL -> (context.right() ? 1 : 0) + (context.left() ? 2 : 0);
            case VERTICAL -> (context.up() ? 1 : 0) + (context.down() ? 2 : 0);
            case OMNIDIRECTIONAL -> omnidirectionalIndex(context);
        };
    }

    static Uv connectedUv(Material material, int cell, float u, float v) {
        int sheetSize = material.type().sheetSize();
        return new Uv(
                (cell % sheetSize + u) / sheetSize,
                (cell / sheetSize + v) / sheetSize
        );
    }

    static boolean sameGroup(GroupKey first, GroupKey second) {
        return first != null && first.equals(second);
    }

    private static boolean reverseUvs(
            CreateDirection facing,
            CreateDirection worldFace
    ) {
        if (worldFace.axis() != CreateDirection.Axis.Y) {
            return false;
        }
        boolean facingNegative = !facing.positive();
        if (worldFace == CreateDirection.DOWN
                && facing.axis() == CreateDirection.Axis.Z) {
            return !facingNegative;
        }
        return facingNegative;
    }

    private static int omnidirectionalIndex(Context c) {
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

    enum Type {
        HORIZONTAL(2),
        VERTICAL(2),
        OMNIDIRECTIONAL(8);

        private final int sheetSize;

        Type(int sheetSize) {
            this.sheetSize = sheetSize;
        }

        int sheetSize() {
            return sheetSize;
        }
    }

    enum Material {
        BRASS_CASING(
                "create:block/brass_casing",
                "create:block/brass_casing_connected",
                Type.OMNIDIRECTIONAL
        ),
        SIDE_HORIZONTAL(
                "create:block/crafter_side",
                "create:block/crafter_side_connected",
                Type.HORIZONTAL
        ),
        SIDE_VERTICAL(
                "create:block/crafter_side",
                "create:block/crafter_side_connected",
                Type.VERTICAL
        );

        private final String originalTexture;
        private final String connectedTexture;
        private final Type type;

        Material(String originalTexture, String connectedTexture, Type type) {
            this.originalTexture = originalTexture;
            this.connectedTexture = connectedTexture;
            this.type = type;
        }

        String originalTexture() {
            return originalTexture;
        }

        String connectedTexture() {
            return connectedTexture;
        }

        Type type() {
            return type;
        }
    }

    record Position(int x, int y, int z) {
        Position add(Position offset) {
            return offset == null ? null
                    : new Position(x + offset.x, y + offset.y, z + offset.z);
        }
    }

    record GroupKey(String facing, Position controller) {
    }

    record Frame(CreateDirection up, CreateDirection right) {
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

    record Uv(float u, float v) {
    }
}
