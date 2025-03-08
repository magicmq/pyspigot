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

package dev.magicmq.pyspigot.config;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.exception.InvalidConfigurationException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ProjectOptionsConfig {

    private static final ThreadLocal<Yaml> YAML;

    private Map<?, ?> configMap;

    static {
        YAML = ThreadLocal.withInitial(() -> {
            DumperOptions dumperOptions = new DumperOptions();
            return new Yaml(new SafeConstructor(new LoaderOptions()), new Representer(dumperOptions), dumperOptions, new ScriptOptionsResolver());
        });
    }

    public ProjectOptionsConfig(Path configPath) {
        File configFile = configPath.toFile();
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            this.configMap = asMap(YAML.get().load(inputStream));
        } catch (IOException | InvalidConfigurationException e) {
            PyCore.get().getLogger().log(Level.SEVERE, "Error when loading configuration file for project '" + configPath.getFileName() + "'");
        }
    }

    public boolean contains(String key) {
        return configMap.containsKey(key);
    }

    public String getMainScript(String defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("main")) {
            try {
                return (String) configMap.get("main");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a string for 'main', but got something else.");
            }
        } else
            return defaultValue;
    }

    public boolean getEnabled(boolean defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("enabled")) {
            try {
                return (boolean) configMap.get("enabled");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a boolean for 'enabled', but got something else.");
            }
        } else
            return defaultValue;
    }

    public int getLoadPriority(int defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("load-priority")) {
            try {
                return (int) configMap.get("load-priority");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected an int for 'load-priority', but got something else.");
            }
        } else
            return defaultValue;
    }

    public List<String> getPluginDepend(List<String> defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("plugin-depend")) {
            try {
                List<String> toReturn = new ArrayList<>();
                for (Object entry : (Iterable<?>) configMap.get("plugin-depend")) {
                    toReturn.add(entry.toString().replace(' ', '_'));
                }
                return toReturn;
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a list for 'plugin-depend', but got something else.");
            }
        } else
            return defaultValue;
    }

    public boolean getFileLoggingEnabled(boolean defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("file-logging-enabled")) {
            try {
                return (boolean) configMap.get("file-logging-enabled");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a boolean for 'file-logging-enabled', but got something else.");
            }
        } else
            return defaultValue;
    }

    public String getMinLoggingLevel(String defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("min-logging-level")) {
            try {
                return (String) configMap.get("min-logging-level");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a string for 'min-logging-level', but got something else.");
            }
        } else
            return defaultValue;
    }

    public String getPermissionDefault(String defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("permission-default")) {
            try {
                return (String) configMap.get("permission-default");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a string for 'permission-default', but got something else.");
            }
        } else
            return defaultValue;
    }

    public Map<?, ?> getPermissions(Map<?, ?> defaultValue) throws InvalidConfigurationException {
        if (configMap.containsKey("permissions")) {
            try {
                return (Map<?, ?>) configMap.get("permissions");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a configuration section for 'permissions', but got something else.");
            }
        } else
            return defaultValue;
    }

    private Map<?, ?> asMap(Object object) throws InvalidConfigurationException {
        if (object == null)
            return new HashMap<>();

        if (object instanceof Map)
            return (Map<?, ?>) object;
        throw new InvalidConfigurationException("Malformed configuration, is " + object + " but should be a map.");
    }

    private static class ScriptOptionsResolver extends Resolver {

        @Override
        protected void addImplicitResolvers() {
            addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
            addImplicitResolver(Tag.INT, INT, "-+0123456789");
            addImplicitResolver(Tag.MERGE, MERGE, "<");
            addImplicitResolver(Tag.NULL, NULL, "~nN\0");
            addImplicitResolver(Tag.NULL, EMPTY, null);
            addImplicitResolver(Tag.TIMESTAMP, TIMESTAMP, "0123456789");
        }
    }
}
