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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Helper class to retrieve configuration values from the script options config.
 */
public class ScriptOptionsConfig {

    private static final ThreadLocal<Yaml> YAML;

    private static Map<?, ?> configMap;

    static {
        YAML = ThreadLocal.withInitial(() -> {
            DumperOptions dumperOptions = new DumperOptions();
            return new Yaml(new SafeConstructor(new LoaderOptions()), new Representer(dumperOptions), dumperOptions, new ScriptOptionsResolver());
        });

        reload();
    }

    public static void reload() {
        File file = new File(PyCore.get().getDataFolder(), "script_options.yml");
        if (!file.exists()) {
            PyCore.get().saveResource("script_options.yml", false);
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            configMap = loadConfig(inputStream);
        } catch (IOException | InvalidConfigurationException e) {
            PyCore.get().getLogger().log(Level.SEVERE, "Error when loading script_options.yml", e);
        }
    }

    public static boolean contains(String key) {
        return configMap.containsKey(key);
    }

    public static Map<?, ?> getScriptSection(String scriptName) throws InvalidConfigurationException {
        try {
            return (Map<?, ?>) configMap.get(scriptName);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Expected a configuration section");
        }
    }

    public static boolean getEnabled(String scriptName, boolean defaultValue) throws InvalidConfigurationException {
        Map<?, ?> scriptSection = getScriptSection(scriptName);
        if (scriptSection.containsKey("enabled")) {
            try {
                return (boolean) scriptSection.get("enabled");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a boolean for 'enabled', but got something else.");
            }
        } else
            return defaultValue;
    }

    public static int getLoadPriority(String scriptName, int defaultValue) throws InvalidConfigurationException {
        Map<?, ?> scriptSection = getScriptSection(scriptName);
        if (scriptSection.containsKey("load-priority")) {
            try {
                return (int) scriptSection.get("load-priority");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected an int for 'load-priority', but got something else.");
            }
        } else
            return defaultValue;
    }

    public static List<String> getPluginDepend(String scriptName, List<String> defaultValue) throws InvalidConfigurationException {
        Map<?, ?> scriptSection = getScriptSection(scriptName);
        if (scriptSection.containsKey("plugin-depend")) {
            try {
                List<String> toReturn = new ArrayList<>();
                for (Object entry : (Iterable<?>) scriptSection.get("plugin-depend")) {
                    toReturn.add(entry.toString().replace(' ', '_'));
                }
                return toReturn;
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a list for 'plugin-depend', but got something else.");
            }
        } else
            return defaultValue;
    }

    public static boolean getFileLoggingEnabled(String scriptName, boolean defaultValue) throws InvalidConfigurationException {
        Map<?, ?> scriptSection = getScriptSection(scriptName);
        if (scriptSection.containsKey("file-logging-enabled")) {
            try {
                return (boolean) scriptSection.get("file-logging-enabled");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a boolean for 'file-logging-enabled', but got something else.");
            }
        } else
            return defaultValue;
    }

    public static String getMinLoggingLevel(String scriptName, String defaultValue) throws InvalidConfigurationException {
        Map<?, ?> scriptSection = getScriptSection(scriptName);
        if (scriptSection.containsKey("min-logging-level")) {
            try {
                return (String) scriptSection.get("min-logging-level");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a string for 'min-logging-level', but got something else.");
            }
        } else
            return defaultValue;
    }

    public static String getPermissionDefault(String scriptName, String defaultValue) throws InvalidConfigurationException {
        Map<?, ?> scriptSection = getScriptSection(scriptName);
        if (scriptSection.containsKey("permission-default")) {
            try {
                return (String) scriptSection.get("permission-default");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a string for 'permission-default', but got something else.");
            }
        } else
            return defaultValue;
    }

    public static Map<?, ?> getPermissions(String scriptName, Map<?, ?> defaultValue) throws InvalidConfigurationException {
        Map<?, ?> scriptSection = getScriptSection(scriptName);
        if (scriptSection.containsKey("permissions")) {
            try {
                return (Map<?, ?>) scriptSection.get("permissions");
            } catch (ClassCastException e) {
                throw new InvalidConfigurationException("Expected a configuration section for 'permissions', but got something else.");
            }
        } else
            return defaultValue;
    }

    private static Map<?, ?> loadConfig(InputStream inputStream) throws InvalidConfigurationException {
        return asMap(YAML.get().load(inputStream));
    }

    private static Map<?, ?> asMap(Object object) throws InvalidConfigurationException {
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
