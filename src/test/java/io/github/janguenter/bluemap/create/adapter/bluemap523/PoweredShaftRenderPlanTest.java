/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PoweredShaftRenderPlanTest {

    @Test
    void rawYAxisAndPerpendicularAxesUseExactVariantRotations() {
        assertEquals(new PoweredShaftRenderPlan(0F, 0F),
                PoweredShaftRenderPlan.select("y").orElseThrow());
        assertEquals(new PoweredShaftRenderPlan(90F, 90F),
                PoweredShaftRenderPlan.select("x").orElseThrow());
        assertEquals(new PoweredShaftRenderPlan(90F, 180F),
                PoweredShaftRenderPlan.select("z").orElseThrow());
        assertFalse(PoweredShaftRenderPlan.select("north").isPresent());
        assertFalse(PoweredShaftRenderPlan.select(null).isPresent());
    }
}
