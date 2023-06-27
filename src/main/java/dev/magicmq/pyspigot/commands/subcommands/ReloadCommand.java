package dev.magicmq.pyspigot.commands.subcommands;

import dev.magicmq.pyspigot.commands.SubCommand;
import dev.magicmq.pyspigot.commands.SubCommandMeta;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@SubCommandMeta(
        command = "reload",
        permission = "pyspigot.command.reload",
        description = "Reload a script with the specified name",
        usage = "<scriptname>"
)
public class ReloadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (ScriptManager.get().isScriptLoaded(args[0])) {
                try {
                    boolean success = ScriptManager.get().reloadScript(args[0]);
                    if (success) {
                        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded script " + args[0]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "There was an error when reloading script " + args[0] + ". See console for details.");
                    }
                } catch (FileNotFoundException e) {
                    sender.sendMessage(ChatColor.RED + "No script found in the scripts folder with the name " + args[0] + " to reload from");
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an error when reloading script " + args[0] + ". See console for details.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Script " + args[0] + " not found");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length > 0) {
            return ScriptManager.get().getLoadedScripts();
        } else {
            return null;
        }
    }
}