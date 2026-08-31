/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Frozen physical conduit eye/cage selected from exact Aquatic Ambitions state. */
record AquaticConduitRenderPlan(List<StableCoreRenderPlan.Part> parts) {

    AquaticConduitRenderPlan {
        parts = List.copyOf(parts);
    }

    static Optional<AquaticConduitRenderPlan> select(Map<String, String> properties) {
        String state = properties.get("conduit");
        if ("idle".equals(state)) {
            return Optional.of(new AquaticConduitRenderPlan(List.of(
                    part("create_aquatic_ambitions:block/conduit_eye", false),
                    part("create_aquatic_ambitions:block/inactive_conduit", false)
            )));
        }
        if ("awakened".equals(state)) {
            return Optional.of(new AquaticConduitRenderPlan(List.of(
                    part("create_aquatic_ambitions:block/conduit_eye", true),
                    part("create_aquatic_ambitions:block/conduit_cage", true)
            )));
        }
        return Optional.empty();
    }

    private static StableCoreRenderPlan.Part part(String model, boolean raised) {
        return new StableCoreRenderPlan.Part(
                model,
                raised ? AffineTransform.identity().translate(0F, 0.2F, 0F)
                        : AffineTransform.identity()
        );
    }
}
