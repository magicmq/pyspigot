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

package dev.magicmq.pyspigot.bungee.command;

import dev.magicmq.pyspigot.bungee.util.player.BungeeCommandSender;
import dev.magicmq.pyspigot.command.PySpigotCommand;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeePluginCommand extends Command implements TabExecutor {

    private final PySpigotCommand baseCommand;

    public BungeePluginCommand() {
        super("pybungee", null, "pb");

        baseCommand = new PySpigotCommand();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CommandSenderAdapter bungeeSender = new BungeeCommandSender(sender);
        baseCommand.onCommand(bungeeSender, getName(), args);
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        CommandSenderAdapter bungeeSender = new BungeeCommandSender(sender);
        return baseCommand.onTabComplete(bungeeSender, args);
    }
}
