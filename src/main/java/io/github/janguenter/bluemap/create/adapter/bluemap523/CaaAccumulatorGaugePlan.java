/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.ArrayList;
import java.util.List;

/** Four neutral physical accumulator gauge housings and dials. */
record CaaAccumulatorGaugePlan(List<Gauge> gauges) {

    CaaAccumulatorGaugePlan {
        gauges = List.copyOf(gauges);
    }

    static CaaAccumulatorGaugePlan select(int width, int height) {
        if (width < 1 || width > 64 || height < 1 || height > 64) {
            return new CaaAccumulatorGaugePlan(List.of());
        }
        ArrayList<Gauge> gauges = new ArrayList<>(4);
        for (CreateDirection direction : new CreateDirection[]{
                CreateDirection.SOUTH, CreateDirection.WEST,
                CreateDirection.NORTH, CreateDirection.EAST
        }) {
            float yaw = switch (direction) {
                case SOUTH -> 0F;
                case WEST -> 90F;
                case NORTH -> 180F;
                case EAST -> 270F;
                default -> throw new IllegalStateException("horizontal gauge side");
            };
            AffineTransform base = AffineTransform.identity()
                    .translate(width / 2F, height - 0.5F, width / 2F)
                    .rotateY(yaw)
                    .uncentered()
                    .translate(width / 2F - 0.375F, 0F, 0F);
            AffineTransform dial = base
                    .translate(0F, 0.375F, 0.5F)
                    .rotateX(0F)
                    .translate(0F, -0.375F, -0.5F);
            gauges.add(new Gauge(direction, base, dial));
        }
        return new CaaAccumulatorGaugePlan(gauges);
    }

    record Gauge(
            CreateDirection direction,
            AffineTransform housing,
            AffineTransform dial
    ) {
    }
}
