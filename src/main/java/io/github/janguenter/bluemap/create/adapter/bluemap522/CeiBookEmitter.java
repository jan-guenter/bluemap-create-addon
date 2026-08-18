/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;

import java.util.List;

/** Emits the frozen vanilla BookModel from the installed CEI atlas texture. */
final class CeiBookEmitter {

    private static final Key TEXTURE =
            Key.parse("create_enchantment_industry:block/blaze_enchanter_book");
    private static final float TEXTURE_WIDTH = 64F;
    private static final float TEXTURE_HEIGHT = 32F;
    static final int FULL_BRIGHT = 15;

    private final ResourcePack resourcePack;
    private final TextureGallery textures;

    CeiBookEmitter(ResourcePack resourcePack, TextureGallery textures) {
        this.resourcePack = resourcePack;
        this.textures = textures;
    }

    boolean emit(
            String facing,
            TileModelView target,
            Color mapColor
    ) {
        if (resourcePack.getTextures().get(TEXTURE) == null) {
            return false;
        }
        CeiBookRenderPlan plan = CeiBookRenderPlan.select(facing).orElse(null);
        if (plan == null) {
            return false;
        }
        int start = target.getTileModel().size();
        for (CeiBookRenderPlan.Box box : plan.boxes()) {
            box(box, target);
        }
        if (target.getTileModel().size() == start) {
            return false;
        }
        mapColor.add(new Color().set(
                resourcePack.getTextures().get(TEXTURE).getColorPremultiplied()
        ));
        mapColor.flatten().straight();
        return true;
    }

    private void box(
            CeiBookRenderPlan.Box box,
            TileModelView target
    ) {
        Point p000 = point(box, box.minX(), box.minY(), box.minZ());
        Point p001 = point(box, box.minX(), box.minY(), box.maxZ());
        Point p010 = point(box, box.minX(), box.maxY(), box.minZ());
        Point p011 = point(box, box.minX(), box.maxY(), box.maxZ());
        Point p100 = point(box, box.maxX(), box.minY(), box.minZ());
        Point p101 = point(box, box.maxX(), box.minY(), box.maxZ());
        Point p110 = point(box, box.maxX(), box.maxY(), box.minZ());
        Point p111 = point(box, box.maxX(), box.maxY(), box.maxZ());

        Point[] points = {p000, p001, p010, p011, p100, p101, p110, p111};
        for (FaceLayout face : faceLayouts(box)) {
            quad(target, points[face.a()], points[face.b()], points[face.c()],
                    points[face.d()], face);
        }
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void quad(
            TileModelView target,
            Point a,
            Point b,
            Point c,
            Point d,
            FaceLayout face
    ) {
        int first = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(first, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(),
                c.x(), c.y(), c.z());
        mesh.setPositions(first + 1, a.x(), a.y(), a.z(), c.x(), c.y(), c.z(),
                d.x(), d.y(), d.z());
        float[] uv = triangleUvs(face);
        mesh.setUvs(first, uv[0], uv[1], uv[2], uv[3], uv[4], uv[5]);
        mesh.setUvs(first + 1, uv[6], uv[7], uv[8], uv[9], uv[10], uv[11]);
        int material = textures.get(TEXTURE);
        for (int triangle = first; triangle < first + 2; triangle++) {
            mesh.setMaterialIndex(triangle, material);
            mesh.setColor(triangle, 1F, 1F, 1F);
            mesh.setAOs(triangle, 1F, 1F, 1F);
            mesh.setSunlight(triangle, FULL_BRIGHT);
            mesh.setBlocklight(triangle, FULL_BRIGHT);
        }
    }

    static List<FaceLayout> faceLayouts(CeiBookRenderPlan.Box box) {
        float u0 = box.textureU();
        float u1 = u0 + box.depth();
        float u2 = u1 + box.width();
        float u3 = u2 + box.width();
        float u4 = u2 + box.depth();
        float u5 = u4 + box.width();
        float v0 = box.textureV();
        float v1 = v0 + box.depth();
        float v2 = v1 + box.height();
        return List.of(
                new FaceLayout(5, 1, 0, 4, u1, v0, u2, v1),
                new FaceLayout(6, 2, 3, 7, u2, v1, u3, v0),
                new FaceLayout(0, 1, 3, 2, u0, v1, u1, v2),
                new FaceLayout(4, 0, 2, 6, u1, v1, u2, v2),
                new FaceLayout(5, 4, 6, 7, u2, v1, u4, v2),
                new FaceLayout(1, 5, 7, 3, u4, v1, u5, v2)
        );
    }

    static float[] triangleUvs(FaceLayout face) {
        float minU = face.minU() / TEXTURE_WIDTH;
        float maxU = face.maxU() / TEXTURE_WIDTH;
        float minV = face.minV() / TEXTURE_HEIGHT;
        float maxV = face.maxV() / TEXTURE_HEIGHT;
        return new float[]{
                maxU, minV, minU, minV, minU, maxV,
                maxU, minV, minU, maxV, maxU, maxV
        };
    }

    private static Point point(
            CeiBookRenderPlan.Box box,
            float x,
            float y,
            float z
    ) {
        AffineTransform.Point point = box.transform().transform(x, y, z);
        return new Point(point.x(), point.y(), point.z());
    }

    private record Point(float x, float y, float z) {
    }

    record FaceLayout(
            int a,
            int b,
            int c,
            int d,
            float minU,
            float minV,
            float maxU,
            float maxV
    ) {
    }
}
