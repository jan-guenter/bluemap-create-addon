/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Frozen physical multipart plan for one mechanical crafter. */
record CrafterRenderPlan(
        AffineTransform cogTransform,
        AffineTransform bodyTransform,
        List<String> bodyModels
) {

    CrafterRenderPlan {
        bodyModels = List.copyOf(bodyModels);
    }

    static Optional<CrafterRenderPlan> select(
            Map<String, String> properties,
            String targetBlockId,
            Map<String, String> targetProperties,
            boolean covered
    ) {
        if (properties == null || targetProperties == null) {
            return Optional.empty();
        }
        String facing = properties.get("facing");
        String pointing = properties.get("pointing");
        AffineTransform cog = DirectionalPartialTransforms.crafterCog(facing)
                .orElse(null);
        AffineTransform body = DirectionalPartialTransforms.crafterBodyPartial(
                facing, pointing
        ).orElse(null);
        if (cog == null || body == null) {
            return Optional.empty();
        }

        ArrayList<String> models = new ArrayList<>(3);
        if (covered) {
            models.add("create:block/mechanical_crafter/lid");
        }
        boolean validTarget = DirectionalPartialTransforms.validCrafterTarget(
                facing,
                pointing,
                targetBlockId,
                targetProperties.get("facing"),
                targetProperties.get("pointing")
        );
        if (validTarget) {
            models.add("create:block/mechanical_crafter/belt_animated");
            models.add("create:block/mechanical_crafter/belt");
        } else {
            models.add("create:block/mechanical_crafter/arrow");
        }
        return Optional.of(new CrafterRenderPlan(cog, body, models));
    }
}
