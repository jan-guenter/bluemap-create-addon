/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Stable powered selection persisted by CEI's augmentor and gem cutter. */
public final class CeiPoweredBlockEntityData extends MCABlockEntity {

    @NBTName("Powered")
    private boolean powered;

    public CeiPoweredBlockEntityData() {
    }

    boolean powered() {
        return powered;
    }
}
