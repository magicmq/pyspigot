package dev.magicmq.pyspigot.commands;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public interface SubCommand {

    boolean onCommand(CommandSender sender, String[] args);

    default List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}