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


import jep.ClassEnquirer;
import jep.ClassList;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitClassEnquirer implements ClassEnquirer {

    private final Set<String> pythonStdLib;
    private final Set<String> sitePackages;
    private final ClassEnquirer delegate;

    public BukkitClassEnquirer() throws IOException {
        try (Stream<Path> path = Files.list(JepBootstrapper.get().getPythonLibDir())) {
            this.pythonStdLib = path
                    .map(entry -> entry.getFileName().toString())
                    .map(FilenameUtils::removeExtension)
                    .collect(Collectors.toSet());
        }

        try (Stream<Path> path = Files.list(JepBootstrapper.get().getSitePackagesDir())) {
            this.sitePackages = path
                    .map(entry -> entry.getFileName().toString())
                    .map(FilenameUtils::removeExtension)
                    .collect(Collectors.toSet());
        }

        this.delegate = ClassList.getInstance();
    }

    @Override
    public boolean isJavaPackage(String name) {
        if (delegate.isJavaPackage(name))
            return true;

        String top = name.contains(".") ? name.substring(0, name.indexOf(".")) : name;

        if (top.startsWith("_"))
            return false;

        return !pythonStdLib.contains(top) && !sitePackages.contains(top);
    }

    @Override
    public String[] getClassNames(String name) {
        return delegate.getClassNames(name);
    }

    @Override
    public String[] getSubPackages(String name) {
        return delegate.getSubPackages(name);
    }

}
