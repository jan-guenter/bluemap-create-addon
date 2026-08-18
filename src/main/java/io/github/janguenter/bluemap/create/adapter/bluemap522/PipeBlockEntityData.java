/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Retains the persistent decorative bracket attached to a Create pipe. */
public final class PipeBlockEntityData extends MCABlockEntity {

    @NBTName("Bracket")
    private BlockState bracket;

    public PipeBlockEntityData() {
    }

    BlockState bracket() {
        return bracket;
    }
}
