/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.janguenter.bluemap.create.adapter.bluemap522.PipeAttachmentSelector.Component.CONNECTION;
import static io.github.janguenter.bluemap.create.adapter.bluemap522.PipeAttachmentSelector.Component.DRAIN;
import static io.github.janguenter.bluemap.create.adapter.bluemap522.PipeAttachmentSelector.Component.RIM;
import static io.github.janguenter.bluemap.create.adapter.bluemap522.PipeAttachmentSelector.Component.RIM_CONNECTOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipeAttachmentSelectorTest {

    @Test
    void regularOpenEndsHaveFullRimsInAllSixDirections() {
        for (CreateDirection direction : CreateDirection.values()) {
            Map<String, String> properties = Map.of(name(direction), "true");
            assertEquals(
                    List.of(RIM_CONNECTOR, RIM),
                    select("create:fluid_pipe", properties, direction, "minecraft:air", Map.of())
            );
            assertTrue(PipeAttachmentSelector.hasPort(
                    "create:fluid_pipe", properties, direction
            ));
            assertFalse(PipeAttachmentSelector.hasPort(
                    "create:fluid_pipe", properties, direction.opposite()
            ));
        }
    }

    @Test
    void regularStraightConnectionDiffersFromCornerAndJunction() {
        Map<String, String> straight = ports(CreateDirection.NORTH, CreateDirection.SOUTH);
        Map<String, String> corner = ports(CreateDirection.NORTH, CreateDirection.EAST);
        Map<String, String> junction = ports(
                CreateDirection.NORTH, CreateDirection.SOUTH, CreateDirection.EAST
        );
        Map<String, String> northSouth = ports(CreateDirection.NORTH, CreateDirection.SOUTH);

        assertEquals(CreateDirection.Axis.Z, PipeAttachmentSelector.straightAxis(straight));
        assertNull(PipeAttachmentSelector.straightAxis(corner));
        assertNull(PipeAttachmentSelector.straightAxis(junction));
        assertEquals(List.of(CONNECTION), select(
                "create:fluid_pipe", straight, CreateDirection.NORTH,
                "create:fluid_pipe", northSouth
        ));
        assertEquals(List.of(RIM_CONNECTOR), select(
                "create:fluid_pipe", corner, CreateDirection.NORTH,
                "create:fluid_pipe", northSouth
        ));
        assertEquals(List.of(RIM_CONNECTOR), select(
                "create:fluid_pipe", junction, CreateDirection.NORTH,
                "create:fluid_pipe", northSouth
        ));
    }

    @Test
    void regularPipeCasingMatchesExactHighDegreeHubRule() {
        assertTrue(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", completePorts(CreateDirection.values())
        ));
        assertTrue(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", completePorts(
                        CreateDirection.NORTH, CreateDirection.SOUTH,
                        CreateDirection.EAST, CreateDirection.WEST
                )
        ));
        assertTrue(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", completePorts(
                        CreateDirection.NORTH, CreateDirection.SOUTH, CreateDirection.EAST
                )
        ));
        assertFalse(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", completePorts(
                        CreateDirection.NORTH, CreateDirection.EAST, CreateDirection.UP
                )
        ));
        assertFalse(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", completePorts(
                        CreateDirection.NORTH, CreateDirection.SOUTH
                )
        ));
        assertFalse(PipeAttachmentSelector.shouldRenderCasing(
                "create:encased_fluid_pipe", completePorts(CreateDirection.values())
        ));
    }

    @Test
    void regularPipeCasingFailsSoftForIncompleteOrMalformedStates() {
        assertFalse(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", null
        ));
        assertFalse(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", Map.of("north", "true")
        ));
        HashMap<String, String> malformed = new HashMap<>(
                completePorts(CreateDirection.values())
        );
        malformed.put("up", "maybe");
        assertFalse(PipeAttachmentSelector.shouldRenderCasing(
                "create:fluid_pipe", malformed
        ));
    }

    @Test
    void regularPipeCasingMatchesExactPredicateForEveryPortMask() {
        CreateDirection[] directions = CreateDirection.values();
        for (int mask = 0; mask < 1 << directions.length; mask++) {
            HashMap<String, String> properties = new HashMap<>();
            for (int index = 0; index < directions.length; index++) {
                properties.put(
                        name(directions[index]),
                        Boolean.toString((mask & 1 << index) != 0)
                );
            }

            boolean expected = false;
            for (CreateDirection.Axis axis : CreateDirection.Axis.values()) {
                int perpendicularPorts = 0;
                for (int index = 0; index < directions.length; index++) {
                    if (directions[index].axis() != axis && (mask & 1 << index) != 0) {
                        perpendicularPorts++;
                    }
                }
                expected |= perpendicularPorts > 2;
            }

            assertEquals(
                    expected,
                    PipeAttachmentSelector.shouldRenderCasing(
                            "create:fluid_pipe", properties
                    ),
                    "port mask " + Integer.toBinaryString(mask)
            );
        }
    }

    @Test
    void neighborBracketOnlyAllowsItsStraightAxisConnection() {
        Map<String, String> source = ports(CreateDirection.NORTH, CreateDirection.SOUTH);
        Map<String, String> bracketedCorner = ports(
                CreateDirection.SOUTH, CreateDirection.EAST
        );
        Map<String, String> bracketedStraight = ports(
                CreateDirection.NORTH, CreateDirection.SOUTH
        );
        assertEquals(List.of(RIM_CONNECTOR, RIM), select(
                "create:fluid_pipe", source, CreateDirection.NORTH,
                "create:fluid_pipe", bracketedCorner, true
        ));
        assertEquals(List.of(CONNECTION), select(
                "create:fluid_pipe", source, CreateDirection.NORTH,
                "create:fluid_pipe", bracketedStraight, true
        ));
    }

    @Test
    void regularToSpecialPipeUsesExactAsymmetricEndParts() {
        Map<String, String> straight = ports(CreateDirection.NORTH, CreateDirection.SOUTH);

        assertEquals(List.of(RIM_CONNECTOR, RIM), select(
                "create:fluid_pipe", straight, CreateDirection.NORTH,
                "create:glass_fluid_pipe", Map.of("axis", "z")
        ));
        assertEquals(List.of(RIM_CONNECTOR, RIM), select(
                "create:fluid_pipe", straight, CreateDirection.NORTH,
                "create:encased_fluid_pipe", Map.of("south", "true")
        ));
        assertEquals(List.of(RIM_CONNECTOR), select(
                "create:fluid_pipe", straight, CreateDirection.NORTH,
                "create:mechanical_pump", Map.of("facing", "south")
        ));
        assertEquals(List.of(), select(
                "create:glass_fluid_pipe", Map.of("axis", "z"), CreateDirection.NORTH,
                "create:fluid_pipe", straight
        ));
    }

    @Test
    void straightPipeAndPumpEndPoliciesMatchCreate() {
        assertEquals(List.of(RIM), select(
                "create:glass_fluid_pipe", Map.of("axis", "x"), CreateDirection.EAST,
                "minecraft:air", Map.of()
        ));
        assertEquals(List.of(RIM), select(
                "create:smart_fluid_pipe", Map.of("face", "wall", "facing", "north"),
                CreateDirection.UP, "minecraft:air", Map.of()
        ));
        assertEquals(List.of(), select(
                "create:encased_fluid_pipe", Map.of("east", "true"), CreateDirection.EAST,
                "minecraft:air", Map.of()
        ));
        assertEquals(List.of(RIM_CONNECTOR, DRAIN), select(
                "create:mechanical_pump", Map.of("facing", "east"), CreateDirection.WEST,
                "create:fluid_tank", Map.of()
        ));
        assertEquals(List.of(), select(
                "create:mechanical_pump", Map.of("facing", "east"), CreateDirection.EAST,
                "minecraft:air", Map.of()
        ));
        assertEquals(List.of(RIM_CONNECTOR, RIM), select(
                "create:fluid_pipe", Map.of("east", "true"), CreateDirection.EAST,
                "create:hose_pulley", Map.of()
        ));
        assertEquals(List.of(RIM), select(
                "create:glass_fluid_pipe", Map.of("axis", "x"), CreateDirection.EAST,
                "create:encased_fluid_pipe", Map.of("west", "true")
        ));
        assertEquals(List.of(), select(
                "create:glass_fluid_pipe", Map.of("axis", "x"), CreateDirection.EAST,
                "create:mechanical_pump", Map.of("facing", "west")
        ));
        assertEquals(List.of(RIM), select(
                "create:glass_fluid_pipe", Map.of("axis", "x"), CreateDirection.EAST,
                "create:mechanical_pump", Map.of("facing", "east")
        ));
        assertEquals(List.of(RIM), select(
                "create:smart_fluid_pipe",
                Map.of("face", "floor", "facing", "east"), CreateDirection.EAST,
                "create:glass_fluid_pipe", Map.of("axis", "x")
        ));
    }

    @Test
    void oneSidedCreateCapabilitiesOnlyDrainOnExposedSides() {
        assertEquals(List.of(RIM_CONNECTOR, RIM), select(
                "create:fluid_pipe", Map.of("down", "true"), CreateDirection.DOWN,
                "create:item_drain", Map.of()
        ));
        assertEquals(List.of(RIM_CONNECTOR, DRAIN), select(
                "create:fluid_pipe", Map.of("north", "true"), CreateDirection.NORTH,
                "create:item_drain", Map.of()
        ));
        assertEquals(List.of(RIM), select(
                "create:glass_fluid_pipe", Map.of("axis", "y"), CreateDirection.UP,
                "create:spout", Map.of()
        ));
        assertEquals(List.of(DRAIN), select(
                "create:glass_fluid_pipe", Map.of("axis", "y"), CreateDirection.DOWN,
                "create:spout", Map.of()
        ));
        assertEquals(List.of(RIM_CONNECTOR, RIM), select(
                "create:fluid_pipe", Map.of("east", "true"), CreateDirection.EAST,
                "minecraft:cauldron", Map.of()
        ));
    }

    @Test
    void portGeometryCoversGlassSmartValveAndPump() {
        for (CreateDirection direction : CreateDirection.values()) {
            assertEquals(direction.axis() == CreateDirection.Axis.X,
                    PipeAttachmentSelector.hasPort(
                            "create:glass_fluid_pipe", Map.of("axis", "x"), direction
                    ));
            assertEquals(direction.axis() == CreateDirection.Axis.Y,
                    PipeAttachmentSelector.hasPort(
                            "create:smart_fluid_pipe",
                            Map.of("face", "wall", "facing", "north"), direction
                    ));
            assertEquals(direction.axis() == CreateDirection.Axis.Z,
                    PipeAttachmentSelector.hasPort(
                            "create:mechanical_pump", Map.of("facing", "south"), direction
                    ));
            assertEquals(direction.axis() == CreateDirection.Axis.X,
                    PipeAttachmentSelector.hasPort(
                            "create:fluid_valve",
                            Map.of("facing", "north", "axis_along_first", "false"),
                            direction
                    ));
        }
    }

    @Test
    void malformedStatesFailSoft() {
        assertEquals(List.of(), PipeAttachmentSelector.select(
                "create:fluid_pipe", null, CreateDirection.UP,
                new PipeAttachmentSelector.Neighbor("minecraft:air", Map.of())
        ));
        assertFalse(PipeAttachmentSelector.hasPort(
                "create:fluid_valve", Map.of("axis_along_first", "maybe"), CreateDirection.UP
        ));
        assertFalse(PipeAttachmentSelector.hasPort(null, Map.of(), CreateDirection.UP));
    }

    private static List<PipeAttachmentSelector.Component> select(
            String blockId,
            Map<String, String> properties,
            CreateDirection direction,
            String neighborId,
            Map<String, String> neighborProperties
    ) {
        return select(
                blockId, properties, direction, neighborId, neighborProperties, false
        );
    }

    private static List<PipeAttachmentSelector.Component> select(
            String blockId,
            Map<String, String> properties,
            CreateDirection direction,
            String neighborId,
            Map<String, String> neighborProperties,
            boolean bracketPresent
    ) {
        return PipeAttachmentSelector.select(
                blockId, properties, direction,
                new PipeAttachmentSelector.Neighbor(
                        neighborId, neighborProperties, bracketPresent
                )
        );
    }

    private static Map<String, String> ports(CreateDirection... directions) {
        HashMap<String, String> properties = new HashMap<>();
        for (CreateDirection direction : directions) {
            properties.put(name(direction), "true");
        }
        return properties;
    }

    private static Map<String, String> completePorts(CreateDirection... directions) {
        HashMap<String, String> properties = new HashMap<>();
        for (CreateDirection direction : CreateDirection.values()) {
            properties.put(name(direction), "false");
        }
        for (CreateDirection direction : directions) {
            properties.put(name(direction), "true");
        }
        return properties;
    }

    private static String name(CreateDirection direction) {
        return direction.name().toLowerCase(java.util.Locale.ROOT);
    }
}
