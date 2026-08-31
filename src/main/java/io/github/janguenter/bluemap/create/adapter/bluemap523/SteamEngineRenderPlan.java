/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.Optional;

/** Frozen exact SteamEngineRenderer transform plan at theta=-pi/2. */
record SteamEngineRenderPlan(
        CreateDirection facing,
        CreateDirection.Axis shaftAxis,
        AffineTransform piston,
        AffineTransform linkage,
        AffineTransform connector
) {

    static Optional<SteamEngineRenderPlan> select(
            String attachmentFace,
            String horizontalFacing,
            String shaftAxisName
    ) {
        CreateDirection facing = outward(attachmentFace, horizontalFacing).orElse(null);
        CreateDirection.Axis shaftAxis = CreateDirection.parseAxis(shaftAxisName)
                .orElse(null);
        if (facing == null || shaftAxis == null || facing.axis() == shaftAxis) {
            return Optional.empty();
        }

        boolean rollNinety = facing.horizontal() && shaftAxis == CreateDirection.Axis.Y
                || !facing.horizontal() && shaftAxis == CreateDirection.Axis.Z;
        AffineTransform orientation = AffineTransform.identity()
                .centered()
                .rotateY(facing.horizontalAngle())
                .rotateX(facing.verticalAngle() + 90F)
                .rotateY(rollNinety ? -90F : 0F)
                .uncentered();

        // At theta=-pi/2 Create's piston is exactly -20/16, linkage angle is
        // zero and connector rotation is zero. This is a stable neutral pose.
        AffineTransform piston = orientation;
        AffineTransform linkage = orientation.translate(0F, 1F, 0F);
        AffineTransform connector = orientation
                .translate(0F, 2F, 0F);
        if (!piston.finite() || !linkage.finite() || !connector.finite()) {
            return Optional.empty();
        }
        return Optional.of(new SteamEngineRenderPlan(
                facing, shaftAxis, piston, linkage, connector
        ));
    }

    static Optional<CreateDirection> outward(
            String attachmentFace,
            String horizontalFacing
    ) {
        if (attachmentFace == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(switch (attachmentFace) {
            case "floor" -> CreateDirection.UP;
            case "ceiling" -> CreateDirection.DOWN;
            case "wall" -> CreateDirection.parse(horizontalFacing)
                    .filter(CreateDirection::horizontal).orElse(null);
            default -> null;
        });
    }

    Offset shaftOffset() {
        return new Offset(facing.x() * 2, facing.y() * 2, facing.z() * 2);
    }

    record Offset(int x, int y, int z) {
    }
}
