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

package dev.magicmq.pyspigot.velocity.command;


import com.velocitypowered.api.command.SimpleCommand;
import dev.magicmq.pyspigot.command.PySpigotCommand;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import dev.magicmq.pyspigot.velocity.util.player.VelocityCommandSender;

import java.util.List;

public class VelocityPluginCommand implements SimpleCommand {

    private final PySpigotCommand baseCommand;

    public VelocityPluginCommand() {
        baseCommand = new PySpigotCommand();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSenderAdapter adapter = new VelocityCommandSender(invocation.source());
        baseCommand.onCommand(adapter, invocation.alias(), invocation.arguments());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSenderAdapter adapter = new VelocityCommandSender(invocation.source());
        return baseCommand.onTabComplete(adapter, invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("pyspigot.command");
    }
}
