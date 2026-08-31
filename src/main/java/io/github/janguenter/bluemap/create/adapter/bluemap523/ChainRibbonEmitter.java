/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.List;

/** Emits the two crossed, double-sided chain ribbons used by Create. */
final class ChainRibbonEmitter {

    private static final Key CHAIN_TEXTURE = Key.parse("minecraft:block/chain");

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    ChainRibbonEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean available() {
        return resourcePack.getTextures().get(CHAIN_TEXTURE) != null;
    }

    boolean emit(
            ChainConveyorRenderPlan.Connection connection,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (connection == null || !available()) {
            return false;
        }
        int start = target.getTileModel().size();
        List<ChainRibbonGeometry.Triangle> triangles =
                ChainRibbonGeometry.triangles(connection.length());
        append(target, block, triangles);
        if (target.getTileModel().size() == start) {
            return false;
        }
        BeltEmitter.apply(target.initialize(start), connection.strand());
        var texture = resourcePack.getTextures().get(CHAIN_TEXTURE);
        mapColor.add(new Color().set(texture.getColorPremultiplied()));
        mapColor.flatten().straight();
        return true;
    }

    private void append(
            TileModelView target,
            BlockNeighborhood block,
            List<ChainRibbonGeometry.Triangle> triangles
    ) {
        int first = target.add(triangles.size());
        TileModel mesh = target.getTileModel();
        LightData light = block.getLightData();
        int material = textures.get(CHAIN_TEXTURE);
        for (int index = 0; index < triangles.size(); index++) {
            int triangleIndex = first + index;
            ChainRibbonGeometry.Triangle triangle = triangles.get(index);
            vertexData(mesh, triangleIndex, triangle);
            mesh.setMaterialIndex(triangleIndex, material);
            mesh.setColor(triangleIndex, 1F, 1F, 1F);
            mesh.setAOs(triangleIndex, 1F, 1F, 1F);
            mesh.setSunlight(triangleIndex, light.getSkyLight());
            mesh.setBlocklight(triangleIndex, light.getBlockLight());
        }
    }

    private static void vertexData(
            TileModel mesh,
            int index,
            ChainRibbonGeometry.Triangle triangle
    ) {
        ChainRibbonGeometry.Vertex first = triangle.first();
        ChainRibbonGeometry.Vertex second = triangle.second();
        ChainRibbonGeometry.Vertex third = triangle.third();
        mesh.setPositions(
                index,
                first.x(), first.y(), first.z(),
                second.x(), second.y(), second.z(),
                third.x(), third.y(), third.z()
        );
        mesh.setUvs(
                index,
                first.u(), first.v(),
                second.u(), second.v(),
                third.u(), third.v()
        );
    }
}
