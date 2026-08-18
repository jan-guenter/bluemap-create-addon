/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Exact frozen ring geometry for one persisted Hypertube Bezier connection. */
record HypertubeRenderPlan(List<Ring> rings, int tubeSegments) {

    private static final float UP_ALIGNMENT_THRESHOLD = 0.999F;
    private static final float REUSE_ORTHOGONAL_THRESHOLD = 0.1F;

    HypertubeRenderPlan {
        rings = List.copyOf(rings);
    }

    static Optional<HypertubeRenderPlan> select(Curve curve) {
        if (curve == null || curve.tubeSegments() < 1 || curve.points().size() < 2) {
            return Optional.empty();
        }
        ArrayList<Ring> rings = new ArrayList<>(curve.points().size());
        Point lastA = null;
        Point lastB = null;
        for (int index = 0; index < curve.points().size(); index++) {
            Point center = curve.points().get(index);
            Point tangent = (index == curve.points().size() - 1
                    ? center.subtract(curve.points().get(index - 1))
                    : curve.points().get(index + 1).subtract(center)).normalized();
            if (tangent == null) {
                return Optional.empty();
            }
            Frame frame = stableFrame(tangent, lastA, lastB);
            if (frame == null) {
                return Optional.empty();
            }
            rings.add(new Ring(
                    center,
                    offsets(frame, 0.7F),
                    offsets(frame, 0.62F),
                    offsets(frame, 0.69F),
                    tangent
            ));
            lastA = frame.a();
            lastB = frame.b();
        }
        return Optional.of(new HypertubeRenderPlan(rings, curve.tubeSegments()));
    }

    /** Tube glass is intentionally thinned by tubeSegments in exact 0.6.0. */
    boolean tubeInterval(int index) {
        return interval(index) && index % tubeSegments == 0
                && (tubeSegments <= 1 || index != 0 && index <= rings.size() - 3);
    }

    /** The pale longitudinal line is emitted for every non-degenerate interval. */
    boolean lineInterval(int index) {
        return interval(index);
    }

    int triangleCount() {
        int triangles = 0;
        for (int index = 0; index < rings.size() - 1; index++) {
            if (tubeInterval(index)) {
                triangles += 24;
            }
            if (lineInterval(index)) {
                triangles += 16;
            }
        }
        return triangles;
    }

    private boolean interval(int index) {
        return index >= 0 && index < rings.size() - 1
                && rings.get(index + 1).center()
                .subtract(rings.get(index).center()).lengthSquared() > 1.0E-12F;
    }

    private static Frame stableFrame(Point tangent, Point lastA, Point lastB) {
        Point up = new Point(0F, 1F, 0F);
        if (Math.abs(tangent.dot(up)) > UP_ALIGNMENT_THRESHOLD) {
            if (lastA != null && lastB != null
                    && Math.abs(tangent.dot(lastA)) <= REUSE_ORTHOGONAL_THRESHOLD
                    && Math.abs(tangent.dot(lastB)) <= REUSE_ORTHOGONAL_THRESHOLD) {
                return new Frame(lastA, lastB);
            }
            Point x = new Point(1F, 0F, 0F);
            Point z = new Point(0F, 0F, 1F);
            Point axis = Math.abs(tangent.dot(x)) < Math.abs(tangent.dot(z)) ? x : z;
            Point a = tangent.cross(axis).normalized();
            Point b = a == null ? null : tangent.cross(a).normalized();
            return a == null || b == null ? null : new Frame(a, b);
        }
        Point projected = up.subtract(tangent.scale(tangent.dot(up))).normalized();
        if (projected == null) {
            return null;
        }
        if (lastA != null && projected.dot(lastA) < 0F) {
            projected = projected.scale(-1F);
        }
        Point b = tangent.cross(projected).normalized();
        return b == null ? null : new Frame(projected, b);
    }

    private static List<Point> offsets(Frame frame, float radius) {
        ArrayList<Point> offsets = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) {
            double angle = index * Math.PI * 0.5D + Math.PI * 0.25D;
            offsets.add(frame.a().scale((float) Math.cos(angle) * radius)
                    .add(frame.b().scale((float) Math.sin(angle) * radius)));
        }
        return List.copyOf(offsets);
    }

    record Curve(List<Point> points, int tubeSegments) {
        Curve {
            points = List.copyOf(points);
        }
    }

    record Ring(
            Point center,
            List<Point> exterior,
            List<Point> interior,
            List<Point> line,
            Point tangent
    ) {
        Ring {
            exterior = List.copyOf(exterior);
            interior = List.copyOf(interior);
            line = List.copyOf(line);
        }
    }

    record Point(float x, float y, float z) {

        Point add(Point other) {
            return new Point(x + other.x, y + other.y, z + other.z);
        }

        Point subtract(Point other) {
            return new Point(x - other.x, y - other.y, z - other.z);
        }

        Point scale(float factor) {
            return new Point(x * factor, y * factor, z * factor);
        }

        float dot(Point other) {
            return x * other.x + y * other.y + z * other.z;
        }

        Point cross(Point other) {
            return new Point(
                    y * other.z - z * other.y,
                    z * other.x - x * other.z,
                    x * other.y - y * other.x
            );
        }

        float lengthSquared() {
            return dot(this);
        }

        Point normalized() {
            float lengthSquared = lengthSquared();
            if (!(lengthSquared > 1.0E-12F) || !Float.isFinite(lengthSquared)) {
                return null;
            }
            return scale((float) (1D / Math.sqrt(lengthSquared)));
        }
    }

    private record Frame(Point a, Point b) {
    }
}
