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

package dev.magicmq.pyspigot.command;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.command.subcommands.*;
import dev.magicmq.pyspigot.util.StringUtils;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PySpigotCommand {

    private static final String HELP_CMD_HEADER = "%prefix%&a&lCommand Syntax:";
    private static final String HELP_CMD_FORMAT = "&6/%maincommand% %subcommand% &7- %description%";
    private static final String HELP_CMD_FOOTER = "&aMade with care by %author%";

    private final List<SubCommand> subCommands;

    public PySpigotCommand() {
        subCommands = new ArrayList<>();
        subCommands.add(new ReloadCommand());
        subCommands.add(new ReloadConfigCommand());
        subCommands.add(new LoadCommand());
        subCommands.add(new UnloadCommand());
        subCommands.add(new LoadLibraryCommand());
        subCommands.add(new ListScriptsCommand());
        subCommands.add(new HelpCommand());
        subCommands.add(new ReloadAllCommand());
        subCommands.add(new InfoCommand());
        subCommands.sort((o1, o2) -> {
            SubCommandMeta subCommandMeta1 = o1.getClass().getAnnotation(SubCommandMeta.class);
            SubCommandMeta subCommandMeta2 = o2.getClass().getAnnotation(SubCommandMeta.class);
            return subCommandMeta1.command().compareTo(subCommandMeta2.command());
        });
    }

    public boolean onCommand(AbstractCommandSender<?> sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("pyspigot.command.listcmds")) {
                printHelp(sender, label);
            } else {
                sender.sendMessage(ChatColor.RED + "You must specify an argument!");
            }
            return true;
        }

        SubCommand subCommand = getSubCmdFromArgument(args[0]);

        if (subCommand == null) {
            if (sender.hasPermission("pyspigot.command.listcmds")) {
                printHelp(sender, label);
            } else {
                sender.sendMessage(ChatColor.RED + "Unrecognized argument " + args[0]);
            }
            return true;
        }

        SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);

        if (!subCommandMeta.permission().isEmpty() && !sender.hasPermission(subCommandMeta.permission())) {
            sender.sendMessage(ChatColor.RED + "Insufficient permissions!");
            return true;
        }

        if (subCommandMeta.playerOnly()) {
            if (!sender.isPlayer()) {
                sender.sendMessage(ChatColor.RED + "This command can only be executed by a player.");
                return true;
            }
        }

        String[] adjustedArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!subCommand.onCommand(sender, adjustedArgs)) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + args[0] + " " + subCommandMeta.usage());
        }
        return true;
    }

    public List<String> onTabComplete(AbstractCommandSender<?> sender, String[] args) {
        List<String> toReturn = new ArrayList<>();

        if (args.length == 1) {
            for (SubCommand subCommand : subCommands) {
                SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
                if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().isEmpty()) {
                    if (subCommandMeta.playerOnly()) {
                        if (sender.isPlayer()) {
                            toReturn.add(subCommandMeta.command());
                        }
                    } else {
                        toReturn.add(subCommandMeta.command());
                    }
                }
            }
        } else if (args.length > 1) {
            SubCommand subCommand = getSubCmdFromArgument(args[0]);
            if (subCommand != null) {
                SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
                if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().isEmpty()) {
                    if (subCommandMeta.playerOnly()) {
                        if (sender.isPlayer()) {
                            String[] adjustedArgs = Arrays.copyOfRange(args, 1, args.length);
                            toReturn = subCommand.onTabComplete(sender, adjustedArgs);
                        }
                    } else {
                        String[] adjustedArgs = Arrays.copyOfRange(args, 1, args.length);
                        toReturn = subCommand.onTabComplete(sender, adjustedArgs);
                    }
                }
            }
        }

        return toReturn;
    }

    private SubCommand getSubCmdFromArgument(String arg) {
        arg = arg.toLowerCase();
        for (SubCommand subCommand : subCommands) {
            SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
            if (subCommandMeta.command().equals(arg))
                return subCommand;
            else if (Arrays.asList(subCommandMeta.aliases()).contains(arg))
                return subCommand;
        }
        return null;
    }

    private void printHelp(AbstractCommandSender<?> sender, String label) {
        sender.sendMessage(HELP_CMD_HEADER
                .replace("%prefix%", PyCore.get().getConfig().getMessage("plugin-prefix", false))
                .replace("%author%", StringUtils.replaceLastOccurrence(String.join(", ", PyCore.get().getAuthor()), ", ", " and ")));
        subCommands.forEach(subCommand -> {
            SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
            if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().isEmpty()) {
                if (!subCommandMeta.playerOnly() || sender.isPlayer()) {
                    sender.sendMessage(HELP_CMD_FORMAT
                            .replace("%maincommand%", label)
                            .replace("%subcommand%", subCommandMeta.usage().isEmpty() ? subCommandMeta.command() : subCommandMeta.command() + " " + subCommandMeta.usage())
                            .replace("%description%", subCommandMeta.description()));
                }
            }
        });
        sender.sendMessage(HELP_CMD_FOOTER
                .replace("%prefix%", PyCore.get().getConfig().getMessage("plugin-prefix", false))
                .replace("%author%", StringUtils.replaceLastOccurrence(String.join(", ", PyCore.get().getAuthor()), ", ", " and ")));
    }
}