/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.Map;
import java.util.Optional;

/** Exact Create RECTANGLE CT selection and tank connectivity identity. */
final class TankConnectedTexture {

    private static final Map<String, Material> STANDARD = Map.of(
            "create:block/fluid_tank", new Material(
                    "create:block/fluid_tank", "create:block/fluid_tank_connected"
            ),
            "create:block/fluid_tank_top", new Material(
                    "create:block/fluid_tank_top", "create:block/fluid_tank_top_connected"
            ),
            "create:block/fluid_tank_inner", new Material(
                    "create:block/fluid_tank_inner", "create:block/fluid_tank_inner_connected"
            )
    );
    private static final Map<String, Material> CREATIVE = Map.of(
            "create:block/creative_fluid_tank", new Material(
                    "create:block/creative_fluid_tank",
                    "create:block/creative_fluid_tank_connected"
            ),
            "create:block/creative_casing", new Material(
                    "create:block/creative_casing", "create:block/creative_casing_connected"
            )
    );

    private TankConnectedTexture() {
    }

    static Optional<Material> material(String blockId, String sourceTexture) {
        if (sourceTexture == null) {
            return Optional.empty();
        }
        Map<String, Material> materials = "create:fluid_tank".equals(blockId)
                ? STANDARD : "create:creative_fluid_tank".equals(blockId)
                ? CREATIVE : Map.of();
        return Optional.ofNullable(materials.get(sourceTexture));
    }

    static int index(Context context) {
        int x = context.left() && context.right() ? 2
                : context.left() ? 3 : context.right() ? 1 : 0;
        int y = context.up() && context.down() ? 1
                : context.up() ? 2 : context.down() ? 0 : 3;
        return x + y * 4;
    }

    static Frame frame(CreateDirection face) {
        CreateDirection right = face.axis() == CreateDirection.Axis.X
                ? CreateDirection.SOUTH : CreateDirection.WEST;
        CreateDirection up = face.horizontal()
                ? CreateDirection.UP : CreateDirection.NORTH;
        if (face.positive()) {
            right = right.opposite();
        }
        if (face == CreateDirection.DOWN) {
            right = right.opposite();
            up = up.opposite();
        }
        return new Frame(right, up);
    }

    static Uv connectedUv(int cell, float sourceU, float sourceV) {
        if (cell < 0 || cell >= 16 || !Float.isFinite(sourceU)
                || !Float.isFinite(sourceV)) {
            throw new IllegalArgumentException("invalid RECTANGLE CT coordinates");
        }
        int column = cell % 4;
        int row = cell / 4;
        return new Uv((column + sourceU) / 4F, (row + sourceV) / 4F);
    }

    static boolean sameGroup(GroupKey first, GroupKey second) {
        return first != null && second != null
                && first.blockId().equals(second.blockId())
                && first.controller().equals(second.controller());
    }

    record Context(boolean up, boolean down, boolean left, boolean right) {
    }

    record Frame(CreateDirection right, CreateDirection up) {
    }

    record Uv(float u, float v) {
    }

    record Material(String sourceTexture, String connectedTexture) {
    }

    record Position(int x, int y, int z) {
    }

    record GroupKey(String blockId, Position controller) {
        GroupKey {
            if (blockId == null || controller == null) {
                throw new IllegalArgumentException("tank group fields are required");
            }
        }
    }
}
