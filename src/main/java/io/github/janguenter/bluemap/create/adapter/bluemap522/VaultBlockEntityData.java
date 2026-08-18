/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/** Retains only the persisted connectivity controller of an item vault part. */
public final class VaultBlockEntityData extends MCABlockEntity {

    @NBTName("Controller")
    private int[] controller;

    public VaultBlockEntityData() {
    }

    VaultConnectedTexture.Position effectiveController(int ownX, int ownY, int ownZ) {
        if (controller == null) {
            return new VaultConnectedTexture.Position(ownX, ownY, ownZ);
        }
        if (controller.length != 3) {
            return null;
        }
        return new VaultConnectedTexture.Position(
                controller[0], controller[1], controller[2]
        );
    }
}
