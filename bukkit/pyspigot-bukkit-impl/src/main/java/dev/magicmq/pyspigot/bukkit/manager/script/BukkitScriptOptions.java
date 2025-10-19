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
import dev.magicmq.pyspigot.bukkit.config.BukkitProjectOptionsConfig;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension of the base {@link ScriptOptions} class that includes Bukkit-specific code for parsing and registering script permissions.
 */
public class BukkitScriptOptions extends ScriptOptions {

    private final PermissionDefault permissionDefault;
    private final List<Permission> permissions;

    /**
     * Initialize a new BukkitScriptOptions for a single-file script, using the appropriate values in the script_options.yml file.
     * @param scriptPath The path of the script file whose script options should be initialized
     */
    public BukkitScriptOptions(Path scriptPath) {
        super(scriptPath);
        String scriptName = scriptPath.getFileName().toString();
        if (PyCore.get().getScriptOptionsConfig().contains(scriptName)) {
            this.permissionDefault = PermissionDefault.getByName(PyCore.get().getScriptOptionsConfig().getPermissionDefault(scriptName, PyCore.get().getConfig().scriptOptionPermissionDefault()));
            this.permissions = Permission.loadPermissions(PyCore.get().getScriptOptionsConfig().getPermissions(scriptName, PyCore.get().getConfig().scriptOptionPermissions()), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
        } else {
            this.permissionDefault = PermissionDefault.getByName(PyCore.get().getConfig().scriptOptionPermissionDefault());
            this.permissions = Permission.loadPermissions(PyCore.get().getConfig().scriptOptionPermissions(), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
        }
    }

    /**
     * Initialize a new BukkitScriptOptions for a multi-file project, using the appropriate values in the project's project.yml file.
     * @param config The project.yml file to parse that belongs to the project.
     *               If the project does not have a project.yml file, pass null, and the default values will be used
     */
    public BukkitScriptOptions(BukkitProjectOptionsConfig config) {
        super(config);
        if (config != null) {
            this.permissionDefault = PermissionDefault.getByName(config.getPermissionDefault(PyCore.get().getConfig().scriptOptionPermissionDefault()));
            this.permissions = Permission.loadPermissions(config.getPermissions(PyCore.get().getConfig().scriptOptionPermissions()), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
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
