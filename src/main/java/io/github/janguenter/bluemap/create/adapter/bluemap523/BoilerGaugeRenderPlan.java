/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.ArrayList;
import java.util.List;

/** Exact stable gauge housing and neutral-dial transforms for an active boiler. */
final class BoilerGaugeRenderPlan {

    private static final float DIAL_PIVOT_Y = 6F / 16F;
    private static final float DIAL_PIVOT_Z = 8F / 16F;

    private BoilerGaugeRenderPlan() {
    }

    static List<Side> select(int width, boolean active) {
        if (!active || width < 1 || width > 3) {
            return List.of();
        }
        ArrayList<Side> sides = new ArrayList<>(4);
        for (CreateDirection direction : List.of(
                CreateDirection.NORTH,
                CreateDirection.SOUTH,
                CreateDirection.WEST,
                CreateDirection.EAST
        )) {
            float yRotation = -toYRotation(direction) - 90F;
            AffineTransform root = AffineTransform.identity()
                    .translate(width / 2F, 0.5F, width / 2F)
                    .rotateY(yRotation)
                    .uncentered()
                    .translate(width / 2F - 6F / 16F, 0F, 0F);
            AffineTransform dial = root
                    .translate(0F, DIAL_PIVOT_Y, DIAL_PIVOT_Z)
                    .rotateX(90F)
                    .translate(0F, -DIAL_PIVOT_Y, -DIAL_PIVOT_Z);
            sides.add(new Side(direction, root, dial));
        }
        return List.copyOf(sides);
    }

    private static float toYRotation(CreateDirection direction) {
        return switch (direction) {
            case SOUTH -> 0F;
            case WEST -> 90F;
            case NORTH -> 180F;
            case EAST -> 270F;
            default -> throw new IllegalArgumentException("boiler side must be horizontal");
        };
    }

    record Side(
            CreateDirection direction,
            AffineTransform housing,
            AffineTransform dial
    ) {
    }
}
