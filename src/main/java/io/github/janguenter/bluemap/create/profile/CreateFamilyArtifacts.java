/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.profile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Independently exact-gated Create-family resource inputs for ATMons 1.2.0. */
public final class CreateFamilyArtifacts {

    /** One independently optional rendering lane. Core Create remains mandatory. */
    public enum Profile {
        CREATE(
                19_123_767L,
                "ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a"
        ),
        AQUATIC_AMBITIONS(
                1_074_131L,
                "d50180fd30dc7f034ea4ad5185d18cfa652457be1d8e7a45f0b491d0e6642d44"
        ),
        CRAFTS_AND_ADDITIONS(
                1_661_802L,
                "41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9"
        ),
        HYPERTUBE(
                546_142L,
                "7bdb8979c7ff7d3b29f7a23771b6ae4870a6dcb7ce2e4a3214fdd6059aacace8"
        ),
        ENCHANTMENT_INDUSTRY(
                1_573_021L,
                "02a184531c11433cd6521f612982568398aaf510b8ff51e052a78cf7d09d9a49"
        );

        private final long size;
        private final String sha256;

        Profile(long size, String sha256) {
            this.size = size;
            this.sha256 = sha256;
        }

        public long size() {
            return size;
        }

        public String sha256() {
            return sha256;
        }
    }

    private final Map<Profile, Path> paths;

    private CreateFamilyArtifacts(Map<Profile, Path> paths) {
        this.paths = Collections.unmodifiableMap(new EnumMap<>(paths));
    }

    public static CreateFamilyArtifacts detect(Iterable<Path> roots) {
        List<Path> bounded = new ArrayList<>();
        int inspected = 0;
        for (Path root : roots) {
            if (++inspected > 8_192 || Thread.currentThread().isInterrupted()) {
                break;
            }
            bounded.add(root);
        }
        EnumMap<Profile, Path> matches = new EnumMap<>(Profile.class);
        for (Profile profile : Profile.values()) {
            ExactArtifactDetector.find(bounded, profile.size, profile.sha256)
                    .ifPresent(path -> matches.put(profile, path));
        }
        return new CreateFamilyArtifacts(matches);
    }

    public boolean has(Profile profile) {
        return paths.containsKey(profile);
    }

    public Optional<Path> path(Profile profile) {
        return Optional.ofNullable(paths.get(profile));
    }

    public Set<Profile> profiles() {
        return paths.keySet();
    }
}
