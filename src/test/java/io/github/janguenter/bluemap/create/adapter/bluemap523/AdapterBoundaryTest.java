/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.create.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdapterBoundaryTest {

    @Test
    void exposesOneFeatureBackportAdapter() {
        assertEquals(
                "io.github.janguenter.bluemap.create.adapter.bluemap523.BlueMap523Adapter",
                BlueMap523Adapter.class.getName()
        );
    }
}
