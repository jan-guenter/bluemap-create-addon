/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.List;
import java.util.EnumMap;
import java.util.Map;

/** Stable-appearance projection of Create's persisted pipe ports and rim rules. */
final class PipeAttachmentSelector {

    private PipeAttachmentSelector() {
    }

    /** Mirrors {@code FluidPipeBlock.shouldDrawCasing} for the regular pipe. */
    static boolean shouldRenderCasing(
            String blockId,
            Map<String, String> properties
    ) {
        if (!"create:fluid_pipe".equals(blockId) || properties == null) {
            return false;
        }

        EnumMap<CreateDirection, Boolean> ports = new EnumMap<>(CreateDirection.class);
        for (CreateDirection direction : CreateDirection.values()) {
            String value = properties.get(name(direction));
            if (!"true".equals(value) && !"false".equals(value)) {
                return false;
            }
            ports.put(direction, "true".equals(value));
        }

        for (CreateDirection.Axis axis : CreateDirection.Axis.values()) {
            int connections = 0;
            for (CreateDirection direction : CreateDirection.values()) {
                if (direction.axis() != axis && ports.get(direction)) {
                    connections++;
                }
            }
            if (connections > 2) {
                return true;
            }
        }
        return false;
    }

    static List<Component> select(
            String blockId,
            Map<String, String> properties,
            CreateDirection direction,
            Neighbor neighbor
    ) {
        if (blockId == null || properties == null || direction == null || neighbor == null) {
            return List.of();
        }
        if (!hasPort(blockId, properties, direction)) {
            return List.of();
        }
        String neighborId = neighbor.id();
        if ("create:encased_fluid_pipe".equals(blockId)) {
            return obviousContainer(neighborId, direction)
                    ? List.of(Component.RIM_CONNECTOR, Component.DRAIN) : List.of();
        }
        if (isStraightPipe(blockId)) {
            return selectStraight(blockId, properties, direction, neighbor);
        }
        if ("create:mechanical_pump".equals(blockId)) {
            return obviousContainer(neighborId, direction)
                    ? List.of(Component.RIM_CONNECTOR, Component.DRAIN) : List.of();
        }
        if ("create:fluid_pipe".equals(blockId)) {
            if ("create:mechanical_pump".equals(neighborId)
                    && hasPort(neighborId, neighbor.properties(), direction.opposite())) {
                return List.of(Component.RIM_CONNECTOR);
            }
            if ("create:fluid_pipe".equals(neighborId)
                    && (!neighbor.bracketPresent()
                        || straightAxis(neighbor.properties()) == direction.axis())) {
                return straightAxis(properties) == direction.axis()
                        ? List.of(Component.CONNECTION)
                        : List.of(Component.RIM_CONNECTOR);
            }
            if (("create:glass_fluid_pipe".equals(neighborId)
                    || "create:encased_fluid_pipe".equals(neighborId))
                    && hasPort(neighborId, neighbor.properties(), direction.opposite())) {
                return List.of(Component.RIM_CONNECTOR, Component.RIM);
            }
            if (("create:smart_fluid_pipe".equals(neighborId)
                    || "create:fluid_valve".equals(neighborId))
                    && hasPort(neighborId, neighbor.properties(), direction.opposite())) {
                return List.of(Component.RIM_CONNECTOR);
            }
            if (obviousContainer(neighborId, direction)) {
                return List.of(Component.RIM_CONNECTOR, Component.DRAIN);
            }
            return List.of(Component.RIM_CONNECTOR, Component.RIM);
        }
        return List.of();
    }

    static boolean hasPort(
            String blockId,
            Map<String, String> properties,
            CreateDirection direction
    ) {
        if (blockId == null || properties == null || direction == null) {
            return false;
        }
        if ("create:fluid_pipe".equals(blockId)
                || "create:encased_fluid_pipe".equals(blockId)) {
            return "true".equals(properties.get(direction.name().toLowerCase(
                    java.util.Locale.ROOT
            )));
        }
        if ("create:glass_fluid_pipe".equals(blockId)) {
            return direction.axis().name().equalsIgnoreCase(properties.get("axis"));
        }
        if ("create:smart_fluid_pipe".equals(blockId)) {
            String face = properties.get("face");
            if ("wall".equals(face)) {
                return direction.axis() == CreateDirection.Axis.Y;
            }
            return CreateDirection.parse(properties.get("facing"))
                    .filter(CreateDirection::horizontal)
                    .map(facing -> facing.axis() == direction.axis())
                    .orElse(false);
        }
        if ("create:fluid_valve".equals(blockId)) {
            CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                    .orElse(null);
            String alongFirst = properties.get("axis_along_first");
            if (facing == null || !("true".equals(alongFirst)
                    || "false".equals(alongFirst))) {
                return false;
            }
            java.util.ArrayList<CreateDirection.Axis> remaining = new java.util.ArrayList<>(2);
            for (CreateDirection.Axis axis : CreateDirection.Axis.values()) {
                if (axis != facing.axis()) {
                    remaining.add(axis);
                }
            }
            CreateDirection.Axis pipeAxis = remaining.get(
                    "true".equals(alongFirst) ? 1 : 0
            );
            return direction.axis() == pipeAxis;
        }
        if ("create:mechanical_pump".equals(blockId)) {
            return CreateDirection.parse(properties.get("facing"))
                    .map(facing -> facing.axis() == direction.axis())
                    .orElse(false);
        }
        return false;
    }

    static CreateDirection.Axis straightAxis(Map<String, String> properties) {
        CreateDirection.Axis found = null;
        int connections = 0;
        for (CreateDirection.Axis axis : CreateDirection.Axis.values()) {
            CreateDirection negative = negative(axis);
            CreateDirection positive = positive(axis);
            boolean atNegative = "true".equals(properties.get(name(negative)));
            boolean atPositive = "true".equals(properties.get(name(positive)));
            if (atNegative) {
                connections++;
            }
            if (atPositive) {
                connections++;
            }
            if (atNegative && atPositive) {
                if (found != null) {
                    return null;
                }
                found = axis;
            }
        }
        return connections == 2 ? found : null;
    }

    private static boolean isStraightPipe(String blockId) {
        return "create:glass_fluid_pipe".equals(blockId)
                || "create:smart_fluid_pipe".equals(blockId)
                || "create:fluid_valve".equals(blockId);
    }

    private static List<Component> selectStraight(
            String blockId,
            Map<String, String> properties,
            CreateDirection direction,
            Neighbor neighbor
    ) {
        String neighborId = neighbor.id();
        boolean exactPumpConnection = "create:mechanical_pump".equals(neighborId)
                && CreateDirection.parse(neighbor.properties().get("facing"))
                .filter(facing -> facing == direction.opposite())
                .isPresent();
        if (exactPumpConnection) {
            return List.of();
        }
        if (obviousContainer(neighborId, direction)) {
            return List.of(Component.DRAIN);
        }
        if ("create:fluid_valve".equals(blockId)) {
            return List.of();
        }
        if (!"create:glass_fluid_pipe".equals(blockId)
                && "create:glass_fluid_pipe".equals(neighborId)) {
            return List.of(Component.RIM);
        }
        if ("create:fluid_pipe".equals(neighborId)) {
            return List.of();
        }
        CreateDirection.Axis ownAxis = pipeAxis(blockId, properties);
        CreateDirection.Axis otherAxis = pipeAxis(neighborId, neighbor.properties());
        if (ownAxis != null && ownAxis == otherAxis) {
            return List.of();
        }
        if ("create:fluid_valve".equals(neighborId)
                && otherAxis == direction.axis()) {
            return List.of();
        }
        return List.of(Component.RIM);
    }

    private static CreateDirection.Axis pipeAxis(
            String blockId,
            Map<String, String> properties
    ) {
        if ("create:glass_fluid_pipe".equals(blockId)) {
            return CreateDirection.parseAxis(properties.get("axis")).orElse(null);
        }
        if ("create:smart_fluid_pipe".equals(blockId)) {
            if ("wall".equals(properties.get("face"))) {
                return CreateDirection.Axis.Y;
            }
            return CreateDirection.parse(properties.get("facing"))
                    .map(CreateDirection::axis).orElse(null);
        }
        if ("create:fluid_valve".equals(blockId)) {
            CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                    .orElse(null);
            String alongFirst = properties.get("axis_along_first");
            if (facing == null || !("true".equals(alongFirst)
                    || "false".equals(alongFirst))) {
                return null;
            }
            java.util.ArrayList<CreateDirection.Axis> remaining = new java.util.ArrayList<>(2);
            for (CreateDirection.Axis axis : CreateDirection.Axis.values()) {
                if (axis != facing.axis()) {
                    remaining.add(axis);
                }
            }
            return remaining.get("true".equals(alongFirst) ? 1 : 0);
        }
        return null;
    }

    private static String name(CreateDirection direction) {
        return direction.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static CreateDirection negative(CreateDirection.Axis axis) {
        return switch (axis) {
            case X -> CreateDirection.WEST;
            case Y -> CreateDirection.DOWN;
            case Z -> CreateDirection.NORTH;
        };
    }

    private static CreateDirection positive(CreateDirection.Axis axis) {
        return switch (axis) {
            case X -> CreateDirection.EAST;
            case Y -> CreateDirection.UP;
            case Z -> CreateDirection.SOUTH;
        };
    }

    private static boolean obviousContainer(String id, CreateDirection direction) {
        if (id.contains("item_drain")) {
            return direction != CreateDirection.DOWN;
        }
        if (id.contains("spout")) {
            return direction != CreateDirection.UP;
        }
        return id.contains("tank") || id.contains("basin")
                || id.contains("portable_fluid_interface");
    }

    enum Component {
        CONNECTION("connection"),
        RIM_CONNECTOR("rim_connector"),
        RIM("rim"),
        DRAIN("drain");

        private final String modelName;

        Component(String modelName) {
            this.modelName = modelName;
        }

        String modelName() {
            return modelName;
        }
    }

    record Neighbor(String id, Map<String, String> properties, boolean bracketPresent) {
        Neighbor {
            if (id == null) {
                throw new IllegalArgumentException("neighbor id is required");
            }
            properties = properties == null ? Map.of() : Map.copyOf(properties);
        }

        Neighbor(String id, Map<String, String> properties) {
            this(id, properties, false);
        }
    }
}
