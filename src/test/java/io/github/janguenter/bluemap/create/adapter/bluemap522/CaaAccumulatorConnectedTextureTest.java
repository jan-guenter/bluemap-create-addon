/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaaAccumulatorConnectedTextureTest {

    @Test
    void exactSideAndTopMaterialsAreFaceGated() {
        assertEquals(
                "createaddition:block/modular_accumulator/block_connected",
                CaaAccumulatorConnectedTexture.material(
                        CreateDirection.NORTH,
                        "createaddition:block/modular_accumulator/block"
                ).orElseThrow().connectedTexture()
        );
        assertEquals(
                "createaddition:block/modular_accumulator/block_top_connected",
                CaaAccumulatorConnectedTexture.material(
                        CreateDirection.UP,
                        "createaddition:block/modular_accumulator/block_top"
                ).orElseThrow().connectedTexture()
        );
        assertTrue(CaaAccumulatorConnectedTexture.material(
                CreateDirection.UP,
                "createaddition:block/modular_accumulator/block"
        ).isEmpty());
    }

    @Test
    void blueNbtDecodesControllerAndDimensions() throws IOException {
        CaaAccumulatorBlockEntityData controller = decode(null, 2, 3);
        CaaAccumulatorBlockEntityData child = decode(new int[]{10, 20, 30}, 0, 0);
        assertTrue(controller.controller());
        assertEquals(2, controller.size());
        assertEquals(3, controller.height());
        assertEquals(new CaaAccumulatorConnectedTexture.Position(10, 20, 30),
                child.effectiveController(11, 21, 31));
    }

    @Test
    void neutralGaugePlanHasFourSidesAndFiniteTransforms() {
        CaaAccumulatorGaugePlan plan = CaaAccumulatorGaugePlan.select(3, 4);
        assertEquals(4, plan.gauges().size());
        assertTrue(plan.gauges().stream().allMatch(gauge ->
                gauge.housing().finite() && gauge.dial().finite()
        ));
        assertTrue(CaaAccumulatorGaugePlan.select(0, 4).gauges().isEmpty());
    }

    private static CaaAccumulatorBlockEntityData decode(
            int[] controller,
            int size,
            int height
    ) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.name("").beginCompound();
            if (controller != null) {
                writer.name("Controller").value(controller);
            }
            writer.name("Size").value(size);
            writer.name("Height").value(height);
            writer.endCompound();
        }
        return new BlueNBT().read(
                new ByteArrayInputStream(bytes.toByteArray()),
                CaaAccumulatorBlockEntityData.class
        );
    }
}
