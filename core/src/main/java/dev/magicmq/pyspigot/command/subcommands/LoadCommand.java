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
import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.exception.ScriptInitializationException;
import dev.magicmq.pyspigot.manager.script.RunResult;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "load",
        aliases = {"start"},
        permission = "pyspigot.command.load",
        description = "Load a script or project with the specified name",
        usage = "<scriptname/projectname>"
)
public class LoadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSenderAdapter sender, String[] args) {
        if (args.length > 0) {
            if (args[0].endsWith(".py")) {
                if (!ScriptManager.get().isScriptRunning(args[0])) {
                    try {
                        RunResult result = ScriptManager.get().loadScript(args[0]);
                        if (result == RunResult.SUCCESS)
                            sender.sendMessage(Component.text("Successfully loaded and ran script '" + args[0] + "'.", NamedTextColor.GREEN));
                        else if (result == RunResult.FAIL_PLUGIN_DEPENDENCY)
                            sender.sendMessage(Component.text("Script '" + args[0] + "' was not run due to missing plugin dependencies. See console for details.", NamedTextColor.RED));
                        else if (result == RunResult.FAIL_DISABLED)
                            sender.sendMessage(Component.text("Script '" + args[0] + "' was not run because it is disabled as per its options in script_options.yml.", NamedTextColor.RED));
                        else if (result == RunResult.FAIL_ERROR)
                            sender.sendMessage(Component.text("There was an error when running script '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                        else if (result == RunResult.FAIL_SCRIPT_NOT_FOUND)
                            sender.sendMessage(Component.text("No script found in the scripts folder with the name '" + args[0] + "'.", NamedTextColor.RED));
                    } catch (ScriptInitializationException e) {
                        PyCore.get().getLogger().error("Error when loading script '{}'", args[0], e);
                        sender.sendMessage(Component.text("There was an error when loading script '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("There is already a loaded and running script with the name '" + args[0] + "'.", NamedTextColor.RED));
                }
            } else {
                if (!ScriptManager.get().isScriptRunning(args[0])) {
                    try {
                        RunResult result = ScriptManager.get().loadProject(args[0]);
                        if (result == RunResult.SUCCESS)
                            sender.sendMessage(Component.text("Successfully loaded and ran project '" + args[0] + "'.", NamedTextColor.GREEN));
                        else if (result == RunResult.FAIL_PLUGIN_DEPENDENCY)
                            sender.sendMessage(Component.text("Project '" + args[0] + "' was not run due to missing plugin dependencies. See console for details.", NamedTextColor.RED));
                        else if (result == RunResult.FAIL_DISABLED)
                            sender.sendMessage(Component.text("Project '" + args[0] + "' was not run because it is disabled as per its options in its project.yml.", NamedTextColor.RED));
                        else if (result == RunResult.FAIL_NO_MAIN)
                            sender.sendMessage(Component.text("Project '" + args[0] + "' was not run because the main script file was not found in the project folder.", NamedTextColor.RED));
                        else if (result == RunResult.FAIL_ERROR)
                            sender.sendMessage(Component.text("There was an error when running project '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                        else if (result == RunResult.FAIL_SCRIPT_NOT_FOUND)
                            sender.sendMessage(Component.text("No project found in the projects folder with the name '" + args[0] + "'.", NamedTextColor.RED));
                    } catch (ScriptInitializationException e) {
                        PyCore.get().getLogger().error("Error when loading project '{}'", args[0], e);
                        sender.sendMessage(Component.text("There was an error when loading project '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("There is already a loaded and running project with the name '" + args[0] + "'.", NamedTextColor.RED));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSenderAdapter sender, String[] args) {
        if (args.length > 0) {
            return List.copyOf(ScriptManager.get().getAllScriptNames());
        } else {
            return List.of();
        }
    }
}
