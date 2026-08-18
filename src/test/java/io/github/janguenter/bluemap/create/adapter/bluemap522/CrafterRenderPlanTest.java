/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CrafterRenderPlanTest {

    @Test
    void isolatedCrafterGetsCogArrowAndOptionalPhysicalCover() {
        CrafterRenderPlan uncovered = plan(false, "minecraft:air", Map.of());
        CrafterRenderPlan covered = plan(true, "minecraft:air", Map.of());
        assertEquals(List.of("create:block/mechanical_crafter/arrow"),
                uncovered.bodyModels());
        assertEquals(List.of(
                "create:block/mechanical_crafter/lid",
                "create:block/mechanical_crafter/arrow"
        ), covered.bodyModels());
    }

    @Test
    void validSameFacingTargetGetsFrozenBeltSurfaceAndFrame() {
        CrafterRenderPlan plan = plan(false, "create:mechanical_crafter", Map.of(
                "facing", "north", "pointing", "left"
        ));
        assertEquals(List.of(
                "create:block/mechanical_crafter/belt_animated",
                "create:block/mechanical_crafter/belt"
        ), plan.bodyModels());
    }

    @Test
    void oppositePointingTargetKeepsArrow() {
        CrafterRenderPlan plan = plan(false, "create:mechanical_crafter", Map.of(
                "facing", "north", "pointing", "down"
        ));
        assertEquals(List.of("create:block/mechanical_crafter/arrow"),
                plan.bodyModels());
    }

    @Test
    void malformedCrafterStateFailsSoft() {
        assertFalse(CrafterRenderPlan.select(
                Map.of("facing", "north"), "minecraft:air", Map.of(), false
        ).isPresent());
    }

    private static CrafterRenderPlan plan(
            boolean covered,
            String targetId,
            Map<String, String> target
    ) {
        return CrafterRenderPlan.select(Map.of(
                "facing", "north", "pointing", "up"
        ), targetId, target, covered).orElseThrow();
    }
}
