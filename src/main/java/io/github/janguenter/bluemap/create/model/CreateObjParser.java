/*
 * SPDX-License-Identifier: MIT
 *
 * Bounded parser for the public Wavefront OBJ/MTL text format used by the
 * operator-installed Create resources.
 */
package io.github.janguenter.bluemap.create.model;

import io.github.janguenter.bluemap.create.model.CreateObjModel.Triangle;
import io.github.janguenter.bluemap.create.model.CreateObjModel.Vertex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Positions/UVs/materials plus polygon triangulation; normals are derived later. */
public final class CreateObjParser {

    private static final int MAX_LINES = 500_000;
    private static final int MAX_TRIANGLES = 250_000;

    private CreateObjParser() {
    }

    public static CreateObjModel parse(byte[] raw) throws IOException {
        List<Vec3> positions = new ArrayList<>();
        List<Vec2> uvs = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();
        String material = null;
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new StringReader(
                new String(raw, StandardCharsets.UTF_8)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (++lineCount > MAX_LINES) {
                    throw new IOException("OBJ exceeds line bound");
                }
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "v" -> {
                        require(tokens, 4, "position");
                        positions.add(new Vec3(number(tokens[1]), number(tokens[2]),
                                number(tokens[3])));
                    }
                    case "vt" -> {
                        require(tokens, 3, "uv");
                        uvs.add(new Vec2(number(tokens[1]), 1F - number(tokens[2])));
                    }
                    case "usemtl" -> {
                        require(tokens, 2, "material");
                        material = tokens[1];
                    }
                    case "f" -> {
                        if (material == null || tokens.length < 4) {
                            throw new IOException("OBJ face missing material or vertices");
                        }
                        List<Vertex> polygon = new ArrayList<>(tokens.length - 1);
                        for (int index = 1; index < tokens.length; index++) {
                            polygon.add(vertex(tokens[index], positions, uvs));
                        }
                        for (int index = 1; index + 1 < polygon.size(); index++) {
                            if (triangles.size() >= MAX_TRIANGLES) {
                                throw new IOException("OBJ exceeds triangle bound");
                            }
                            triangles.add(new Triangle(
                                    polygon.get(0), polygon.get(index), polygon.get(index + 1),
                                    material
                            ));
                        }
                    }
                    default -> {
                        // Object/group names, normals, libraries and smoothing are not needed.
                    }
                }
            }
        }
        if (triangles.isEmpty()) {
            throw new IOException("OBJ contains no textured triangles");
        }
        return new CreateObjModel(triangles);
    }

    /** Parses MTL material names to the {@code #texture_variable} in model JSON. */
    public static Map<String, String> parseMaterials(byte[] raw) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        String current = null;
        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(
                new String(raw, StandardCharsets.UTF_8)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (++lineCount > 10_000) {
                    throw new IOException("MTL exceeds line bound");
                }
                line = line.trim();
                if (line.startsWith("newmtl ")) {
                    current = line.substring(7).trim();
                } else if (line.startsWith("map_Kd ") && current != null) {
                    String variable = line.substring(7).trim();
                    if (!variable.startsWith("#") || variable.length() < 2) {
                        throw new IOException("expected MTL model-texture variable");
                    }
                    result.put(current, variable.substring(1));
                }
            }
        }
        if (result.isEmpty()) {
            throw new IOException("MTL has no texture mappings");
        }
        return Map.copyOf(result);
    }

    private static Vertex vertex(String token, List<Vec3> positions, List<Vec2> uvs)
            throws IOException {
        String[] indices = token.split("/", -1);
        if (indices.length < 2 || indices[0].isEmpty() || indices[1].isEmpty()) {
            throw new IOException("OBJ face lacks position or UV index");
        }
        Vec3 position = positions.get(resolveIndex(indices[0], positions.size()));
        Vec2 uv = uvs.get(resolveIndex(indices[1], uvs.size()));
        return new Vertex(position.x(), position.y(), position.z(), uv.u(), uv.v());
    }

    private static int resolveIndex(String token, int size) throws IOException {
        try {
            int raw = Integer.parseInt(token);
            int index = raw > 0 ? raw - 1 : size + raw;
            if (index < 0 || index >= size) {
                throw new IOException("OBJ index out of range");
            }
            return index;
        } catch (NumberFormatException exception) {
            throw new IOException("invalid OBJ index", exception);
        }
    }

    private static float number(String token) throws IOException {
        try {
            float value = Float.parseFloat(token);
            if (!Float.isFinite(value)) {
                throw new IOException("non-finite OBJ number");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new IOException("invalid OBJ number", exception);
        }
    }

    private static void require(String[] tokens, int length, String kind) throws IOException {
        if (tokens.length < length) {
            throw new IOException("short OBJ " + kind);
        }
    }

    private record Vec3(float x, float y, float z) {
    }

    private record Vec2(float u, float v) {
    }
}
