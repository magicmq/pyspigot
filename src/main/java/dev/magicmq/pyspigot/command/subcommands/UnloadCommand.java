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

import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "unload",
        permission = "pyspigot.command.unload",
        description = "Unload a script with the specified name",
        usage = "<scriptname>"
)
public class UnloadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].endsWith(".py")) {
                if (ScriptManager.get().isScriptRunning(args[0])) {
                    boolean success = ScriptManager.get().unloadScript(args[0]);
                    if (success)
                        sender.sendMessage(ChatColor.GREEN + "Successfully unloaded script '" + args[0] + "'.");
                    else
                        sender.sendMessage(ChatColor.RED + "There was an error when unloading script '" + args[0] + "'. See console for details.");
                } else {
                    sender.sendMessage(ChatColor.RED + "No running script found with the name '" + args[0] + "'.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Script names must end in .py.");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length > 0) {
            return new ArrayList<>(ScriptManager.get().getLoadedScriptNames());
        } else {
            return null;
        }
    }
}
