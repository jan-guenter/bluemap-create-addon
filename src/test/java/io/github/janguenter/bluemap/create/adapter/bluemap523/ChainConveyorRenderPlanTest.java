/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainConveyorRenderPlanTest {

    @Test
    void blueNbtDecodesRawIntArrayConnections() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("Connections").beginList(3, TagType.INT_ARRAY);
            writer.value(new int[]{4, 1, -2});
            writer.value(new int[]{-4, -1, 2});
            writer.value(new int[]{0, 0, 0});
            writer.endList();
            writer.name("Speed").value(-32F);
            writer.endCompound();
        }
        ChainConveyorBlockEntityData decoded = new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                ChainConveyorBlockEntityData.class
        );
        assertEquals(List.of(
                new ChainConveyorRenderPlan.Offset(4, 1, -2),
                new ChainConveyorRenderPlan.Offset(-4, -1, 2)
        ), decoded.connections());
        assertEquals(-32F, decoded.speed());
    }

    @Test
    void strandTransformMapsLocalAxisToExactTangentEndpoints() {
        ChainConveyorRenderPlan.Offset offset =
                new ChainConveyorRenderPlan.Offset(4, 1, 0);
        ChainConveyorRenderPlan.Connection connection =
                ChainConveyorRenderPlan.select(List.of(offset))
                        .connections().getFirst();
        AffineTransform.Point start = expectedTangent(offset, false, false);
        AffineTransform.Point end = expectedTangent(offset, true, false);
        assertPoint(start, connection.strand().transform(0.5F, 0F, 0.5F));
        assertPoint(end, connection.strand().transform(
                0.5F, connection.length(), 0.5F
        ));
    }

    @Test
    void negativeSpeedMirrorsExactTangentBranch() {
        ChainConveyorRenderPlan.Offset offset =
                new ChainConveyorRenderPlan.Offset(6, 0, 0);
        ChainConveyorRenderPlan.Connection connection =
                ChainConveyorRenderPlan.select(List.of(offset), -32F)
                        .connections().getFirst();

        assertPoint(
                expectedTangent(offset, false, true),
                connection.strand().transform(0.5F, 0F, 0.5F)
        );
        assertPoint(
                expectedTangent(offset, true, true),
                connection.strand().transform(
                        0.5F, connection.length(), 0.5F
                )
        );
    }

    @Test
    void reciprocalEntriesRemainDistinctPhysicalStrands() {
        ChainConveyorRenderPlan plan = ChainConveyorRenderPlan.select(List.of(
                new ChainConveyorRenderPlan.Offset(5, 0, 0),
                new ChainConveyorRenderPlan.Offset(-5, 0, 0)
        ));
        assertEquals(2, plan.connections().size());
        assertTrue(plan.connections().stream()
                .allMatch(connection -> connection.length() > 0F
                        && connection.strand().finite()
                        && connection.guard().finite()));
    }

    @Test
    void malformedOrUnboundedOffsetsFailSoft() {
        ChainConveyorRenderPlan plan = ChainConveyorRenderPlan.select(List.of(
                new ChainConveyorRenderPlan.Offset(0, 0, 0),
                new ChainConveyorRenderPlan.Offset(Integer.MAX_VALUE, 0, 0),
                new ChainConveyorRenderPlan.Offset(0, -257, 0),
                new ChainConveyorRenderPlan.Offset(0, 3, 0),
                new ChainConveyorRenderPlan.Offset(2, 0, 0),
                new ChainConveyorRenderPlan.Offset(2, 2, 2)
        ));
        assertTrue(plan.connections().isEmpty());
    }

    @Test
    void legalHorizontalAndSlopedOffsetsRemainRenderable() {
        ChainConveyorRenderPlan plan = ChainConveyorRenderPlan.select(List.of(
                new ChainConveyorRenderPlan.Offset(3, 0, 0),
                new ChainConveyorRenderPlan.Offset(2, 1, 2)
        ));
        assertEquals(2, plan.connections().size());
    }

    private static AffineTransform.Point expectedTangent(
            ChainConveyorRenderPlan.Offset offset,
            boolean target,
            boolean reversed
    ) {
        double direction = Math.toDegrees(Math.atan2(offset.x(), offset.z()));
        double angle;
        if (target) {
            angle = direction + (reversed ? 145D : 215D);
        } else {
            angle = direction + (reversed ? 35D : -35D);
        }
        double radians = Math.toRadians(angle);
        return new AffineTransform.Point(
                (target ? offset.x() : 0) + 0.5F
                        + (float) Math.sin(radians) * 1.25F,
                (target ? offset.y() : 0) + 0.375F,
                (target ? offset.z() : 0) + 0.5F
                        + (float) Math.cos(radians) * 1.25F
        );
    }

    private static void assertPoint(
            AffineTransform.Point expected,
            AffineTransform.Point actual
    ) {
        assertEquals(expected.x(), actual.x(), 0.0001F);
        assertEquals(expected.y(), actual.y(), 0.0001F);
        assertEquals(expected.z(), actual.z(), 0.0001F);
    }
}
