/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.create.model.CreateObjModel.Triangle;
import io.github.janguenter.bluemap.create.model.CreateObjModel.Vertex;

import java.util.LinkedHashMap;
import java.util.Map;

/** Emits one frozen Wavefront model from exact installed Create resources. */
final class CreateObjEmitter {

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;

    CreateObjEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
    }

    boolean emit(
            CompiledCreateObj object,
            Variant orientation,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (object == null) {
            return false;
        }
        Map<String, Material> materials = resolve(object.materials());
        if (materials.size() != object.materials().size()) {
            return false;
        }
        int start = target.getTileModel().size();
        for (Triangle triangle : object.model().triangles()) {
            Direction direction = nearestDirection(triangle);
            if (settings.isRenderTopOnly() && direction != Direction.UP) {
                continue;
            }
            var normal = direction.toVector();
            LightData own = block.getLightData();
            LightData faced = block.getNeighborBlock(
                    normal.getX(), normal.getY(), normal.getZ()
            ).getLightData();
            int sunlight = Math.max(own.getSkyLight(), faced.getSkyLight());
            int blocklight = Math.max(own.getBlockLight(), faced.getBlockLight());
            int visible = settings.isCaveDetectionUsesBlockLight()
                    ? Math.max(sunlight, blocklight) : sunlight;
            if (block.isRemoveIfCave() && visible == 0) {
                continue;
            }
            Material material = materials.get(triangle.material());
            if (material == null) {
                return false;
            }
            int index = target.add(1);
            TileModel mesh = target.getTileModel();
            positions(mesh, index, triangle.first(), triangle.second(), triangle.third());
            uvs(mesh, index, triangle.first(), triangle.second(), triangle.third());
            mesh.setMaterialIndex(index, material.index());
            mesh.setColor(index, 1F, 1F, 1F);
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, sunlight);
            mesh.setBlocklight(index, blocklight);
        }
        if (target.getTileModel().size() == start) {
            return false;
        }
        if (orientation.isTransformed()) {
            target.initialize(start).transform(orientation.getTransformMatrix());
        } else {
            target.initialize(start);
        }
        materials.values().stream().map(Material::texture).distinct().forEach(texture ->
                mapColor.add(new Color().set(texture.getColorPremultiplied()))
        );
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return true;
    }

    private Map<String, Material> resolve(Map<String, Key> mappings) {
        LinkedHashMap<String, Material> result = new LinkedHashMap<>();
        mappings.forEach((name, path) -> {
            Texture texture = resourcePack.getTextures().get(path);
            if (texture != null) {
                result.put(name, new Material(textures.get(path), texture));
            }
        });
        return result;
    }

    private static Direction nearestDirection(Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        float abx = b.x() - a.x();
        float aby = b.y() - a.y();
        float abz = b.z() - a.z();
        float acx = c.x() - a.x();
        float acy = c.y() - a.y();
        float acz = c.z() - a.z();
        float x = aby * acz - abz * acy;
        float y = abz * acx - abx * acz;
        float z = abx * acy - aby * acx;
        float ax = Math.abs(x);
        float ay = Math.abs(y);
        float az = Math.abs(z);
        if (ay >= ax && ay >= az) {
            return y >= 0 ? Direction.UP : Direction.DOWN;
        }
        if (ax >= az) {
            return x >= 0 ? Direction.EAST : Direction.WEST;
        }
        return z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void positions(TileModel mesh, int index, Vertex a, Vertex b, Vertex c) {
        mesh.setPositions(index, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(),
                c.x(), c.y(), c.z());
    }

    private static void uvs(TileModel mesh, int index, Vertex a, Vertex b, Vertex c) {
        mesh.setUvs(index, a.u(), a.v(), b.u(), b.v(), c.u(), c.v());
    }

    private record Material(int index, Texture texture) {
    }
}
