/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import java.util.Optional;

/** Small dependency-free direction model shared by exact selectors and transforms. */
enum CreateDirection {
    DOWN(0, -1, 0, Axis.Y, false),
    UP(0, 1, 0, Axis.Y, true),
    NORTH(0, 0, -1, Axis.Z, false),
    SOUTH(0, 0, 1, Axis.Z, true),
    WEST(-1, 0, 0, Axis.X, false),
    EAST(1, 0, 0, Axis.X, true);

    private final int x;
    private final int y;
    private final int z;
    private final Axis axis;
    private final boolean positive;

    CreateDirection(int x, int y, int z, Axis axis, boolean positive) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.axis = axis;
        this.positive = positive;
    }

    int x() {
        return x;
    }

    int y() {
        return y;
    }

    int z() {
        return z;
    }

    Axis axis() {
        return axis;
    }

    boolean positive() {
        return positive;
    }

    boolean horizontal() {
        return axis != Axis.Y;
    }

    CreateDirection opposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }

    CreateDirection clockwise() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> throw new IllegalStateException("vertical direction has no clockwise face");
        };
    }

    float horizontalAngle() {
        return switch (this) {
            case SOUTH, UP, DOWN -> 0F;
            case NORTH -> 180F;
            case WEST -> -90F;
            case EAST -> 90F;
        };
    }

    float verticalAngle() {
        return switch (this) {
            case UP -> -90F;
            case DOWN -> 90F;
            default -> 0F;
        };
    }

    static Optional<CreateDirection> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.toUpperCase(java.util.Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    static Optional<Axis> parseAxis(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Axis.valueOf(value.toUpperCase(java.util.Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    enum Axis {
        X,
        Y,
        Z
    }
}
