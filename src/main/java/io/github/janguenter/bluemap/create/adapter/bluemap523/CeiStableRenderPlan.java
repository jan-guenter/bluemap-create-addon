/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Stable material CEI partials with contents and processing motion omitted. */
record CeiStableRenderPlan(List<StableCoreRenderPlan.Part> parts) {

    CeiStableRenderPlan {
        parts = List.copyOf(parts);
    }

    static Optional<CeiStableRenderPlan> select(
            String blockId,
            Map<String, String> properties,
            boolean powered
    ) {
        return switch (blockId) {
            case "create_enchantment_industry:printer" -> Optional.of(parts(
                    part("create_enchantment_industry:block/printer/nozzle_top"),
                    part("create_enchantment_industry:block/printer/nozzle_bottom"),
                    part("create_enchantment_industry:block/printer/piston")
            ));
            case "create_enchantment_industry:brass_bookshelf" -> Optional.of(parts(
                    new StableCoreRenderPlan.Part(
                            "create:block/shaft_half", partialFacing(CreateDirection.DOWN)
                    )
            ));
            case "create_enchantment_industry:ender_woven_bag" ->
                    bag(properties).map(CeiStableRenderPlan::new);
            case "create_enchantment_industry:affix_augmentor" ->
                    augmentor(properties, powered).map(CeiStableRenderPlan::new);
            case "create_enchantment_industry:gem_cutter" ->
                    gemCutter(properties, powered).map(CeiStableRenderPlan::new);
            case "create_enchantment_industry:infuser" -> Optional.of(parts(
                    translated("create_enchantment_industry:block/infuser/eterna_needle"),
                    translated("create_enchantment_industry:block/infuser/arcana_needle"),
                    translated("create_enchantment_industry:block/infuser/quanta_needle")
            ));
            default -> Optional.empty();
        };
    }

    static Optional<Grindstone> grindstone(Map<String, String> properties) {
        CreateDirection facing = CreateDirection.parse(properties.get("facing"))
                .filter(CreateDirection::horizontal).orElse(null);
        if (facing == null) {
            return Optional.empty();
        }
        return Optional.of(switch (facing.axis()) {
            case X -> new Grindstone(90F, 90F);
            case Z -> new Grindstone(90F, 180F);
            case Y -> throw new IllegalStateException("horizontal facing has vertical axis");
        });
    }

    static Optional<StableCoreRenderPlan.Part> support(
            String belowId,
            Map<String, String> belowProperties
    ) {
        if ("create:depot".equals(belowId)) {
            return Optional.of(new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/belt_casing/special_top_only",
                    AffineTransform.identity().translate(0F, -1F, 0F)
            ));
        }
        if (!"create:belt".equals(belowId)
                || !"horizontal".equals(belowProperties.get("slope"))
                || "true".equals(belowProperties.get("casing"))) {
            return Optional.empty();
        }
        CreateDirection facing = CreateDirection.parse(belowProperties.get("facing"))
                .filter(CreateDirection::horizontal).orElse(null);
        if (facing == null) {
            return Optional.empty();
        }
        String model = "middle".equals(belowProperties.get("part"))
                ? "create_enchantment_industry:block/belt_casing/special"
                : "create_enchantment_industry:block/belt_casing/special_with_shaft";
        return Optional.of(new StableCoreRenderPlan.Part(
                model,
                AffineTransform.identity().translate(0F, -1F, 0F)
                        .centered().rotateY(facing.horizontalAngle()).uncentered()
        ));
    }

    private static Optional<List<StableCoreRenderPlan.Part>> bag(
            Map<String, String> properties
    ) {
        CreateDirection stateFacing = CreateDirection.parse(properties.get("facing"))
                .filter(CreateDirection::horizontal).orElse(null);
        if (stateFacing == null) {
            return Optional.empty();
        }
        CreateDirection facing = stateFacing.opposite();
        float rotation = -toYRotation(facing);
        AffineTransform light = AffineTransform.identity()
                .centered().rotateY(rotation).uncentered();
        AffineTransform pocket = AffineTransform.identity()
                .translate(facing.x() * -6F / 16F, 0F, facing.z() * -6F / 16F)
                .centered().rotateY(rotation).uncentered();
        return Optional.of(List.of(
                new StableCoreRenderPlan.Part(
                        "create_enchantment_industry:block/ender_woven_bag/light_off",
                        light
                ),
                new StableCoreRenderPlan.Part(
                        "create_enchantment_industry:block/ender_woven_bag/open_pocket",
                        pocket
                )
        ));
    }

    private static Optional<List<StableCoreRenderPlan.Part>> augmentor(
            Map<String, String> properties,
            boolean powered
    ) {
        CreateDirection facing = horizontalFacing(properties);
        if (facing == null) {
            return Optional.empty();
        }
        AffineTransform base = partialFacing(facing);
        ArrayList<StableCoreRenderPlan.Part> parts = new ArrayList<>();
        parts.add(new StableCoreRenderPlan.Part(
                "create_enchantment_industry:block/affix_augmentor/plate"
                        + (powered ? "_powered" : ""),
                powered ? base : base.translate(0F, .99F / 16F, 0F)
        ));
        if (powered) {
            parts.add(new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/affix_augmentor/big_column", base
            ));
            parts.add(new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/affix_augmentor/small_column", base
            ));
            parts.add(new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/affix_augmentor/needle", base
            ));
        }
        return Optional.of(List.copyOf(parts));
    }

    private static Optional<List<StableCoreRenderPlan.Part>> gemCutter(
            Map<String, String> properties,
            boolean powered
    ) {
        CreateDirection facing = horizontalFacing(properties);
        if (facing == null) {
            return Optional.empty();
        }
        String suffix = powered ? "_powered" : "";
        AffineTransform base = partialFacing(facing);
        ArrayList<StableCoreRenderPlan.Part> parts = new ArrayList<>();
        parts.add(new StableCoreRenderPlan.Part(
                "create_enchantment_industry:block/gem_cutter/crystal_needle" + suffix,
                base
        ));
        if (powered) {
            parts.add(new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/gem_cutter/vertical_aligned" + suffix,
                    base
            ));
            parts.add(new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/gem_cutter/vertical" + suffix,
                    base
            ));
            parts.add(new StableCoreRenderPlan.Part(
                    "create_enchantment_industry:block/gem_cutter/horizontal" + suffix,
                    base
            ));
            return Optional.of(List.copyOf(parts));
        }
        AffineTransform verticalAligned = rotateCentered(
                base, clockwiseAxis(facing), 180F, 1F
        );
        AffineTransform vertical = rotateCentered(
                rotateCentered(base, facing.axis(), 180F, .95F),
                CreateDirection.Axis.Y, 270F, 1F
        );
        AffineTransform horizontal = rotateCentered(
                rotateCentered(
                        rotateCentered(base, clockwiseAxis(facing), 180F, .9F),
                        CreateDirection.Axis.Y, 270F, 1F
                ), facing.axis(), 90F, 1F
        );
        parts.add(new StableCoreRenderPlan.Part(
                "create_enchantment_industry:block/gem_cutter/vertical_aligned",
                verticalAligned
        ));
        parts.add(new StableCoreRenderPlan.Part(
                "create_enchantment_industry:block/gem_cutter/vertical", vertical
        ));
        parts.add(new StableCoreRenderPlan.Part(
                "create_enchantment_industry:block/gem_cutter/horizontal", horizontal
        ));
        return Optional.of(List.copyOf(parts));
    }

    private static AffineTransform rotateCentered(
            AffineTransform transform,
            CreateDirection.Axis axis,
            float degrees,
            float scale
    ) {
        AffineTransform result = transform.centered().scale(scale, scale, scale);
        result = switch (axis) {
            case X -> result.rotateX(degrees);
            case Y -> result.rotateY(degrees);
            case Z -> result.rotateZ(degrees);
        };
        return result.uncentered();
    }

    private static CreateDirection.Axis clockwiseAxis(CreateDirection facing) {
        return facing.axis() == CreateDirection.Axis.X
                ? CreateDirection.Axis.Z : CreateDirection.Axis.X;
    }

    private static StableCoreRenderPlan.Part translated(String model) {
        return new StableCoreRenderPlan.Part(
                model, AffineTransform.identity().translate(0F, -.001F, 0F)
        );
    }

    private static CeiStableRenderPlan parts(StableCoreRenderPlan.Part... parts) {
        return new CeiStableRenderPlan(List.of(parts));
    }

    private static StableCoreRenderPlan.Part part(String model) {
        return new StableCoreRenderPlan.Part(model, AffineTransform.identity());
    }

    private static CreateDirection horizontalFacing(Map<String, String> properties) {
        return CreateDirection.parse(properties.get("facing"))
                .filter(CreateDirection::horizontal).orElse(null);
    }

    private static AffineTransform partialFacing(CreateDirection facing) {
        return AffineTransform.identity().centered()
                .rotateY(facing.horizontalAngle())
                .rotateX(facing.verticalAngle()).uncentered();
    }

    private static float toYRotation(CreateDirection direction) {
        return switch (direction) {
            case SOUTH -> 0F;
            case WEST -> 90F;
            case NORTH -> 180F;
            case EAST -> 270F;
            case UP, DOWN -> throw new IllegalArgumentException("vertical direction");
        };
    }

    record Grindstone(float xRotation, float yRotation) {
    }
}
