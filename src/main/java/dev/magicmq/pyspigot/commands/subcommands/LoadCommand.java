package dev.magicmq.pyspigot.commands.subcommands;

import dev.magicmq.pyspigot.commands.SubCommand;
import dev.magicmq.pyspigot.commands.SubCommandMeta;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import dev.magicmq.pyspigot.managers.script.ScriptType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@SubCommandMeta(
        command = "load",
        permission = "pyspigot.command.load",
        description = "Load a script with the specified name",
        usage = "<scriptname>"
)
public class LoadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (!ScriptManager.get().isScript(args[0])) {
                try {
                    boolean success = ScriptManager.get().loadScript(args[0], ScriptType.NORMAL);
                    if (success)
                        sender.sendMessage(ChatColor.GREEN + "Successfully loaded script " + args[0]);
                    else
                        sender.sendMessage(ChatColor.GREEN + "Could not load script " + args[0] + ". See console for details.");
                } catch (FileNotFoundException e) {
                    sender.sendMessage(ChatColor.RED + "No script found in the scripts folder with the name " + args[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "There was an error loading the script! See the console for details.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "There is already a loaded script with the name " + args[0]);
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
