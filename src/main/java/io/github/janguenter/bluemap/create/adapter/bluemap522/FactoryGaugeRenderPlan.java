/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Frozen physical factory-gauge panels; bulbs, items and readings are omitted. */
record FactoryGaugeRenderPlan(List<Panel> panels) {

    FactoryGaugeRenderPlan {
        panels = List.copyOf(panels);
    }

    static Optional<FactoryGaugeRenderPlan> select(
            Map<String, String> properties,
            Set<Slot> active,
            boolean restocker
    ) {
        if (properties == null || active == null) {
            return Optional.empty();
        }
        CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                .filter(CreateDirection::horizontal)
                .orElse(null);
        String face = properties.get("face");
        if (facing == null || !"floor".equals(face)
                && !"wall".equals(face) && !"ceiling".equals(face)) {
            return Optional.empty();
        }

        float xRotation = switch (face) {
            case "ceiling" -> 180F;
            case "wall" -> 90F;
            default -> 0F;
        };
        float yRotation = facing.horizontalAngle()
                + ("ceiling".equals(face) ? 180F : 0F);
        String model = restocker
                ? "create:block/factory_gauge/panel_restocker"
                : "create:block/factory_gauge/panel";
        List<Panel> panels = new ArrayList<>(active.size());
        for (Slot slot : Slot.values()) {
            if (!active.contains(slot)) {
                continue;
            }
            AffineTransform transform = AffineTransform.identity()
                    .centered()
                    .rotateY(yRotation)
                    .rotateX(xRotation)
                    .rotateY(180F)
                    .uncentered()
                    .translate(slot.xOffset * 0.5F, 0F, slot.yOffset * 0.5F);
            panels.add(new Panel(slot, model, transform));
        }
        return Optional.of(new FactoryGaugeRenderPlan(panels));
    }

    enum Slot {
        TOP_LEFT(1, 1),
        TOP_RIGHT(0, 1),
        BOTTOM_LEFT(1, 0),
        BOTTOM_RIGHT(0, 0);

        private final int xOffset;
        private final int yOffset;

        Slot(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }

    record Panel(Slot slot, String model, AffineTransform transform) {
    }
}
