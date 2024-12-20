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

package dev.magicmq.pyspigot.bukkit.manager.script;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

import java.nio.file.Path;
import java.util.logging.Level;

/**
 * An extension of the base {@link Script} class that includes Bukkit-specific code for initializing script permissions.
 */
public class SpigotScript extends Script {

    /**
     *
     * @param path The path that corresponds to the file where the script lives
     * @param name The name of this script. Should contain its extension (.py)
     * @param options The {@link ScriptOptions} for this script
     */
    public SpigotScript(Path path, String name, SpigotScriptOptions options) {
        super(path, name, options);
    }

    /**
     * Adds the script's permission (from its options) to the server.
     */
    public void initPermissions() {
        SpigotScriptOptions options = (SpigotScriptOptions) getOptions();
        for (Permission permission : options.getPermissions()) {
            try {
                Bukkit.getPluginManager().addPermission(permission);
            } catch (IllegalArgumentException exception) {
                getLogger().log(Level.WARNING, "The permission '" + permission.getName() + "' is already defined by another plugin/script.");
            }
        }
    }

    /**
     * Removes the script's permissions from the server.
     */
    public void removePermissions() {
        SpigotScriptOptions options = (SpigotScriptOptions) getOptions();
        for (Permission permission : options.getPermissions()) {
            Bukkit.getPluginManager().removePermission(permission);
        }
    }

}
