/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.ArrayList;
import java.util.List;

/** Repeat-safe static chain mesh matching Create's custom ribbon renderer. */
final class ChainRibbonGeometry {

    static final float RADIUS = 1.5F / 16F;
    static final float FIRST_U_MIN = 0F;
    static final float FIRST_U_MAX = 3F / 16F;
    static final float SECOND_U_MIN = 3F / 16F;
    static final float SECOND_U_MAX = 6F / 16F;

    private static final double NEUTRAL_ANIMATION = -0.5D;

    private ChainRibbonGeometry() {
    }

    static List<Span> spans(float length) {
        if (!(length > 0F) || !Float.isFinite(length)) {
            return List.of();
        }
        ArrayList<Span> spans = new ArrayList<>((int) Math.ceil(length) + 1);
        float start = 0F;
        double rawAtStart = length + NEUTRAL_ANIMATION;
        int firstBoundary = (int) Math.ceil(rawAtStart) - 1;
        for (int boundary = firstBoundary; boundary >= 0; boundary--) {
            double split = length + NEUTRAL_ANIMATION - boundary;
            if (split <= 0D || split >= length) {
                continue;
            }
            float end = (float) split;
            spans.add(span(length, start, end));
            start = end;
        }
        spans.add(span(length, start, length));
        return List.copyOf(spans);
    }

    static List<Triangle> triangles(float length) {
        List<Span> spans = spans(length);
        if (spans.isEmpty()) {
            return List.of();
        }
        ArrayList<Triangle> triangles = new ArrayList<>(spans.size() * 8);
        for (Span span : spans) {
            plane(
                    triangles, span,
                    0.5F, 0.5F + RADIUS,
                    0.5F, 0.5F - RADIUS,
                    FIRST_U_MIN, FIRST_U_MAX
            );
            plane(
                    triangles, span,
                    0.5F + RADIUS, 0.5F,
                    0.5F - RADIUS, 0.5F,
                    SECOND_U_MIN, SECOND_U_MAX
            );
        }
        return List.copyOf(triangles);
    }

    private static Span span(float length, float sourceY, float targetY) {
        double sourceRawV = length + NEUTRAL_ANIMATION - sourceY;
        double targetRawV = length + NEUTRAL_ANIMATION - targetY;
        int period = (int) Math.floor((sourceRawV + targetRawV) * 0.5D);
        return new Span(
                sourceY, targetY, period,
                clamp((float) (sourceRawV - period)),
                clamp((float) (targetRawV - period))
        );
    }

    private static void plane(
            List<Triangle> target,
            Span span,
            float ax,
            float az,
            float bx,
            float bz,
            float uMin,
            float uMax
    ) {
        quad(target, span, ax, az, bx, bz, uMin, uMax);
        quad(target, span, bx, bz, ax, az, uMin, uMax);
    }

    private static void quad(
            List<Triangle> target,
            Span span,
            float ax,
            float az,
            float bx,
            float bz,
            float uMin,
            float uMax
    ) {
        Vertex topA = new Vertex(
                ax, span.targetY(), az, uMax, span.targetV()
        );
        Vertex bottomA = new Vertex(
                ax, span.sourceY(), az, uMax, span.sourceV()
        );
        Vertex bottomB = new Vertex(
                bx, span.sourceY(), bz, uMin, span.sourceV()
        );
        Vertex topB = new Vertex(
                bx, span.targetY(), bz, uMin, span.targetV()
        );
        target.add(new Triangle(topA, bottomA, bottomB));
        target.add(new Triangle(topA, bottomB, topB));
    }

    private static float clamp(float value) {
        return Math.max(0F, Math.min(1F, value));
    }

    record Span(
            float sourceY,
            float targetY,
            int period,
            float sourceV,
            float targetV
    ) {
    }

    record Vertex(float x, float y, float z, float u, float v) {
    }

    record Triangle(Vertex first, Vertex second, Vertex third) {
    }
}
