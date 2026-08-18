/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.model;

import java.util.List;

/** Minimal immutable Wavefront geometry consumed from installed Create resources. */
public record CreateObjModel(List<Triangle> triangles) {

    public CreateObjModel {
        triangles = List.copyOf(triangles);
    }

    /** One textured triangle and its source MTL material. */
    public record Triangle(Vertex first, Vertex second, Vertex third, String material) {
    }

    /** Direct block-space position plus normalized UV. */
    public record Vertex(float x, float y, float z, float u, float v) {
    }
}
