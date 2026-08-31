/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaaStableRenderPlanTest {

    @Test
    void alternatorAndMotorUseExactShaftHalves() {
        for (CreateDirection facing : CreateDirection.values()) {
            Map<String, String> state = Map.of(
                    "facing", facing.name().toLowerCase()
            );
            assertEquals(2, CaaStableRenderPlan.select(
                    "createaddition:alternator", state
            ).orElseThrow().parts().size());
            assertEquals(1, CaaStableRenderPlan.select(
                    "createaddition:electric_motor", state
            ).orElseThrow().parts().size());
        }
    }

    @Test
    void rollingMillResetsBufferBetweenExactIndependentRollerTransforms() {
        CaaStableRenderPlan axisX = CaaStableRenderPlan.select(
                "createaddition:rolling_mill", Map.of("facing", "east")
        ).orElseThrow();
        assertEquals(3, axisX.parts().size());
        AffineTransformTest.assertPoint(
                axisX.parts().get(1).transform().transform(.5F, .5F, 1F),
                1F, .75F, .5F
        );
        AffineTransformTest.assertPoint(
                axisX.parts().get(2).transform().transform(.5F, .5F, 1F),
                0F, .75F, .5F
        );

        CaaStableRenderPlan axisZ = CaaStableRenderPlan.select(
                "createaddition:rolling_mill", Map.of("facing", "north")
        ).orElseThrow();
        AffineTransformTest.assertPoint(
                axisZ.parts().get(1).transform().transform(.5F, .5F, 1F),
                .5F, .75F, 1F
        );
        AffineTransformTest.assertPoint(
                axisZ.parts().get(2).transform().transform(.5F, .5F, 1F),
                .5F, .75F, 0F
        );
    }

    @Test
    void portableEnergyUsesDisconnectedPhysicalPartsForAllFacings() {
        for (CreateDirection facing : CreateDirection.values()) {
            CaaStableRenderPlan plan = CaaStableRenderPlan.select(
                    "createaddition:portable_energy_interface",
                    Map.of("facing", facing.name().toLowerCase())
            ).orElseThrow();
            assertEquals(2, plan.parts().size());
            assertTrue(plan.parts().get(0).model().endsWith("block_middle"));
            assertTrue(plan.parts().get(1).model().endsWith("block_top"));
        }
    }
}
