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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
     * @param appendTo The TextComponent that platform-specific manager info should be appended to
     */
    protected abstract void printPlatformManagerInfo(Script script, TextComponent.Builder appendTo);

    /**
     * Print a script's info (for the /pyspigot info command).
     * @param script The script whose information should be printed
     * @return The info for the script
     */
    public TextComponent printScriptInfo(Script script) {
        TextComponent.Builder builder = Component.text();

        builder.append(Component.text("Information about " + script.getName(), NamedTextColor.GOLD, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        builder.appendNewline();

        Path relativePath = PyCore.get().getDataFolderPath().relativize(script.getPath());
        builder.append(Component.text().append(Component.text("Location: ", NamedTextColor.GOLD)).append(Component.text(relativePath.toString())));
        builder.appendNewline();

        Duration uptime = Duration.ofMillis(script.getUptime());
        builder.append(Component.text().append(Component.text("Uptime: ", NamedTextColor.GOLD)).append(Component.text(StringUtils.formatDuration(uptime))));
        builder.appendNewline();

        List<?> registeredCommands = CommandManager.get().getCommands(script);
        List<String> commandNames = new ArrayList<>();
        if (registeredCommands != null)
            registeredCommands.forEach(command -> commandNames.add(command.toString()));
        builder.append(Component.text().append(Component.text("Registered commands: ", NamedTextColor.GOLD)).append(Component.text(commandNames.toString())));
        builder.appendNewline();

        List<?> registeredListeners = ListenerManager.get().getListeners(script);
        List<String> eventsListening = new ArrayList<>();
        if (registeredListeners != null)
            registeredListeners.forEach(listener -> eventsListening.add(listener.toString()));
        builder.append(Component.text().append(Component.text("Listening to events: ", NamedTextColor.GOLD)).append(Component.text(eventsListening.toString())));
        builder.appendNewline();

        List<? extends Task<?>> scriptTasks = TaskManager.get().getTasks(script);
        List<String> tasksInfo = new ArrayList<>();
        if (scriptTasks != null)
            scriptTasks.forEach(task -> tasksInfo.add(task.toString()));
        builder.append(Component.text().append(Component.text("Running tasks: ", NamedTextColor.GOLD)).append(Component.text(tasksInfo.toString())));
        builder.appendNewline();

        List<Database> scriptDatabases = DatabaseManager.get().getConnections(script);
        List<String> databasesInfo = new ArrayList<>();
        if (scriptDatabases != null)
            scriptDatabases.forEach(database -> databasesInfo.add(database.toString()));
        builder.append(Component.text().append(Component.text("Database connections: ", NamedTextColor.GOLD)).append(Component.text(databasesInfo.toString())));
        builder.appendNewline();

        List<ScriptRedisClient> scriptRedisClients = RedisManager.get().getRedisClients(script);
        List<String> redisInfo = new ArrayList<>();
        if (scriptRedisClients != null)
            scriptRedisClients.forEach(redisClient -> redisInfo.add(redisClient.toString()));
        builder.append(Component.text().append(Component.text("Redis client: ", NamedTextColor.GOLD)).append(Component.text(redisInfo.toString())));
        builder.appendNewline();

        printPlatformManagerInfo(script, builder);

        builder.append(Component.text().append(Component.text("Script options: ", NamedTextColor.GOLD)).append(Component.text(script.getOptions().toString())));

        return builder.build();
    }

    /**
     * Print a script's info (for the /pyspigot info command), if the script is not loaded.
     * @param scriptName The name of the script whose information should be printed
     * @param scriptPath The path of the script
     * @param options The options of the script
     * @return The info for the script
     */
    public TextComponent printOfflineScriptInfo(String scriptName, Path scriptPath, ScriptOptions options) {
        TextComponent.Builder builder = Component.text();

        builder.append(Component.text("Information about " + scriptName, NamedTextColor.GOLD, TextDecoration.BOLD, TextDecoration.UNDERLINED));
        builder.appendNewline();

        Path relativePath = PyCore.get().getDataFolderPath().relativize(scriptPath);
        builder.append(Component.text().append(Component.text("Location: ", NamedTextColor.GOLD)).append(Component.text(relativePath.toString())));
        builder.appendNewline();

        builder.append(Component.text().append(Component.text("Uptime: ", NamedTextColor.GOLD)).append(Component.text("Not loaded")));
        builder.appendNewline();

        builder.append(Component.text().append(Component.text("Script options: ", NamedTextColor.GOLD)).append(Component.text(options.toString())));

        return builder.build();
    }
}
