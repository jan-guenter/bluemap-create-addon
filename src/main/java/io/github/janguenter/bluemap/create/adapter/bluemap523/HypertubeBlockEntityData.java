/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

import java.util.ArrayList;
import java.util.List;

/** Persisted version-1 Hypertube curves and physical tube attachments. */
public final class HypertubeBlockEntityData extends MCABlockEntity {

    @NBTName("ConnectionTo")
    private ConnectionData connectionTo;
    @NBTName("ConnectionTo_version")
    private int connectionToVersion;
    @NBTName("ConnectionFrom")
    private ConnectionData connectionFrom;
    @NBTName("ConnectionFrom_version")
    private int connectionFromVersion;
    @NBTName("Connection")
    private ConnectionData connection;
    @NBTName("Connection_version")
    private int connectionVersion;
    @NBTName("ConnectionOne")
    private ConnectionData connectionOne;
    @NBTName("ConnectionOne_version")
    private int connectionOneVersion;
    @NBTName("ConnectionTwo")
    private ConnectionData connectionTwo;
    @NBTName("ConnectionTwo_version")
    private int connectionTwoVersion;
    @NBTName("ConnectionThree")
    private ConnectionData connectionThree;
    @NBTName("ConnectionThree_version")
    private int connectionThreeVersion;
    @NBTName("attachments")
    private AttachmentsData attachments;

    public HypertubeBlockEntityData() {
    }

    List<HypertubeRenderPlan.Curve> curves() {
        ArrayList<HypertubeRenderPlan.Curve> curves = new ArrayList<>();
        add(curves, connectionTo, connectionToVersion);
        add(curves, connectionFrom, connectionFromVersion);
        add(curves, connection, connectionVersion);
        add(curves, connectionOne, connectionOneVersion);
        add(curves, connectionTwo, connectionTwoVersion);
        add(curves, connectionThree, connectionThreeVersion);
        return List.copyOf(curves);
    }

    List<HypertubeAttachmentPlan.SavedAttachment> attachments() {
        return attachments == null ? List.of() : attachments.decode();
    }

    private static void add(
            List<HypertubeRenderPlan.Curve> target,
            ConnectionData data,
            int version
    ) {
        HypertubeRenderPlan.Curve curve = data == null || version != 1
                ? null : data.decode();
        if (curve != null) {
            target.add(curve);
        }
    }

    /** Union decoder; only full Bezier records survive decode. */
    public static final class ConnectionData {

        @NBTName("fromPos")
        private SimpleData from;
        @NBTName("toPos")
        private SimpleData to;
        @NBTName("tubeSegments")
        private int tubeSegments;
        @NBTName("curvePoints")
        private List<double[]> curvePoints;

        public ConnectionData() {
        }

        private HypertubeRenderPlan.Curve decode() {
            if (from == null || to == null || !from.valid() || !to.valid()
                    || !from.local() || tubeSegments < 1 || tubeSegments > 256
                    || curvePoints == null || curvePoints.size() < 2
                    || curvePoints.size() > 4096) {
                return null;
            }
            ArrayList<HypertubeRenderPlan.Point> points = new ArrayList<>();
            for (double[] point : curvePoints) {
                if (point == null || point.length != 3
                        || !Double.isFinite(point[0]) || !Double.isFinite(point[1])
                        || !Double.isFinite(point[2])
                        || !Float.isFinite((float) point[0])
                        || !Float.isFinite((float) point[1])
                        || !Float.isFinite((float) point[2])) {
                    return null;
                }
                points.add(new HypertubeRenderPlan.Point(
                        (float) point[0], (float) point[1], (float) point[2]
                ));
            }
            return new HypertubeRenderPlan.Curve(points, tubeSegments);
        }
    }

    /** Version-1 simple endpoint codec. */
    public static final class SimpleData {

        @NBTName("pos")
        private int[] position;
        @NBTName("direction")
        private String direction;
        @NBTName("offset")
        private float offset;

        public SimpleData() {
        }

        private boolean valid() {
            return position != null && position.length == 3
                    && CreateDirection.parse(direction).isPresent()
                    && Float.isFinite(offset);
        }

        private boolean local() {
            return valid() && position[0] == 0 && position[1] == 0
                    && position[2] == 0;
        }
    }

    /** Dynamic-key compound decoded through six exact direction fields. */
    public static final class AttachmentsData {

        @NBTName("down")
        private String down;
        @NBTName("up")
        private String up;
        @NBTName("north")
        private String north;
        @NBTName("south")
        private String south;
        @NBTName("west")
        private String west;
        @NBTName("east")
        private String east;

        public AttachmentsData() {
        }

        private List<HypertubeAttachmentPlan.SavedAttachment> decode() {
            ArrayList<HypertubeAttachmentPlan.SavedAttachment> decoded =
                    new ArrayList<>();
            add(decoded, CreateDirection.DOWN, down);
            add(decoded, CreateDirection.UP, up);
            add(decoded, CreateDirection.NORTH, north);
            add(decoded, CreateDirection.SOUTH, south);
            add(decoded, CreateDirection.WEST, west);
            add(decoded, CreateDirection.EAST, east);
            return List.copyOf(decoded);
        }

        private static void add(
                List<HypertubeAttachmentPlan.SavedAttachment> target,
                CreateDirection face,
                String type
        ) {
            if ("redstone_input".equals(type) || "tube_scanner".equals(type)) {
                target.add(new HypertubeAttachmentPlan.SavedAttachment(face, type));
            }
        }
    }
}
