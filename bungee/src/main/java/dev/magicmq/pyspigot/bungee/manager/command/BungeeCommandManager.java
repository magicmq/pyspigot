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

import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import net.md_5.bungee.api.ProxyServer;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.List;

public class BungeeCommandManager extends CommandManager<BungeeScriptCommand> {

    private static BungeeCommandManager instance;

    private BungeeCommandManager() {
        super();
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name) {
        return registerCommand(commandFunction, null, name, new ArrayList<>(), null);
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name) {
        return registerCommand(commandFunction, tabFunction, name, new ArrayList<>(), null);
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name, String permission) {
        return registerCommand(commandFunction, null, name, new ArrayList<>(), permission);
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String permission) {
        return registerCommand(commandFunction, tabFunction, name, new ArrayList<>(), permission);
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name, List<String> aliases, String permission) {
        return registerCommand(commandFunction, null, name, aliases, permission);
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, List<String> aliases, String permission) {
        Script script = ScriptUtils.getScriptFromCallStack();
        BungeeScriptCommand command = getCommand(script, name);
        if (command == null) {
            BungeeScriptCommand newCommand = new BungeeScriptCommand(script, commandFunction, tabFunction, name, aliases, permission);
            addCommandToBungee(newCommand);
            return newCommand;
        } else
            throw new RuntimeException("Command '" + name + "' is already registered");
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage) {
        throw new UnsupportedOperationException("Setting command descriptions is not possible in BungeeCord. Use registerCommand(command)");
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage) {
        throw new UnsupportedOperationException("Setting command descriptions is not possible in BungeeCord.");
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage, List<String> aliases) {
        throw new UnsupportedOperationException("Setting command descriptions is not possible in BungeeCord.");
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases) {
        throw new UnsupportedOperationException("Setting command descriptions is not possible in BungeeCord.");
    }

    @Override
    public BungeeScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission) {
        throw new UnsupportedOperationException("Setting command descriptions is not possible in BungeeCord.");
    }

    @Override
    public void unregisterCommand(BungeeScriptCommand command) {
        removeCommandFromBungee(command);
        removeCommand(command.getScript(), command);
    }

    @Override
    public void unregisterCommands(Script script) {
        List<BungeeScriptCommand> associatedCommands = getCommands(script);
        if (associatedCommands != null) {
            for (BungeeScriptCommand command : associatedCommands) {
                removeCommandFromBungee(command);
            }
            removeCommands(script);
        }
    }

    @Override
    public BungeeScriptCommand getCommand(Script script, String name) {
        List<BungeeScriptCommand> scriptCommands = getCommands(script);
        if (scriptCommands != null) {
            for (BungeeScriptCommand command : scriptCommands) {
                if (command.getName().equalsIgnoreCase(name))
                    return command;
            }
        }
        return null;
    }

    private void addCommandToBungee(BungeeScriptCommand command) {
        ProxyServer.getInstance().getPluginManager().registerCommand(PyBungee.get(), command);
    }

    private void removeCommandFromBungee(BungeeScriptCommand command) {
        ProxyServer.getInstance().getPluginManager().unregisterCommand(command);
    }

    /**
     * Get the singleton instance of this BungeeCommandManager.
     * @return The instance
     */
    public static BungeeCommandManager get() {
        if (instance == null)
            instance = new BungeeCommandManager();
        return instance;
    }
}
