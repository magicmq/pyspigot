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

import dev.magicmq.pyspigot.command.PySpigotCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class BukkitPluginCommand implements TabExecutor {

    private final PySpigotCommand baseCommand;

    public BukkitPluginCommand() {
        baseCommand = new PySpigotCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BukkitCommandSender commandSender = new BukkitCommandSender(sender);
        baseCommand.onCommand(commandSender, label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        BukkitCommandSender commandSender = new BukkitCommandSender(sender);
        return baseCommand.onTabComplete(commandSender, args);
    }
}
