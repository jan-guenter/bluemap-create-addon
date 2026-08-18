/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Stable head, rods and physical liquid hat for a C&A liquid burner. */
record CaaLiquidBurnerRenderPlan(
        BlazeBurnerRenderPlan blaze,
        StableCoreRenderPlan.Part hat
) {

    static Optional<CaaLiquidBurnerRenderPlan> select(Map<String, String> properties) {
        Heat heat = Heat.parse(properties.get("blaze")).orElse(null);
        CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                .filter(CreateDirection::horizontal).orElse(null);
        if (heat == null || facing == null || heat == Heat.NONE) {
            return Optional.empty();
        }
        BlazeBurnerRenderPlan blaze = BlazeBurnerRenderPlan.select(
                heat.name().toLowerCase(Locale.ROOT),
                facing.name().toLowerCase(Locale.ROOT), false
        ).orElseThrow();
        float headAngle = facing.horizontalAngle() + 180F;
        AffineTransform hat = AffineTransform.identity()
                .translate(0F, heat.ordinal() < Heat.FADING.ordinal() ? 0.5F : 0.75F, 0F);
        if (heat.ordinal() < Heat.FADING.ordinal()) {
            hat = hat.centered().scale(0.75F, 0.75F, 0.75F).uncentered();
        }
        hat = hat.centered().rotateY(headAngle + 180F).uncentered()
                .translate(0.5F, 0F, 0.5F);
        return Optional.of(new CaaLiquidBurnerRenderPlan(
                blaze,
                new StableCoreRenderPlan.Part(
                        "createaddition:entity/liquid_hat", hat
                )
        ));
    }

    private enum Heat {
        NONE,
        SMOULDERING,
        FADING,
        KINDLED,
        SEETHING;

        static Optional<Heat> parse(String value) {
            if (value == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(valueOf(value.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }
    }
}
