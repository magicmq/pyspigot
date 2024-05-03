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
import dev.magicmq.pyspigot.manager.script.RunResult;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "load",
        permission = "pyspigot.command.load",
        description = "Load a script with the specified name",
        usage = "<scriptname>"
)
public class LoadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].endsWith(".py")) {
                if (!ScriptManager.get().isScriptRunning(args[0])) {
                    try {
                        RunResult result = ScriptManager.get().loadScript(args[0]);
                        if (result == RunResult.SUCCESS)
                            sender.sendMessage(ChatColor.GREEN + "Successfully loaded and ran script '" + args[0] + "'.");
                        else if (result == RunResult.FAIL_DEPENDENCY)
                            sender.sendMessage(ChatColor.RED + "Script '" + args[0] + "' was not run due to missing dependencies. See console for details.");
                        else if (result == RunResult.FAIL_DISABLED)
                            sender.sendMessage(ChatColor.RED + "Script '" + args[0] + "' was not run because it is disabled as per its options in script_options.yml.");
                        else if (result == RunResult.FAIL_ERROR)
                            sender.sendMessage(ChatColor.RED + "There was an error when running script '" + args[0] + "'. See console for details.");
                    } catch (FileNotFoundException e) {
                        sender.sendMessage(ChatColor.RED + "No script found in the scripts folder with the name '" + args[0] + "'.");
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage(ChatColor.RED + "There was an error when loading script '" + args[0] + "'. See console for details.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "There is already a loaded and running script with the name '" + args[0] + "'.");
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
            return new ArrayList<>(ScriptManager.get().getAllScriptNames());
        } else {
            return null;
        }
    }
}
