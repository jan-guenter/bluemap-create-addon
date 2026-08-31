/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AffineTransformTest {

    @Test
    void scaleComposesInsideCenteredTransforms() {
        AffineTransform.Point point = AffineTransform.identity()
                .centered().scale(0.5F, 0.5F, 0.5F).uncentered()
                .transform(1F, 1F, 1F);
        assertEquals(0.75F, point.x(), 0.00001F);
        assertEquals(0.75F, point.y(), 0.00001F);
        assertEquals(0.75F, point.z(), 0.00001F);
    }

    private static final float EPSILON = 1E-5F;

    @Test
    void centeredRotationKeepsBlockCenterFixed() {
        AffineTransform transform = AffineTransform.identity()
                .centered().rotateY(90F).rotateX(-90F).uncentered();

        assertPoint(transform.transform(0.5F, 0.5F, 0.5F), 0.5F, 0.5F, 0.5F);
        assertTrue(transform.finite());
        assertEquals(16, transform.copyValues().length);
    }

    @Test
    void poseStackOrderUsesPostMultiplication() {
        AffineTransform transform = AffineTransform.identity()
                .rotateY(90F)
                .translate(0F, 0F, 1F);

        assertPoint(transform.transform(0F, 0F, 0F), 1F, 0F, 0F);
    }

    @Test
    void componentBoundsAreFailFast() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> AffineTransform.identity().component(4, 0));
    }

    static void assertPoint(
            AffineTransform.Point actual,
            float x,
            float y,
            float z
    ) {
        assertEquals(x, actual.x(), EPSILON);
        assertEquals(y, actual.y(), EPSILON);
        assertEquals(z, actual.z(), EPSILON);
    }
}
