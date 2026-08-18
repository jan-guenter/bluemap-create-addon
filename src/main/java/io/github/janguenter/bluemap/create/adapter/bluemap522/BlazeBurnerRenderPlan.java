/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Stable physical blaze-head and rod projection from Create's burner renderer. */
record BlazeBurnerRenderPlan(
        String headModel,
        AffineTransform headTransform,
        List<Part> rods
) {

    private static final float FULL_CHASE = 0.175F;

    BlazeBurnerRenderPlan {
        rods = List.copyOf(rods);
    }

    static Optional<BlazeBurnerRenderPlan> select(
            String heatName,
            String facingName,
            boolean validBlockAbove
    ) {
        Heat heat = Heat.parse(heatName).orElse(null);
        CreateDirection facing = CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal)
                .orElse(null);
        if (heat == null || facing == null || heat == Heat.NONE) {
            return Optional.empty();
        }

        boolean active = heat.atLeast(Heat.FADING) && validBlockAbove;
        float animation = active ? FULL_CHASE : 0F;
        String head = switch (heat) {
            case SMOULDERING -> "create:block/blaze_burner/blaze/inert";
            case FADING -> "create:block/blaze_burner/blaze/idle";
            case KINDLED -> active
                    ? "create:block/blaze_burner/blaze/active"
                    : "create:block/blaze_burner/blaze/idle";
            case SEETHING -> active
                    ? "create:block/blaze_burner/blaze/super_active"
                    : "create:block/blaze_burner/blaze/super";
            case NONE -> throw new IllegalStateException("none was filtered above");
        };
        AffineTransform headTransform = AffineTransform.identity()
                .translate(0F, -0.75F * animation, 0F)
                .centered()
                .rotateY(facing.horizontalAngle() + 180F)
                .uncentered();

        List<Part> rods = heat.atLeast(Heat.FADING)
                ? List.of(
                new Part(
                        heat == Heat.SEETHING
                                ? "create:block/blaze_burner/superheated_rods_small"
                                : "create:block/blaze_burner/rods_small",
                        AffineTransform.identity().translate(
                                0F, animation + 0.125F, 0F
                        )
                ),
                new Part(
                        heat == Heat.SEETHING
                                ? "create:block/blaze_burner/superheated_rods_large"
                                : "create:block/blaze_burner/rods_large",
                        AffineTransform.identity().translate(
                                0F, animation - 3F / 16F, 0F
                        )
                )
        ) : List.of();
        return Optional.of(new BlazeBurnerRenderPlan(head, headTransform, rods));
    }

    record Part(String model, AffineTransform transform) {
    }

    private enum Heat {
        NONE,
        SMOULDERING,
        FADING,
        KINDLED,
        SEETHING;

        boolean atLeast(Heat other) {
            return ordinal() >= other.ordinal();
        }

        static Optional<Heat> parse(String name) {
            if (name == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }
    }
}
