/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.dependency;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.magicmq.pyspigot.PyCore;
import me.lucko.jarrelocator.Relocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class which contains all runtime dependencies and relocation rules for the core of PySpigot.
 * <p>
 * Platform-specific implementations also add their respective dependencies and relocations to this registry.
 */
public final class DependencyRegistry {

    private final List<Dependency> dependencies;
    private final List<Relocation> relocations;

    public DependencyRegistry() {
        dependencies = new ArrayList<>();
        relocations = new ArrayList<>();
    }

    /**
     * Load all dependencies into the registry, including core dependencies and platform-specific dependencies.
     */
    public void loadDependencies() {
        loadFromJson(PyCore.get().getResourceAsStream("deps/core-dependencies.json"));

        String platformDependencies = "deps/" + PyCore.get().getDependenciesFileName();
        loadFromJson(PyCore.get().getResourceAsStream(platformDependencies));
    }

    /**
     * Get all dependencies in the registry.
     * @return An immutable list containing all dependencies in the registry
     */
    public List<Dependency> getDependencies() {
        return List.copyOf(dependencies);
    }

    /**
     * Get all relocations in the registry.
     * @return An immutable list containing all relocations in the registry
     */
    public List<Relocation> getRelocations() {
        return List.copyOf(relocations);
    }

    private void loadFromJson(InputStream stream) {
        try (Reader reader = new InputStreamReader(stream)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray dependencies = root.getAsJsonArray("dependencies");
            for (JsonElement element : dependencies) {
                JsonObject dependency = element.getAsJsonObject();
                this.dependencies.add(Dependency.builder()
                        .groupId(dependency.get("groupId").getAsString())
                        .artifactId(dependency.get("artifactId").getAsString())
                        .version(dependency.get("version").getAsString())
                        .checksum(dependency.get("checksum").getAsString())
                        .build());
            }

            JsonArray relocations = root.getAsJsonArray("relocations");
            for (JsonElement element : relocations) {
                JsonObject relocation = element.getAsJsonObject();
                this.relocations.add(new Relocation(
                        relocation.get("pattern").getAsString(),
                        relocation.get("shadedPattern").getAsString()
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when reading dependencies JSON file", e);
        }
    }
}
