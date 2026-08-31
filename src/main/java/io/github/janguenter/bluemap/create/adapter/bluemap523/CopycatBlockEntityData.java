/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Retains only Create's persistent copycat material state. */
public final class CopycatBlockEntityData extends MCABlockEntity {

    @NBTName("Material")
    private BlockState material;

    public CopycatBlockEntityData() {
    }

    BlockState material() {
        return material;
    }
}
