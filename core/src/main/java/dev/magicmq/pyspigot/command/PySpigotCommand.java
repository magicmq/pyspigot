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
import dev.magicmq.pyspigot.command.subcommands.HelpCommand;
import dev.magicmq.pyspigot.command.subcommands.InfoCommand;
import dev.magicmq.pyspigot.command.subcommands.ListScriptsCommand;
import dev.magicmq.pyspigot.command.subcommands.LoadCommand;
import dev.magicmq.pyspigot.command.subcommands.LoadLibraryCommand;
import dev.magicmq.pyspigot.command.subcommands.ReloadAllCommand;
import dev.magicmq.pyspigot.command.subcommands.ReloadCommand;
import dev.magicmq.pyspigot.command.subcommands.ReloadConfigCommand;
import dev.magicmq.pyspigot.command.subcommands.UnloadCommand;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PySpigotCommand {

    public static final Component PLUGIN_PREFIX = Component.text()
            .append(Component.text("[", NamedTextColor.DARK_GRAY))
            .append(Component.text(PyCore.get().getPluginIdentifier(), NamedTextColor.GOLD))
            .append(Component.text("] ", NamedTextColor.DARK_GRAY))
            .build();
    private static final Component HELP_CMD_HEADER = Component.text()
            .append(PLUGIN_PREFIX)
            .append(Component.text("Command Syntax:", NamedTextColor.GREEN, TextDecoration.BOLD))
            .build();

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

    public boolean onCommand(CommandSenderAdapter sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("pyspigot.command.listcmds")) {
                printHelp(sender, label);
            } else {
                sender.sendMessage(Component.text("You must specify an argument!", NamedTextColor.RED));
            }
            return true;
        }

        SubCommand subCommand = getSubCmdFromArgument(args[0]);

        if (subCommand == null) {
            if (sender.hasPermission("pyspigot.command.listcmds")) {
                printHelp(sender, label);
            } else {
                sender.sendMessage(Component.text("Unrecognized argument " + args[0], NamedTextColor.RED));
            }
            return true;
        }

        SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);

        if (!subCommandMeta.permission().isEmpty() && !sender.hasPermission(subCommandMeta.permission())) {
            sender.sendMessage(Component.text("Insufficient permissions!", NamedTextColor.RED));
            return true;
        }

        if (subCommandMeta.playerOnly()) {
            if (!sender.isPlayer()) {
                sender.sendMessage(Component.text("This command can only be executed by a player.", NamedTextColor.RED));
                return true;
            }
        }

        String[] adjustedArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!subCommand.onCommand(sender, adjustedArgs)) {
            sender.sendMessage(Component.text("Usage: /" + label + " " + args[0] + " " + subCommandMeta.usage(), NamedTextColor.RED));
        }
        return true;
    }

    public List<String> onTabComplete(CommandSenderAdapter sender, String[] args) {
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

    private void printHelp(CommandSenderAdapter sender, String label) {
        TextComponent.Builder builder = Component.text();
        builder.append(HELP_CMD_HEADER);
        builder.appendNewline();

        subCommands.forEach(subCommand -> {
            SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
            if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().isEmpty()) {
                if (!subCommandMeta.playerOnly() || sender.isPlayer()) {
                    String subCommandString = subCommandMeta.usage().isEmpty() ? subCommandMeta.command() : subCommandMeta.command() + " " + subCommandMeta.usage();
                    builder.append(Component.text()
                                    .append(Component.text("/" + label + " " + subCommandString + " ", NamedTextColor.GOLD))
                                    .append(Component.text("- " + subCommandMeta.description(), NamedTextColor.GRAY))
                                    .build());
                    builder.appendNewline();
                }
            }
        });

        sender.sendMessage(builder.build());
    }
}