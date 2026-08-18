/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Stable CEI blaze head, rods and authored hat with all content omitted. */
record CeiBlazeRenderPlan(
        BlazeBurnerRenderPlan blaze,
        StableCoreRenderPlan.Part hat
) {

    static Optional<CeiBlazeRenderPlan> select(
            String blockId,
            Map<String, String> properties
    ) {
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
        if ("create_enchantment_industry:classic_blaze_enchanter".equals(blockId)) {
            blaze = new BlazeBurnerRenderPlan(
                    blaze.headModel(),
                    blaze.headTransform().translatedBefore(0F, .2F, 0F),
                    blaze.rods().stream().map(part -> new BlazeBurnerRenderPlan.Part(
                            part.model(),
                            part.transform().translatedBefore(0F, .2F, 0F)
                    )).toList()
            );
        }
        String hatName = switch (blockId) {
            case "create_enchantment_industry:blaze_enchanter" -> "enchanter_hat";
            case "create_enchantment_industry:blaze_forger" -> "forger_hat";
            case "create_enchantment_industry:blaze_composer" -> "composer_hat";
            case "create_enchantment_industry:classic_blaze_enchanter" -> null;
            default -> null;
        };
        StableCoreRenderPlan.Part hat = null;
        if (hatName != null) {
            String suffix = heat.atLeast(Heat.FADING) ? "" : "_small";
            float headAngle = facing.horizontalAngle() + 180F;
            AffineTransform transform = AffineTransform.identity()
                    .translate(0F, .75F, 0F)
                    .centered().rotateY(headAngle + 180F).uncentered()
                    .translate(.5F, 0F, .5F);
            hat = new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/blaze/" + hatName + suffix,
                    transform
            );
        }
        return Optional.of(new CeiBlazeRenderPlan(blaze, hat));
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
