/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Emits Create's exact authored frozen belt surfaces and endpoint pulleys. */
final class BeltEmitter {

    private final ResourcePack resourcePack;
    private final ResourceModelRenderer models;

    BeltEmitter(ResourcePack resourcePack, ResourceModelRenderer models) {
        this.resourcePack = resourcePack;
        this.models = models;
    }

    boolean emit(BlockNeighborhood block, TileModelView target, Color mapColor) {
        BeltRenderPlan plan = BeltRenderPlan.select(
                block.getBlockState().getProperties()
        ).orElse(null);
        if (plan == null) {
            return false;
        }
        Key mapTextureKey = Key.parse(plan.mapTexture());
        Texture mapTexture = resourcePack.getTextures().get(mapTextureKey);
        if (mapTexture == null) {
            return false;
        }

        int start = target.getTileModel().size();
        for (String model : plan.models()) {
            if (!emitModel(model, plan.transform(), block, target)) {
                reset(target, start);
                return false;
            }
        }
        if (plan.pulleyTransform().isPresent() && !emitModel(
                "create:block/belt_pulley",
                plan.pulleyTransform().orElseThrow(),
                block,
                target
        )) {
            reset(target, start);
            return false;
        }
        mapColor.add(new Color().set(mapTexture.getColorPremultiplied()));
        mapColor.flatten().straight();
        target.initialize(start);
        return true;
    }

    private boolean emitModel(
            String model,
            AffineTransform transform,
            BlockNeighborhood block,
            TileModelView target
    ) {
        int start = target.getTileModel().size();
        models.render(
                block,
                new Variant(new ResourcePath<Model>(model)),
                target.initialize(),
                new Color().set(0F, 0F, 0F, 0F, true)
        );
        if (target.getTileModel().size() == start) {
            return false;
        }
        apply(target.initialize(start), transform);
        return true;
    }

    static void apply(TileModelView target, AffineTransform transform) {
        float[] value = transform.copyValues();
        target.transform(
                value[0], value[1], value[2], value[3],
                value[4], value[5], value[6], value[7],
                value[8], value[9], value[10], value[11],
                value[12], value[13], value[14], value[15]
        );
    }

    private static void reset(TileModelView target, int start) {
        target.getTileModel().reset(start);
        target.initialize(start);
    }
}
