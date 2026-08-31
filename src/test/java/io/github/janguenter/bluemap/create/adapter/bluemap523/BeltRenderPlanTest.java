/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeltRenderPlanTest {

    @Test
    void nonDiagonalBeltAlwaysSelectsTopAndUnderside() {
        for (String slope : List.of("horizontal", "vertical", "sideways")) {
            BeltRenderPlan plan = select("north", slope, "middle");
            assertEquals(List.of(
                    "create:block/belt/middle",
                    "create:block/belt/middle_bottom"
            ), plan.models());
            assertEquals("create:block/belt", plan.mapTexture());
            AffineTransformTest.assertPoint(
                    plan.transform().transform(0.5F, 0.5F, 0.5F),
                    0.5F, 0.5F, 0.5F
            );
        }
    }

    @Test
    void diagonalBeltUsesSingleCompletePartial() {
        assertEquals(List.of("create:block/belt/diagonal_start"),
                select("south", "upward", "start").models());
        assertEquals(List.of("create:block/belt/diagonal_end"),
                select("south", "downward", "start").models());
        assertEquals("create:block/belt_diagonal",
                select("south", "upward", "start").mapTexture());
    }

    @Test
    void verticalPositiveFacingSwapsStartAndEnd() {
        assertEquals(List.of(
                "create:block/belt/end", "create:block/belt/end_bottom"
        ), select("east", "vertical", "start").models());
        assertEquals(List.of(
                "create:block/belt/start", "create:block/belt/start_bottom"
        ), select("west", "vertical", "start").models());
    }

    @Test
    void pulleyAddsItsIndependentExactTransform() {
        BeltRenderPlan plan = select("west", "sideways", "pulley");
        assertTrue(plan.pulleyTransform().isPresent());
        AffineTransform pulley = plan.pulleyTransform().orElseThrow();
        assertTrue(pulley.finite());
        AffineTransformTest.assertPoint(
                pulley.transform(0.5F, 0.5F, 0.5F), 0.5F, 0.5F, 0.5F
        );
    }

    @Test
    void physicalChainEndsAlsoHavePulleys() {
        assertTrue(select("north", "horizontal", "start").pulleyTransform().isPresent());
        assertTrue(select("north", "horizontal", "end").pulleyTransform().isPresent());
        assertFalse(select("north", "horizontal", "middle").pulleyTransform().isPresent());
    }

    @Test
    void malformedBeltStateFailsSoft() {
        assertFalse(BeltRenderPlan.select(null).isPresent());
        assertFalse(BeltRenderPlan.select(Map.of()).isPresent());
        assertFalse(BeltRenderPlan.select(Map.of(
                "facing", "up", "slope", "horizontal", "part", "start"
        )).isPresent());
        assertFalse(BeltRenderPlan.select(Map.of(
                "facing", "north", "slope", "curved", "part", "start"
        )).isPresent());
    }

    private static BeltRenderPlan select(String facing, String slope, String part) {
        return BeltRenderPlan.select(Map.of(
                "facing", facing, "slope", slope, "part", part
        )).orElseThrow();
    }
}
