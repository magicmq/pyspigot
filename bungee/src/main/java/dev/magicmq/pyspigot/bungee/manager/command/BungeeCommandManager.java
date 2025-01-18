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
import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.script.Script;
import net.md_5.bungee.api.ProxyServer;
import org.python.core.PyFunction;

import java.util.List;

/**
 * The BungeeCord-specific implementation of the command manager.
 */
public class BungeeCommandManager extends CommandManager {

    private static BungeeCommandManager instance;

    private BungeeCommandManager() {
        super();
    }

    @Override
    protected ScriptCommand registerWithPlatform(Script script, PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission) {
        BungeeScriptCommand newCommand = new BungeeScriptCommand(script, commandFunction, tabFunction, name, aliases, permission);
        ProxyServer.getInstance().getPluginManager().registerCommand(PyBungee.get(), newCommand);
        return newCommand;
    }

    @Override
    protected void unregisterFromPlatform(ScriptCommand command) {
        ProxyServer.getInstance().getPluginManager().unregisterCommand((BungeeScriptCommand) command);
    }

    @Override
    protected void unregisterFromPlatform(List<ScriptCommand> commands) {
        commands.forEach(this::unregisterFromPlatform);
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
