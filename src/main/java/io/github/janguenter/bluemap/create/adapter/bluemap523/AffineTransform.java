/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.Arrays;

/** Immutable row-major affine matrix using column-vector coordinates. */
final class AffineTransform {

    private static final int SIZE = 4;
    private final float[] matrix;

    private AffineTransform(float[] matrix) {
        this.matrix = matrix;
    }

    static AffineTransform identity() {
        return new AffineTransform(new float[]{
                1F, 0F, 0F, 0F,
                0F, 1F, 0F, 0F,
                0F, 0F, 1F, 0F,
                0F, 0F, 0F, 1F
        });
    }

    AffineTransform translate(float x, float y, float z) {
        return postMultiply(new float[]{
                1F, 0F, 0F, x,
                0F, 1F, 0F, y,
                0F, 0F, 1F, z,
                0F, 0F, 0F, 1F
        });
    }

    AffineTransform scale(float x, float y, float z) {
        return postMultiply(new float[]{
                x, 0F, 0F, 0F,
                0F, y, 0F, 0F,
                0F, 0F, z, 0F,
                0F, 0F, 0F, 1F
        });
    }

    AffineTransform rotateX(float degrees) {
        double radians = Math.toRadians(degrees);
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        return postMultiply(new float[]{
                1F, 0F, 0F, 0F,
                0F, cosine, -sine, 0F,
                0F, sine, cosine, 0F,
                0F, 0F, 0F, 1F
        });
    }

    AffineTransform rotateY(float degrees) {
        double radians = Math.toRadians(degrees);
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        return postMultiply(new float[]{
                cosine, 0F, sine, 0F,
                0F, 1F, 0F, 0F,
                -sine, 0F, cosine, 0F,
                0F, 0F, 0F, 1F
        });
    }

    AffineTransform rotateZ(float degrees) {
        double radians = Math.toRadians(degrees);
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        return postMultiply(new float[]{
                cosine, -sine, 0F, 0F,
                sine, cosine, 0F, 0F,
                0F, 0F, 1F, 0F,
                0F, 0F, 0F, 1F
        });
    }

    AffineTransform centered() {
        return translate(0.5F, 0.5F, 0.5F);
    }

    AffineTransform uncentered() {
        return translate(-0.5F, -0.5F, -0.5F);
    }

    AffineTransform translatedBefore(float x, float y, float z) {
        return identity().translate(x, y, z).postMultiply(matrix);
    }

    float component(int row, int column) {
        if (row < 0 || row >= SIZE || column < 0 || column >= SIZE) {
            throw new IndexOutOfBoundsException("matrix component outside 4x4 bounds");
        }
        return matrix[row * SIZE + column];
    }

    Point transform(float x, float y, float z) {
        return new Point(
                matrix[0] * x + matrix[1] * y + matrix[2] * z + matrix[3],
                matrix[4] * x + matrix[5] * y + matrix[6] * z + matrix[7],
                matrix[8] * x + matrix[9] * y + matrix[10] * z + matrix[11]
        );
    }

    boolean finite() {
        for (float value : matrix) {
            if (!Float.isFinite(value)) {
                return false;
            }
        }
        return true;
    }

    float[] copyValues() {
        return Arrays.copyOf(matrix, matrix.length);
    }

    private AffineTransform postMultiply(float[] right) {
        float[] result = new float[SIZE * SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                float value = 0F;
                for (int index = 0; index < SIZE; index++) {
                    value += matrix[row * SIZE + index] * right[index * SIZE + column];
                }
                result[row * SIZE + column] = value;
            }
        }
        return new AffineTransform(result);
    }

    record Point(float x, float y, float z) {
    }
}
