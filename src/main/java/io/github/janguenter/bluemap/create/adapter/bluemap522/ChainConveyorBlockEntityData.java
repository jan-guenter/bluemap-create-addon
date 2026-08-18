/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

import java.util.ArrayList;
import java.util.List;

/** Exact stable local chain endpoints for one chain conveyor. */
public final class ChainConveyorBlockEntityData extends MCABlockEntity {

    @NBTName("Connections")
    private List<int[]> connections;

    @NBTName("Speed")
    private float speed;

    public ChainConveyorBlockEntityData() {
    }

    List<ChainConveyorRenderPlan.Offset> connections() {
        if (connections == null || connections.isEmpty()) {
            return List.of();
        }
        ArrayList<ChainConveyorRenderPlan.Offset> decoded = new ArrayList<>();
        for (int[] connection : connections) {
            if (connection != null && connection.length == 3
                    && (connection[0] != 0 || connection[1] != 0
                    || connection[2] != 0)) {
                decoded.add(new ChainConveyorRenderPlan.Offset(
                        connection[0], connection[1], connection[2]
                ));
            }
        }
        return List.copyOf(decoded);
    }

    float speed() {
        return Float.isFinite(speed) ? speed : 0F;
    }
}
