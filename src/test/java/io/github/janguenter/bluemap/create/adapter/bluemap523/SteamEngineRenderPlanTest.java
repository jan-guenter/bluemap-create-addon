/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SteamEngineRenderPlanTest {

    @Test
    void validEngineFindsPoweredShaftTwoBlocksOutward() {
        assertOffset(plan("floor", "north", "x"), 0, 2, 0);
        assertOffset(plan("ceiling", "south", "z"), 0, -2, 0);
        assertOffset(plan("wall", "east", "y"), 2, 0, 0);
        assertOffset(plan("wall", "north", "x"), 0, 0, -2);
    }

    @Test
    void parallelShaftAndMalformedStatesFailSoft() {
        assertFalse(SteamEngineRenderPlan.select("floor", "north", "y").isPresent());
        assertFalse(SteamEngineRenderPlan.select("wall", "north", "z").isPresent());
        assertFalse(SteamEngineRenderPlan.select("wall", "up", "x").isPresent());
        assertFalse(SteamEngineRenderPlan.select("side", "north", "x").isPresent());
        assertFalse(SteamEngineRenderPlan.select(null, "north", "x").isPresent());
    }

    @Test
    void frozenNeutralPoseUsesExactPistonLinkageConnectorOffsets() {
        for (SteamEngineRenderPlan plan : List.of(
                plan("floor", "north", "x"),
                plan("floor", "north", "z"),
                plan("ceiling", "south", "x"),
                plan("wall", "east", "y"),
                plan("wall", "west", "z")
        )) {
            assertTrue(plan.piston().finite());
            assertTrue(plan.linkage().finite());
            assertTrue(plan.connector().finite());
            AffineTransform.Point pistonOrigin = plan.piston().transform(0F, 0F, 0F);
            AffineTransform.Point linkageOrigin = plan.linkage().transform(0F, 0F, 0F);
            AffineTransform.Point connectorOrigin = plan.connector().transform(0F, 0F, 0F);
            AffineTransform.Point expectedLinkage = plan.piston().transform(0F, 1F, 0F);
            AffineTransform.Point expectedConnector = plan.piston().transform(0F, 2F, 0F);
            AffineTransformTest.assertPoint(
                    linkageOrigin, expectedLinkage.x(), expectedLinkage.y(), expectedLinkage.z()
            );
            AffineTransformTest.assertPoint(
                    connectorOrigin, expectedConnector.x(), expectedConnector.y(), expectedConnector.z()
            );
            assertTrue(Float.isFinite(pistonOrigin.x()));
        }
    }

    @Test
    void rollSelectionChangesVerticalAndHorizontalMatrices() {
        SteamEngineRenderPlan verticalX = plan("floor", "north", "x");
        SteamEngineRenderPlan verticalZ = plan("floor", "north", "z");
        SteamEngineRenderPlan horizontalY = plan("wall", "north", "y");
        SteamEngineRenderPlan horizontalX = plan("wall", "north", "x");

        assertFalse(java.util.Arrays.equals(
                verticalX.piston().copyValues(), verticalZ.piston().copyValues()
        ));
        assertFalse(java.util.Arrays.equals(
                horizontalY.piston().copyValues(), horizontalX.piston().copyValues()
        ));
    }

    private static SteamEngineRenderPlan plan(
            String face,
            String facing,
            String shaftAxis
    ) {
        return SteamEngineRenderPlan.select(face, facing, shaftAxis).orElseThrow();
    }

    private static void assertOffset(
            SteamEngineRenderPlan plan,
            int x,
            int y,
            int z
    ) {
        assertEquals(new SteamEngineRenderPlan.Offset(x, y, z), plan.shaftOffset());
    }
}
