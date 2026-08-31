/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainRibbonGeometryTest {

    private static final float EPSILON = 0.00001F;

    @Test
    void sixUnitNeutralRibbonSplitsAtHalfPhaseBoundaries() {
        List<ChainRibbonGeometry.Span> spans =
                ChainRibbonGeometry.spans(6F);

        assertEquals(7, spans.size());
        assertEquals(56, ChainRibbonGeometry.triangles(6F).size());
        assertSpan(spans.getFirst(), 0F, 0.5F, 5, 0.5F, 0F);
        assertSpan(spans.get(1), 0.5F, 1.5F, 4, 1F, 0F);
        assertSpan(spans.get(5), 4.5F, 5.5F, 0, 1F, 0F);
        assertSpan(spans.getLast(), 5.5F, 6F, -1, 1F, 0.5F);
    }

    @Test
    void fractionalLengthPreservesNegativeRawVPeriod() {
        float length = 2.75F;
        List<ChainRibbonGeometry.Span> spans =
                ChainRibbonGeometry.spans(length);

        assertEquals(4, spans.size());
        assertEquals(32, ChainRibbonGeometry.triangles(length).size());
        assertSpan(spans.getFirst(), 0F, 0.25F, 2, 0.25F, 0F);
        assertSpan(spans.getLast(), 2.25F, 2.75F, -1, 1F, 0.5F);
        for (ChainRibbonGeometry.Span span : spans) {
            assertEquals(
                    length - 0.5F - span.sourceY(),
                    span.period() + span.sourceV(),
                    EPSILON
            );
            assertEquals(
                    length - 0.5F - span.targetY(),
                    span.period() + span.targetV(),
                    EPSILON
            );
        }
    }

    @Test
    void trianglesUseExactBandsUvOrderAndDoubleSidedWinding() {
        List<ChainRibbonGeometry.Triangle> triangles =
                ChainRibbonGeometry.triangles(6F);

        ChainRibbonGeometry.Triangle frontFirstPlane = triangles.getFirst();
        assertVertex(
                frontFirstPlane.first(),
                0.5F, 0.5F, 0.5F + ChainRibbonGeometry.RADIUS,
                ChainRibbonGeometry.FIRST_U_MAX, 0F
        );
        assertVertex(
                frontFirstPlane.second(),
                0.5F, 0F, 0.5F + ChainRibbonGeometry.RADIUS,
                ChainRibbonGeometry.FIRST_U_MAX, 0.5F
        );
        assertVertex(
                frontFirstPlane.third(),
                0.5F, 0F, 0.5F - ChainRibbonGeometry.RADIUS,
                ChainRibbonGeometry.FIRST_U_MIN, 0.5F
        );

        assertTrue(normalX(triangles.get(0)) > 0F);
        assertTrue(normalX(triangles.get(2)) < 0F);
        assertTrue(normalZ(triangles.get(4)) < 0F);
        assertTrue(normalZ(triangles.get(6)) > 0F);

        for (int index = 0; index < triangles.size(); index++) {
            ChainRibbonGeometry.Triangle triangle = triangles.get(index);
            for (ChainRibbonGeometry.Vertex vertex : vertices(triangle)) {
                assertTrue(vertex.v() >= 0F && vertex.v() <= 1F);
                if (index % 8 < 4) {
                    assertTrue(vertex.u() == ChainRibbonGeometry.FIRST_U_MIN
                            || vertex.u() == ChainRibbonGeometry.FIRST_U_MAX);
                } else {
                    assertTrue(vertex.u() == ChainRibbonGeometry.SECOND_U_MIN
                            || vertex.u() == ChainRibbonGeometry.SECOND_U_MAX);
                }
            }
        }
    }

    @Test
    void invalidLengthsFailSoft() {
        for (float length : new float[]{
                0F, -1F, Float.NaN, Float.POSITIVE_INFINITY
        }) {
            assertTrue(ChainRibbonGeometry.spans(length).isEmpty());
            assertTrue(ChainRibbonGeometry.triangles(length).isEmpty());
        }
    }

    private static List<ChainRibbonGeometry.Vertex> vertices(
            ChainRibbonGeometry.Triangle triangle
    ) {
        return List.of(triangle.first(), triangle.second(), triangle.third());
    }

    private static float normalX(ChainRibbonGeometry.Triangle triangle) {
        return normal(triangle)[0];
    }

    private static float normalZ(ChainRibbonGeometry.Triangle triangle) {
        return normal(triangle)[2];
    }

    private static float[] normal(ChainRibbonGeometry.Triangle triangle) {
        ChainRibbonGeometry.Vertex a = triangle.first();
        ChainRibbonGeometry.Vertex b = triangle.second();
        ChainRibbonGeometry.Vertex c = triangle.third();
        float abx = b.x() - a.x();
        float aby = b.y() - a.y();
        float abz = b.z() - a.z();
        float acx = c.x() - a.x();
        float acy = c.y() - a.y();
        float acz = c.z() - a.z();
        return new float[]{
                aby * acz - abz * acy,
                abz * acx - abx * acz,
                abx * acy - aby * acx
        };
    }

    private static void assertSpan(
            ChainRibbonGeometry.Span span,
            float sourceY,
            float targetY,
            int period,
            float sourceV,
            float targetV
    ) {
        assertEquals(sourceY, span.sourceY(), EPSILON);
        assertEquals(targetY, span.targetY(), EPSILON);
        assertEquals(period, span.period());
        assertEquals(sourceV, span.sourceV(), EPSILON);
        assertEquals(targetV, span.targetV(), EPSILON);
    }

    private static void assertVertex(
            ChainRibbonGeometry.Vertex vertex,
            float x,
            float y,
            float z,
            float u,
            float v
    ) {
        assertEquals(x, vertex.x(), EPSILON);
        assertEquals(y, vertex.y(), EPSILON);
        assertEquals(z, vertex.z(), EPSILON);
        assertEquals(u, vertex.u(), EPSILON);
        assertEquals(v, vertex.v(), EPSILON);
    }
}
