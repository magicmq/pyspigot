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

package dev.magicmq.pyspigot.manager.script;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.database.Database;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.redis.client.ScriptRedisClient;
import dev.magicmq.pyspigot.manager.task.Task;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.StringUtils;
import net.md_5.bungee.api.ChatColor;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that fetches and returns a script's info.
 */
public abstract class ScriptInfo {

    /**
     * Print platform-specific manager information for a script.
     * @param script The script whose information should be printed
     * @param appendTo The info StringBuilder that platform-specific manager info should be appended to
     */
    protected abstract void printPlatformManagerInfo(Script script, StringBuilder appendTo);

    /**
     * Print a script's info (for the /pyspigot info command).
     * @param script The script whose information should be printed
     * @return The info for the script
     */
    public String printScriptInfo(Script script) {
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "Information about " + script.getName() + "\n");

        Path relativePath = PyCore.get().getDataFolderPath().relativize(script.getPath());
        builder.append(ChatColor.GOLD + "Location: " + ChatColor.RESET + relativePath + "\n");

        Duration uptime = Duration.ofMillis(script.getUptime());
        builder.append(ChatColor.GOLD + "Uptime: " + ChatColor.RESET + StringUtils.formatDuration(uptime) + "\n");

        List<?> registeredCommands = CommandManager.get().getCommands(script);
        List<String> commandNames = new ArrayList<>();
        if (registeredCommands != null)
            registeredCommands.forEach(command -> commandNames.add(command.toString()));
        builder.append(ChatColor.GOLD + "Registered commands: " + ChatColor.RESET + commandNames + "\n");

        List<?> registeredListeners = ListenerManager.get().getListeners(script);
        List<String> eventsListening = new ArrayList<>();
        if (registeredListeners != null)
            registeredListeners.forEach(listener -> eventsListening.add(listener.toString()));
        builder.append(ChatColor.GOLD + "Listening to events: " + ChatColor.RESET + eventsListening + "\n");

        List<Task> scriptTasks = TaskManager.get().getTasks(script);
        List<String> tasksInfo = new ArrayList<>();
        if (scriptTasks != null)
            scriptTasks.forEach(task -> tasksInfo.add(task.toString()));
        builder.append(ChatColor.GOLD + "Running tasks: " + ChatColor.RESET + tasksInfo + "\n");

        List<Database> scriptDatabases = DatabaseManager.get().getConnections(script);
        List<String> databasesInfo = new ArrayList<>();
        if (scriptDatabases != null)
            scriptDatabases.forEach(database -> databasesInfo.add(database.toString()));
        builder.append(ChatColor.GOLD + "Database connections: " + ChatColor.RESET + databasesInfo + "\n");

        List<ScriptRedisClient> scriptRedisClients = RedisManager.get().getRedisClients(script);
        List<String> redisInfo = new ArrayList<>();
        if (scriptRedisClients != null)
            scriptRedisClients.forEach(redisClient -> redisInfo.add(redisClient.toString()));
        builder.append(ChatColor.GOLD + "Redis clients: " + ChatColor.RESET + redisInfo + "\n");

        printPlatformManagerInfo(script, builder);

        builder.append(ChatColor.GOLD + "Script options: " + ChatColor.RESET + script.getOptions().toString());

        return builder.toString();
    }

    /**
     * Print a script's info (for the /pyspigot info command), if the script is not loaded.
     * @param scriptName The name of the script whose information should be printed
     * @param scriptPath The path of the script
     * @param options The options of the script
     * @return The info for the script
     */
    public String printOfflineScriptInfo(String scriptName, Path scriptPath, ScriptOptions options) {
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "Information about " + scriptName + "\n");

        Path relativePath = PyCore.get().getDataFolderPath().relativize(scriptPath);
        builder.append(ChatColor.GOLD + "Location: " + ChatColor.RESET + relativePath + "\n");

        builder.append(ChatColor.GOLD + "Uptime: " + ChatColor.RESET + "Currently not loaded" + "\n");

        builder.append(ChatColor.GOLD + "Script options: " + ChatColor.RESET + options.toString());

        return builder.toString();
    }
}
