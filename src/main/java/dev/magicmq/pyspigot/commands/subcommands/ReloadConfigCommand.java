package dev.magicmq.pyspigot.commands.subcommands;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.commands.SubCommand;
import dev.magicmq.pyspigot.commands.SubCommandMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@SubCommandMeta(
        command = "reloadconfig",
        aliases = {"configreload"},
        permission = "pyspigot.command.reloadconfig",
        description = "Reload the config (This does not reload scripts!)"
)
public class ReloadConfigCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        PySpigot.get().reload();
        sender.sendMessage(ChatColor.GREEN + "Configuration has been reloaded.");
        return false;
    }
}
