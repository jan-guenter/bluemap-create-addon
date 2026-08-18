/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

/** Shared bounded size-and-SHA gate for one operator-installed resource archive. */
public final class ExactArtifactDetector {

    private ExactArtifactDetector() {
    }

    public static Optional<Path> find(
            Iterable<Path> roots,
            long expectedSize,
            String expectedSha256
    ) {
        int inspected = 0;
        for (Path root : roots) {
            if (++inspected > 8_192 || Thread.currentThread().isInterrupted()) {
                return Optional.empty();
            }
            try {
                if (root != null && Files.isRegularFile(root)
                        && Files.size(root) == expectedSize
                        && expectedSha256.equals(digest(root))) {
                    return Optional.of(root);
                }
            } catch (IOException ignored) {
                // One unreadable candidate cannot hide a later exact match.
            }
        }
        return Optional.empty();
    }

    private static String digest(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[64 * 1_024];
            try (InputStream input = Files.newInputStream(path)) {
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
