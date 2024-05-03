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
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.listener.ScriptEventListener;
import dev.magicmq.pyspigot.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.manager.placeholder.ScriptPlaceholder;
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.protocol.ScriptPacketListener;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.task.Task;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "info",
        permission = "pyspigot.command.info",
        description = "Print information about a script, including uptime, registered listeners, commands, and more info",
        aliases = {"scriptinfo"},
        usage = "<scriptname>"
)
public class InfoCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].endsWith(".py")) {
                StringBuilder builder = new StringBuilder();
                builder.append(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "Information about " + args[0] + "\n");
                if (ScriptManager.get().isScriptRunning(args[0])) {
                    Script script = ScriptManager.get().getScript(args[0]);

                    Duration uptime = Duration.ofMillis(script.getUptime());
                    builder.append(ChatColor.GOLD + "Uptime: " + ChatColor.RESET + StringUtils.formatDuration(uptime) + "\n");

                    List<ScriptCommand> registeredCommands = CommandManager.get().getCommands(script);
                    List<String> commandNames = new ArrayList<>();
                    if (registeredCommands != null)
                        registeredCommands.forEach(command -> commandNames.add(command.toString()));
                    builder.append(ChatColor.GOLD + "Registered commands: " + ChatColor.RESET + commandNames + "\n");

                    List<ScriptEventListener> registeredListeners = ListenerManager.get().getListeners(script);
                    List<String> eventsListening = new ArrayList<>();
                    if (registeredListeners != null)
                        registeredListeners.forEach(listener -> eventsListening.add(listener.toString()));
                    builder.append(ChatColor.GOLD + "Listening to events: " + ChatColor.RESET + eventsListening + "\n");

                    ScriptPlaceholder placeholder = PlaceholderManager.get().getPlaceholder(script);
                    if (placeholder != null)
                        builder.append(ChatColor.GOLD + "Registered placeholder: " + ChatColor.RESET + placeholder + "\n");
                    else
                        builder.append(ChatColor.GOLD + "Registered placeholder: " + ChatColor.RESET + "None" + "\n");

                    List<ScriptPacketListener> registeredPacketListeners = ProtocolManager.get().getPacketListeners(script);
                    List<String> packetTypes = new ArrayList<>();
                    if (registeredPacketListeners != null)
                        registeredPacketListeners.forEach(listener -> packetTypes.add(listener.toString()));
                    builder.append(ChatColor.GOLD + "Listening to packet types: " + ChatColor.RESET + packetTypes + "\n");

                    List<ScriptPacketListener> registeredPacketListenersAsync = ProtocolManager.get().async().getAsyncPacketListeners(script);
                    List<String> packetTypesAsync = new ArrayList<>();
                    if (registeredPacketListenersAsync != null)
                        registeredPacketListenersAsync.forEach(listener -> packetTypesAsync.add(listener.toString()));
                    builder.append(ChatColor.GOLD + "Listening to packet types (async): " + ChatColor.RESET + packetTypesAsync + "\n");

                    List<Task> scriptTasks = TaskManager.get().getTasks(script);
                    List<String> tasksInfo = new ArrayList<>();
                    if (scriptTasks != null)
                        scriptTasks.forEach(task -> tasksInfo.add(task.toString()));
                    builder.append(ChatColor.GOLD + "Running tasks: " + ChatColor.RESET + tasksInfo + "\n");

                    builder.append(ChatColor.GOLD + "Script options: " + ChatColor.RESET + script.getOptions().toString());

                    sender.sendMessage(builder.toString());
                } else {
                    try {
                        builder.append(ChatColor.GOLD + "Uptime: " + ChatColor.RESET + "Currently not loaded" + "\n");

                        builder.append(ChatColor.GOLD + "Script options: " + ChatColor.RESET + ScriptManager.get().getScriptOptions(args[0]).toString());

                        sender.sendMessage(builder.toString());
                    } catch (FileNotFoundException e) {
                        sender.sendMessage(ChatColor.RED + "No script found in the scripts folder with the name '" + args[0] + "'.");
                    }
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
