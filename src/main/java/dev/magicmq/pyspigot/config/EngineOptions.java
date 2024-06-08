/*
 *    Copyright 2023 magicmq
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

import org.bukkit.configuration.ConfigurationSection;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.SandboxPolicy;

import java.util.HashMap;

/**
 * A class that represents options that can be set for an {@link org.graalvm.polyglot.Engine}.
 */
public class EngineOptions {

    private final boolean allowExperimentalOptions;
    private final SandboxPolicy sandboxPolicy;
    private final boolean useSystemProperties;
    private final HashMap<String, String> options;

    /**
     * Initialize a new EngineOptions using values from the provided ConfigurationSection.
     * @param config The configuration section from which engine options should be read
     */
    public EngineOptions(ConfigurationSection config) {
        this.allowExperimentalOptions = config.getBoolean("allow-experimental-options", false);
        this.sandboxPolicy = SandboxPolicy.valueOf(config.getString("sandbox-policy", "TRUSTED"));
        this.useSystemProperties = config.getBoolean("use-system-properties", true);
        this.options = new HashMap<>();
        for (String option : config.getStringList("options")) {
            String[] optionParts = option.split(",");
            this.options.put(optionParts[0], optionParts[1]);
        }
    }

    /**
     * Initialize a new EngineOptions using the default values.
     */
    public EngineOptions() {
        this.allowExperimentalOptions = false;
        this.sandboxPolicy = SandboxPolicy.TRUSTED;
        this.useSystemProperties = true;
        this.options = new HashMap<>();
    }

    /**
     * Get the options as an Engine.Builder object, which can subsequently be built for creation and usage of contexts.
     * @return The engine, with all options set as per this class
     */
    public Engine.Builder getAsBuilder() {
        Engine.Builder builder = Engine.newBuilder();
        builder.allowExperimentalOptions(allowExperimentalOptions)
                .sandbox(sandboxPolicy)
                .useSystemProperties(useSystemProperties)
                .options(options);
        return builder;
    }
}
