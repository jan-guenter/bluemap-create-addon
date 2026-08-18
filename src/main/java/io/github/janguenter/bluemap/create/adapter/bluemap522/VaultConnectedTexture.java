/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.Map;
import java.util.Optional;

/** Exact group, face-frame and RECTANGLE texture selection for Create item vaults. */
final class VaultConnectedTexture {

    private static final String PREFIX = "create:block/vault/vault_";

    private VaultConnectedTexture() {
    }

    static Optional<Material> material(
            String axis,
            boolean large,
            CreateDirection face,
            String sourceTexture
    ) {
        if (!("x".equals(axis) || "z".equals(axis)) || face == null) {
            return Optional.empty();
        }
        String surface;
        if (face.axis().name().equalsIgnoreCase(axis)) {
            surface = "front";
        } else if (face == CreateDirection.UP) {
            surface = "top";
        } else if (face == CreateDirection.DOWN) {
            surface = "bottom";
        } else {
            surface = "side";
        }
        String original = PREFIX + surface + "_small";
        if (!original.equals(sourceTexture)) {
            return Optional.empty();
        }
        return Optional.of(new Material(
                original, PREFIX + surface + (large ? "_large" : "_medium")
        ));
    }

    static Optional<Frame> frame(String axis, CreateDirection face) {
        if (!("x".equals(axis) || "z".equals(axis)) || face == null) {
            return Optional.empty();
        }
        CreateDirection up = face.horizontal() ? CreateDirection.UP : CreateDirection.NORTH;
        CreateDirection right = face.axis() == CreateDirection.Axis.X
                ? CreateDirection.SOUTH : CreateDirection.WEST;

        boolean axisX = "x".equals(axis);
        CreateDirection.Axis vaultAxis = axisX
                ? CreateDirection.Axis.X : CreateDirection.Axis.Z;
        if (face.axis() == CreateDirection.Axis.Y && axisX) {
            up = up.clockwise();
            right = right.clockwise();
        } else if (face.axis() != vaultAxis && face.axis() != CreateDirection.Axis.Y) {
            up = axisX ? CreateDirection.EAST : CreateDirection.NORTH;
            right = face.positive() ? CreateDirection.UP : CreateDirection.DOWN;
        }

        if (face.positive()) {
            right = right.opposite();
        }
        if (face == CreateDirection.DOWN) {
            up = up.opposite();
            right = right.opposite();
        }
        return Optional.of(new Frame(up, right));
    }

    static int index(Context context) {
        int tileX = context.left() && context.right() ? 2
                : context.left() ? 3 : context.right() ? 1 : 0;
        int tileY = context.up() && context.down() ? 1
                : context.up() ? 2 : context.down() ? 0 : 3;
        return tileX + 4 * tileY;
    }

    static Uv connectedUv(int cell, float localU, float localV) {
        if (cell < 0 || cell >= 16 || !Float.isFinite(localU)
                || !Float.isFinite(localV)) {
            throw new IllegalArgumentException("invalid vault connected texture coordinate");
        }
        return new Uv(
                (localU + cell % 4) / 4F,
                (localV + cell / 4) / 4F
        );
    }

    static Optional<GroupKey> group(
            String blockId,
            Map<String, String> properties,
            Position controller
    ) {
        if (!"create:item_vault".equals(blockId) || properties == null
                || controller == null) {
            return Optional.empty();
        }
        String axis = properties.get("axis");
        String large = properties.get("large");
        if (!("x".equals(axis) || "z".equals(axis))
                || !("true".equals(large) || "false".equals(large))) {
            return Optional.empty();
        }
        return Optional.of(new GroupKey(axis, Boolean.parseBoolean(large), controller));
    }

    static boolean sameGroup(GroupKey first, GroupKey second) {
        return first != null && first.equals(second);
    }

    record Material(String originalTexture, String connectedTexture) {
    }

    record Frame(CreateDirection up, CreateDirection right) {
    }

    record Context(boolean up, boolean down, boolean left, boolean right) {
    }

    record Uv(float u, float v) {
    }

    record Position(int x, int y, int z) {
    }

    record GroupKey(String axis, boolean large, Position controller) {
    }
}
