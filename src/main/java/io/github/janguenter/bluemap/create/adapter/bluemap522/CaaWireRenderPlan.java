/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Exact frozen 24-segment C&A wire curve and endpoint offsets. */
record CaaWireRenderPlan(
        List<Point> points,
        float width,
        boolean steep,
        float offsetX,
        float offsetZ,
        WireColor color
) {

    static final int SEGMENTS = 24;

    CaaWireRenderPlan {
        points = List.copyOf(points);
    }

    static Optional<CaaWireRenderPlan> select(
            Point start,
            Point end,
            float centerDistance,
            int type
    ) {
        WireColor color = WireColor.from(type).orElse(null);
        if (start == null || end == null || color == null
                || !Float.isFinite(centerDistance) || centerDistance <= 0F) {
            return Optional.empty();
        }
        float x = end.x() - start.x();
        float y = end.y() - start.y();
        float z = end.z() - start.z();
        boolean steep = Math.abs(x) + Math.abs(z) < Math.abs(y);
        float inverseHorizontal = (float) (1D / Math.sqrt(x * x + z * z));
        float factor = steep || !Float.isFinite(inverseHorizontal)
                ? 0F : inverseHorizontal * 0.025F / 2F;
        ArrayList<Point> points = new ArrayList<>(SEGMENTS + 1);
        for (int index = 0; index <= SEGMENTS; index++) {
            float fraction = index / (float) SEGMENTS;
            float curvedY = y > 0F
                    ? y * fraction * fraction
                    : y - y * (1F - fraction) * (1F - fraction);
            curvedY += (float) Math.sin(-fraction * Math.PI)
                    * (0.5F * centerDistance / 16F);
            points.add(new Point(
                    start.x() + x * fraction,
                    start.y() + curvedY,
                    start.z() + z * fraction
            ));
        }
        return Optional.of(new CaaWireRenderPlan(
                points, steep ? 0.015F : 0.0125F, steep,
                z * factor, x * factor, color
        ));
    }

    static Optional<Point> endpoint(
            String blockId,
            Map<String, String> properties,
            int node,
            int blockX,
            int blockY,
            int blockZ
    ) {
        Point local = localOffset(blockId, properties, node).orElse(null);
        if (local == null) {
            return Optional.empty();
        }
        return Optional.of(new Point(
                blockX + 0.5F + local.x(),
                blockY + 0.5F + local.y(),
                blockZ + 0.5F + local.z()
        ));
    }

    static Optional<Point> localOffset(
            String blockId,
            Map<String, String> properties,
            int node
    ) {
        CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                .orElse(null);
        if (facing == null) {
            return Optional.empty();
        }
        float scale = switch (blockId) {
            case "createaddition:connector",
                    "createaddition:small_light_connector" -> 0.1875F;
            case "createaddition:large_connector" -> 0.0625F;
            default -> Float.NaN;
        };
        if (Float.isFinite(scale)) {
            return Optional.of(new Point(
                    facing.x() * scale, facing.y() * scale, facing.z() * scale
            ));
        }
        if (!"createaddition:redstone_relay".equals(blockId)
                || !facing.horizontal() || node < 0 || node > 7) {
            return Optional.empty();
        }
        return Optional.of(relayOffset(
                facing, "true".equals(properties.get("vertical")), node > 3
        ));
    }

    private static Point relayOffset(
            CreateDirection facing,
            boolean vertical,
            boolean output
    ) {
        float five = 5F / 16F;
        float one = 1F / 16F;
        if (!vertical) {
            CreateDirection side = output ? facing : facing.opposite();
            return new Point(side.x() * five, -one, side.z() * five);
        }
        return switch (facing) {
            case NORTH -> new Point(output ? -five : five, 0F, -one);
            case WEST -> new Point(-one, 0F, output ? five : -five);
            case SOUTH -> new Point(output ? five : -five, 0F, one);
            case EAST -> new Point(one, 0F, output ? -five : five);
            default -> throw new IllegalArgumentException("vertical relay facing");
        };
    }

    record Offset(int x, int y, int z) {

        boolean bounded() {
            return (x != 0 || y != 0 || z != 0)
                    && Math.abs((long) x) <= 256L
                    && Math.abs((long) y) <= 256L
                    && Math.abs((long) z) <= 256L;
        }

        float distance() {
            return (float) Math.sqrt((double) x * x + (double) y * y
                    + (double) z * z);
        }
    }

    record Point(float x, float y, float z) {
    }

    record WireColor(float red, float green, float blue) {

        static Optional<WireColor> from(int type) {
            return switch (type) {
                case 0 -> Optional.of(rgb(78, 37, 30));
                case 1 -> Optional.of(rgb(98, 83, 29));
                case 2 -> Optional.of(rgb(88, 66, 37));
                case 3 -> Optional.of(rgb(26, 94, 12));
                default -> Optional.empty();
            };
        }

        private static WireColor rgb(int red, int green, int blue) {
            return new WireColor(red / 255F, green / 255F, blue / 255F);
        }
    }
}
