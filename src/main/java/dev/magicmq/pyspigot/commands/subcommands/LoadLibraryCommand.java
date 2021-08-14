package dev.magicmq.pyspigot.commands.subcommands;

import dev.magicmq.pyspigot.commands.SubCommand;
import dev.magicmq.pyspigot.commands.SubCommandMeta;
import dev.magicmq.pyspigot.managers.libraries.LibraryManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@SubCommandMeta(
        command = "loadlibrary",
        aliases = {"loadlib"},
        permission = "pyspigot.command.loadlibrary",
        description = "Load a library as the specified file name in the libs folder.",
        usage = "<filename>"
)
public class LoadLibraryCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (args.length > 0) {
            LibraryManager.LoadResult result = LibraryManager.get().loadLibrary(args[0]);
            if (result == LibraryManager.LoadResult.FAILED_FILE)
                sender.sendMessage(ChatColor.RED + "File " + args[0] + " not found. Did you make sure to include the extension (.jar)?");
            else if (result == LibraryManager.LoadResult.FAILED_LOADED)
                sender.sendMessage(ChatColor.RED + "This library appears to be already loaded.");
            else if (result == LibraryManager.LoadResult.FAILED_ERROR)
                sender.sendMessage(ChatColor.RED + "Loading library failed unexpectedly. Please see console for details.");
            else
                sender.sendMessage(ChatColor.GREEN + "Successfully loaded library " + args[0] + ". Scripts can now use this library.");
            return true;
        }
        return false;
    }
}
