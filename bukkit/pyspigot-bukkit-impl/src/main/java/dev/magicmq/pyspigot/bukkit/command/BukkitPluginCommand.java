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

package dev.magicmq.pyspigot.bukkit.command;

import dev.magicmq.pyspigot.bukkit.util.player.BukkitCommandSender;
import dev.magicmq.pyspigot.command.PySpigotCommand;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

/**
 * The executor for the /pyspigot command.
 */
public class BukkitPluginCommand implements TabExecutor {

    private final PySpigotCommand baseCommand;

    public BukkitPluginCommand() {
        baseCommand = new PySpigotCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandSenderAdapter bukkitSender = new BukkitCommandSender(sender);
        baseCommand.onCommand(bukkitSender, label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CommandSenderAdapter bukkitSender = new BukkitCommandSender(sender);
        return baseCommand.onTabComplete(bukkitSender, args);
    }
}
