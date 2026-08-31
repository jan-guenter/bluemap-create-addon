/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.List;
import java.util.Optional;

/** Exact time-zero vanilla BookModel pose used by the classic blaze enchanter. */
record CeiBookRenderPlan(List<Box> boxes) {

    private static final float OPEN = 1.25F;
    private static final float PAGE_OFFSET = (float) Math.sin(OPEN);

    CeiBookRenderPlan {
        boxes = List.copyOf(boxes);
    }

    static Optional<CeiBookRenderPlan> select(String facingName) {
        CreateDirection facing = CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal).orElse(null);
        if (facing == null) {
            return Optional.empty();
        }
        float frozenHeadAngle = facing.horizontalAngle() + 180F;
        AffineTransform outer = AffineTransform.identity()
                .translate(.5F, .35F, .5F)
                .rotateY(frozenHeadAngle + 90F)
                .rotateZ(80F)
                .scale(1.2F, 1.2F, 1.2F);
        return Optional.of(new CeiBookRenderPlan(List.of(
                box(outer, 0F, 0F, -1F, 180F + degrees(OPEN),
                        -6F, -5F, -.005F, 6F, 10F, .005F, 0F, 0F),
                box(outer, 0F, 0F, 1F, -degrees(OPEN),
                        0F, -5F, -.005F, 6F, 10F, .005F, 16F, 0F),
                box(outer, 0F, 0F, 0F, 90F,
                        -1F, -5F, 0F, 2F, 10F, .005F, 12F, 0F),
                box(outer, PAGE_OFFSET, 0F, 0F, degrees(OPEN),
                        0F, -4F, -.99F, 5F, 8F, 1F, 0F, 10F),
                box(outer, PAGE_OFFSET, 0F, 0F, -degrees(OPEN),
                        0F, -4F, -.01F, 5F, 8F, 1F, 12F, 10F),
                box(outer, PAGE_OFFSET, 0F, 0F, degrees(1F),
                        0F, -4F, 0F, 5F, 8F, .005F, 24F, 10F),
                box(outer, PAGE_OFFSET, 0F, 0F, degrees(-1F),
                        0F, -4F, 0F, 5F, 8F, .005F, 24F, 10F)
        )));
    }

    int triangleCount() {
        return boxes.size() * 12;
    }

    private static Box box(
            AffineTransform outer,
            float originX,
            float originY,
            float originZ,
            float yRotation,
            float x,
            float y,
            float z,
            float width,
            float height,
            float depth,
            float textureU,
            float textureV
    ) {
        return new Box(
                outer.translate(originX / 16F, originY / 16F, originZ / 16F)
                        .rotateY(yRotation),
                x / 16F, y / 16F, z / 16F,
                (x + width) / 16F, (y + height) / 16F, (z + depth) / 16F,
                textureU, textureV, width, height, depth
        );
    }

    private static float degrees(float radians) {
        return (float) Math.toDegrees(radians);
    }

    record Box(
            AffineTransform transform,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            float textureU,
            float textureV,
            float width,
            float height,
            float depth
    ) {
    }
}
