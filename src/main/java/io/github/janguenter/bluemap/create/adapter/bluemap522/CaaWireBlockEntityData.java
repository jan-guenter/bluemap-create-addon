/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

import java.util.List;

/** Exact persisted C&A wire-node endpoints. */
public final class CaaWireBlockEntityData extends MCABlockEntity {

    @NBTName("nodes")
    private List<Node> nodes;

    public CaaWireBlockEntityData() {
    }

    List<Node> nodes() {
        return nodes == null ? List.of() : List.copyOf(nodes);
    }

    /** One local endpoint record; xyz is relative remote block position. */
    public static final class Node {

        @NBTName("id")
        private int id;
        @NBTName("other")
        private int other;
        @NBTName("type")
        private int type;
        @NBTName("x")
        private int x;
        @NBTName("y")
        private int y;
        @NBTName("z")
        private int z;

        public Node() {
        }

        int id() {
            return id;
        }

        int other() {
            return other;
        }

        int type() {
            return type;
        }

        CaaWireRenderPlan.Offset offset() {
            return new CaaWireRenderPlan.Offset(x, y, z);
        }

        boolean reciprocal(Node local) {
            return local != null && id == local.other && other == local.id
                    && type == local.type && x == -local.x
                    && y == -local.y && z == -local.z;
        }
    }
}
