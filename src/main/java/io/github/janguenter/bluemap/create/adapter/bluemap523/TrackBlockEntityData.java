/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

import java.util.ArrayList;
import java.util.List;

/** Exact stable Bezier connection compounds persisted by Create tracks. */
public final class TrackBlockEntityData extends MCABlockEntity {

    @NBTName("Connections")
    private List<ConnectionData> connections;

    public TrackBlockEntityData() {
    }

    List<Connection> connections() {
        if (connections == null) {
            return List.of();
        }
        ArrayList<Connection> decoded = new ArrayList<>();
        for (ConnectionData data : connections) {
            Connection connection = data == null ? null : data.decode();
            if (connection != null) {
                decoded.add(connection);
            }
        }
        return List.copyOf(decoded);
    }

    /** Nested exact connection payload. */
    public static final class ConnectionData {

        @NBTName("Primary")
        private boolean primary;
        @NBTName("Girder")
        private boolean girder;
        @NBTName("Positions")
        private List<PositionData> positions;
        @NBTName("Starts")
        private List<VectorData> starts;
        @NBTName("Axes")
        private List<VectorData> axes;
        @NBTName("Normals")
        private List<VectorData> normals;
        @NBTName("Material")
        private String material;

        public ConnectionData() {
        }

        private Connection decode() {
            if (positions == null || starts == null || axes == null || normals == null
                    || positions.size() != 2 || starts.size() != 2
                    || axes.size() != 2 || normals.size() != 2) {
                return null;
            }
            TrackRenderPlan.IntPoint firstPosition = positions.get(0).decode();
            TrackRenderPlan.IntPoint secondPosition = positions.get(1).decode();
            TrackRenderPlan.Vector firstStart = starts.get(0).decode();
            TrackRenderPlan.Vector secondStart = starts.get(1).decode();
            TrackRenderPlan.Vector firstAxis = axes.get(0).decode();
            TrackRenderPlan.Vector secondAxis = axes.get(1).decode();
            TrackRenderPlan.Vector firstNormal = normals.get(0).decode();
            TrackRenderPlan.Vector secondNormal = normals.get(1).decode();
            if (firstPosition == null || secondPosition == null || firstStart == null
                    || secondStart == null || firstAxis == null || secondAxis == null
                    || firstNormal == null || secondNormal == null) {
                return null;
            }
            return new Connection(
                    primary, girder, material,
                    firstPosition, secondPosition,
                    firstStart, secondStart,
                    firstAxis, secondAxis,
                    firstNormal, secondNormal
            );
        }
    }

    /** Codec wrapper around an int-array BlockPos. */
    public static final class PositionData {

        @NBTName("Pos")
        private int[] position;

        public PositionData() {
        }

        private TrackRenderPlan.IntPoint decode() {
            return position != null && position.length == 3
                    ? new TrackRenderPlan.IntPoint(
                            position[0], position[1], position[2]
                    ) : null;
        }
    }

    /** Codec wrapper around a double-array Vec3. */
    public static final class VectorData {

        @NBTName("V")
        private double[] vector;

        public VectorData() {
        }

        private TrackRenderPlan.Vector decode() {
            if (vector == null || vector.length != 3
                    || !Double.isFinite(vector[0])
                    || !Double.isFinite(vector[1])
                    || !Double.isFinite(vector[2])) {
                return null;
            }
            return new TrackRenderPlan.Vector(vector[0], vector[1], vector[2]);
        }
    }

    record Connection(
            boolean primary,
            boolean girder,
            String material,
            TrackRenderPlan.IntPoint firstPosition,
            TrackRenderPlan.IntPoint secondPosition,
            TrackRenderPlan.Vector firstStart,
            TrackRenderPlan.Vector secondStart,
            TrackRenderPlan.Vector firstAxis,
            TrackRenderPlan.Vector secondAxis,
            TrackRenderPlan.Vector firstNormal,
            TrackRenderPlan.Vector secondNormal
    ) {

        boolean supportedMaterial() {
            return "create:andesite".equals(material);
        }

        boolean localPrimary() {
            return primary && firstPosition.isZero() && !secondPosition.isZero();
        }

        boolean reciprocal(Connection other) {
            if (other == null || other.primary || primary == other.primary
                    || girder != other.girder || !java.util.Objects.equals(material, other.material)
                    || !other.firstPosition.isZero()
                    || !secondPosition.negated().equals(other.secondPosition)) {
                return false;
            }
            TrackRenderPlan.Vector delta = secondPosition.vector();
            return near(other.firstStart, secondStart.subtract(delta))
                    && near(other.secondStart, firstStart.subtract(delta))
                    && near(other.firstAxis, secondAxis)
                    && near(other.secondAxis, firstAxis)
                    && near(other.firstNormal, secondNormal)
                    && near(other.secondNormal, firstNormal);
        }

        private static boolean near(
                TrackRenderPlan.Vector first,
                TrackRenderPlan.Vector second
        ) {
            return first.subtract(second).length() < 0.00001D;
        }
    }
}
