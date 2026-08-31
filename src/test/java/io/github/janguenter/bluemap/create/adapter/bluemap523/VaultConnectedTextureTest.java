/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

class VaultConnectedTextureTest {

    private static final float EPSILON = 1E-6F;

    @Test
    void rectangleSelectorAndUvProjectionCoverEntireFourByFourSheet() {
        HashSet<Integer> cells = new HashSet<>();
        for (int mask = 0; mask < 16; mask++) {
            cells.add(VaultConnectedTexture.index(new VaultConnectedTexture.Context(
                    (mask & 1) != 0,
                    (mask & 2) != 0,
                    (mask & 4) != 0,
                    (mask & 8) != 0
            )));
        }
        assertEquals(16, cells.size());
        assertEquals(6, VaultConnectedTexture.index(
                new VaultConnectedTexture.Context(true, true, true, true)
        ));
        VaultConnectedTexture.Uv uv = VaultConnectedTexture.connectedUv(
                11, 0.25F, 0.75F
        );
        assertEquals((3.25F) / 4F, uv.u(), EPSILON);
        assertEquals((2.75F) / 4F, uv.v(), EPSILON);
    }

    @Test
    void materialSelectionUsesWorldFaceAndMediumOrLargeSheet() {
        assertMaterial("z", false, CreateDirection.NORTH, "front_medium");
        assertMaterial("z", true, CreateDirection.SOUTH, "front_large");
        assertMaterial("z", false, CreateDirection.UP, "top_medium");
        assertMaterial("z", true, CreateDirection.DOWN, "bottom_large");
        assertMaterial("z", false, CreateDirection.WEST, "side_medium");
        assertMaterial("x", true, CreateDirection.EAST, "front_large");
        assertMaterial("x", false, CreateDirection.NORTH, "side_medium");
        assertFalse(VaultConnectedTexture.material(
                "x", false, CreateDirection.EAST,
                "create:block/vault/vault_side_small"
        ).isPresent());
    }

    @Test
    void exactFramesCoverBothVaultAxesAndAllFaces() {
        assertFrame("z", CreateDirection.NORTH, CreateDirection.UP, CreateDirection.WEST);
        assertFrame("z", CreateDirection.SOUTH, CreateDirection.UP, CreateDirection.EAST);
        assertFrame("z", CreateDirection.UP, CreateDirection.NORTH, CreateDirection.EAST);
        assertFrame("z", CreateDirection.DOWN, CreateDirection.SOUTH, CreateDirection.EAST);
        assertFrame("z", CreateDirection.WEST, CreateDirection.NORTH, CreateDirection.DOWN);
        assertFrame("z", CreateDirection.EAST, CreateDirection.NORTH, CreateDirection.DOWN);

        assertFrame("x", CreateDirection.WEST, CreateDirection.UP, CreateDirection.SOUTH);
        assertFrame("x", CreateDirection.EAST, CreateDirection.UP, CreateDirection.NORTH);
        assertFrame("x", CreateDirection.UP, CreateDirection.EAST, CreateDirection.SOUTH);
        assertFrame("x", CreateDirection.DOWN, CreateDirection.WEST, CreateDirection.SOUTH);
        assertFrame("x", CreateDirection.NORTH, CreateDirection.EAST, CreateDirection.DOWN);
        assertFrame("x", CreateDirection.SOUTH, CreateDirection.EAST, CreateDirection.DOWN);
    }

    @Test
    void connectivityRequiresSameStateShapeAndController() {
        VaultConnectedTexture.Position controller =
                new VaultConnectedTexture.Position(1, 2, 3);
        VaultConnectedTexture.GroupKey first = VaultConnectedTexture.group(
                "create:item_vault", Map.of("axis", "z", "large", "false"), controller
        ).orElseThrow();
        VaultConnectedTexture.GroupKey same = VaultConnectedTexture.group(
                "create:item_vault", Map.of("axis", "z", "large", "false"),
                new VaultConnectedTexture.Position(1, 2, 3)
        ).orElseThrow();
        VaultConnectedTexture.GroupKey otherAxis = VaultConnectedTexture.group(
                "create:item_vault", Map.of("axis", "x", "large", "false"), controller
        ).orElseThrow();
        assertTrue(VaultConnectedTexture.sameGroup(first, same));
        assertFalse(VaultConnectedTexture.sameGroup(first, otherAxis));
        assertFalse(VaultConnectedTexture.sameGroup(first, null));
    }

    @Test
    void blueNbtDecodesAbsoluteControllerAndOwnFallback() throws IOException {
        VaultBlockEntityData member = decodeController(10, 20, 30);
        VaultBlockEntityData controller = decodeWithoutController();
        assertEquals(
                new VaultConnectedTexture.Position(10, 20, 30),
                member.effectiveController(11, 21, 31)
        );
        assertEquals(
                new VaultConnectedTexture.Position(4, 5, 6),
                controller.effectiveController(4, 5, 6)
        );
    }

    private static VaultBlockEntityData decodeController(int x, int y, int z)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("Controller").value(new int[]{x, y, z});
            writer.endCompound();
        }
        return new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()), VaultBlockEntityData.class
        );
    }

    private static VaultBlockEntityData decodeWithoutController() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.endCompound();
        }
        return new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()), VaultBlockEntityData.class
        );
    }

    private static void assertMaterial(
            String axis,
            boolean large,
            CreateDirection face,
            String suffix
    ) {
        String surface = suffix.substring(0, suffix.indexOf('_'));
        assertEquals(
                "create:block/vault/vault_" + suffix,
                VaultConnectedTexture.material(
                        axis, large, face,
                        "create:block/vault/vault_" + surface + "_small"
                ).orElseThrow().connectedTexture()
        );
    }

    private static void assertFrame(
            String axis,
            CreateDirection face,
            CreateDirection up,
            CreateDirection right
    ) {
        VaultConnectedTexture.Frame frame = VaultConnectedTexture.frame(
                axis, face
        ).orElseThrow();
        assertEquals(up, frame.up());
        assertEquals(right, frame.right());
    }
}
