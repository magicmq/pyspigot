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


import java.nio.file.Path;

public enum Platform {

    LINUX_X86_64(
            JepConstants.ASTRAL_URL + JepConstants.ASTRAL_RELEASE + "/cpython-" + JepConstants.PYTHON_VERSION + "+"
                    + JepConstants.ASTRAL_RELEASE + "-x86_64-unknown-linux-gnu-install_only.tar.gz",
            "e17275eaf95ceb5877aa6816e209b7733f41fee401d39c3921b88fb73fc4a4ba",
            "linux-x86_64",
            "jep.so",
            "lib/python" + JepConstants.PYTHON_MAJOR + "." + JepConstants.PYTHON_MINOR + "/",
            "lib/libpython" + JepConstants.PYTHON_MAJOR + "." + JepConstants.PYTHON_MINOR + ".so.1.0"
    ),

    LINUX_AARCH64(
            JepConstants.ASTRAL_URL + JepConstants.ASTRAL_RELEASE + "/cpython-" + JepConstants.PYTHON_VERSION + "+"
                    + JepConstants.ASTRAL_RELEASE + "-aarch64-unknown-linux-gnu-install_only.tar.gz",
            "5c8db1c21023316adad827a46d917bbbd6a85ae4e39bc3a58febda712c2f963d",
            "linux-aarch64",
            "jep.so",
            "lib/python" + JepConstants.PYTHON_MAJOR + "." + JepConstants.PYTHON_MINOR + "/",
            "lib/libpython" + JepConstants.PYTHON_MAJOR + "." + JepConstants.PYTHON_MINOR + ".so.1.0"
    ),

    WINDOWS_X86_64(
            JepConstants.ASTRAL_URL + JepConstants.ASTRAL_RELEASE + "/cpython-" + JepConstants.PYTHON_VERSION + "+"
                    + JepConstants.ASTRAL_RELEASE + "-x86_64-pc-windows-msvc-install_only.tar.gz",
            "9647bb46d3c236e34c1c11bbb7113444d9711811f0d11c39956168807a955b1a",
            "windows-x86_64",
            "jep.dll",
            "Lib/",
            "python" + JepConstants.PYTHON_MAJOR + JepConstants.PYTHON_MINOR + ".dll"
    ),

    MACOS_AARCH64(
            JepConstants.ASTRAL_URL + JepConstants.ASTRAL_RELEASE + "/cpython-" + JepConstants.PYTHON_VERSION + "+"
                    + JepConstants.ASTRAL_RELEASE + "-aarch64-apple-darwin-install_only.tar.gz",
            "8b7865e511b17093e090449bf71eb52933c17d45ad5257ddeacaffbb2c7239df",
            "macos-aarch64",
            "libjep.so",
            "lib/python" + JepConstants.PYTHON_MAJOR + "." + JepConstants.PYTHON_MINOR + "/",
            "lib/libpython" + JepConstants.PYTHON_MAJOR + "." + JepConstants.PYTHON_MINOR + ".dylib"
    );

    private final String downloadURL;
    private final String sha256;
    private final String jniLibDir;
    private final String jniLibName;
    private final Path libFolderPath;
    private final Path nativeLibPath;

    Platform(String downloadURL, String sha256, String jniLibDir, String jniLibName, String libFolderPath, String nativeLibPath) {
        this.downloadURL = downloadURL;
        this.sha256 = sha256;
        this.jniLibDir = jniLibDir;
        this.jniLibName = jniLibName;
        this.libFolderPath = Path.of(libFolderPath);
        this.nativeLibPath = Path.of(nativeLibPath);
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public String getSha256() {
        return sha256;
    }

    public String getJniLibDir() {
        return jniLibDir;
    }

    public String getJniLibName() {
        return jniLibName;
    }

    public Path getLibFolderPath() {
        return libFolderPath;
    }

    public Path getNativeLibPath() {
        return nativeLibPath;
    }

    public static Platform detectPlatform() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        boolean isLinux = os.contains("linux");
        boolean isWindows = os.contains("windows");
        boolean isMac = os.contains("mac");

        boolean isX64 = arch.equals("amd64") || arch.equals("x86_64");
        boolean isArm = arch.equals("aarch64") || arch.equals("arm64");

        if (isLinux && isX64)
            return Platform.LINUX_X86_64;
        if (isLinux && isArm)
            return Platform.LINUX_AARCH64;
        if (isWindows && isX64)
            return Platform.WINDOWS_X86_64;
        if (isMac && isArm)
            return Platform.MACOS_AARCH64;

        throw new UnsupportedOperationException("Unsupported platform: " + os + " / " + arch);
    }
}
