/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CeiStableRenderPlanTest {

    @Test
    void binaryBlueNbtDecodesPersistedPoweredSelection() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("Powered").value((byte) 1);
            writer.name("ProcessingTicks").value(-1);
            writer.endCompound();
        }
        CeiPoweredBlockEntityData data = new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                CeiPoweredBlockEntityData.class
        );
        assertTrue(data.powered());
    }

    @Test
    void printerInfuserAndBookshelfKeepStablePhysicalPartRoster() {
        assertModels("create_enchantment_industry:printer", false, List.of(
                "create_enchantment_industry:block/printer/nozzle_top",
                "create_enchantment_industry:block/printer/nozzle_bottom",
                "create_enchantment_industry:block/printer/piston"
        ));
        assertModels("create_enchantment_industry:infuser", false, List.of(
                "create_enchantment_industry:block/infuser/eterna_needle",
                "create_enchantment_industry:block/infuser/arcana_needle",
                "create_enchantment_industry:block/infuser/quanta_needle"
        ));
        CeiStableRenderPlan shelf = CeiStableRenderPlan.select(
                "create_enchantment_industry:brass_bookshelf",
                Map.of("facing", "north"), false
        ).orElseThrow();
        assertEquals(List.of("create:block/shaft_half"), shelf.parts().stream()
                .map(StableCoreRenderPlan.Part::model).toList());
        AffineTransformTest.assertPoint(
                shelf.parts().getFirst().transform().transform(.5F, .5F, .5F),
                .5F, .5F, .5F
        );
    }

    @Test
    void poweredAndRetractedAugmentorAndGemCutterRemainMateriallyDistinct() {
        CeiStableRenderPlan offAugmentor = plan("affix_augmentor", false);
        CeiStableRenderPlan onAugmentor = plan("affix_augmentor", true);
        assertEquals(1, offAugmentor.parts().size());
        assertEquals(4, onAugmentor.parts().size());
        assertTrue(onAugmentor.parts().getFirst().model().endsWith("plate_powered"));

        CeiStableRenderPlan offCutter = plan("gem_cutter", false);
        CeiStableRenderPlan onCutter = plan("gem_cutter", true);
        assertEquals(4, offCutter.parts().size());
        assertEquals(4, onCutter.parts().size());
        assertTrue(onCutter.parts().stream().allMatch(part ->
                part.model().endsWith("_powered") && part.transform().finite()
        ));
        assertTrue(offCutter.parts().stream().allMatch(part -> part.transform().finite()));
    }

    @Test
    void exactSupportBranchesSelectMiddleEndpointDepotAndNone() {
        assertTrue(CeiStableRenderPlan.support(
                "create:belt", Map.of(
                        "facing", "east", "slope", "horizontal",
                        "casing", "false", "part", "middle"
                )
        ).orElseThrow().model().endsWith("/special"));
        assertTrue(CeiStableRenderPlan.support(
                "create:belt", Map.of(
                        "facing", "east", "slope", "horizontal",
                        "casing", "false", "part", "start"
                )
        ).orElseThrow().model().endsWith("/special_with_shaft"));
        assertTrue(CeiStableRenderPlan.support("create:depot", Map.of())
                .orElseThrow().model().endsWith("/special_top_only"));
        assertTrue(CeiStableRenderPlan.support(
                "create:belt", Map.of(
                        "facing", "east", "slope", "horizontal",
                        "casing", "true", "part", "middle"
                )
        ).isEmpty());
    }

    @Test
    void grindstoneAxisAndEmptyBagExteriorUseExactStateTransforms() {
        assertEquals(new CeiStableRenderPlan.Grindstone(90F, 90F),
                CeiStableRenderPlan.grindstone(Map.of("facing", "east"))
                        .orElseThrow());
        assertEquals(new CeiStableRenderPlan.Grindstone(90F, 180F),
                CeiStableRenderPlan.grindstone(Map.of("facing", "north"))
                        .orElseThrow());
        CeiStableRenderPlan bag = CeiStableRenderPlan.select(
                "create_enchantment_industry:ender_woven_bag",
                Map.of("facing", "north", "powered", "false"), false
        ).orElseThrow();
        assertEquals(List.of(
                "create_enchantment_industry:block/ender_woven_bag/light_off",
                "create_enchantment_industry:block/ender_woven_bag/open_pocket"
        ), bag.parts().stream().map(StableCoreRenderPlan.Part::model).toList());
        AffineTransform.Point pocket = bag.parts().get(1).transform()
                .transform(0F, 0F, 0F);
        assertEquals(-6F / 16F, pocket.z(), .00001F);
    }

    @Test
    void blazeFamiliesKeepInactiveHeadRodsAndHeatSpecificHat() {
        CeiBlazeRenderPlan kindled = CeiBlazeRenderPlan.select(
                "create_enchantment_industry:blaze_enchanter",
                Map.of("blaze", "kindled", "facing", "east")
        ).orElseThrow();
        assertTrue(kindled.blaze().headModel().endsWith("/idle"));
        assertEquals(2, kindled.blaze().rods().size());
        assertTrue(kindled.hat().model().endsWith("/enchanter_hat"));

        CeiBlazeRenderPlan smouldering = CeiBlazeRenderPlan.select(
                "create_enchantment_industry:blaze_forger",
                Map.of("blaze", "smouldering", "facing", "south")
        ).orElseThrow();
        assertTrue(smouldering.blaze().headModel().endsWith("/inert"));
        assertEquals(0, smouldering.blaze().rods().size());
        assertTrue(smouldering.hat().model().endsWith("/forger_hat_small"));

        CeiBlazeRenderPlan classic = CeiBlazeRenderPlan.select(
                "create_enchantment_industry:classic_blaze_enchanter",
                Map.of("blaze", "seething", "facing", "north")
        ).orElseThrow();
        assertNull(classic.hat());
        assertTrue(classic.blaze().rods().stream()
                .allMatch(part -> part.model().contains("superheated")));
        BlazeBurnerRenderPlan unshifted = BlazeBurnerRenderPlan.select(
                "seething", "north", false
        ).orElseThrow();
        assertEquals(
                unshifted.headTransform().transform(.5F, .5F, .5F).y() + .2F,
                classic.blaze().headTransform().transform(.5F, .5F, .5F).y(),
                .00001F
        );
    }

    @Test
    void classicBookUsesExactFrozenSevenPartVanillaPose() {
        CeiBookRenderPlan plan = CeiBookRenderPlan.select("north").orElseThrow();
        assertEquals(7, plan.boxes().size());
        assertEquals(84, plan.triangleCount());
        assertTrue(plan.boxes().stream().allMatch(box -> box.transform().finite()));
        AffineTransform first = plan.boxes().getFirst().transform();
        float scale = (float) Math.sqrt(
                first.component(0, 0) * first.component(0, 0)
                        + first.component(1, 0) * first.component(1, 0)
                        + first.component(2, 0) * first.component(2, 0)
        );
        assertEquals(1.2F, scale, .00001F);
        assertTrue(CeiBookRenderPlan.select("up").isEmpty());
    }

    @Test
    void classicBookUsesVanillaCubeFaceWindingUvUnwrapAndFullBright() {
        CeiBookRenderPlan.Box box = new CeiBookRenderPlan.Box(
                AffineTransform.identity(), 0F, 0F, 0F, 1F, 1F, 1F,
                3F, 4F, 6F, 10F, 2F
        );
        assertEquals(List.of(
                new CeiBookEmitter.FaceLayout(5, 1, 0, 4, 5F, 4F, 11F, 6F),
                new CeiBookEmitter.FaceLayout(6, 2, 3, 7, 11F, 6F, 17F, 4F),
                new CeiBookEmitter.FaceLayout(0, 1, 3, 2, 3F, 6F, 5F, 16F),
                new CeiBookEmitter.FaceLayout(4, 0, 2, 6, 5F, 6F, 11F, 16F),
                new CeiBookEmitter.FaceLayout(5, 4, 6, 7, 11F, 6F, 13F, 16F),
                new CeiBookEmitter.FaceLayout(1, 5, 7, 3, 13F, 6F, 19F, 16F)
        ), CeiBookEmitter.faceLayouts(box));

        float[] westUvs = CeiBookEmitter.triangleUvs(
                CeiBookEmitter.faceLayouts(box).get(2)
        );
        assertEquals(List.of(
                5F / 64F, 6F / 32F,
                3F / 64F, 6F / 32F,
                3F / 64F, 16F / 32F,
                5F / 64F, 6F / 32F,
                3F / 64F, 16F / 32F,
                5F / 64F, 16F / 32F
        ), java.util.stream.IntStream.range(0, westUvs.length)
                .mapToObj(index -> westUvs[index]).toList());
        assertEquals(15, CeiBookEmitter.FULL_BRIGHT);
    }

    private static CeiStableRenderPlan plan(String id, boolean powered) {
        return CeiStableRenderPlan.select(
                "create_enchantment_industry:" + id,
                Map.of("facing", "west"), powered
        ).orElseThrow();
    }

    private static void assertModels(
            String blockId,
            boolean powered,
            List<String> models
    ) {
        CeiStableRenderPlan plan = CeiStableRenderPlan.select(
                blockId, Map.of("facing", "north"), powered
        ).orElseThrow();
        assertEquals(models, plan.parts().stream()
                .map(StableCoreRenderPlan.Part::model).toList());
    }
}
