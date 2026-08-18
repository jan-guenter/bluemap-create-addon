/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;

/** Resource-pack extension factory registered before resource loading. */
final class CreateResourceExtensionType
        implements ResourcePack.Extension<CreateResourceExtension> {

    private static final Key KEY = Key.parse("bluemap_create:prototype");

    private final BlockRendererType renderer;
    private final CreateRuntime runtime;

    CreateResourceExtensionType(BlockRendererType renderer, CreateRuntime runtime) {
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public CreateResourceExtension create(ResourcePack pack) {
        return new CreateResourceExtension(pack, renderer, runtime);
    }
}
