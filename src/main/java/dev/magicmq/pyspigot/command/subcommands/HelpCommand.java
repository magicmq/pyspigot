package dev.magicmq.pyspigot.command.subcommands;

import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@SubCommandMeta(
        command = "help",
        permission = "pyspigot.command.help",
        description = "Useful information for PySpigot and how to get help",
        aliases = {"gethelp", "info", "getinfo"}
)
public class HelpCommand implements SubCommand {

    private static final String[] HELP_MESSAGE = {
            "&b&l- PySpigot documentation: &b&nhttps://pyspigot-docs.magicmq.dev",
            "&b&l- Spigot Plugin Page: &b&nhttps://spigotmc.org/resources/pyspigot.111006/",
            "&b- Need help? Reach out on the PySpigot Discord: &b&nhttps://discord.gg/f2u7nzRwuk",
            "&b- Found a bug? Please let me know on Discord (link above) and/or submit a bug report on the PySpigot Github repo: &b&nhttps://github.com/magicmq/pyspigot/issues"
    };

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        for (String s : HELP_MESSAGE) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
        return true;
    }
}
