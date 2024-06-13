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
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.SandboxPolicy;
import org.graalvm.polyglot.io.IOAccess;

import java.util.HashMap;

/**
 * A class that represents options that can be set for a {@link org.graalvm.polyglot.Context}.
 */
public class ContextOptions {

    private final boolean allowAllAccess;
    private final boolean allowCreateProcess;
    private final boolean allowCreateThread;
    private final EnvironmentAccess allowEnvironmentAccess;
    private final boolean allowExperimentalOptions;
    private final HostAccess allowHostAccess;
    private final boolean allowInnerContextOptions;
    private final IOAccess allowIOAccess;
    private final boolean allowNativeAccess;
    private final boolean allowValueSharing;
    private final String[] arguments;
    private final HashMap<String, String> environmentVariables;
    private final HashMap<String, String> options;
    private final SandboxPolicy sandboxPolicy;

    /**
     * Initialize a new ContextOptions using values from the provided ConfigurationSection.
     * @param config The configuration section from which context options should be read
     */
    public ContextOptions(ConfigurationSection config) {
        this.allowAllAccess = config.getBoolean("allow-all-access");
        this.allowCreateProcess = config.getBoolean("allow-create-process");
        this.allowCreateThread = config.getBoolean("allow-create-thread");

        String environment = config.getString("environment-access-policy").toUpperCase();
        switch (environment) {
            case "INHERIT":
                this.allowEnvironmentAccess = EnvironmentAccess.INHERIT;
                break;
            case "NONE":
                this.allowEnvironmentAccess = EnvironmentAccess.NONE;
                break;
            default:
                throw new IllegalArgumentException("Environment access policy " + environment + " was not found. Available options are INHERIT or NONE.");
        }
        this.allowExperimentalOptions = config.getBoolean("allow-experimental-options");
        String hostAccess = config.getString("host-access-policy").toUpperCase();
        switch (hostAccess) {
            case "ALL":
                this.allowHostAccess = HostAccess.ALL;
                break;
            case "CONSTRAINED":
                this.allowHostAccess = HostAccess.CONSTRAINED;
                break;
            case "EXPLICIT":
                this.allowHostAccess = HostAccess.EXPLICIT;
                break;
            case "ISOLATED":
                this.allowHostAccess = HostAccess.ISOLATED;
                break;
            case "NONE":
                this.allowHostAccess = HostAccess.NONE;
                break;
            case "SCOPED":
                this.allowHostAccess = HostAccess.SCOPED;
                break;
            case "UNTRUSTED":
                this.allowHostAccess = HostAccess.UNTRUSTED;
                break;
            default:
                throw new IllegalArgumentException("Host access policy " + hostAccess + " was not found. Available options are ALL, CONSTRAINED, EXPLICIT, ISOLATED, NONE, SCOPED, or UNTRUSTED.");
        }
        this.allowInnerContextOptions = config.getBoolean("allow-inner-context-options");
        String ioAccess = config.getString("io-access-policy").toUpperCase();
        switch (ioAccess) {
            case "ALL":
                this.allowIOAccess = IOAccess.ALL;
                break;
            case "NONE":
                this.allowIOAccess = IOAccess.NONE;
                break;
            default:
                throw new IllegalArgumentException("IO access policy " + environment + " was not found. Available options are ALL or NONE.");
        }
        this.allowNativeAccess = config.getBoolean("allow-native-access");
        this.allowValueSharing = config.getBoolean("allow-value-sharing");
        this.arguments = config.getStringList("arguments").toArray(new String[0]);
        this.environmentVariables = new HashMap<>();
        for (String environmentVariable : config.getStringList("environment-variables")) {
            String[] parts = environmentVariable.split(",");
            this.environmentVariables.put(parts[0], parts[1]);
        }
        this.options = new HashMap<>();
        for (String option : config.getStringList("options")) {
            String[] parts = option.split(",");
            this.options.put(parts[0], parts[1]);
        }
        this.sandboxPolicy = SandboxPolicy.valueOf(config.getString("sandbox-policy"));
    }

    /**
     * Initialize a new ContextOptions using the default values.
     */
    public ContextOptions() {
        this.allowAllAccess = true;
        this.allowCreateProcess = true;
        this.allowCreateThread = true;
        this.allowEnvironmentAccess = EnvironmentAccess.INHERIT;
        this.allowExperimentalOptions = true;
        this.allowHostAccess = HostAccess.ALL;
        this.allowInnerContextOptions = true;
        this.allowIOAccess = IOAccess.ALL;
        this.allowNativeAccess = true;
        this.allowValueSharing = true;
        this.arguments = new String[]{};
        this.environmentVariables = new HashMap<>();
        this.options = new HashMap<>();
        this.options.put("python.EmulateJython", "true");
        this.sandboxPolicy = SandboxPolicy.TRUSTED;
    }

    /**
     * Get the options as a Context.Builder object, which can subsequently be built for execution of script code.
     * @return The builder, with all options set as per this class
     */
    public Context.Builder getAsBuilder() {
        Context.Builder builder = Context.newBuilder();
        builder.allowAllAccess(allowAllAccess)
                .allowCreateProcess(allowCreateProcess)
                .allowCreateThread(allowCreateThread)
                .allowEnvironmentAccess(allowEnvironmentAccess)
                .allowExperimentalOptions(allowExperimentalOptions)
                .allowHostAccess(allowHostAccess)
                .allowInnerContextOptions(allowInnerContextOptions)
                .allowIO(allowIOAccess)
                .allowNativeAccess(allowNativeAccess)
                .allowValueSharing(allowValueSharing)
                .arguments("python", arguments)
                .environment(environmentVariables)
                .options(options)
                .sandbox(sandboxPolicy);
        return builder;
    }
}
