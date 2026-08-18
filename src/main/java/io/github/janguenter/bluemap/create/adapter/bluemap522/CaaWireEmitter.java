/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Validates reciprocal C&A nodes and emits one canonical static wire mesh. */
final class CaaWireEmitter {

    private static final Key MATERIAL = Key.parse("minecraft:block/white_concrete");

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    CaaWireEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(BlockNeighborhood block, TileModelView target, Color mapColor) {
        if (resourcePack.getTextures().get(MATERIAL) == null
                || !(block.getBlockEntity() instanceof CaaWireBlockEntityData own)) {
            return false;
        }
        boolean emitted = false;
        for (CaaWireBlockEntityData.Node node : own.nodes()) {
            CaaWireRenderPlan.Offset offset = node.offset();
            if (!offset.bounded() || !canonical(block, offset)) {
                continue;
            }
            var remote = block.getNeighborBlock(offset.x(), offset.y(), offset.z());
            if (!(remote.getBlockEntity() instanceof CaaWireBlockEntityData other)
                    || other.nodes().stream().noneMatch(candidate -> candidate.reciprocal(node))) {
                continue;
            }
            String ownId = block.getBlockState().getId().getFormatted();
            String remoteId = remote.getBlockState().getId().getFormatted();
            CaaWireRenderPlan.Point start = CaaWireRenderPlan.endpoint(
                    ownId, block.getBlockState().getProperties(), node.id(), 0, 0, 0
            ).orElse(null);
            CaaWireRenderPlan.Point end = CaaWireRenderPlan.endpoint(
                    remoteId, remote.getBlockState().getProperties(), node.other(),
                    offset.x(), offset.y(), offset.z()
            ).orElse(null);
            CaaWireRenderPlan plan = CaaWireRenderPlan.select(
                    start, end, offset.distance(), node.type()
            ).orElse(null);
            if (plan != null) {
                emit(plan, block, target);
                emitted = true;
                mapColor.add(new Color().set(
                        plan.color().red(), plan.color().green(), plan.color().blue(),
                        1F, true
                ));
            }
        }
        if (emitted && mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return emitted;
    }

    private void emit(
            CaaWireRenderPlan plan,
            BlockNeighborhood block,
            TileModelView target
    ) {
        for (int index = 0; index < plan.points().size() - 1; index++) {
            CaaWireRenderPlan.Point from = plan.points().get(index);
            CaaWireRenderPlan.Point to = plan.points().get(index + 1);
            ribbon(plan, block, target, from, to, false);
            ribbon(plan, block, target, from, to, true);
        }
    }

    private void ribbon(
            CaaWireRenderPlan plan,
            BlockNeighborhood block,
            TileModelView target,
            CaaWireRenderPlan.Point from,
            CaaWireRenderPlan.Point to,
            boolean secondStrip
    ) {
        Point a = vertex(plan, from, secondStrip, true);
        Point b = vertex(plan, from, secondStrip, false);
        Point c = vertex(plan, to, secondStrip, false);
        Point d = vertex(plan, to, secondStrip, true);
        int first = target.add(4);
        TileModel mesh = target.getTileModel();
        position(mesh, first, a, b, c);
        position(mesh, first + 1, a, c, d);
        position(mesh, first + 2, a, c, b);
        position(mesh, first + 3, a, d, c);
        mesh.setUvs(first, 0F, 0F, 1F, 0F, 1F, 1F);
        mesh.setUvs(first + 1, 0F, 0F, 1F, 1F, 0F, 1F);
        mesh.setUvs(first + 2, 0F, 0F, 1F, 1F, 1F, 0F);
        mesh.setUvs(first + 3, 0F, 0F, 0F, 1F, 1F, 1F);
        LightData light = block.getLightData();
        int material = textures.get(MATERIAL);
        // TileModel colors are per triangle, while the source alternates color
        // per sample vertex. Their exact segment-average avoids a diagonal
        // color seam across the two triangles of one ribbon quad.
        float shade = 0.85F;
        for (int triangle = first; triangle < first + 4; triangle++) {
            mesh.setMaterialIndex(triangle, material);
            mesh.setColor(triangle, plan.color().red() * shade,
                    plan.color().green() * shade, plan.color().blue() * shade);
            mesh.setAOs(triangle, 1F, 1F, 1F);
            mesh.setSunlight(triangle, light.getSkyLight());
            mesh.setBlocklight(triangle, light.getBlockLight());
        }
    }

    private static Point vertex(
            CaaWireRenderPlan plan,
            CaaWireRenderPlan.Point point,
            boolean secondStrip,
            boolean firstSide
    ) {
        if (plan.steep()) {
            float x = point.x() + (firstSide ? -plan.width() : plan.width());
            boolean sameSigns = !secondStrip;
            float zSign = firstSide == sameSigns ? -1F : 1F;
            return new Point(x, point.y(), point.z() + zSign * plan.width());
        }
        float x = point.x() + (firstSide ? plan.offsetX() : -plan.offsetX());
        float z = point.z() + (firstSide ? -plan.offsetZ() : plan.offsetZ());
        float y;
        if (secondStrip) {
            y = point.y() + (firstSide ? 0.025F : 0F);
        } else {
            y = point.y() + (firstSide ? 0F : 0.025F);
        }
        return new Point(x, y, z);
    }

    private static boolean canonical(
            BlockNeighborhood block,
            CaaWireRenderPlan.Offset offset
    ) {
        int comparison = Integer.compare(block.getX(), block.getX() + offset.x());
        if (comparison == 0) {
            comparison = Integer.compare(block.getY(), block.getY() + offset.y());
        }
        if (comparison == 0) {
            comparison = Integer.compare(block.getZ(), block.getZ() + offset.z());
        }
        return comparison < 0;
    }

    private static void position(TileModel mesh, int index, Point a, Point b, Point c) {
        mesh.setPositions(index, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(),
                c.x(), c.y(), c.z());
    }

    private record Point(float x, float y, float z) {
    }
}
