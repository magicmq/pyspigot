package dev.magicmq.pyspigot.commands.subcommands;

import dev.magicmq.pyspigot.commands.SubCommand;
import dev.magicmq.pyspigot.commands.SubCommandMeta;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandMeta(
        command = "unload",
        permission = "pyspigot.command.unload",
        description = "Unload a script with the specified name",
        usage = "<scriptname>"
)
public class UnloadCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (!ScriptManager.get().isScript(args[0])) {
                boolean success = ScriptManager.get().unloadScript(args[0]);
                if (success)
                    sender.sendMessage(ChatColor.GREEN + "Successfully unloaded script " + args[0] + ". Run it with /pyspigot run " + args[0]);
                else
                    sender.sendMessage(ChatColor.GREEN + "Could not unload script " + args[0] + ". Unloading was cancelled by a listener.");
            } else {
                sender.sendMessage(ChatColor.RED + "There is already a script with the name " + args[0]);
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
