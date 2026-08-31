/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockStateModelRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.Locale;

/** Appends frozen pipe rims/connections and a persisted decorative bracket. */
final class PipeAttachmentEmitter {

    private final ResourcePack resourcePack;
    private final ResourceModelRenderer partials;
    private final BlockStateModelRenderer materials;

    PipeAttachmentEmitter(
            ResourcePack resourcePack,
            ResourceModelRenderer partials,
            BlockStateModelRenderer materials
    ) {
        this.resourcePack = resourcePack;
        this.partials = partials;
        this.materials = materials;
    }

    void emit(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        for (Direction direction : Direction.values()) {
            var offset = direction.toVector();
            var neighbor = block.getNeighborBlock(
                    offset.getX(), offset.getY(), offset.getZ()
            );
            PipeAttachmentSelector.Neighbor selectedNeighbor =
                    new PipeAttachmentSelector.Neighbor(
                            neighbor.getBlockState().getId().getFormatted(),
                            neighbor.getBlockState().getProperties(),
                            neighbor.getBlockEntity() instanceof PipeBlockEntityData data
                                    && data.bracket() != null && !data.bracket().isAir()
                    );
            CreateDirection selectedDirection = CreateDirection.valueOf(direction.name());
            for (PipeAttachmentSelector.Component component : PipeAttachmentSelector.select(
                    blockId,
                    block.getBlockState().getProperties(),
                    selectedDirection,
                    selectedNeighbor
            )) {
                render(component.modelName(), direction, block, target);
            }
        }
        if (PipeAttachmentSelector.shouldRenderCasing(
                blockId, block.getBlockState().getProperties()
        )) {
            render("create:block/fluid_pipe/casing", block, target);
        }
        if (block.getBlockEntity() instanceof PipeBlockEntityData data) {
            BlockState bracket = data.bracket();
            if (bracket != null && !bracket.isAir()
                    && resourcePack.getBlockState(bracket) != null) {
                materials.render(block, bracket, target.initialize(), mapColor);
            }
        }
    }

    private void render(
            String component,
            Direction direction,
            BlockNeighborhood block,
            TileModelView target
    ) {
        String directionName = direction.name().toLowerCase(Locale.ROOT);
        Variant model = new Variant(new ResourcePath<Model>(
                "create:block/fluid_pipe/" + component + '/' + directionName
        ));
        partials.render(
                block, model, target.initialize(),
                new Color().set(0F, 0F, 0F, 0F, true)
        );
    }

    private void render(
            String modelId,
            BlockNeighborhood block,
            TileModelView target
    ) {
        partials.render(
                block,
                new Variant(new ResourcePath<Model>(modelId)),
                target.initialize(),
                new Color().set(0F, 0F, 0F, 0F, true)
        );
    }

}
