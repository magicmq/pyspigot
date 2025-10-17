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


import me.lucko.jarrelocator.Relocation;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class which contains all runtime dependencies and relocation rules for the core of PySpigot.
 * <p>
 * Platform-specific implementations also add their respective dependencies and relocations to this registry.
 */
public final class DependencyRegistry {

    public static final String RELOCATION_PREFIX = "dev.magicmq.lib.";

    private static final List<Dependency> dependencies;
    private static final List<Relocation> relocations;

    static {
        dependencies = new ArrayList<>();
        relocations = new ArrayList<>();

        //HikariCP
        addDependency(Dependency.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("7.0.2")
                .checksum("8eYS+ic0W+MQeoVDHoqK6yBcFTZKsvLUEeQKnXuwgJU=")
                .build()
        );

        //mongodb-driver-sync
        addDependency(Dependency.builder()
                .groupId("org{}mongodb")
                .artifactId("mongodb-driver-sync")
                .version("5.6.0")
                .checksum("LoOHjdHDMdGdQiXTo4Zs0+fWH4fd5eY5uTw4AyXMAfc=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}mongodb")
                .artifactId("mongodb-driver-core")
                .version("5.6.0")
                .checksum("+fu0sDuGEv6EYmfdv2/dS9IuXa24hZy2X3RC2CRqyXg=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}mongodb")
                .artifactId("bson")
                .version("5.6.0")
                .checksum("NickDNLhh7Jncx8KOKfMmqZQLkUArszvPetfv7iCFEU=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}mongodb")
                .artifactId("bson-record-codec")
                .version("5.6.0")
                .checksum("8p4NFFk+v3IQAIKt0y47KTMJAjvUQU4NlZS3k41htjI=")
                .build());

        //lettuce-core
        addDependency(Dependency.builder()
                .groupId("io{}lettuce")
                .artifactId("lettuce-core")
                .version("6.8.1.RELEASE")
                .checksum("Q7rP6Su7K4SP0q+xgm/P6LRVR4Xf0p6Z7GxlVMJrF0I=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-common")
                .version("4.1.118.Final")
                .checksum("ZczpAezw+dZZHMd1B3JhSrQBqEQV3JrsnaTQRvD5p3w=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-handler")
                .version("4.1.118.Final")
                .checksum("JuP4pehZ/WLPPBPcbXXk4Yh58ACl0K1/WPhnlnXSPa4=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-buffer")
                .version("4.1.118.Final")
                .checksum("DupOhmapY2oociZh2LpfqFZEd+df7G3S/z4yTjYfizw=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-codec")
                .version("4.1.118.Final")
                .checksum("Sr0hX9HtfOhlCdFpzJy+3lBCF2wmWnmztwYCsBcibD8=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-transport")
                .version("4.1.118.Final")
                .checksum("qzdR5xfa75yNkeTXRyikhzC9hTC3LiRmsiKy6j+wfbk=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-resolver")
                .version("4.1.118.Final")
                .checksum("MXDCJZcsGLaFDSit1g2xW7KNg8Tj1baGyiIOC9cnPIo=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-transport-native-unix-common")
                .version("4.1.118.Final")
                .checksum("abFnk9e0HqdqdivSvRRPxPfDnBVqelnr9puutWD7ELc=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-resolver-dns")
                .version("4.1.118.Final")
                .checksum("wOD9r/q6hJ4xRbK5Yoj8j8bzsqYjz3KqunCCiDSOSTg=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}netty")
                .artifactId("netty-codec-dns")
                .version("4.1.118.Final")
                .checksum("4RXkLKHjzI2F46Yy2PqhAtGMDrwfpFEa8wvsefjBR9Q=")
                .build());
        addDependency(Dependency.builder()
                .groupId("io{}projectreactor")
                .artifactId("reactor-core")
                .version("3.6.6")
                .checksum("RPBV+9Aztsl2xT+y4EtZAn55+yMSw30uqlTHfqHqgP4=")
                .build());
        addDependency(Dependency.builder()
                .groupId("org{}reactivestreams")
                .artifactId("reactive-streams")
                .version("1.0.4")
                .checksum("91yll3ibPaxY9hhXuawuEDSmj6Zy2zUFWo+0UJ4yXyg=")
                .build());
        addDependency(Dependency.builder()
                .groupId("redis{}clients{}authentication")
                .artifactId("redis-authx-core")
                .version("0.1.1-beta2")
                .checksum("zFbtsIs9+FYs3awQjcphkH0hzErQTeBen10bAQWDkK8=")
                .build());

        addRelocation("com{}zaxxer", RELOCATION_PREFIX + "com{}zaxxer");
        addRelocation("com{}mongodb", RELOCATION_PREFIX + "com{}mongodb");
        addRelocation("org{}bson", RELOCATION_PREFIX + "org{}bson");
        addRelocation("io{}lettuce", RELOCATION_PREFIX + "io{}lettuce");
        addRelocation("io{}netty", RELOCATION_PREFIX + "io{}netty");
        addRelocation("reactor{}adapter", RELOCATION_PREFIX + "reactor{}adapter");
        addRelocation("reactor{}core", RELOCATION_PREFIX + "reactor{}core");
        addRelocation("reactor{}util", RELOCATION_PREFIX + "reactor{}util");
        addRelocation("org{}reactivestreams", RELOCATION_PREFIX + "org{}reactivestreams");
        addRelocation("redis{}clients", RELOCATION_PREFIX + "redis{}clients");
    }

    /**
     * Add a dependency to the registry.
     * @param dependency The dependency to add
     */
    public static void addDependency(Dependency dependency) {
        dependencies.add(dependency);
    }

    /**
     * Add a relocation rule to the registry
     * @param pattern The pattern to relocate
     * @param relocatedPattern The relocation to apply
     */
    public static void addRelocation(String pattern, String relocatedPattern) {
        relocations.add(new Relocation(pattern.replace("{}", "."), relocatedPattern.replace("{}", ".")));
    }

    /**
     * Get all dependencies in the registry.
     * @return An immutable list containing all dependencies in the registry
     */
    public static List<Dependency> getDependencies() {
        return List.copyOf(dependencies);
    }

    /**
     * Get all relocations in the registry.
     * @return An immutable list containing all relocations in the registry
     */
    public static List<Relocation> getRelocations() {
        return List.copyOf(relocations);
    }

}
