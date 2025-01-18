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

package dev.magicmq.pyspigot.manager.config;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a config file belonging to a script. Meant to be implemented by a class that also implements/extends a platform-specific configuration API.
 */
public interface ScriptConfig {

    /**
     * Get the file associated with this configuration.
     * @return The file associated with this configuration
     */
    File getConfigFile();

    /**
     * Get the absolute path of the file associated with this configuration.
     * @return The path of the file
     */
    Path getConfigPath();

    /**
     * Loads the config from the configuration file. Will also set defaults for the configuration, if they were specified.
     * @throws IOException If there was an exception when loading the file
     */
    void load() throws IOException;

    /**
     * Reload the configuration. Will read all changes made to the configuration file since the configuration was last loaded/reloaded.
     * @throws IOException If there was an exception when loading the file
     */
    void reload()throws IOException;

    /**
     * Save the configuration to its associated file. For continuity purposes, the configuration is also reloaded from the file after saving.
     * @throws IOException If there is an IOException when saving the file
     */
    void save() throws IOException;

    /**
     * Sets the specified path to the given value only if the path is not already set in the config file. Any specified default values are ignored when checking if the path is set.
     * @param path Path of the object to set
     * @param value Value to set the path to
     * @return True if the path was set to the value (in other words the path was not previously set), false if the path was not set to the value (in other words the path was already previously set)
     */
    boolean setIfNotExists(String path, Object value);

}
