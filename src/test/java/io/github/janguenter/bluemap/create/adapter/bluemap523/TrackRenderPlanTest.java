/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackRenderPlanTest {

    @Test
    void canonicalQuarterMatchesExactHandleLengthAndSegmentCount() {
        TrackRenderPlan plan = TrackRenderPlan.select(primary(false)).orElseThrow();
        assertEquals(4.14213562373095D, plan.handleLength(), 0.0000001D);
        assertEquals(11.7778872141D, plan.length(), 0.00001D);
        assertEquals(23, plan.segments());
        assertEquals(23, plan.pieces().stream()
                .filter(piece -> piece.kind() == TrackRenderPlan.Kind.TRACK_TIE)
                .count());
        assertEquals(46, plan.pieces().stream()
                .filter(piece -> piece.kind() == TrackRenderPlan.Kind.TRACK_RAIL)
                .count());
    }

    @Test
    void girderAddsExactSixPhysicalPartsPerSegment() {
        TrackRenderPlan plan = TrackRenderPlan.select(primary(true)).orElseThrow();
        assertEquals(23 * 3 + 23 * 6, plan.pieces().size());
        assertEquals(23 * 6, plan.pieces().stream()
                .filter(piece -> piece.kind() == TrackRenderPlan.Kind.JSON)
                .count());
        assertTrue(plan.pieces().stream()
                .allMatch(piece -> piece.transform().finite()));
    }

    @Test
    void exactSecondaryReciprocalValidatesButNeverPlans() {
        TrackBlockEntityData.Connection primary = primary(false);
        TrackBlockEntityData.Connection secondary = secondary(false);
        assertTrue(primary.reciprocal(secondary));
        assertTrue(TrackRenderPlan.select(secondary).isEmpty());
    }

    @Test
    void binaryBlueNbtDecodesNestedCodecWrappers() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("Connections").beginList(1, TagType.COMPOUND);
            writer.beginCompound();
            writer.name("Primary").value((byte) 1);
            writer.name("Girder").value((byte) 0);
            writer.name("Material").value("create:andesite");
            positions(writer, new int[]{0, 0, 0}, new int[]{8, 0, 8});
            vectors(writer, "Starts", new double[]{1, 0, .5},
                    new double[]{8.5, 0, 8});
            vectors(writer, "Axes", new double[]{1, 0, 0},
                    new double[]{0, 0, -1});
            vectors(writer, "Normals", new double[]{0, 1, 0},
                    new double[]{0, 1, 0});
            writer.endCompound();
            writer.endList();
            writer.endCompound();
        }
        TrackBlockEntityData data = new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                TrackBlockEntityData.class
        );
        assertEquals(1, data.connections().size());
        assertEquals(23, TrackRenderPlan.select(data.connections().getFirst())
                .orElseThrow().segments());
    }

    private static void positions(
            NBTWriter writer,
            int[] first,
            int[] second
    ) throws IOException {
        writer.name("Positions").beginList(2, TagType.COMPOUND);
        writer.beginCompound();
        writer.name("Pos").value(first);
        writer.endCompound();
        writer.beginCompound();
        writer.name("Pos").value(second);
        writer.endCompound();
        writer.endList();
    }

    private static void vectors(
            NBTWriter writer,
            String name,
            double[] first,
            double[] second
    ) throws IOException {
        writer.name(name).beginList(2, TagType.COMPOUND);
        writer.beginCompound();
        writer.name("V").beginList(first.length, TagType.DOUBLE);
        for (double value : first) {
            writer.value(value);
        }
        writer.endList();
        writer.endCompound();
        writer.beginCompound();
        writer.name("V").beginList(second.length, TagType.DOUBLE);
        for (double value : second) {
            writer.value(value);
        }
        writer.endList();
        writer.endCompound();
        writer.endList();
    }

    private static TrackBlockEntityData.Connection primary(boolean girder) {
        return new TrackBlockEntityData.Connection(
                true, girder, "create:andesite",
                p(0, 0, 0), p(8, 0, 8),
                v(1, 0, .5), v(8.5, 0, 8),
                v(1, 0, 0), v(0, 0, -1),
                v(0, 1, 0), v(0, 1, 0)
        );
    }

    private static TrackBlockEntityData.Connection secondary(boolean girder) {
        return new TrackBlockEntityData.Connection(
                false, girder, "create:andesite",
                p(0, 0, 0), p(-8, 0, -8),
                v(.5, 0, 0), v(-7, 0, -7.5),
                v(0, 0, -1), v(1, 0, 0),
                v(0, 1, 0), v(0, 1, 0)
        );
    }

    private static TrackRenderPlan.IntPoint p(int x, int y, int z) {
        return new TrackRenderPlan.IntPoint(x, y, z);
    }

    private static TrackRenderPlan.Vector v(double x, double y, double z) {
        return new TrackRenderPlan.Vector(x, y, z);
    }
}
