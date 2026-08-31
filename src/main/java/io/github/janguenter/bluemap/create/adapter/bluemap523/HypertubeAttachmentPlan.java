/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Exact inactive/no-cog Hypertube attachment transforms. */
record HypertubeAttachmentPlan(List<StableCoreRenderPlan.Part> parts) {

    HypertubeAttachmentPlan {
        parts = List.copyOf(parts);
    }

    static HypertubeAttachmentPlan select(
            String blockId,
            Map<String, String> properties,
            List<SavedAttachment> saved
    ) {
        if ("create_hypertube:hypertube".equals(blockId)) {
            return new HypertubeAttachmentPlan(List.of());
        }
        CreateDirection tubeFacing = CreateDirection.parse(properties.get("facing"))
                .orElse(null);
        if (tubeFacing == null) {
            return new HypertubeAttachmentPlan(List.of());
        }
        boolean vertical = !"create_hypertube:hypertube_junction".equals(blockId)
                && tubeFacing.axis() == CreateDirection.Axis.Y;
        ArrayList<StableCoreRenderPlan.Part> parts = new ArrayList<>();
        if ("create_hypertube:hypertube_accelerator".equals(blockId)
                || "create_hypertube:hypertube_entrance".equals(blockId)) {
            parts.add(new StableCoreRenderPlan.Part(
                    "create_hypertube:block/hypertube_entrance/cogwheel_hole",
                    AffineTransform.identity().centered()
                            .rotateY(tubeFacing.horizontalAngle())
                            .rotateX(tubeFacing.verticalAngle()).uncentered()
            ));
        }
        for (SavedAttachment attachment : saved == null ? List.<SavedAttachment>of() : saved) {
            CreateDirection direction = attachment.face().opposite();
            AffineTransform transform = AffineTransform.identity().centered();
            transform = switch (direction) {
                case NORTH -> transform.rotateY(-90F);
                case EAST -> transform.rotateY(180F);
                case SOUTH -> transform.rotateY(90F);
                case WEST -> transform;
                case UP -> transform.rotateZ(-90F);
                case DOWN -> transform.rotateZ(90F);
            };
            transform = transform.uncentered();
            boolean early = direction.axis() == CreateDirection.Axis.Y
                    && tubeFacing.axis() == CreateDirection.Axis.X;
            if (!vertical && !early) {
                transform = transform.centered().rotateX(90F).uncentered();
            }
            String model = "redstone_input".equals(attachment.type())
                    ? "create_hypertube:block/redstone_detector_tube_attachment_no_cog"
                    : "create_hypertube:block/tube_scanner_attachment_no_cog";
            parts.add(new StableCoreRenderPlan.Part(model, transform));
        }
        return new HypertubeAttachmentPlan(parts);
    }

    record SavedAttachment(CreateDirection face, String type) {
    }
}
