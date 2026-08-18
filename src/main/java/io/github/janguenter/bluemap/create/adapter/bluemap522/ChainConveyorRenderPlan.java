/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.ArrayList;
import java.util.List;

/** Exact neutral guards and directed physical chain strands. */
record ChainConveyorRenderPlan(List<Connection> connections) {

    private static final float TANGENT = 1.25F;

    ChainConveyorRenderPlan {
        connections = List.copyOf(connections);
    }

    static ChainConveyorRenderPlan select(List<Offset> offsets) {
        return select(offsets, 0F);
    }

    static ChainConveyorRenderPlan select(List<Offset> offsets, float speed) {
        if (offsets == null || offsets.isEmpty()) {
            return new ChainConveyorRenderPlan(List.of());
        }
        boolean reversed = Float.isFinite(speed) && speed < 0F;
        ArrayList<Connection> selected = new ArrayList<>(offsets.size());
        for (Offset offset : offsets) {
            if (offset == null || !offset.renderable()) {
                continue;
            }
            double direction = Math.toDegrees(Math.atan2(offset.x(), offset.z()));
            double startAngle = direction + (reversed ? 35D : -35D);
            double targetAngle = direction + (reversed ? 145D : 215D);
            Point start = tangent(0F, 0F, 0F, startAngle);
            Point end = tangent(
                    offset.x(), offset.y(), offset.z(), targetAngle
            );
            float dx = end.x() - start.x();
            float dy = end.y() - start.y();
            float dz = end.z() - start.z();
            float horizontal = (float) Math.hypot(dx, dz);
            float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (!(length > 0F) || !Float.isFinite(length)) {
                continue;
            }
            float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
            float pitch = (float) Math.toDegrees(Math.atan2(dy, horizontal));
            AffineTransform guard = AffineTransform.identity()
                    .centered().rotateY((float) direction).uncentered();
            AffineTransform strand = AffineTransform.identity()
                    .centered()
                    .translate(start.x() - 0.5F, start.y() - 0.5F,
                            start.z() - 0.5F)
                    .rotateY(yaw)
                    .rotateX(90F - pitch)
                    .rotateY(45F)
                    .translate(0F, 0.5F, 0F)
                    .uncentered();
            selected.add(new Connection(offset, guard, strand, length));
        }
        return new ChainConveyorRenderPlan(selected);
    }

    private static Point tangent(float x, float y, float z, double degrees) {
        double radians = Math.toRadians(degrees);
        return new Point(
                x + 0.5F + (float) Math.sin(radians) * TANGENT,
                y + 0.375F,
                z + 0.5F + (float) Math.cos(radians) * TANGENT
        );
    }

    record Offset(int x, int y, int z) {

        boolean isZero() {
            return x == 0 && y == 0 && z == 0;
        }

        boolean bounded() {
            return !isZero() && Math.abs((long) x) <= 256L
                    && Math.abs((long) y) <= 256L
                    && Math.abs((long) z) <= 256L;
        }

        boolean renderable() {
            if (!bounded()) {
                return false;
            }
            double distance = Math.sqrt((double) x * x + (double) y * y
                    + (double) z * z);
            if (distance < 2.5D) {
                return false;
            }
            double horizontal = Math.hypot(x, z) - 1.5D;
            return horizontal > 0D && Math.abs((double) y) / horizontal <= 1D;
        }
    }

    record Connection(
            Offset offset,
            AffineTransform guard,
            AffineTransform strand,
            float length
    ) {
    }

    private record Point(float x, float y, float z) {
    }
}
