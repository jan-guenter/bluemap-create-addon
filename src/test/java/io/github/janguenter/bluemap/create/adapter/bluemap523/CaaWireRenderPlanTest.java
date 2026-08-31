/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaaWireRenderPlanTest {

    @Test
    void binaryBlueNbtDecodesExactLowercaseNodeList() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("nodes").beginList(1, TagType.COMPOUND);
            writer.beginCompound();
            writer.name("id").value(2);
            writer.name("other").value(5);
            writer.name("type").value(2);
            writer.name("x").value(6);
            writer.name("y").value(1);
            writer.name("z").value(-3);
            writer.endCompound();
            writer.endList();
            writer.endCompound();
        }
        CaaWireBlockEntityData data = new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                CaaWireBlockEntityData.class
        );
        assertEquals(1, data.nodes().size());
        assertEquals(2, data.nodes().getFirst().id());
        assertEquals(5, data.nodes().getFirst().other());
        assertEquals(2, data.nodes().getFirst().type());
        assertEquals(new CaaWireRenderPlan.Offset(6, 1, -3),
                data.nodes().getFirst().offset());
    }

    @Test
    void allWireTypesUseExactColorsAndTwentyFourSegments() {
        CaaWireRenderPlan.Point start = new CaaWireRenderPlan.Point(0F, 0F, 0F);
        CaaWireRenderPlan.Point end = new CaaWireRenderPlan.Point(6F, 2F, 0F);
        for (int type = 0; type <= 3; type++) {
            CaaWireRenderPlan plan = CaaWireRenderPlan.select(
                    start, end, (float) Math.sqrt(40D), type
            ).orElseThrow();
            assertEquals(25, plan.points().size());
            assertEquals(start, plan.points().getFirst());
            assertEquals(end.x(), plan.points().getLast().x(), 0.00001F);
            assertEquals(end.y(), plan.points().getLast().y(), 0.00001F);
            assertEquals(end.z(), plan.points().getLast().z(), 0.00001F);
            assertEquals(0.0125F, plan.width());
            assertTrue(!plan.steep());
        }
        assertTrue(CaaWireRenderPlan.select(start, end, 6F, 4).isEmpty());
    }

    @Test
    void connectorAndRelayOffsetsMatchExactNodeGeometry() {
        assertEquals(new CaaWireRenderPlan.Point(0F, 0F, -0.1875F),
                CaaWireRenderPlan.localOffset(
                        "createaddition:connector", Map.of("facing", "north"), 0
                ).orElseThrow());
        assertEquals(new CaaWireRenderPlan.Point(0.0625F, 0F, 0F),
                CaaWireRenderPlan.localOffset(
                        "createaddition:large_connector", Map.of("facing", "east"), 5
                ).orElseThrow());
        assertEquals(new CaaWireRenderPlan.Point(5F / 16F, 0F, -1F / 16F),
                CaaWireRenderPlan.localOffset(
                        "createaddition:redstone_relay",
                        Map.of("facing", "north", "vertical", "true"), 0
                ).orElseThrow());
        assertEquals(new CaaWireRenderPlan.Point(-5F / 16F, 0F, -1F / 16F),
                CaaWireRenderPlan.localOffset(
                        "createaddition:redstone_relay",
                        Map.of("facing", "north", "vertical", "true"), 4
                ).orElseThrow());
        assertEquals(new CaaWireRenderPlan.Point(0F, -1F / 16F, 5F / 16F),
                CaaWireRenderPlan.localOffset(
                        "createaddition:redstone_relay",
                        Map.of("facing", "south", "vertical", "false"), 4
                ).orElseThrow());
    }

    @Test
    void verticalWireUsesNarrowExactStrip() {
        CaaWireRenderPlan plan = CaaWireRenderPlan.select(
                new CaaWireRenderPlan.Point(0F, 0F, 0F),
                new CaaWireRenderPlan.Point(0F, 5F, 0F), 5F, 0
        ).orElseThrow();
        assertEquals(0.015F, plan.width());
        assertTrue(plan.steep());
    }
}
