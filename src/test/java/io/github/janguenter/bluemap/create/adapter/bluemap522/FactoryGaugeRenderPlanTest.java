/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

class FactoryGaugeRenderPlanTest {

    private static final float EPSILON = 1E-5F;

    @Test
    void selectsOnlyPersistedPhysicalSlotsAndRestockerModel() {
        FactoryGaugeRenderPlan plan = FactoryGaugeRenderPlan.select(
                state("floor", "south"),
                EnumSet.of(
                        FactoryGaugeRenderPlan.Slot.TOP_LEFT,
                        FactoryGaugeRenderPlan.Slot.BOTTOM_RIGHT
                ),
                true
        ).orElseThrow();

        assertEquals(2, plan.panels().size());
        assertTrue(plan.panels().stream().allMatch(panel ->
                "create:block/factory_gauge/panel_restocker".equals(panel.model())));
    }

    @Test
    void allFaceAndFacingTransformsStayFiniteAndPreserveSlotSeparation() {
        for (String face : new String[]{"floor", "wall", "ceiling"}) {
            for (String facing : new String[]{"north", "south", "west", "east"}) {
                FactoryGaugeRenderPlan plan = FactoryGaugeRenderPlan.select(
                        state(face, facing),
                        EnumSet.allOf(FactoryGaugeRenderPlan.Slot.class),
                        false
                ).orElseThrow();
                assertEquals(4, plan.panels().size());
                assertTrue(plan.panels().stream().allMatch(panel ->
                        panel.transform().finite()));
                long distinct = plan.panels().stream()
                        .map(panel -> panel.transform().transform(0.25F, 0F, 0.25F))
                        .distinct()
                        .count();
                assertEquals(4, distinct);
            }
        }
    }

    @Test
    void floorSouthMatchesExactSlotThenY180TransformOrder() {
        FactoryGaugeRenderPlan plan = FactoryGaugeRenderPlan.select(
                state("floor", "south"),
                EnumSet.of(FactoryGaugeRenderPlan.Slot.TOP_LEFT),
                false
        ).orElseThrow();
        AffineTransform.Point point = plan.panels().getFirst().transform()
                .transform(0F, 0F, 0F);
        assertEquals(0.5F, point.x(), EPSILON);
        assertEquals(0F, point.y(), EPSILON);
        assertEquals(0.5F, point.z(), EPSILON);
    }

    @Test
    void blueNbtDecodesNonEmptyBehaviourCompoundsAndRejectsEmptySlots()
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("top_left").beginCompound();
            writer.name("RecipeAddress").value("");
            writer.endCompound();
            writer.name("top_right").beginCompound();
            writer.endCompound();
            writer.name("bottom_right").beginCompound();
            writer.name("RecipeAddress").value("factory:accepted");
            writer.endCompound();
            writer.name("Restocker").value((byte) 1);
            writer.endCompound();
        }
        FactoryGaugeBlockEntityData decoded = new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                FactoryGaugeBlockEntityData.class
        );
        assertEquals(
                EnumSet.of(
                        FactoryGaugeRenderPlan.Slot.TOP_LEFT,
                        FactoryGaugeRenderPlan.Slot.BOTTOM_RIGHT
                ),
                decoded.activeSlots()
        );
        assertTrue(decoded.restocker());
    }

    private static Map<String, String> state(String face, String facing) {
        return Map.of("face", face, "facing", facing);
    }
}
