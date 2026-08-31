/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.Optional;

/** Exact JSON-equivalent orientation for the powered-shaft BER partial. */
record PoweredShaftRenderPlan(float xRotation, float yRotation) {

    static Optional<PoweredShaftRenderPlan> select(String axis) {
        if (axis == null) {
            return Optional.empty();
        }
        return switch (axis) {
            case "x" -> Optional.of(new PoweredShaftRenderPlan(90F, 90F));
            case "y" -> Optional.of(new PoweredShaftRenderPlan(0F, 0F));
            case "z" -> Optional.of(new PoweredShaftRenderPlan(90F, 180F));
            default -> Optional.empty();
        };
    }
}
