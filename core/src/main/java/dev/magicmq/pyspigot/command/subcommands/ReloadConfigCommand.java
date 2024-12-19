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
import dev.magicmq.pyspigot.command.AbstractCommandSender;
import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import net.md_5.bungee.api.ChatColor;

@SubCommandMeta(
        command = "reloadconfig",
        aliases = {"configreload"},
        permission = "pyspigot.command.reloadconfig",
        description = "Reload the config (This does not reload scripts!). This command has no effect on already loaded scripts."
)
public class ReloadConfigCommand implements SubCommand {

    @Override
    public boolean onCommand(AbstractCommandSender<?> sender, String[] args) {
        PyCore.get().reloadConfigs();
        sender.sendMessage(ChatColor.GREEN + "Configuration has been reloaded.");
        return true;
    }
}
