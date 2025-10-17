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

package dev.magicmq.pyspigot.velocity;


import dev.magicmq.pyspigot.dependency.Dependency;

import static dev.magicmq.pyspigot.dependency.DependencyRegistry.RELOCATION_PREFIX;
import static dev.magicmq.pyspigot.dependency.DependencyRegistry.addDependency;
import static dev.magicmq.pyspigot.dependency.DependencyRegistry.addRelocation;

/**
 * A utility class which contains all runtime dependencies for the Velocity implementation.
 */
public final class VelocityDependencies {

    /**
     * Add all Velocity-specific dependencies to the core {@link dev.magicmq.pyspigot.dependency.DependencyRegistry}.
     */
    public static void addToRegistry() {
        //bstats-velocity
        addDependency(Dependency.builder()
                .groupId("org{}bstats")
                .artifactId("bstats-velocity")
                .version("3.1.0")
                .checksum("PLta9QmwXhyQNGF+nzxiZQxsVM01opQ0gvLjuDAuc7Y=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}bstats")
                .artifactId("bstats-base")
                .version("3.1.0")
                .checksum("2g5RgIzYzCi+8wAOcctPoLBeU5Mag6f59NPEAoNvQKA=")
                .build());

        //mysql-connector-j
        addDependency(Dependency.builder()
                .groupId("com{}mysql")
                .artifactId("mysql-connector-j")
                .version("9.4.0")
                .checksum("Se2TyLK+qcsJKbhaiiiDexkdD46saRn9zvFuNuLNU7M=")
                .build());
        addDependency(Dependency.builder()
                .groupId("com{}google{}protobuf")
                .artifactId("protobuf-java")
                .version("4.31.1")
                .checksum("1g3+fGig04okjMqWkk8oncfhlmqIfufK45dwGvCFda4=")
                .build());

        //sqlite-jdbc
        addDependency(Dependency.builder()
                .groupId("org{}xerial")
                .artifactId("sqlite-jdbc")
                .version("3.50.3.0")
                .checksum("o/U6KqFa6UJannk7vpyOUoj+vrS2XvXBpOgNTCBFzwg=")
                .build());

        addRelocation("org{}bstats", RELOCATION_PREFIX + "org{}bstats");
    }

}
