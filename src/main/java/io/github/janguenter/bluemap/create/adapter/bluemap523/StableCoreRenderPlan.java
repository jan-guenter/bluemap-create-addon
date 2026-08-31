/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Frozen, content-free physical parts omitted by Create's block models. */
record StableCoreRenderPlan(List<Part> parts) {

    record Part(String model, AffineTransform transform) {
    }

    StableCoreRenderPlan {
        parts = List.copyOf(parts);
    }

    static Optional<StableCoreRenderPlan> saw(Map<String, String> properties) {
        CreateDirection facing = facing(properties);
        if (facing == null) {
            return Optional.empty();
        }
        boolean alongFirst = flag(properties, "axis_along_first");
        ArrayList<Part> parts = new ArrayList<>(2);
        AffineTransform blade = partialFacing(facing);
        if (facing.horizontal()) {
            parts.add(new Part(
                    "create:block/mechanical_saw/blade_horizontal_inactive", blade
            ));
            parts.add(new Part(
                    "create:block/shaft_half", partialFacing(facing.opposite())
            ));
        } else {
            if (alongFirst) {
                blade = blade.centered().rotateY(90F).uncentered();
            }
            parts.add(new Part(
                    "create:block/mechanical_saw/blade_vertical_inactive", blade
            ));
            parts.add(new Part("create:block/shaft", shaft(
                    directionalAxis(facing, alongFirst)
            )));
        }
        return Optional.of(new StableCoreRenderPlan(parts));
    }

    static Optional<StableCoreRenderPlan> deployer(Map<String, String> properties) {
        CreateDirection facing = facing(properties);
        if (facing == null) {
            return Optional.empty();
        }
        boolean alongFirst = flag(properties, "axis_along_first");
        AffineTransform common = partialFacing(facing);
        boolean rotatePole = alongFirst ^ facing.axis() == CreateDirection.Axis.Z;
        AffineTransform pole = rotatePole
                ? common.centered().rotateZ(90F).uncentered() : common;
        return Optional.of(new StableCoreRenderPlan(List.of(
                new Part("create:block/shaft", shaft(directionalAxis(facing, alongFirst))),
                new Part("create:block/deployer/pole", pole),
                new Part("create:block/deployer/hand_pointing", common)
        )));
    }

    static StableCoreRenderPlan millstone() {
        return new StableCoreRenderPlan(List.of(new Part(
                "create:block/millstone/inner", AffineTransform.identity()
        )));
    }

    static Optional<StableCoreRenderPlan> portable(
            String blockId,
            Map<String, String> properties
    ) {
        CreateDirection facing = facing(properties);
        String directory = switch (blockId) {
            case "create:portable_storage_interface" -> "portable_storage_interface";
            case "create:portable_fluid_interface" -> "portable_fluid_interface";
            default -> null;
        };
        if (facing == null || directory == null) {
            return Optional.empty();
        }
        float xRotation = facing == CreateDirection.UP ? 0F
                : facing == CreateDirection.DOWN ? 180F : 90F;
        AffineTransform orientation = AffineTransform.identity()
                .centered()
                .rotateY(facing.horizontalAngle())
                .rotateX(xRotation)
                .uncentered();
        return Optional.of(new StableCoreRenderPlan(List.of(
                new Part(
                        "create:block/" + directory + "/block_middle",
                        orientation.translate(0F, 0.375F, 0F)
                ),
                new Part("create:block/" + directory + "/block_top", orientation)
        )));
    }

    static StableCoreRenderPlan arm(boolean ceiling, boolean goggles) {
        AffineTransform root = AffineTransform.identity().centered();
        if (ceiling) {
            root = root.rotateX(180F);
        }
        AffineTransform base = root.translate(0F, 0.25F, 0F);
        AffineTransform lower = base.translate(0F, 0.125F, 0F).rotateX(135F);
        AffineTransform upper = lower.translate(0F, 0F, -14F / 16F)
                .rotateX(-135F);
        AffineTransform head = upper.translate(0F, 0F, -15F / 16F)
                .rotateX(-45F);
        AffineTransform claw = ceiling ? head.rotateZ(180F) : head;
        AffineTransform lowerGrip = head.translate(0F, -1F / 16F, -6F / 16F);
        AffineTransform upperGrip = head.translate(0F, 1F / 16F, -6F / 16F);
        return new StableCoreRenderPlan(List.of(
                new Part("create:block/mechanical_arm/cog", AffineTransform.identity()),
                new Part("create:block/mechanical_arm/base", base.uncentered()),
                new Part("create:block/mechanical_arm/lower_body", lower.uncentered()),
                new Part("create:block/mechanical_arm/upper_body", upper.uncentered()),
                new Part(
                        "create:block/mechanical_arm/claw_base"
                                + (goggles ? "_goggles" : ""),
                        claw.uncentered()
                ),
                new Part("create:block/mechanical_arm/lower_claw_grip",
                        lowerGrip.uncentered()),
                new Part("create:block/mechanical_arm/upper_claw_grip",
                        upperGrip.uncentered())
        ));
    }

    private static AffineTransform partialFacing(CreateDirection facing) {
        return AffineTransform.identity()
                .centered()
                .rotateY(facing.horizontalAngle())
                .rotateX(facing.verticalAngle())
                .uncentered();
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

    private static CreateDirection.Axis directionalAxis(
            CreateDirection facing,
            boolean alongFirst
    ) {
        return switch (facing.axis()) {
            case X -> alongFirst ? CreateDirection.Axis.Y : CreateDirection.Axis.Z;
            case Y -> alongFirst ? CreateDirection.Axis.X : CreateDirection.Axis.Z;
            case Z -> alongFirst ? CreateDirection.Axis.X : CreateDirection.Axis.Y;
        };
    }

    private static CreateDirection facing(Map<String, String> properties) {
        return CreateDirection.parse(properties.get("facing")).orElse(null);
    }

    private static boolean flag(Map<String, String> properties, String name) {
        return "true".equals(properties.get(name));
    }
}
