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

package dev.magicmq.pyspigot.command.subcommands;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@SubCommandMeta(
        command = "reloadall",
        aliases = {"reset"},
        permission = "pyspigot.command.reloadall",
        description = "Perform a complete reload of the plugin, including configs and all scripts."
)
public class ReloadAllCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        ScriptManager.get().unloadScripts();
        PySpigot.get().reload();
        ScriptManager.get().loadScripts();
        sender.sendMessage(ChatColor.GREEN + "All scripts and plugin config have been reloaded.");
        return true;
    }
}
