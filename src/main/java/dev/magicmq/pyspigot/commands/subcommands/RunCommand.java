package dev.magicmq.pyspigot.commands.subcommands;

import dev.magicmq.pyspigot.commands.SubCommand;
import dev.magicmq.pyspigot.commands.SubCommandMeta;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

@SubCommandMeta(
        command = "run",
        permission = "pyspigot.command.run",
        description = "Run a script with the specified name",
        usage = "<scriptname>"
)
public class RunCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (ScriptManager.get().isScript(args[0])) {
                if (!ScriptManager.get().isScriptRunning(args[0])) {
                    boolean success = ScriptManager.get().runScript(args[0]);
                    if (success)
                        sender.sendMessage(ChatColor.GREEN + "Successfully started script " + args[0]);
                    else
                        sender.sendMessage(ChatColor.RED + "Script was not run. See console for details.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Script " + args[0] + " is already running");
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
