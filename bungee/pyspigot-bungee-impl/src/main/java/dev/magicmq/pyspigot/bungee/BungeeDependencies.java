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

package dev.magicmq.pyspigot.bungee;


import dev.magicmq.pyspigot.dependency.Dependency;

import static dev.magicmq.pyspigot.dependency.DependencyRegistry.RELOCATION_PREFIX;
import static dev.magicmq.pyspigot.dependency.DependencyRegistry.addDependency;
import static dev.magicmq.pyspigot.dependency.DependencyRegistry.addRelocation;

/**
 * A utility class which contains all runtime dependencies for the Bungee implementation.
 */
public final class BungeeDependencies {

    /**
     * Add all Bungee-specific dependencies to the core {@link dev.magicmq.pyspigot.dependency.DependencyRegistry}.
     */
    public static void addToRegistry() {
        //bstats-bungeecord
        addDependency(Dependency.builder()
                .groupId("org{}bstats")
                .artifactId("bstats-bungeecord")
                .version("3.1.0")
                .checksum("yYxuFdNO78VnEhDPC/zaOn0gZy/NkHjUd5nhJrfrAEg=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}bstats")
                .artifactId("bstats-base")
                .version("3.1.0")
                .checksum("2g5RgIzYzCi+8wAOcctPoLBeU5Mag6f59NPEAoNvQKA=")
                .build());

        //adventure-platform-bungeecord
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-bungeecord")
                .version("4.4.1")
                .checksum("hT4CTTc+Vk7QYqNjSEc32a0oeJpIoCUea+192MPaNVo=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-nbt")
                .version("4.21.0")
                .checksum("r27AXxaClKLmiLs8UhGxI70NTXh5HiS7fxE5rZi16zU=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}jetbrains")
                .artifactId("annotations")
                .version("26.0.2")
                .checksum("IDe+N4mA07qTM+l5VfOyzeOSqhJNBMpzzi7uZlcZkpc=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("examination-api")
                .version("1.3.0")
                .checksum("ySN//ssFQo9u/4YhYkascM4LR7BMCOp8o1Ag/eV/hJI=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("examination-string")
                .version("1.3.0")
                .checksum("fQH8JaS7OvDhZiaFRV9FQfv0YmIW6lhG5FXBSR4Va4w=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-api")
                .version("4.4.1")
                .checksum("7GBGKMK3wWXqdMH8s6LQ8DNZwsd6FJYOOgvC43lnCsI=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-api")
                .version("4.21.0")
                .checksum("ZKdldgQB541DOofb1M2lcBazA4U8b9Hclwh7WjqmgTM=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-bungeecord")
                .version("4.4.1")
                .checksum("4bw3bG3HohAAFgFXNc5MzFNNKya/WrgqrHUcUDIFbDk=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-gson")
                .version("4.21.0")
                .checksum("ObUc+poE324dL0MdJqaX7WGWI2ls3rHAChr9NHxc4IQ=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-legacy")
                .version("4.21.0")
                .checksum("msG5n4KEWOGQCPZ1eBoOjIUXzfoiofxbuHp79pVUw1w=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-json")
                .version("4.21.0")
                .checksum("kocXstx2RathZhfAvVXoCpwDWHYrWQoNPAu+MCsuVIE=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("option")
                .version("1.1.0")
                .checksum("l7abSxff4CIXyRMa00JWTLya69BMdetoljm194/UsRw=")
                .build());
        addDependency(Dependency.builder()
                .groupId("com{}google{}auto{}service")
                .artifactId("auto-service-annotations")
                .version("1.1.1")
                .checksum("Fqdt0AomUFaER/XW46niyAnZpCNn1WtFIVz7iXMfTSQ=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}jspecify")
                .artifactId("jspecify")
                .version("1.0.0")
                .checksum("H61ua+dVd4Hk0zcp1Jrhzcj92m/kd7sMxozjUer9+6s=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-platform-facet")
                .version("4.4.1")
                .checksum("IPjm2zTXIqSszL7cybbALo7ms8q5NQsGqz0cCwuLRU8=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-text-serializer-commons")
                .version("4.21.0")
                .checksum("LusmYFxyOlfltj0kwt1kGCFToUhsqOnzC8ukLSB/LCo=")
                .build());
        addDependency(Dependency.builder()
                .groupId("net{}kyori")
                .artifactId("adventure-key")
                .version("4.21.0")
                .checksum("apOYydQCCJ+doGCla0XK59WMvxmTcabY7Bom2PlMxrs=")
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

        addRelocation("net{}kyori", RELOCATION_PREFIX + "net{}kyori");
        addRelocation("org{}bstats", RELOCATION_PREFIX + "org{}bstats");
    }

}
