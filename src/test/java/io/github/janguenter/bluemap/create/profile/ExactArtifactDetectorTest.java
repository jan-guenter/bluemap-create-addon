/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExactArtifactDetectorTest {

    private static final String ABC_SHA256 =
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @TempDir
    Path directory;

    @Test
    void requiresBothExactSizeAndDigestAndContinuesAfterDecoys() throws IOException {
        Path wrongSize = Files.writeString(directory.resolve("wrong-size.jar"), "ab");
        Path wrongDigest = Files.writeString(directory.resolve("wrong-digest.jar"), "abd");
        Path exact = Files.writeString(directory.resolve("exact.jar"), "abc");

        assertEquals(
                exact,
                ExactArtifactDetector.find(
                        List.of(directory, wrongSize, wrongDigest, exact), 3, ABC_SHA256
                ).orElseThrow()
        );
        assertTrue(ExactArtifactDetector.find(List.of(wrongSize), 3, ABC_SHA256).isEmpty());
        assertTrue(ExactArtifactDetector.find(List.of(wrongDigest), 3, ABC_SHA256).isEmpty());
    }

    @Test
    void familyProfilesRetainIndependentAtmons120Identities() {
        assertProfile(
                CreateFamilyArtifacts.Profile.CREATE,
                19_123_767L,
                "ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a"
        );
        assertProfile(
                CreateFamilyArtifacts.Profile.AQUATIC_AMBITIONS,
                1_074_131L,
                "d50180fd30dc7f034ea4ad5185d18cfa652457be1d8e7a45f0b491d0e6642d44"
        );
        assertProfile(
                CreateFamilyArtifacts.Profile.CRAFTS_AND_ADDITIONS,
                1_661_802L,
                "41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9"
        );
        assertProfile(
                CreateFamilyArtifacts.Profile.HYPERTUBE,
                546_142L,
                "7bdb8979c7ff7d3b29f7a23771b6ae4870a6dcb7ce2e4a3214fdd6059aacace8"
        );
        assertProfile(
                CreateFamilyArtifacts.Profile.ENCHANTMENT_INDUSTRY,
                1_573_021L,
                "02a184531c11433cd6521f612982568398aaf510b8ff51e052a78cf7d09d9a49"
        );
        assertEquals(5, CreateFamilyArtifacts.Profile.values().length);
    }

    @Test
    void absentFamilyInputsRemainIndependentlyDisabled() {
        CreateFamilyArtifacts artifacts = CreateFamilyArtifacts.detect(List.of(directory));
        for (CreateFamilyArtifacts.Profile profile : CreateFamilyArtifacts.Profile.values()) {
            assertFalse(artifacts.has(profile));
            assertTrue(artifacts.path(profile).isEmpty());
        }
    }

    private static void assertProfile(
            CreateFamilyArtifacts.Profile profile,
            long size,
            String sha256
    ) {
        assertEquals(size, profile.size());
        assertEquals(sha256, profile.sha256());
    }
}
