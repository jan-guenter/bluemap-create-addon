/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

import java.util.List;

/** Retains stable crafter-slot cover and connected-input group identity. */
public final class CrafterBlockEntityData extends MCABlockEntity {

    @NBTName("Cover")
    private boolean cover;

    @NBTName("ConnectedInput")
    private ConnectedInputData connectedInput;

    public CrafterBlockEntityData() {
    }

    boolean covered() {
        return cover;
    }

    CrafterConnectedTexture.Position effectiveController(
            int ownX,
            int ownY,
            int ownZ
    ) {
        if (connectedInput == null || connectedInput.data == null
                || connectedInput.data.isEmpty()) {
            return new CrafterConnectedTexture.Position(ownX, ownY, ownZ);
        }
        OffsetData offset = connectedInput.data.getFirst();
        if (offset == null) {
            return null;
        }
        return new CrafterConnectedTexture.Position(
                ownX + offset.x, ownY + offset.y, ownZ + offset.z
        );
    }

    /** Minimal nested decoder for Create's persisted ConnectedInput compound. */
    public static final class ConnectedInputData {

        @NBTName("Data")
        private List<OffsetData> data;

        public ConnectedInputData() {
        }
    }

    /** One relative controller/group offset in ConnectedInput.Data. */
    public static final class OffsetData {

        @NBTName("X")
        private int x;

        @NBTName("Y")
        private int y;

        @NBTName("Z")
        private int z;

        public OffsetData() {
        }
    }
}
