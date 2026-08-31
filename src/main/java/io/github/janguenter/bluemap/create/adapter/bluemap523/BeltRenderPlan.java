/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Exact frozen model and transform selection from Create's BeltRenderer. */
record BeltRenderPlan(
        List<String> models,
        String mapTexture,
        AffineTransform transform,
        Optional<AffineTransform> pulleyTransform
) {

    BeltRenderPlan {
        models = List.copyOf(models);
        pulleyTransform = pulleyTransform == null ? Optional.empty() : pulleyTransform;
    }

    static Optional<BeltRenderPlan> select(Map<String, String> properties) {
        if (properties == null) {
            return Optional.empty();
        }
        CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                .filter(CreateDirection::horizontal)
                .orElse(null);
        String slope = properties.get("slope");
        String part = properties.get("part");
        if (facing == null || !validSlope(slope) || !validPart(part)) {
            return Optional.empty();
        }

        boolean diagonal = "upward".equals(slope) || "downward".equals(slope);
        boolean start = "start".equals(part);
        boolean end = "end".equals(part);
        if ("downward".equals(slope)
                || "vertical".equals(slope) && facing.positive()) {
            boolean swap = start;
            start = end;
            end = swap;
        }

        String segment = start ? "start" : end ? "end" : "middle";
        List<String> models = new ArrayList<>(2);
        if (diagonal) {
            models.add("create:block/belt/diagonal_" + segment);
        } else {
            models.add("create:block/belt/" + segment);
            models.add("create:block/belt/" + segment + "_bottom");
        }

        boolean upward = "upward".equals(slope);
        boolean sideways = "sideways".equals(slope);
        AffineTransform transform = AffineTransform.identity()
                .centered()
                .rotateY(facing.horizontalAngle()
                        + (upward ? 180F : 0F)
                        + (sideways ? 270F : 0F))
                .rotateZ(sideways ? 90F : 0F)
                .rotateX(!diagonal && !"horizontal".equals(slope) ? 90F : 0F)
                .uncentered();

        Optional<AffineTransform> pulley = !"middle".equals(part)
                ? Optional.of(pulley(facing, sideways)) : Optional.empty();
        return Optional.of(new BeltRenderPlan(
                models,
                diagonal ? "create:block/belt_diagonal" : "create:block/belt",
                transform,
                pulley
        ));
    }

    private static AffineTransform pulley(CreateDirection facing, boolean sideways) {
        CreateDirection direction = sideways ? CreateDirection.UP : facing.clockwise();
        AffineTransform transform = AffineTransform.identity().centered();
        if (direction.axis() == CreateDirection.Axis.X) {
            transform = transform.rotateY(90F);
        }
        if (direction.axis() == CreateDirection.Axis.Y) {
            transform = transform.rotateX(90F);
        }
        return transform.rotateX(90F).uncentered();
    }

    private static boolean validSlope(String slope) {
        return "horizontal".equals(slope) || "upward".equals(slope)
                || "downward".equals(slope) || "vertical".equals(slope)
                || "sideways".equals(slope);
    }

    private static boolean validPart(String part) {
        return "start".equals(part) || "middle".equals(part)
                || "end".equals(part) || "pulley".equals(part);
    }
}
