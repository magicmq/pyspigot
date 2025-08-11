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

import java.util.List;
import java.util.SortedSet;

@SubCommandMeta(
        command = "reload",
        permission = "pyspigot.command.reload",
        description = "Reload a script or project with the specified name",
        usage = "<scriptname/projectname>"
)
public class ReloadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSenderAdapter sender, String[] args) {
        if (args.length > 0) {
            if (ScriptManager.get().isScriptRunning(args[0])) {
                boolean success = ScriptManager.get().unloadScript(args[0]);
                if (!success) {
                    sender.sendMessage(Component.text("There was an error when unloading script/project '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                    return true;
                }
            }

            if (args[0].endsWith(".py")) {
                try {
                    RunResult result = ScriptManager.get().loadScript(args[0]);
                    if (result == RunResult.SUCCESS)
                        sender.sendMessage(Component.text("Successfully reloaded script '" + args[0] + "'.", NamedTextColor.GREEN));
                    else if (result == RunResult.FAIL_PLUGIN_DEPENDENCY)
                        sender.sendMessage(Component.text("Script '" + args[0] + "' was not reloaded due to missing plugin dependencies. See console for details.", NamedTextColor.RED));
                    else if (result == RunResult.FAIL_DISABLED)
                        sender.sendMessage(Component.text("Script '" + args[0] + "' was not reloaded because it is disabled as per its options in script_options.yml.", NamedTextColor.RED));
                    else if (result == RunResult.FAIL_ERROR)
                        sender.sendMessage(Component.text("There was an error when reloading script '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                    else if (result == RunResult.FAIL_SCRIPT_NOT_FOUND)
                        sender.sendMessage(Component.text("No script found in the scripts folder with the name '" + args[0] + "'.", NamedTextColor.RED));
                } catch (ScriptInitializationException e) {
                    PyCore.get().getLogger().error("Error when loading script '{}'", args[0], e);
                    sender.sendMessage(Component.text("There was an error when reloading script '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                }
            } else {
                try {
                    RunResult result = ScriptManager.get().loadProject(args[0]);
                    if (result == RunResult.SUCCESS)
                        sender.sendMessage(Component.text("Successfully reloaded project '" + args[0] + "'.", NamedTextColor.GREEN));
                    else if (result == RunResult.FAIL_PLUGIN_DEPENDENCY)
                        sender.sendMessage(Component.text("Project '" + args[0] + "' was not reloaded due to missing plugin dependencies. See console for details.", NamedTextColor.RED));
                    else if (result == RunResult.FAIL_DISABLED)
                        sender.sendMessage(Component.text("Project '" + args[0] + "' was not run because it is disabled as per its options in its project.yml.", NamedTextColor.RED));
                    else if (result == RunResult.FAIL_NO_MAIN)
                        sender.sendMessage(Component.text("Project '" + args[0] + "' was not run because the main script file was not found in the project folder.", NamedTextColor.RED));
                    else if (result == RunResult.FAIL_ERROR)
                        sender.sendMessage(Component.text("There was an error when reloading project '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                    else if (result == RunResult.FAIL_SCRIPT_NOT_FOUND)
                        sender.sendMessage(Component.text("No project found in the projects folder with the name '" + args[0] + "'.", NamedTextColor.RED));
                } catch (ScriptInitializationException e) {
                    PyCore.get().getLogger().error("Error when loading script '{}'", args[0], e);
                    sender.sendMessage(Component.text("There was an error when reloading project '" + args[0] + "'. See console for details.", NamedTextColor.RED));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSenderAdapter sender, String[] args) {
        if (args.length > 0) {
            SortedSet<String> scripts = ScriptManager.get().getAllScriptNames();
            scripts.addAll(ScriptManager.get().getAllProjectNames());
            return List.copyOf(scripts);
        } else {
            return List.of();
        }
    }
}