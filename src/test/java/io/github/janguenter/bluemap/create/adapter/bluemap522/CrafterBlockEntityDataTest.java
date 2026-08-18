/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrafterBlockEntityDataTest {

    @Test
    void blueNbtDecodesControllerAndChildConnectedInputOffsets() throws IOException {
        CrafterBlockEntityData controller = decode(true, List.of(
                new Offset(0, 0, 0),
                new Offset(1, 0, 0),
                new Offset(0, 1, 0),
                new Offset(1, 1, 0)
        ));
        CrafterBlockEntityData child = decode(false, List.of(
                new Offset(-1, -1, 0)
        ));

        assertTrue(controller.covered());
        assertEquals(
                new CrafterConnectedTexture.Position(20, 30, 40),
                controller.effectiveController(20, 30, 40)
        );
        assertEquals(
                new CrafterConnectedTexture.Position(20, 30, 40),
                child.effectiveController(21, 31, 40)
        );
    }

    @Test
    void emptyOrWipedConnectedInputUsesCreateSelfFallback() throws IOException {
        CrafterBlockEntityData empty = decode(false, List.of());
        CrafterBlockEntityData missing = decodeWithoutConnectedInput();
        CrafterConnectedTexture.Position own =
                new CrafterConnectedTexture.Position(7, 8, 9);
        assertEquals(own, empty.effectiveController(7, 8, 9));
        assertEquals(own, missing.effectiveController(7, 8, 9));
    }

    private static CrafterBlockEntityData decode(
            boolean covered,
            List<Offset> offsets
    ) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("Cover").value((byte) (covered ? 1 : 0));
            writer.name("ConnectedInput").beginCompound();
            writer.name("Controller").value((byte) 0);
            writer.name("Data").beginList(offsets.size(), TagType.COMPOUND);
            for (Offset offset : offsets) {
                writer.beginCompound();
                writer.name("X").value(offset.x());
                writer.name("Y").value(offset.y());
                writer.name("Z").value(offset.z());
                writer.endCompound();
            }
            writer.endList();
            writer.endCompound();
            writer.endCompound();
        }
        return new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                CrafterBlockEntityData.class
        );
    }

    private static CrafterBlockEntityData decodeWithoutConnectedInput() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            writer.name("Cover").value((byte) 0);
            writer.endCompound();
        }
        return new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                CrafterBlockEntityData.class
        );
    }

    private record Offset(int x, int y, int z) {
    }
}
