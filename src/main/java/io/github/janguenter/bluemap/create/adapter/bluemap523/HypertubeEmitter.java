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

/** Emits exact static exterior, interior and line meshes for Hypertube curves. */
final class HypertubeEmitter {

    private static final Key GLASS =
            Key.parse("create_hypertube:block/tube_base_glass");
    private static final Key LINE =
            Key.parse("create_hypertube:block/tube_base_glass_2");

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    HypertubeEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            List<HypertubeRenderPlan.Curve> curves,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        if (resourcePack.getTextures().get(GLASS) == null
                || resourcePack.getTextures().get(LINE) == null) {
            return false;
        }
        int start = target.getTileModel().size();
        for (HypertubeRenderPlan.Curve curve : curves) {
            HypertubeRenderPlan.select(curve).ifPresent(plan -> emit(plan, block, target));
        }
        if (target.getTileModel().size() == start) {
            return false;
        }
        mapColor.add(new Color().set(
                resourcePack.getTextures().get(GLASS).getColorPremultiplied()
        ));
        mapColor.add(new Color().set(
                resourcePack.getTextures().get(LINE).getColorPremultiplied()
        ));
        mapColor.flatten().straight();
        return true;
    }

    private void emit(
            HypertubeRenderPlan plan,
            BlockNeighborhood block,
            TileModelView target
    ) {
        for (int index = 0; index < plan.rings().size() - 1; index++) {
            HypertubeRenderPlan.Ring from = plan.rings().get(index);
            HypertubeRenderPlan.Ring to = plan.rings().get(index + 1);
            if (plan.tubeInterval(index)) {
                section(from, to, from.exterior(), to.exterior(), GLASS,
                        Winding.DOUBLE, block, target);
                section(from, to, from.interior(), to.interior(), GLASS,
                        Winding.REVERSE, block, target);
            }
            if (plan.lineInterval(index)) {
                section(from, to, from.line(), to.line(), LINE,
                        Winding.DOUBLE, block, target);
            }
        }
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void section(
            HypertubeRenderPlan.Ring from,
            HypertubeRenderPlan.Ring to,
            List<HypertubeRenderPlan.Point> fromOffsets,
            List<HypertubeRenderPlan.Point> toOffsets,
            Key material,
            Winding winding,
            BlockNeighborhood block,
            TileModelView target
    ) {
        HypertubeRenderPlan.Point direction = to.center().subtract(from.center())
                .normalized();
        if (direction == null) {
            return;
        }
        for (int side = 0; side < 4; side++) {
            int next = (side + 1) % 4;
            HypertubeRenderPlan.Point a = from.center().add(fromOffsets.get(next));
            HypertubeRenderPlan.Point b = to.center().add(toOffsets.get(next));
            HypertubeRenderPlan.Point c = to.center().add(toOffsets.get(side));
            HypertubeRenderPlan.Point d = from.center().add(fromOffsets.get(side));
            float nextU = 0.8F + b.subtract(a).dot(direction);
            float currentU = 0.8F + c.subtract(d).dot(direction);
            if (winding != Winding.REVERSE) {
                quad(a, b, c, d, 0.8F, nextU, currentU,
                        false, material, block, target);
            }
            if (winding != Winding.FORWARD) {
                quad(a, b, c, d, 0.8F, nextU, currentU,
                        true, material, block, target);
            }
        }
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void quad(
            HypertubeRenderPlan.Point a,
            HypertubeRenderPlan.Point b,
            HypertubeRenderPlan.Point c,
            HypertubeRenderPlan.Point d,
            float startU,
            float nextU,
            float currentU,
            boolean reverse,
            Key material,
            BlockNeighborhood block,
            TileModelView target
    ) {
        int first = target.add(2);
        TileModel mesh = target.getTileModel();
        if (reverse) {
            positions(mesh, first, a, c, b);
            positions(mesh, first + 1, a, d, c);
            mesh.setUvs(first, startU, 1F, currentU, 0F, nextU, 1F);
            mesh.setUvs(first + 1, startU, 1F, startU, 0F, currentU, 0F);
        } else {
            positions(mesh, first, a, b, c);
            positions(mesh, first + 1, a, c, d);
            mesh.setUvs(first, startU, 1F, nextU, 1F, currentU, 0F);
            mesh.setUvs(first + 1, startU, 1F, currentU, 0F, startU, 0F);
        }
        LightData light = block.getLightData();
        int materialIndex = textures.get(material);
        for (int triangle = first; triangle < first + 2; triangle++) {
            mesh.setMaterialIndex(triangle, materialIndex);
            mesh.setColor(triangle, 1F, 1F, 1F);
            mesh.setAOs(triangle, 1F, 1F, 1F);
            mesh.setSunlight(triangle, light.getSkyLight());
            mesh.setBlocklight(triangle, light.getBlockLight());
        }
    }

    private static void positions(
            TileModel mesh,
            int index,
            HypertubeRenderPlan.Point a,
            HypertubeRenderPlan.Point b,
            HypertubeRenderPlan.Point c
    ) {
        mesh.setPositions(index, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(),
                c.x(), c.y(), c.z());
    }

    private enum Winding {
        FORWARD,
        REVERSE,
        DOUBLE
    }
}
