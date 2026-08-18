/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AquaticConduitRenderPlanTest {

    @Test
    void idleAndAwakenedSelectOnlyStablePhysicalParts() {
        AquaticConduitRenderPlan idle = AquaticConduitRenderPlan.select(Map.of(
                "conduit", "idle"
        )).orElseThrow();
        AquaticConduitRenderPlan awakened = AquaticConduitRenderPlan.select(Map.of(
                "conduit", "awakened"
        )).orElseThrow();

        assertEquals("create_aquatic_ambitions:block/conduit_eye",
                idle.parts().get(0).model());
        assertEquals("create_aquatic_ambitions:block/inactive_conduit",
                idle.parts().get(1).model());
        assertEquals("create_aquatic_ambitions:block/conduit_cage",
                awakened.parts().get(1).model());
        assertEquals(0.2F,
                awakened.parts().get(0).transform().transform(0F, 0F, 0F).y(),
                0.00001F);
        assertTrue(AquaticConduitRenderPlan.select(Map.of()).isEmpty());
    }
}
