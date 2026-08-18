/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.create.model.CreateObjModel;

import java.util.Map;

/** Parsed installed OBJ plus resolved texture paths. */
record CompiledCreateObj(CreateObjModel model, Map<String, Key> materials) {

    CompiledCreateObj {
        materials = Map.copyOf(materials);
    }
}
