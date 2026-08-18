/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectionalPartialTransformsTest {

    @Test
    void pumpPartialFacingSupportsAllSixDirectionsWithoutVariantRotation() {
        Map<String, AffineTransform.Point> expected = Map.of(
                "south", new AffineTransform.Point(0.5F, 0.5F, 1.5F),
                "north", new AffineTransform.Point(0.5F, 0.5F, -0.5F),
                "east", new AffineTransform.Point(1.5F, 0.5F, 0.5F),
                "west", new AffineTransform.Point(-0.5F, 0.5F, 0.5F),
                "up", new AffineTransform.Point(0.5F, 1.5F, 0.5F),
                "down", new AffineTransform.Point(0.5F, -0.5F, 0.5F)
        );
        for (Map.Entry<String, AffineTransform.Point> entry : expected.entrySet()) {
            AffineTransform transform = DirectionalPartialTransforms.pump(entry.getKey())
                    .orElseThrow();
            AffineTransform.Point actual = transform.transform(0.5F, 0.5F, 1.5F);
            AffineTransform.Point point = entry.getValue();
            AffineTransformTest.assertPoint(actual, point.x(), point.y(), point.z());
        }
        assertFalse(DirectionalPartialTransforms.pump("sideways").isPresent());
    }

    @Test
    void crafterCogHasTwoAxisSpecificNeutralMatrices() {
        AffineTransform north = DirectionalPartialTransforms.crafterCog("north")
                .orElseThrow();
        AffineTransform east = DirectionalPartialTransforms.crafterCog("east")
                .orElseThrow();
        AffineTransformTest.assertPoint(
                north.transform(0.5F, 1.5F, 0.5F), 0.5F, 0.5F, 1.5F
        );
        AffineTransformTest.assertPoint(
                east.transform(0.5F, 1.5F, 0.5F), 1.5F, 0.5F, 0.5F
        );
    }

    @Test
    void crafterTargetAndBodyMatrixFollowPointingProperty() {
        assertEquals(CreateDirection.UP,
                DirectionalPartialTransforms.crafterTarget("north", "up").orElseThrow());
        assertEquals(CreateDirection.DOWN,
                DirectionalPartialTransforms.crafterTarget("east", "down").orElseThrow());
        assertEquals(CreateDirection.WEST,
                DirectionalPartialTransforms.crafterTarget("south", "left").orElseThrow());
        assertEquals(CreateDirection.SOUTH,
                DirectionalPartialTransforms.crafterTarget("east", "left").orElseThrow());

        AffineTransform body = DirectionalPartialTransforms.crafterBodyPartial(
                "west", "right"
        ).orElseThrow();
        assertTrue(body.finite());
        AffineTransformTest.assertPoint(
                body.transform(0.5F, 0.5F, 0.5F), 0.5F, 0.5F, 0.5F
        );
    }

    @Test
    void crafterTargetValidityMatchesExactNeighborRules() {
        assertTrue(DirectionalPartialTransforms.validCrafterTarget(
                "north", "up", "create:mechanical_crafter", "north", "left"
        ));
        assertFalse(DirectionalPartialTransforms.validCrafterTarget(
                "north", "up", "create:mechanical_crafter", "north", "down"
        ));
        assertFalse(DirectionalPartialTransforms.validCrafterTarget(
                "north", "up", "create:mechanical_crafter", "south", "up"
        ));
        assertFalse(DirectionalPartialTransforms.validCrafterTarget(
                "north", "up", "minecraft:stone", "north", "up"
        ));
    }
}
