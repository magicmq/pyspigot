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


import dev.magicmq.pyspigot.exception.DependencyDownloadException;
import org.python.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * A utility class which fetches and downloads dependencies from remote repositories.
 */
public enum DependencyRepository {

    /**
     * A self-hosted Maven Central mirror repository, to reduce strain on Maven Central.
     */
    MAVEN_CENTRAL_MIRROR("https://repo.magicmq.dev/repository/maven-central/") {
        // Add timeout in case mirror goes offline
        @Override
        protected URLConnection openConnection(Dependency dependency) throws URISyntaxException, IOException {
            URLConnection connection = super.openConnection(dependency);

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            return connection;
        }
    },

    /**
     * The Maven Central repository.
     */
    MAVEN_CENTRAL("https://repo1.maven.org/maven2/");

    private final String url;

    DependencyRepository(String url) {
        this.url = url;
    }

    /**
     * Download the specified dependency into an array of bytes.
     * @param dependency The dependency to download.
     * @return The downloaded dependency, as a byte array
     * @throws DependencyDownloadException If there was an error while downloading the dependency
     */
    public byte[] download(Dependency dependency) throws DependencyDownloadException {
        byte[] bytes = downloadRaw(dependency);
        byte[] hash = getDigest().digest(bytes);

        if (!Arrays.equals(dependency.getChecksum(), hash))
            throw new DependencyDownloadException("Downloaded file for dependency '" + dependency + "' did not match the expected hash. " +
                    "Expected: " + Base64.getEncoder().encodeToString(dependency.getChecksum()) + " " +
                    "Actual: " + Base64.getEncoder().encodeToString(hash));

        return bytes;
    }

    /**
     * Download the dependency to the specified file path.
     * @param dependency The dependency download
     * @param path The path where the downloaded file should be placed
     * @throws DependencyDownloadException If there was an error while downloading the dependency
     */
    public void download(Dependency dependency, Path path) throws DependencyDownloadException {
        try {
            Files.write(path, download(dependency));
        } catch (IOException e) {
            throw new DependencyDownloadException("Unhandled exception when downloading dependency '" + dependency + "'", e);
        }
    }

    private byte[] downloadRaw(Dependency dependency) throws DependencyDownloadException {
        try {
            URLConnection connection = openConnection(dependency);
            try (InputStream input = connection.getInputStream()) {
                byte[] bytes = ByteStreams.toByteArray(input);
                if (bytes.length == 0)
                    throw new DependencyDownloadException("Encountered an empty stream when attempting to download dependency '" + dependency + "'");
                return bytes;
            }
        } catch (Exception e) {
            throw new DependencyDownloadException("Unhandled exception when downloading dependency '" + dependency + "'", e);
        }
    }

    protected URLConnection openConnection(Dependency dependency) throws URISyntaxException, IOException {
        URI uri = new URI(url + dependency.getRepositoryPath());
        return uri.toURL().openConnection();
    }

    private static MessageDigest getDigest() throws DependencyDownloadException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new DependencyDownloadException("Unhandled exception when analyzing checksum", e);
        }
    }
}
