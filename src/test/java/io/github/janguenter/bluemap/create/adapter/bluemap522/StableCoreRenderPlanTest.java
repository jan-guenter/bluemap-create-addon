/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StableCoreRenderPlanTest {

    @Test
    void sawSelectsInactiveBladeAndExactShaftFamily() {
        StableCoreRenderPlan wall = StableCoreRenderPlan.saw(Map.of(
                "facing", "east", "axis_along_first", "false"
        )).orElseThrow();
        assertEquals("create:block/mechanical_saw/blade_horizontal_inactive",
                wall.parts().get(0).model());
        assertEquals("create:block/shaft_half", wall.parts().get(1).model());

        StableCoreRenderPlan floor = StableCoreRenderPlan.saw(Map.of(
                "facing", "up", "axis_along_first", "true"
        )).orElseThrow();
        assertEquals("create:block/mechanical_saw/blade_vertical_inactive",
                floor.parts().get(0).model());
        assertEquals("create:block/shaft", floor.parts().get(1).model());
        assertEquals(new AffineTransform.Point(1F, 0.5F, 0.5F),
                floor.parts().get(1).transform().transform(0.5F, 1F, 0.5F));
    }

    @Test
    void deployerCoversAllDirectionalAxisBranches() {
        for (CreateDirection facing : CreateDirection.values()) {
            for (boolean along : new boolean[]{false, true}) {
                StableCoreRenderPlan plan = StableCoreRenderPlan.deployer(Map.of(
                        "facing", facing.name().toLowerCase(),
                        "axis_along_first", Boolean.toString(along)
                )).orElseThrow();
                assertEquals(3, plan.parts().size());
                assertEquals("create:block/shaft", plan.parts().get(0).model());
                assertEquals("create:block/deployer/pole", plan.parts().get(1).model());
                assertEquals("create:block/deployer/hand_pointing",
                        plan.parts().get(2).model());
                assertTrue(plan.parts().stream()
                        .allMatch(part -> part.transform().finite()));
            }
        }
    }

    @Test
    void armIsCompleteNeutralNoItemAssembly() {
        StableCoreRenderPlan floor = StableCoreRenderPlan.arm(false, false);
        StableCoreRenderPlan ceiling = StableCoreRenderPlan.arm(true, true);
        assertEquals(7, floor.parts().size());
        assertEquals("create:block/mechanical_arm/cog", floor.parts().get(0).model());
        assertEquals("create:block/mechanical_arm/claw_base_goggles",
                ceiling.parts().get(4).model());
        assertTrue(floor.parts().stream().allMatch(part -> part.transform().finite()));
        assertTrue(ceiling.parts().stream().allMatch(part -> part.transform().finite()));
    }

    @Test
    void portableInterfacesUseDisconnectedMiddleAndTop() {
        for (String id : new String[]{
                "create:portable_storage_interface",
                "create:portable_fluid_interface"
        }) {
            for (CreateDirection facing : CreateDirection.values()) {
                StableCoreRenderPlan plan = StableCoreRenderPlan.portable(
                        id, Map.of("facing", facing.name().toLowerCase())
                ).orElseThrow();
                assertEquals(2, plan.parts().size());
                assertTrue(plan.parts().get(0).model().endsWith("block_middle"));
                assertTrue(plan.parts().get(1).model().endsWith("block_top"));
            }
        }
    }

    @Test
    void malformedStatesFailClosed() {
        assertTrue(StableCoreRenderPlan.saw(Map.of()).isEmpty());
        assertTrue(StableCoreRenderPlan.deployer(Map.of("facing", "sideways"))
                .isEmpty());
        assertTrue(StableCoreRenderPlan.portable("other:block", Map.of(
                "facing", "north"
        )).isEmpty());
    }
}
