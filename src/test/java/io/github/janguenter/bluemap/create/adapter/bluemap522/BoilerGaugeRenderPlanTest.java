/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoilerGaugeRenderPlanTest {

    @Test
    void activeBoilerEmitsFourOpenSideHousingsAndNeutralDials() {
        var sides = BoilerGaugeRenderPlan.select(2, true);
        assertEquals(4, sides.size());
        assertEquals(Set.of(
                        CreateDirection.NORTH, CreateDirection.SOUTH,
                        CreateDirection.WEST, CreateDirection.EAST
                ), sides.stream().map(BoilerGaugeRenderPlan.Side::direction)
                        .collect(Collectors.toSet()));
        for (BoilerGaugeRenderPlan.Side side : sides) {
            assertTrue(side.housing().finite());
            assertTrue(side.dial().finite());
        }
    }

    @Test
    void inactiveAndMalformedBoilersHaveNoGaugeGeometry() {
        assertTrue(BoilerGaugeRenderPlan.select(2, false).isEmpty());
        assertTrue(BoilerGaugeRenderPlan.select(0, true).isEmpty());
        assertTrue(BoilerGaugeRenderPlan.select(4, true).isEmpty());
    }
}
