package dev.magicmq.pyspigot.commands;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.commands.subcommands.*;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PySpigotCommand implements TabExecutor {

    private static final String HELP_CMD_HEADER = ChatColor.translateAlternateColorCodes('&', "%prefix%&a&lCommand Syntax:");
    private static final String HELP_CMD_FORMAT = ChatColor.translateAlternateColorCodes('&', "&6/%maincommand% %subcommand% &7- %description%");
    private static final String HELP_CMD_FOOTER = ChatColor.translateAlternateColorCodes('&', "&aMade with care by %author%");

    private final List<SubCommand> subCommands;

    public PySpigotCommand() {
        subCommands = new ArrayList<>();
        subCommands.add(new ReloadCommand());
        subCommands.add(new ReloadConfigCommand());
        subCommands.add(new LoadCommand());
        subCommands.add(new UnloadCommand());
        subCommands.sort((o1, o2) -> {
            SubCommandMeta subCommandMeta1 = o1.getClass().getAnnotation(SubCommandMeta.class);
            SubCommandMeta subCommandMeta2 = o2.getClass().getAnnotation(SubCommandMeta.class);
            return subCommandMeta1.command().compareTo(subCommandMeta2.command());
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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

        if (!subCommandMeta.permission().equals("") && !sender.hasPermission(subCommandMeta.permission())) {
            sender.sendMessage(ChatColor.RED + "Insufficient permissions!");
            return true;
        }

        if (subCommandMeta.playerOnly()) {
            if (!(sender instanceof Player)) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> toReturn = new ArrayList<>();

        if (args.length == 1) {
            for (SubCommand subCommand : subCommands) {
                SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
                if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().equals("")) {
                    if (subCommandMeta.playerOnly()) {
                        if (sender instanceof Player) {
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
                if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().equals("")) {
                    if (subCommandMeta.playerOnly()) {
                        if (sender instanceof Player) {
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

    private void printHelp(CommandSender sender, String label) {
        sender.sendMessage(HELP_CMD_HEADER
                .replace("%prefix%", PluginConfig.getMessage("plugin-prefix", false))
                .replace("%author%", StringUtils.replaceLastOccurrence(String.join(", ", PySpigot.get().getDescription().getAuthors()), ", ", " and ")));
        subCommands.forEach(subCommand -> {
            SubCommandMeta subCommandMeta = subCommand.getClass().getAnnotation(SubCommandMeta.class);
            if (sender.hasPermission(subCommandMeta.permission()) || subCommandMeta.permission().equals("")) {
                if ((subCommandMeta.playerOnly() && sender instanceof Player) || !subCommandMeta.playerOnly()) {
                    sender.sendMessage(HELP_CMD_FORMAT
                            .replace("%maincommand%", label)
                            .replace("%subcommand%", subCommandMeta.usage().equals("") ? subCommandMeta.command() : subCommandMeta.command() + " " + subCommandMeta.usage())
                            .replace("%description%", subCommandMeta.description()));
                }
            }
        });
        sender.sendMessage(HELP_CMD_FOOTER
                .replace("%prefix%", PluginConfig.getMessage("plugin-prefix", false))
                .replace("%author%", StringUtils.replaceLastOccurrence(String.join(", ", PySpigot.get().getDescription().getAuthors()), ", ", " and ")));
    }
}