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

package dev.magicmq.pyspigot.jep;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.magicmq.pyspigot.PyCore;
import jep.MainInterpreter;
import jep.PyConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class JepBootstrapper {

    private static JepBootstrapper instance;

    private final Platform platform;
    private final Path pythonDir;
    private final Path pythonLibDir;
    private final Path sitePackagesDir;
    private final Path nativeLibPath;

    private JepBootstrapper() {
        this.platform = Platform.detectPlatform();
        PyCore.get().getLogger().info("Detected platform: {}", platform);

        this.pythonDir = PyCore.get().getDataFolderPath().resolve("python");
        this.pythonLibDir = pythonDir.resolve(platform.getLibFolderPath());
        this.sitePackagesDir = pythonLibDir.resolve("site-packages");
        this.nativeLibPath = pythonDir.resolve(platform.getNativeLibPath());
    }

    public void setupJep() throws Exception {
        if (!verifyLatest()) {
            deletePython();

            Path downloaded = downloadPython();
            extractPython(pythonDir, downloaded);
            extractJepPackage();

            PyCore.get().extractFolder("Lib", pythonLibDir);

            Path manifestPath = pythonDir.resolve("manifest.json");
            updateManifest(manifestPath);
        } else {
            PyCore.get().getLogger().info("Python and JEP set up and up to date.");
        }

        initJep();
    }

    public Path getPythonDir() {
        return pythonDir;
    }

    public Path getPythonLibDir() {
        return pythonLibDir;
    }

    public Path getSitePackagesDir() {
        return sitePackagesDir;
    }

    public Path getNativeLibPath() {
        return nativeLibPath;
    }

    private void initJep() {
        PyCore.get().getLogger().info("Initializing JEP...");

        //Preload native Python lib before native JEP lib
        if (Files.exists(nativeLibPath))
            System.load(nativeLibPath.toAbsolutePath().toString());
        else
            PyCore.get().getLogger().warn("Python shared library not found under {}, JEP may fail to load", nativeLibPath);

        MainInterpreter.setJepLibraryPath(nativeLibPath.toString());

        PyConfig config = PyConfig.python();
        config.setHome(pythonDir.toString());
        //TODO Set other params
        MainInterpreter.setInitParams(config);
    }

    private boolean verifyLatest() {
        if (!Files.exists(pythonDir))
            return false;

        Path manifest = pythonDir.resolve("version.json");
        if (!Files.exists(manifest))
            return false;

        try {
            String jsonString = Files.readString(manifest);
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

            String pythonVersion = json.get("pythonVersion").getAsString();
            if (!pythonVersion.equals(JepConstants.PYTHON_VERSION))
                return false;

            String pythonBuild = json.get("astralRelease").getAsString();
            if (!pythonBuild.equals(JepConstants.ASTRAL_RELEASE))
                return false;

            String jepVersion = json.get("jepVersion").getAsString();
            if (!jepVersion.equals(JepConstants.JEP_VERSION))
                return false;

            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private void deletePython() throws IOException {
        try (Stream<Path> paths = Files.list(pythonDir)) {
            if (paths.findAny().isEmpty())
                return;
        }

        PyCore.get().getLogger().info("Deleting Python runtime...");

        try (Stream<Path> walk = Files.walk(pythonDir)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private Path downloadPython() throws Exception {
        PyCore.get().getLogger().info("Downloading Python runtime...");

        String archiveName = platform.getDownloadURL().substring(platform.getDownloadURL().lastIndexOf('/') + 1);

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(platform.getDownloadURL()))
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();

        Path tempFile = Files.createTempFile(archiveName, ".tmp");

        boolean success = false;
        try {
            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + " from " + platform.getDownloadURL());
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (DigestInputStream in = new DigestInputStream(response.body(), digest);
                 OutputStream out = Files.newOutputStream(tempFile)) {
                byte[] buf = new byte[65536];
                int read;
                while ((read = in.read(buf)) != -1)
                    out.write(buf, 0, read);
            }

            String actual = bytesToHex(digest.digest());
            if (!actual.equalsIgnoreCase(platform.getSha256()))
                throw new IOException("SHA-256 mismatch: expected " + platform.getSha256() + ", but got " + actual);

            success = true;
            return tempFile;
        } finally {
            if (!success)
                Files.deleteIfExists(tempFile);
        }
    }

    private void extractPython(Path archivePath, Path targetDir) throws Exception {
        final long MAX_EXTRACTED_BYTES = 2L * 1024L * 1024L * 1024L;

        PyCore.get().getLogger().info("Extracting Python runtime...");

        Files.createDirectories(targetDir);
        Path normalizedTarget = targetDir.normalize();
        long totalExtracted = 0L;

        try (InputStream fileIn = Files.newInputStream(archivePath);
             BufferedInputStream bufIn = new BufferedInputStream(fileIn);
             GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(bufIn);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);
        ) {
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                String name = entry.getName();

                if (!name.startsWith("python/"))
                    continue;

                String relative = name.substring("python/".length());
                if (relative.isEmpty())
                    continue;

                Path dest = targetDir.resolve("python").resolve(relative);

                if (!dest.normalize().startsWith(normalizedTarget)) {
                    throw new IOException("Path traversal detected in archive entry: " + name);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(dest);
                } else if (entry.isSymbolicLink()) {
                    Path linkTarget = Path.of(entry.getLinkName());
                    Path resolvedTarget = dest.getParent().resolve(linkTarget).normalize();
                    if (!resolvedTarget.startsWith(normalizedTarget))
                        continue;

                    Files.createDirectories(dest.getParent());

                    if (Files.exists(dest, LinkOption.NOFOLLOW_LINKS))
                        Files.delete(dest);

                    try {
                        Files.createSymbolicLink(dest, linkTarget);
                    } catch (UnsupportedOperationException | IOException ignored) {}
                } else if (entry.isLink()) {
                    Path linkDest = targetDir.resolve("python")
                            .resolve(entry.getLinkName().replaceFirst("^python/", ""));
                    Files.createDirectories(dest.getParent());

                    try {
                        if (Files.exists(linkDest))
                            Files.createLink(dest, linkDest);
                    } catch (UnsupportedOperationException | IOException ignored) {}
                } else {
                    Files.createDirectories(dest.getParent());

                    try (OutputStream out = Files.newOutputStream(dest)) {
                        tarIn.transferTo(out);
                    }

                    totalExtracted += Files.size(dest);
                    if (totalExtracted > MAX_EXTRACTED_BYTES) {
                        throw new IOException("Archive extraction exceeded size limit of "
                                + (MAX_EXTRACTED_BYTES / 1024 / 1024) + " MB");
                    }

                    setPermissions(dest, entry.getMode());
                }
            }
        } finally {
            Files.deleteIfExists(archivePath);
        }
    }

    private void extractJepPackage() throws IOException {
        PyCore.get().getLogger().info("Installing JEP package...");

        if (!Files.exists(sitePackagesDir))
            throw new IOException("Unable to locate site-packages directory");

        Path jepPkgDir = sitePackagesDir.resolve("jep");
        Files.createDirectories(jepPkgDir);

        PyCore.get().extractFolder("jep-pkg/jep/", jepPkgDir);

        String jniPath = "jep-pkg/native/" + platform.getJniLibDir() + "/" + platform.getJniLibName();
        Path jniDest = jepPkgDir.resolve(platform.getJniLibName());

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(jniPath)) {
            Files.copy(in, jniDest, StandardCopyOption.REPLACE_EXISTING);
            setPermissions(jniDest, 0755);
        }
    }

    private void updateManifest(Path manifestPath) throws IOException {
        JsonObject manifest = new JsonObject();
        manifest.addProperty("pythonVersion", JepConstants.PYTHON_VERSION);
        manifest.addProperty("astralRelease", JepConstants.ASTRAL_RELEASE);
        manifest.addProperty("jepVersion", JepConstants.JEP_VERSION);
        manifest.addProperty("downloadedAt", Instant.now().toString());

        String jsonString = manifest.toString();
        Files.writeString(manifestPath, jsonString);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void setPermissions(Path path, int mode) throws IOException {
        try {
            Set<PosixFilePermission> perms = new HashSet<>();
            if ((mode & 0400) != 0)
                perms.add(PosixFilePermission.OWNER_READ);
            if ((mode & 0200) != 0)
                perms.add(PosixFilePermission.OWNER_WRITE);
            if ((mode & 0100) != 0)
                perms.add(PosixFilePermission.OWNER_EXECUTE);
            if ((mode & 0040) != 0)
                perms.add(PosixFilePermission.GROUP_READ);
            if ((mode & 0020) != 0)
                perms.add(PosixFilePermission.GROUP_WRITE);
            if ((mode & 0010) != 0)
                perms.add(PosixFilePermission.GROUP_EXECUTE);
            if ((mode & 0004) != 0)
                perms.add(PosixFilePermission.OTHERS_READ);
            if ((mode & 0002) != 0)
                perms.add(PosixFilePermission.OTHERS_WRITE);
            if ((mode & 0001) != 0)
                perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(path, perms);
        } catch (UnsupportedOperationException ignored) {
            if ((mode & 0111) != 0)
                path.toFile().setExecutable(true);
        }
    }

    public static JepBootstrapper get() {
        if (instance == null)
            instance = new JepBootstrapper();
        return instance;
    }
}
