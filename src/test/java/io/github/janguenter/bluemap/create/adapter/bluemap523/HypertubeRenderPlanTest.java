/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HypertubeRenderPlanTest {

    @Test
    void binaryBlueNbtDecodesOnlyFullVersionOneBezierAndAttachments()
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("ConnectionTo_version").value(1);
            writer.name("ConnectionTo").beginCompound();
            simple(writer, "fromPos", new int[]{0, 0, 0}, "west");
            simple(writer, "toPos", new int[]{-2, 0, 2}, "south");
            writer.name("tubeSegments").value(1);
            writer.name("curvePoints").beginList(4, TagType.LIST);
            point(writer, .5D, .5D, .5D);
            point(writer, -.5213500073622857D, .5D, .767102774096635D);
            point(writer, -1.232897225903365D, .5D, 1.4786499926377141D);
            point(writer, -1.5D, .5D, 2.5D);
            writer.endList();
            writer.endCompound();
            writer.name("ConnectionFrom_version").value(1);
            writer.name("ConnectionFrom").beginCompound();
            writer.name("pos").value(new int[]{2, 0, -2});
            writer.name("direction").value("west");
            writer.endCompound();
            writer.name("ConnectionOne_version").value(2);
            writer.name("ConnectionOne").beginCompound();
            simple(writer, "fromPos", new int[]{0, 0, 0}, "west");
            simple(writer, "toPos", new int[]{1, 0, 0}, "east");
            writer.name("tubeSegments").value(1);
            writer.name("curvePoints").beginList(2, TagType.LIST);
            point(writer, .5D, .5D, .5D);
            point(writer, 1.5D, .5D, .5D);
            writer.endList();
            writer.endCompound();
            writer.name("attachments").beginCompound();
            writer.name("up").value("tube_scanner");
            writer.name("south").value("redstone_input");
            writer.name("north").value("unknown_future_attachment");
            writer.endCompound();
            writer.endCompound();
        }
        HypertubeBlockEntityData data = new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                HypertubeBlockEntityData.class
        );
        assertEquals(1, data.curves().size());
        HypertubeRenderPlan plan = HypertubeRenderPlan.select(
                data.curves().getFirst()
        ).orElseThrow();
        assertEquals(4, plan.rings().size());
        assertEquals(120, plan.triangleCount());
        assertEquals(List.of(
                new HypertubeAttachmentPlan.SavedAttachment(
                        CreateDirection.UP, "tube_scanner"
                ),
                new HypertubeAttachmentPlan.SavedAttachment(
                        CreateDirection.SOUTH, "redstone_input"
                )
        ), data.attachments());
    }

    @Test
    void exactStableFramesKeepAllThreeRadiiAndSurviveVerticalTurns() {
        HypertubeRenderPlan plan = HypertubeRenderPlan.select(new HypertubeRenderPlan.Curve(
                List.of(
                        p(.5F, .5F, .5F), p(.5F, 1.5F, .5F),
                        p(.5F, 2.5F, .5F), p(1.5F, 3.5F, .5F)
                ), 1
        )).orElseThrow();
        for (HypertubeRenderPlan.Ring ring : plan.rings()) {
            for (HypertubeRenderPlan.Point offset : ring.exterior()) {
                assertEquals(.7F, length(offset), .00001F);
            }
            for (HypertubeRenderPlan.Point offset : ring.interior()) {
                assertEquals(.62F, length(offset), .00001F);
            }
            for (HypertubeRenderPlan.Point offset : ring.line()) {
                assertEquals(.69F, length(offset), .00001F);
            }
        }
    }

    @Test
    void exactTubeSegmentThinningDoesNotThinLongitudinalLine() {
        HypertubeRenderPlan plan = HypertubeRenderPlan.select(new HypertubeRenderPlan.Curve(
                List.of(
                        p(0F, 0F, 0F), p(1F, 0F, 0F), p(2F, 0F, 0F),
                        p(3F, 0F, 0F), p(4F, 0F, 0F)
                ), 2
        )).orElseThrow();
        assertEquals(List.of(false, false, true, false), booleans(plan, true));
        assertEquals(List.of(true, true, true, true), booleans(plan, false));
        assertEquals(88, plan.triangleCount());
    }

    @Test
    void attachmentsAreInactiveNoCogAndEntranceCogDoesNotRequireSavedParts() {
        HypertubeAttachmentPlan emptyEntrance = HypertubeAttachmentPlan.select(
                "create_hypertube:hypertube_entrance",
                Map.of("facing", "east"), List.of()
        );
        assertEquals(List.of("create_hypertube:block/hypertube_entrance/cogwheel_hole"),
                emptyEntrance.parts().stream().map(StableCoreRenderPlan.Part::model).toList());

        ArrayList<HypertubeAttachmentPlan.SavedAttachment> all = new ArrayList<>();
        for (CreateDirection direction : CreateDirection.values()) {
            all.add(new HypertubeAttachmentPlan.SavedAttachment(
                    direction,
                    direction.ordinal() % 2 == 0 ? "redstone_input" : "tube_scanner"
            ));
        }
        HypertubeAttachmentPlan junction = HypertubeAttachmentPlan.select(
                "create_hypertube:hypertube_junction",
                Map.of("facing", "north"), all
        );
        assertEquals(6, junction.parts().size());
        assertTrue(junction.parts().stream().allMatch(part ->
                part.model().endsWith("_no_cog") && part.transform().finite()
        ));
    }

    @Test
    void malformedCurveFailsSoft() {
        HypertubeRenderPlan.Point same = p(0F, 0F, 0F);
        assertTrue(HypertubeRenderPlan.select(new HypertubeRenderPlan.Curve(
                List.of(same, same), 1
        )).isEmpty());
        assertTrue(HypertubeRenderPlan.select(new HypertubeRenderPlan.Curve(
                List.of(same, p(1F, 0F, 0F)), 0
        )).isEmpty());
    }

    private static List<Boolean> booleans(
            HypertubeRenderPlan plan,
            boolean tube
    ) {
        ArrayList<Boolean> values = new ArrayList<>();
        for (int index = 0; index < plan.rings().size() - 1; index++) {
            values.add(tube ? plan.tubeInterval(index) : plan.lineInterval(index));
        }
        return values;
    }

    private static float length(HypertubeRenderPlan.Point point) {
        return (float) Math.sqrt(point.lengthSquared());
    }

    private static HypertubeRenderPlan.Point p(float x, float y, float z) {
        return new HypertubeRenderPlan.Point(x, y, z);
    }

    private static void simple(
            NBTWriter writer,
            String name,
            int[] position,
            String direction
    ) throws IOException {
        writer.name(name).beginCompound();
        writer.name("pos").value(position);
        writer.name("direction").value(direction);
        writer.endCompound();
    }

    private static void point(NBTWriter writer, double x, double y, double z)
            throws IOException {
        writer.beginList(3, TagType.DOUBLE);
        writer.value(x);
        writer.value(y);
        writer.value(z);
        writer.endList();
    }
}
