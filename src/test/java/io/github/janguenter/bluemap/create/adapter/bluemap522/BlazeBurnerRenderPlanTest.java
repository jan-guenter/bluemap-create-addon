/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlazeBurnerRenderPlanTest {

    private static final float EPSILON = 1E-6F;

    @Test
    void smoulderingUsesInertHeadAndNoRods() {
        BlazeBurnerRenderPlan plan = BlazeBurnerRenderPlan.select(
                "smouldering", "south", true
        ).orElseThrow();
        assertEquals("create:block/blaze_burner/blaze/inert", plan.headModel());
        assertTrue(plan.rods().isEmpty());
    }

    @Test
    void formedKindledBurnerUsesActiveHeadAndFullChaseOffsets() {
        BlazeBurnerRenderPlan plan = BlazeBurnerRenderPlan.select(
                "kindled", "east", true
        ).orElseThrow();
        assertEquals("create:block/blaze_burner/blaze/active", plan.headModel());
        assertEquals(2, plan.rods().size());
        assertEquals(-0.13125F, plan.headTransform().transform(0.5F, 0.5F, 0.5F).y()
                - 0.5F, EPSILON);
        assertEquals(0.300F,
                plan.rods().get(0).transform().transform(0F, 0F, 0F).y(), EPSILON);
        assertEquals(-0.0125F,
                plan.rods().get(1).transform().transform(0F, 0F, 0F).y(), EPSILON);
    }

    @Test
    void formedSeethingBurnerUsesSuperheatedPhysicalParts() {
        BlazeBurnerRenderPlan plan = BlazeBurnerRenderPlan.select(
                "seething", "north", true
        ).orElseThrow();
        assertEquals("create:block/blaze_burner/blaze/super_active", plan.headModel());
        assertEquals("create:block/blaze_burner/superheated_rods_small",
                plan.rods().get(0).model());
        assertEquals("create:block/blaze_burner/superheated_rods_large",
                plan.rods().get(1).model());
    }

    @Test
    void unformedBurnerUsesIdleOrSuperAndRestingRods() {
        BlazeBurnerRenderPlan kindled = BlazeBurnerRenderPlan.select(
                "kindled", "west", false
        ).orElseThrow();
        BlazeBurnerRenderPlan seething = BlazeBurnerRenderPlan.select(
                "seething", "west", false
        ).orElseThrow();
        assertEquals("create:block/blaze_burner/blaze/idle", kindled.headModel());
        assertEquals("create:block/blaze_burner/blaze/super", seething.headModel());
        assertEquals(0.125F,
                kindled.rods().get(0).transform().transform(0F, 0F, 0F).y(), EPSILON);
        assertEquals(-0.1875F,
                kindled.rods().get(1).transform().transform(0F, 0F, 0F).y(), EPSILON);
    }

    @Test
    void absentAndMalformedStatesKeepOnlyTheCage() {
        assertFalse(BlazeBurnerRenderPlan.select("none", "south", true).isPresent());
        assertFalse(BlazeBurnerRenderPlan.select("hot", "south", true).isPresent());
        assertFalse(BlazeBurnerRenderPlan.select("kindled", "up", true).isPresent());
    }
}
