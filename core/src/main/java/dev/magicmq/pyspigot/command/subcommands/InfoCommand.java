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

import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import net.md_5.bungee.api.ChatColor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "info",
        aliases = {"scriptinfo", "projectinfo"},
        permission = "pyspigot.command.info",
        description = "Print information about a script or project, including uptime, registered listeners, commands, and more info",
        usage = "<scriptname>"
)
public class InfoCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSenderAdapter sender, String[] args) {
        if (args.length > 0) {
            if (ScriptManager.get().isScriptRunning(args[0])) {
                Script script = ScriptManager.get().getScriptByName(args[0]);
                String scriptInfo = ScriptManager.get().getScriptInfo().printScriptInfo(script);
                sender.sendMessage(scriptInfo);
            } else {
                if (args[0].endsWith(".py")) {
                    Path scriptPath = ScriptManager.get().getScriptPath(args[0]);
                    ScriptOptions options = ScriptManager.get().getScriptOptions(scriptPath);
                    if (options != null) {
                        String scriptInfo = ScriptManager.get().getScriptInfo().printOfflineScriptInfo(args[0], scriptPath, options);
                        sender.sendMessage(scriptInfo);
                    } else
                        sender.sendMessage(ChatColor.RED + "No script found in the scripts folder with the name '" + args[0] + "'.");
                } else {
                    Path projectPath = ScriptManager.get().getProjectPath(args[0]);
                    ScriptOptions options = ScriptManager.get().getScriptOptions(projectPath);
                    if (options != null) {
                        String projectInfo = ScriptManager.get().getScriptInfo().printOfflineScriptInfo(args[0], projectPath, options);
                        sender.sendMessage(projectInfo);
                    } else
                        sender.sendMessage(ChatColor.RED + "No project found in the projects folder with the name '" + args[0] + "'.");
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSenderAdapter sender, String[] args) {
        if (args.length > 0) {
            return new ArrayList<>(ScriptManager.get().getAllScriptNames());
        } else {
            return null;
        }
    }
}
