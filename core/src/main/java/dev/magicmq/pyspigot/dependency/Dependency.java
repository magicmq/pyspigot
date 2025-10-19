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


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * A QOL class which represents a plugin dependency.
 */
public class Dependency {

    private static final String REPOSITORY_PATH = "%s/%s/%s/%s-%s.jar";
    private static final String FILE_PATH = "%s-%s.jar";
    private static final String RELOCATED_FILE_PATH = "%s-%s-relocated.jar";

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final byte[] checksum;
    private final String repositoryPath;
    private final Path filePath;
    private final Path relocatedFilePath;

    /**
     *
     * @param groupId The groupId of the dependency
     * @param artifactId The artifactId of the dependency
     * @param version The version of the dependency
     * @param checksum The SHA-256 checksum of the dependency, used for file integrity verification
     */
    public Dependency(String groupId, String artifactId, String version, String checksum) {
        this.groupId = unescape(groupId);
        this.artifactId = unescape(artifactId);
        this.version = version;
        this.checksum = Base64.getDecoder().decode(checksum);

        this.repositoryPath = String.format(REPOSITORY_PATH,
                unescape(groupId).replace(".", "/"),
                unescape(artifactId),
                version,
                unescape(artifactId),
                version);
        this.filePath = Paths.get(String.format(FILE_PATH,
                unescape(artifactId),
                version));
        this.relocatedFilePath = Paths.get(String.format(RELOCATED_FILE_PATH,
                unescape(artifactId),
                version));
    }

    /**
     * Get the dependency's groupId.
     * @return The groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Get the dependency's artifactId.
     * @return The artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get the dependency's version.
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the dependency's SHA-256 checksum.
     * @return The checksum (as a byte array)
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * Get the repository path to the dependency, as it would be structured for a maven2 repository.
     * @return The repository path
     */
    public String getRepositoryPath() {
        return repositoryPath;
    }

    /**
     * Get the path of the dependency file.
     * @return The file path
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * Get the path of the relocated dependency file.
     * @return The relocated file path
     */
    public Path getRelocatedFilePath() {
        return relocatedFilePath;
    }

    /**
     * Get a string representation of the dependency
     * @return A string representation of the dependency, formatted as "groupId:artifactId:version"
     */
    @Override
    public String toString() {
        return groupId + ':' + artifactId + ':' + version;
    }

    /**
     * Return a new dependency builder.
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A helper class for declaring new dependencies.
     */
    public static class Builder {

        private String groupId;
        private String artifactId;
        private String version;
        private String checksum;

        /**
         * Set the groupId.
         * @param groupId The groupId to set
         * @return The builder
         */
        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        /**
         * Set the artifactId.
         * @param artifactId The artifactId to set
         * @return The builder
         */
        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        /**
         * Set the version.
         * @param version The version to set
         * @return The builder
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Set the SHA-256 checksum.
         * @param checksum The checksum to set
         * @return The builder
         */
        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        /**
         * Build a new dependency from the Builder.
         * @return A new Dependency object, built from the Builder's values
         */
        public Dependency build() {
            return new Dependency(groupId, artifactId, version, checksum);
        }
    }

    private static String unescape(String string) {
        return string.replace("{}", ".");
    }
}
