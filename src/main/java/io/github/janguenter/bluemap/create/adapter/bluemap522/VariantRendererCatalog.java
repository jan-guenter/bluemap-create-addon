/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Multipart;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/** Exact original renderer identity for each wrapped Create variant. */
final class VariantRendererCatalog {

    private static final Key EMPTY_CARRIER = Key.parse("minecraft:block/air");
    private static final Set<String> DYNAMIC_MULTIPART_BLOCKS = Set.of(
            "create:fluid_pipe",
            "create:encased_fluid_pipe",
            "create_hypertube:hypertube",
            "create_hypertube:hypertube_accelerator",
            "create_hypertube:hypertube_entrance",
            "create_hypertube:hypertube_junction",
            "createaddition:connector",
            "createaddition:large_connector",
            "createaddition:small_light_connector"
    );

    private final Map<Variant, BlockRendererType> originals;

    private VariantRendererCatalog(Map<Variant, BlockRendererType> originals) {
        this.originals = Collections.unmodifiableMap(originals);
    }

    static VariantRendererCatalog wrap(ResourcePack pack, BlockRendererType wrapper) {
        return wrap(pack, wrapper, CreateCatalog.CUSTOM_BLOCKS);
    }

    static VariantRendererCatalog wrap(
            ResourcePack pack,
            BlockRendererType wrapper,
            Set<String> routedBlocks
    ) {
        IdentityHashMap<Variant, BlockRendererType> originals = new IdentityHashMap<>();
        routedBlocks.forEach(id -> {
            Key blockKey = Key.parse(id);
            var state = pack.getBlockStates().get(blockKey);
            if (state != null) {
                if (DYNAMIC_MULTIPART_BLOCKS.contains(id)
                        && installCarrier(
                                pack, blockKey, state, wrapper, originals
                        )) {
                    return;
                }
                state.forEach(variant -> {
                    if (variant.getRenderer() != wrapper) {
                        originals.put(variant, variant.getRenderer());
                        variant.setRenderer(wrapper);
                    }
                });
            }
        });
        return new VariantRendererCatalog(originals);
    }

    /**
     * Adds one unconditional empty carrier for a dynamic multipart renderer.
     *
     * <p>BlueMap dispatches once per matching authored part. A carrier both
     * covers legal zero-match states (notably high-degree regular pipes) and
     * prevents dynamic geometry from being duplicated on multipart Hypertube
     * states. Authored parts keep their stock renderers.</p>
     */
    private static boolean installCarrier(
            ResourcePack pack,
            Key blockKey,
            BlockState state,
            BlockRendererType wrapper,
            IdentityHashMap<Variant, BlockRendererType> originals
    ) {
        Multipart multipart = state.getMultipart();
        if (multipart == null) {
            return false;
        }

        Model empty = pack.getModels().get(EMPTY_CARRIER);
        if (empty == null || empty.getElements() != null
                && empty.getElements().length != 0) {
            return false;
        }

        for (VariantSet part : multipart.getParts()) {
            for (Variant variant : part.getVariants()) {
                if (EMPTY_CARRIER.equals(variant.getModel())) {
                    variant.setRenderer(wrapper);
                    originals.put(variant, BlockRendererType.DEFAULT);
                    return true;
                }
            }
        }

        Variant carrier = new Variant(new ResourcePath<Model>(EMPTY_CARRIER));
        carrier.setRenderer(wrapper);
        VariantSet[] parts = Arrays.copyOf(
                multipart.getParts(), multipart.getParts().length + 1
        );
        parts[parts.length - 1] = new VariantSet(carrier);
        pack.getBlockStates().put(blockKey, new BlockState(new Multipart(parts)));
        originals.put(carrier, BlockRendererType.DEFAULT);
        return true;
    }

    BlockRendererType original(Variant variant) {
        return originals.getOrDefault(variant, BlockRendererType.DEFAULT);
    }

    int size() {
        return originals.size();
    }
}
