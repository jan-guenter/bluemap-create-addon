/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaaLiquidBurnerRenderPlanTest {

    @Test
    void noneIsCageOnlyAndStableHeatAddsHatHeadAndExpectedRods() {
        assertTrue(CaaLiquidBurnerRenderPlan.select(Map.of(
                "blaze", "none", "facing", "south"
        )).isEmpty());
        CaaLiquidBurnerRenderPlan smouldering =
                CaaLiquidBurnerRenderPlan.select(Map.of(
                        "blaze", "smouldering", "facing", "east"
                )).orElseThrow();
        assertTrue(smouldering.blaze().headModel().endsWith("/inert"));
        assertTrue(smouldering.blaze().rods().isEmpty());
        assertEquals("createaddition:entity/liquid_hat", smouldering.hat().model());

        CaaLiquidBurnerRenderPlan seething = CaaLiquidBurnerRenderPlan.select(Map.of(
                "blaze", "seething", "facing", "north"
        )).orElseThrow();
        assertTrue(seething.blaze().headModel().endsWith("/super"));
        assertEquals(2, seething.blaze().rods().size());
        assertTrue(seething.blaze().rods().stream()
                .allMatch(part -> part.model().contains("superheated")));
    }
}
