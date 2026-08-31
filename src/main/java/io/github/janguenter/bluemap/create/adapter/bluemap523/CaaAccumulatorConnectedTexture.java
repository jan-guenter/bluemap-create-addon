/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.Optional;

/** Exact RECTANGLE CT identity and material selection for modular accumulators. */
final class CaaAccumulatorConnectedTexture {

    static final String BLOCK_ID = "createaddition:modular_accumulator";
    private static final String SIDE =
            "createaddition:block/modular_accumulator/block";
    private static final String SIDE_CONNECTED = SIDE + "_connected";
    private static final String TOP =
            "createaddition:block/modular_accumulator/block_top";
    private static final String TOP_CONNECTED = TOP + "_connected";

    private CaaAccumulatorConnectedTexture() {
    }

    static Optional<TankConnectedTexture.Material> material(
            CreateDirection face,
            String source
    ) {
        if (face == null || source == null) {
            return Optional.empty();
        }
        String original = face.axis() == CreateDirection.Axis.Y ? TOP : SIDE;
        if (!original.equals(source)) {
            return Optional.empty();
        }
        return Optional.of(new TankConnectedTexture.Material(
                original,
                face.axis() == CreateDirection.Axis.Y ? TOP_CONNECTED : SIDE_CONNECTED
        ));
    }

    static boolean sameGroup(Group first, Group second) {
        return first != null && first.equals(second);
    }

    record Position(int x, int y, int z) {
    }

    record Group(Position controller) {
        Group {
            if (controller == null) {
                throw new IllegalArgumentException("accumulator controller required");
            }
        }
    }
}
