/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Stable physical goggles selection for a mechanical arm. */
public final class ArmBlockEntityData extends MCABlockEntity {

    @NBTName("Goggles")
    private boolean goggles;

    public ArmBlockEntityData() {
    }

    boolean goggles() {
        return goggles;
    }
}
