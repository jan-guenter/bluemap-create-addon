/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Exact neutral segmented geometry plan for one primary Create Bezier track. */
record TrackRenderPlan(
        double handleLength,
        double length,
        int segments,
        List<Piece> pieces
) {

    private static final double EPSILON = 1.0E-7D;

    TrackRenderPlan {
        pieces = List.copyOf(pieces);
    }

    static Optional<TrackRenderPlan> select(TrackBlockEntityData.Connection input) {
        if (input == null || !input.localPrimary() || !input.supportedMaterial()) {
            return Optional.empty();
        }
        Vector end1 = input.firstStart().add(new Vector(0D, 3D / 16D, 0D));
        Vector end2 = input.secondStart().add(new Vector(0D, 3D / 16D, 0D));
        Vector axis1 = input.firstAxis().normalized();
        Vector axis2 = input.secondAxis().normalized();
        Vector normal1 = input.firstNormal().normalized();
        Vector normal2 = input.secondNormal().normalized();
        if (axis1 == null || axis2 == null || normal1 == null || normal2 == null) {
            return Optional.empty();
        }
        double handle = determineHandle(end1, end2, axis1, axis2);
        if (!(handle > 0D) || !Double.isFinite(handle)) {
            return Optional.empty();
        }
        Vector finish1 = end1.add(axis1.scale(handle));
        Vector finish2 = end2.add(axis2.scale(handle));
        double length = computeLength(end1, end2, finish1, finish2, 16);
        int segments = (int) (length * 2D);
        if (segments < 1 || segments > 512) {
            return Optional.empty();
        }
        float[] lut = stepLut(end1, end2, finish1, finish2, length, segments);
        ArrayList<Sample> samples = new ArrayList<>(segments + 1);
        for (int index = 0; index <= segments; index++) {
            float t = index == segments ? 1F : index * lut[index] / segments;
            Vector position = bezier(end1, end2, finish1, finish2, t);
            Vector derivative = derivative(end1, end2, finish1, finish2, t)
                    .normalized();
            Vector faceNormal = near(normal1, normal2)
                    ? normal1 : slerp(normal1, normal2, t);
            Vector lateral = faceNormal == null || derivative == null
                    ? null : faceNormal.cross(derivative).normalized();
            if (faceNormal == null || derivative == null || lateral == null) {
                return Optional.empty();
            }
            samples.add(new Sample(position, derivative, faceNormal, lateral));
        }
        return Optional.of(new TrackRenderPlan(
                handle, length, segments,
                pieces(samples, input.girder())
        ));
    }

    private static List<Piece> pieces(List<Sample> samples, boolean girder) {
        ArrayList<Piece> pieces = new ArrayList<>();
        RailOffsets previousRails = null;
        GirderOffsets previousGirders = null;
        int count = samples.size() - 1;
        for (int index = 0; index <= count; index++) {
            Sample sample = samples.get(index);
            RailOffsets rails = new RailOffsets(
                    sample.position().add(sample.lateral().scale(0.965D)),
                    sample.position().subtract(sample.lateral().scale(0.965D))
            );
            GirderOffsets girders = girder ? girders(sample) : null;
            if (previousRails != null) {
                boolean end = index == count;
                Vector middle = rails.middle();
                Vector previousMiddle = previousRails.middle();
                Angles tie = angles(sample.lateral(), middle.subtract(previousMiddle));
                pieces.add(new Piece(
                        Kind.TRACK_TIE, "track_tie",
                        transform(previousMiddle, tie)
                                .translate(-0.5F, -2F / 16F - 1F / 256F, 0F)
                ));
                double railScale = end ? 2.2D : 2.1D;
                addRail(pieces, "track_left", previousRails.first(), rails.first(),
                        sample.lateral(), railScale);
                addRail(pieces, "track_right", previousRails.second(), rails.second(),
                        sample.lateral(), railScale);
                if (girder && previousGirders != null) {
                    addGirders(pieces, previousGirders, girders, sample.lateral(),
                            index, end ? 2.3D : 2.2D);
                }
            }
            previousRails = rails;
            previousGirders = girders;
        }
        return List.copyOf(pieces);
    }

    private static void addRail(
            List<Piece> pieces,
            String object,
            Vector previous,
            Vector current,
            Vector normal,
            double scale
    ) {
        Vector diff = current.subtract(previous);
        pieces.add(new Piece(
                Kind.TRACK_RAIL, object,
                transform(previous, angles(normal, diff))
                        .translate(0F, -2F / 16F - 1F / 256F, -1F / 32F)
                        .scale(1F, 1F, (float) (diff.length() * scale))
        ));
    }

    private static GirderOffsets girders(Sample sample) {
        Vector up = sample.derivative().cross(sample.lateral()).normalized();
        Vector first = up.scale(-8D / 16D);
        Vector second = up.scale(-10D / 16D);
        Vector leftTop = sample.position().add(sample.lateral()).add(first);
        Vector rightTop = sample.position().subtract(sample.lateral()).add(first);
        return new GirderOffsets(
                leftTop, rightTop, leftTop.add(second), rightTop.add(second)
        );
    }

    private static void addGirders(
            List<Piece> pieces,
            GirderOffsets previous,
            GirderOffsets current,
            Vector normal,
            int index,
            double scale
    ) {
        float yOffset = 2F / 16F
                + (index % 2 == 0 ? 1F : -1F) / 2048F - 1F / 1024F;
        for (boolean left : new boolean[]{true, false}) {
            Vector previousTop = left ? previous.leftTop() : previous.rightTop();
            Vector currentTop = left ? current.leftTop() : current.rightTop();
            Vector previousBottom = left ? previous.leftBottom() : previous.rightBottom();
            Vector currentBottom = left ? current.leftBottom() : current.rightBottom();
            Vector previousMiddle = previousTop.add(previousBottom).scale(0.5D);
            Vector currentMiddle = currentTop.add(currentBottom).scale(0.5D);
            addGirderPiece(pieces, "create:block/metal_girder/segment_middle",
                    previousMiddle, currentMiddle, normal, yOffset, scale);
            addGirderPiece(pieces, "create:block/metal_girder/segment_top",
                    previousTop, currentTop, normal, yOffset, scale);
            addGirderPiece(pieces, "create:block/metal_girder/segment_bottom",
                    previousBottom, currentBottom, normal, yOffset, scale);
        }
    }

    private static void addGirderPiece(
            List<Piece> pieces,
            String model,
            Vector previous,
            Vector current,
            Vector normal,
            float yOffset,
            double scale
    ) {
        Vector diff = current.subtract(previous);
        pieces.add(new Piece(
                Kind.JSON, model,
                transform(previous, angles(normal, diff))
                        .translate(0F, yOffset, -1F / 32F)
                        .scale(1F, 1F, (float) (diff.length() * scale))
        ));
    }

    private static AffineTransform transform(Vector position, Angles angles) {
        return AffineTransform.identity()
                .translate((float) position.x(), (float) position.y(), (float) position.z())
                .rotateY((float) Math.toDegrees(angles.yaw()))
                .rotateX((float) Math.toDegrees(angles.pitch()))
                .rotateZ((float) Math.toDegrees(angles.roll()));
    }

    static Angles angles(Vector normal, Vector diff) {
        double horizontal = Math.hypot(diff.x(), diff.z());
        double yaw = Math.atan2(diff.x(), diff.z());
        double pitch = Math.atan2(horizontal, diff.y()) - Math.PI * 0.5D;
        Vector yawPitchNormal = rotateY(
                rotateX(new Vector(0D, 1D, 0D), pitch), yaw
        );
        double sign = Math.signum(yawPitchNormal.dot(normal));
        if (Math.abs(sign) < 0.5D) {
            sign = yawPitchNormal.subtract(normal).lengthSquared() < 0.5D ? -1D : 1D;
        }
        Vector crossed = diff.cross(normal).normalized();
        double dot = crossed == null ? 1D : clamp(crossed.dot(yawPitchNormal));
        double roll = Math.acos(dot) * sign;
        return new Angles(pitch, yaw, roll);
    }

    private static double determineHandle(
            Vector end1,
            Vector end2,
            Vector axis1,
            Vector axis2
    ) {
        Vector up = new Vector(0D, 1D, 0D);
        Vector cross1 = axis1.cross(up);
        Vector cross2 = axis2.cross(up);
        double first = Math.atan2(-axis2.z(), -axis2.x());
        double second = Math.atan2(axis1.z(), axis1.x());
        double circle = Math.PI * 2D;
        double angle = (first - second + circle) % circle;
        if (Math.abs(circle - angle) < Math.abs(angle)) {
            angle = circle - angle;
        }
        if (Math.abs(angle) < 1.0E-5D) {
            double[] intersection = intersect(end1, end2, axis1, cross2);
            if (intersection != null) {
                double firstDistance = Math.abs(intersection[0]);
                double secondDistance = Math.abs(intersection[1]);
                double min = Math.min(firstDistance, secondDistance);
                double max = Math.max(firstDistance, secondDistance);
                if (min > 1.2D && max / min > 1D && max / min < 3D) {
                    return max - min;
                }
            }
            return end2.subtract(end1).length() / 3D;
        }
        double turns = circle / angle;
        double factor = 4D / 3D * Math.tan(Math.PI / (2D * turns));
        double[] intersection = intersect(end1, end2, cross1, cross2);
        if (intersection == null) {
            return end2.subtract(end1).length() / 3D;
        }
        double handle = Math.abs(intersection[1]) * factor;
        return Math.abs(handle) < 1.0E-5D ? 1D : handle;
    }

    private static double[] intersect(Vector first, Vector second, Vector a, Vector b) {
        double denominator = a.x() * b.z() - a.z() * b.x();
        if (Math.abs(denominator) < EPSILON) {
            return null;
        }
        Vector delta = second.subtract(first);
        double t = (delta.x() * b.z() - delta.z() * b.x()) / denominator;
        double u = (delta.x() * a.z() - delta.z() * a.x()) / denominator;
        return new double[]{t, u};
    }

    private static float[] stepLut(
            Vector end1,
            Vector end2,
            Vector finish1,
            Vector finish2,
            double length,
            int segments
    ) {
        float[] lut = new float[segments + 1];
        lut[0] = 1F;
        double combined = 0D;
        Vector previous = end1;
        for (int index = 0; index <= segments; index++) {
            float t = index / (float) segments;
            Vector point = bezier(end1, end2, finish1, finish2, t);
            if (index > 0) {
                combined += point.subtract(previous).length() / length;
                lut[index] = (float) (t / combined);
            }
            previous = point;
        }
        return lut;
    }

    private static double computeLength(
            Vector end1,
            Vector end2,
            Vector finish1,
            Vector finish2,
            int scan
    ) {
        double length = 0D;
        Vector previous = end1;
        for (int index = 0; index <= scan; index++) {
            Vector point = bezier(
                    end1, end2, finish1, finish2, index / (float) scan
            );
            length += point.subtract(previous).length();
            previous = point;
        }
        return length;
    }

    private static Vector bezier(
            Vector start,
            Vector end,
            Vector firstControl,
            Vector secondControl,
            float t
    ) {
        double inverse = 1D - t;
        return start.scale(inverse * inverse * inverse)
                .add(firstControl.scale(3D * inverse * inverse * t))
                .add(secondControl.scale(3D * inverse * t * t))
                .add(end.scale(t * t * t));
    }

    private static Vector derivative(
            Vector start,
            Vector end,
            Vector firstControl,
            Vector secondControl,
            float t
    ) {
        double inverse = 1D - t;
        return firstControl.subtract(start).scale(3D * inverse * inverse)
                .add(secondControl.subtract(firstControl).scale(6D * inverse * t))
                .add(end.subtract(secondControl).scale(3D * t * t));
    }

    private static Vector slerp(Vector first, Vector second, float t) {
        double dot = clamp(first.dot(second));
        double angle = Math.acos(dot);
        if (Math.abs(angle) < EPSILON) {
            return first;
        }
        double sine = Math.sin(angle);
        return first.scale(Math.sin((1D - t) * angle) / sine)
                .add(second.scale(Math.sin(t * angle) / sine))
                .normalized();
    }

    private static Vector rotateX(Vector vector, double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        return new Vector(vector.x(), vector.y() * cosine - vector.z() * sine,
                vector.y() * sine + vector.z() * cosine);
    }

    private static Vector rotateY(Vector vector, double angle) {
        double cosine = Math.cos(angle);
        double sine = Math.sin(angle);
        return new Vector(vector.x() * cosine + vector.z() * sine, vector.y(),
                -vector.x() * sine + vector.z() * cosine);
    }

    private static boolean near(Vector first, Vector second) {
        return first.subtract(second).lengthSquared() < EPSILON;
    }

    private static double clamp(double value) {
        return Math.max(-1D, Math.min(1D, value));
    }

    enum Kind {
        TRACK_TIE,
        TRACK_RAIL,
        JSON
    }

    record Piece(Kind kind, String resource, AffineTransform transform) {
    }

    record IntPoint(int x, int y, int z) {
        boolean isZero() {
            return x == 0 && y == 0 && z == 0;
        }

        IntPoint negated() {
            return new IntPoint(-x, -y, -z);
        }

        Vector vector() {
            return new Vector(x, y, z);
        }
    }

    record Vector(double x, double y, double z) {
        Vector add(Vector other) {
            return new Vector(x + other.x, y + other.y, z + other.z);
        }

        Vector subtract(Vector other) {
            return new Vector(x - other.x, y - other.y, z - other.z);
        }

        Vector scale(double scale) {
            return new Vector(x * scale, y * scale, z * scale);
        }

        double dot(Vector other) {
            return x * other.x + y * other.y + z * other.z;
        }

        Vector cross(Vector other) {
            return new Vector(y * other.z - z * other.y,
                    z * other.x - x * other.z,
                    x * other.y - y * other.x);
        }

        double lengthSquared() {
            return x * x + y * y + z * z;
        }

        double length() {
            return Math.sqrt(lengthSquared());
        }

        Vector normalized() {
            double length = length();
            return length > EPSILON && Double.isFinite(length)
                    ? scale(1D / length) : null;
        }
    }

    record Angles(double pitch, double yaw, double roll) {
    }

    private record Sample(
            Vector position,
            Vector derivative,
            Vector faceNormal,
            Vector lateral
    ) {
    }

    private record RailOffsets(Vector first, Vector second) {
        Vector middle() {
            return first.add(second).scale(0.5D);
        }
    }

    private record GirderOffsets(
            Vector leftTop,
            Vector rightTop,
            Vector leftBottom,
            Vector rightBottom
    ) {
    }
}
