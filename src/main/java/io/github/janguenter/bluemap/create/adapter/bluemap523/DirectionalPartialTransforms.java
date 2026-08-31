/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.Optional;

/** Exact neutral transforms for Create directional partial models. */
final class DirectionalPartialTransforms {

    private static final java.util.Map<String, Float> CRAFTER_X_ROTATIONS = java.util.Map.of(
            "up", 0F,
            "left", 270F,
            "down", 180F,
            "right", 90F
    );

    private DirectionalPartialTransforms() {
    }

    static Optional<AffineTransform> pump(String facingName) {
        return CreateDirection.parse(facingName).map(facing -> AffineTransform.identity()
                .centered()
                .rotateY(facing.horizontalAngle())
                .rotateX(facing.verticalAngle())
                .uncentered());
    }

    static Optional<AffineTransform> crafterCog(String facingName) {
        return CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal)
                .map(facing -> AffineTransform.identity()
                        .centered()
                        .rotateY(facing.axis() == CreateDirection.Axis.X ? 90F : 0F)
                        .rotateX(90F)
                        .uncentered());
    }

    static Optional<AffineTransform> crafterBodyPartial(
            String facingName,
            String pointingName
    ) {
        CreateDirection facing = CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal)
                .orElse(null);
        Float xRotation = crafterRotation(pointingName);
        if (facing == null || xRotation == null) {
            return Optional.empty();
        }
        return Optional.of(AffineTransform.identity()
                .centered()
                .rotateY(facing.horizontalAngle() + 90F)
                .rotateX(xRotation)
                .uncentered());
    }

    static Optional<CreateDirection> crafterTarget(
            String facingName,
            String pointingName
    ) {
        CreateDirection facing = CreateDirection.parse(facingName)
                .filter(CreateDirection::horizontal)
                .orElse(null);
        Float xRotation = crafterRotation(pointingName);
        if (facing == null || xRotation == null) {
            return Optional.empty();
        }
        AffineTransform.Point target = AffineTransform.identity()
                .rotateY(facing.horizontalAngle())
                .rotateZ(-xRotation)
                .transform(0F, 1F, 0F);
        return nearest(target);
    }

    static boolean validCrafterTarget(
            String facingName,
            String pointingName,
            String targetBlockId,
            String targetFacingName,
            String targetPointingName
    ) {
        Float pointing = crafterRotation(pointingName);
        Float targetPointing = crafterRotation(targetPointingName);
        return "create:mechanical_crafter".equals(targetBlockId)
                && CreateDirection.parse(facingName).filter(CreateDirection::horizontal).isPresent()
                && facingName.equals(targetFacingName)
                && pointing != null && targetPointing != null
                && Math.abs(pointing - targetPointing) != 180F;
    }

    private static Float crafterRotation(String pointingName) {
        return pointingName == null ? null : CRAFTER_X_ROTATIONS.get(pointingName);
    }

    private static Optional<CreateDirection> nearest(AffineTransform.Point point) {
        CreateDirection nearest = null;
        float best = 0F;
        for (CreateDirection direction : CreateDirection.values()) {
            float dot = point.x() * direction.x()
                    + point.y() * direction.y()
                    + point.z() * direction.z();
            if (nearest == null || dot > best) {
                nearest = direction;
                best = dot;
            }
        }
        return best > 0.99F ? Optional.of(nearest) : Optional.empty();
    }
}
