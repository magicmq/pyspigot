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

package dev.magicmq.pyspigot.bungee.manager.command;

import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyFunction;

import java.util.List;

/**
 * Manager to interface with BungeeCord's command framework. Primarily used by scripts to register and unregister commands.
 */
public class BungeeCommandManager extends CommandManager<BungeeScriptCommand> {

    private static BungeeCommandManager manager;

    private BungeeCommandManager() {
        super();
    }

    //TODO

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name) {
        return null;
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name) {
        return null;
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage) {
        return null;
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage) {
        return null;
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage, List<String> aliases) {
        return null;
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases) {
        return null;
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission, String permissionMessage) {
        return null;
    }

    @Override
    public void unregisterCommand(BungeeScriptCommand command) {

    }

    @Override
    public void unregisterCommands(Script script) {

    }

    @Override
    public BungeeScriptCommand getCommand(Script script, String name) {
        return null;
    }

    /**
     * Get the singleton instance of this BungeeCommandManager.
     * @return The instance
     */
    public static BungeeCommandManager get() {
        if (manager == null)
            manager = new BungeeCommandManager();
        return manager;
    }
}
