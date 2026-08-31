/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Retains only stable tank connectivity and physical boiler data. */
public final class TankBlockEntityData extends MCABlockEntity {

    @NBTName("Controller")
    private int[] controller;

    @NBTName("Size")
    private int size;

    @NBTName("Boiler")
    private BoilerData boiler;

    public TankBlockEntityData() {
    }

    TankConnectedTexture.Position effectiveController(int ownX, int ownY, int ownZ) {
        if (controller == null) {
            return new TankConnectedTexture.Position(ownX, ownY, ownZ);
        }
        if (controller.length != 3) {
            return null;
        }
        return new TankConnectedTexture.Position(
                controller[0], controller[1], controller[2]
        );
    }

    boolean isController() {
        return controller == null;
    }

    int size() {
        return size;
    }

    boolean activeBoiler() {
        return boiler != null && (boiler.engines > 0 || boiler.whistles > 0);
    }

    /** Minimal nested decoder for Create's persisted Boiler compound. */
    public static final class BoilerData {

        @NBTName("Engines")
        private int engines;

        @NBTName("Whistles")
        private int whistles;

        public BoilerData() {
        }
    }
}
