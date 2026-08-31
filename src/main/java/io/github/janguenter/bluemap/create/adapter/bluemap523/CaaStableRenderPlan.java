/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Stable physical BER parts from exact Create Crafts & Additions 1.6.0. */
record CaaStableRenderPlan(List<StableCoreRenderPlan.Part> parts) {

    CaaStableRenderPlan {
        parts = List.copyOf(parts);
    }

    static Optional<CaaStableRenderPlan> select(
            String blockId,
            Map<String, String> properties
    ) {
        CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                .orElse(null);
        if (facing == null) {
            return Optional.empty();
        }
        return switch (blockId) {
            case "createaddition:alternator" -> Optional.of(new CaaStableRenderPlan(
                    List.of(shaftHalf(facing), shaftHalf(facing.opposite()))
            ));
            case "createaddition:electric_motor" -> Optional.of(
                    new CaaStableRenderPlan(List.of(shaftHalf(facing)))
            );
            case "createaddition:rolling_mill" -> facing.horizontal()
                    ? Optional.of(rollingMill(facing.axis())) : Optional.empty();
            case "createaddition:portable_energy_interface" -> Optional.of(
                    portableEnergy(facing)
            );
            default -> Optional.empty();
        };
    }

    private static CaaStableRenderPlan rollingMill(CreateDirection.Axis axis) {
        float firstYaw = axis == CreateDirection.Axis.Z ? 0F : 90F;
        float secondYaw = axis == CreateDirection.Axis.Z ? 180F : 270F;
        AffineTransform first = AffineTransform.identity()
                .centered().rotateY(firstYaw).uncentered()
                .translate(0F, 0.25F, 0F);
        AffineTransform second = AffineTransform.identity()
                .centered().rotateY(secondYaw).uncentered()
                .translate(0F, 0.25F, 0F);
        return new CaaStableRenderPlan(List.of(
                new StableCoreRenderPlan.Part("create:block/shaft", shaft(axis)),
                new StableCoreRenderPlan.Part("create:block/shaft_half", first),
                new StableCoreRenderPlan.Part("create:block/shaft_half", second)
        ));
    }

    private static CaaStableRenderPlan portableEnergy(CreateDirection facing) {
        float xRotation = facing == CreateDirection.UP ? 0F
                : facing == CreateDirection.DOWN ? 180F : 90F;
        AffineTransform orientation = AffineTransform.identity()
                .centered().rotateY(facing.horizontalAngle())
                .rotateX(xRotation).uncentered();
        return new CaaStableRenderPlan(List.of(
                new StableCoreRenderPlan.Part(
                        "createaddition:block/portable_energy_interface/block_middle",
                        orientation.translate(0F, 0.375F, 0F)
                ),
                new StableCoreRenderPlan.Part(
                        "createaddition:block/portable_energy_interface/block_top",
                        orientation
                )
        ));
    }

    private static StableCoreRenderPlan.Part shaftHalf(CreateDirection facing) {
        return new StableCoreRenderPlan.Part(
                "create:block/shaft_half",
                AffineTransform.identity().centered()
                        .rotateY(facing.horizontalAngle())
                        .rotateX(facing.verticalAngle()).uncentered()
        );
    }

    private static AffineTransform shaft(CreateDirection.Axis axis) {
        return switch (axis) {
            case X -> AffineTransform.identity().centered()
                    .rotateZ(-90F).uncentered();
            case Y -> AffineTransform.identity();
            case Z -> AffineTransform.identity().centered()
                    .rotateX(90F).uncentered();
        };
    }
}
