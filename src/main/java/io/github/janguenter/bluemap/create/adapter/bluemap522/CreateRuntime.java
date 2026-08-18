/* SPDX-License-Identifier: MIT */
package io.github.janguenter.bluemap.create.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.create.profile.CreateFamilyArtifacts.Profile;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Shared exact-profile activation and bounded diagnostic state. */
final class CreateRuntime {

    static final CreateRuntime INSTANCE = new CreateRuntime();

    private final AtomicBoolean active = new AtomicBoolean();
    private final AtomicInteger diagnostics = new AtomicInteger();
    private final Map<ResourcePack, VariantRendererCatalog> catalogs = new WeakHashMap<>();
    private final Map<ResourcePack, Map<String, CompiledCreateObj>> objects =
            new WeakHashMap<>();
    private final Map<ResourcePack, Set<Profile>> profiles = new WeakHashMap<>();

    private CreateRuntime() {
    }

    boolean active() {
        return active.get();
    }

    void activate() {
        active.set(true);
    }

    void inactive(String reason) {
        active.set(false);
        report("inactive-" + reason);
    }

    synchronized void catalog(ResourcePack pack, VariantRendererCatalog catalog) {
        catalogs.put(pack, catalog);
    }

    synchronized VariantRendererCatalog catalog(ResourcePack pack) {
        return catalogs.get(pack);
    }

    synchronized void objects(ResourcePack pack, Map<String, CompiledCreateObj> compiled) {
        objects.put(pack, Map.copyOf(compiled));
    }

    synchronized Map<String, CompiledCreateObj> objects(ResourcePack pack) {
        return objects.getOrDefault(pack, Map.of());
    }

    synchronized void profiles(ResourcePack pack, Set<Profile> enabled) {
        profiles.put(pack, Set.copyOf(enabled));
    }

    synchronized Set<Profile> profiles(ResourcePack pack) {
        return profiles.getOrDefault(pack, Set.of());
    }

    void report(String reason) {
        if (diagnostics.incrementAndGet() <= 16) {
            System.err.println("BlueMap Create add-on: " + reason + '.');
        }
    }

    @SuppressWarnings("removal")
    static void throwIfFatal(Error error) {
        if (error instanceof OutOfMemoryError outOfMemory) {
            throw outOfMemory;
        }
        if (error instanceof ThreadDeath threadDeath) {
            throw threadDeath;
        }
    }
}
