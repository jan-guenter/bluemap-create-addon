/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import io.github.janguenter.bluemap.create.profile.CreateFamilyArtifacts.Profile;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateCatalogProfileTest {

    @Test
    void optionalProfilesAddOnlyTheirOwnExactRoutes() {
        Set<String> core = CreateCatalog.blocks(Set.of(Profile.CREATE));
        assertTrue(core.contains(CreateCatalog.TRACK));
        assertFalse(core.contains(CreateCatalog.AQUATIC_CONDUIT));
        assertFalse(core.contains("createaddition:connector"));
        assertFalse(core.contains("create_hypertube:hypertube"));
        assertFalse(core.contains("create_enchantment_industry:printer"));

        for (Profile profile : Profile.values()) {
            if (profile == Profile.CREATE) {
                continue;
            }
            Set<String> routed = CreateCatalog.blocks(
                    EnumSet.of(Profile.CREATE, profile)
            );
            assertTrue(routed.containsAll(core));
            assertTrue(routed.containsAll(CreateCatalog.PROFILE_BLOCKS.get(profile)));
            for (Profile other : Profile.values()) {
                if (other != Profile.CREATE && other != profile) {
                    assertTrue(CreateCatalog.PROFILE_BLOCKS.get(other).stream()
                            .noneMatch(routed::contains));
                }
            }
        }
    }
}
