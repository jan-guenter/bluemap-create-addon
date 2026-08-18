/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Connectivity identity and physical dimensions of a modular accumulator. */
public final class CaaAccumulatorBlockEntityData extends MCABlockEntity {

    @NBTName("Controller")
    private int[] controller;
    @NBTName("Size")
    private int size;
    @NBTName("Height")
    private int height;

    public CaaAccumulatorBlockEntityData() {
    }

    CaaAccumulatorConnectedTexture.Position effectiveController(
            int ownX,
            int ownY,
            int ownZ
    ) {
        if (controller == null) {
            return new CaaAccumulatorConnectedTexture.Position(ownX, ownY, ownZ);
        }
        return controller.length == 3
                ? new CaaAccumulatorConnectedTexture.Position(
                        controller[0], controller[1], controller[2]
                ) : null;
    }

    boolean controller() {
        return controller == null;
    }

    int size() {
        return size;
    }

    int height() {
        return height;
    }
}
