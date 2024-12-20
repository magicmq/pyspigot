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

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.exception.InvalidConfigurationException;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of the base {@link ScriptOptions} class that includes Bukkit-specific code for parsing script permissions.
 */
public class SpigotScriptOptions extends ScriptOptions {

    private final PermissionDefault permissionDefault;
    private final List<Permission> permissions;

    /**
     * Initialize a new ScriptOptions with the default values.
     */
    public SpigotScriptOptions() {
        super();
        this.permissionDefault = PermissionDefault.getByName(PyCore.get().getConfig().scriptOptionPermissionDefault());
        this.permissions = Permission.loadPermissions(PyCore.get().getConfig().scriptOptionPermissions(), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
    }

    /**
     * Initialize a new ScriptOptions using the appropriate values in the script_options.yml file, using the script name to search for the values.
     * @param scriptName The name of the script whose script options should be initialized
     */
    public SpigotScriptOptions(String scriptName) throws InvalidConfigurationException {
        super(scriptName);
        if (ScriptOptionsConfig.contains(scriptName)) {
            this.permissionDefault = PermissionDefault.getByName(ScriptOptionsConfig.getPermissionDefault(scriptName, PyCore.get().getConfig().scriptOptionPermissionDefault()));
            this.permissions = Permission.loadPermissions(ScriptOptionsConfig.getPermissions(scriptName, PyCore.get().getConfig().scriptOptionPermissions()), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
        } else {
            this.permissionDefault = PermissionDefault.getByName(PyCore.get().getConfig().scriptOptionPermissionDefault());
            this.permissions = Permission.loadPermissions(PyCore.get().getConfig().scriptOptionPermissions(), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
        }
    }

    /**
     * Get the default permissions for permissions defined for this script.
     * @return The default permission level
     */
    public PermissionDefault getPermissionDefault() {
        return permissionDefault;
    }

    /**
     * Get a list of permissions defined for this script.
     * @return A list of permissions. Will return an empty list if this script has no permissions defined
     */
    public List<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        String superString = super.toString();
        superString = superString.substring(0, superString.length() - 1);

        return String.format("%s, Permission Default: %s, Permissions: %s]", superString, permissionDefault, printPermissions());
    }

    private List<String> printPermissions() {
        List<String> toReturn = new ArrayList<>();
        for (Permission permission : permissions) {
            toReturn.add(String.format("Permission[Name: %s, Description: %s, Default: %s, Children: %s]", permission.getName(), permission.getDescription(), permission.getDefault(), permission.getChildren()));
        }
        return toReturn;
    }
}
