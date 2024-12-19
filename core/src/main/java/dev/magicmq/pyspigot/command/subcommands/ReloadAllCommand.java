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

package dev.magicmq.pyspigot.command.subcommands;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.command.AbstractCommandSender;
import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import net.md_5.bungee.api.ChatColor;

@SubCommandMeta(
        command = "reloadall",
        aliases = {"reset", "restart", "reboot", "resetall"},
        permission = "pyspigot.command.reloadall",
        description = "Perform a complete reload of the plugin, including configs, libraries, and all scripts."
)
public class ReloadAllCommand implements SubCommand {

    @Override
    public boolean onCommand(AbstractCommandSender<?> sender, String[] args) {
        ScriptManager.get().unloadScripts();
        PyCore.get().reloadConfigs();
        LibraryManager.get().reload();
        ScriptManager.get().loadScripts();
        sender.sendMessage(ChatColor.GREEN + "All scripts, plugin config, and script_options.yml have been reloaded.");
        return true;
    }
}
