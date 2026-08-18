/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.profile;

import java.nio.file.Path;
import java.util.Optional;

/** Activates only for the exact Create 6.0.10 artifact in ATMons 1.2.0. */
public final class ExactCreateArtifactDetector {

    private static final long SIZE = 19_123_767L;
    private static final String SHA256 =
            "ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a";

    private ExactCreateArtifactDetector() {
    }

    public static boolean matches(Iterable<Path> roots) {
        return find(roots).isPresent();
    }

    /** Returns the exact installed core resource archive when present. */
    public static Optional<Path> find(Iterable<Path> roots) {
        return ExactArtifactDetector.find(roots, SIZE, SHA256);
    }
}
