package dev.magicmq.pyspigot.commands.subcommands;

import dev.magicmq.pyspigot.commands.SubCommand;
import dev.magicmq.pyspigot.commands.SubCommandMeta;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "listscripts",
        permission = "pyspigot.command.listscripts",
        description = "List all scripts",
        aliases = {"list", "scriptslist", "ls"},
        usage = "[page]"
)
public class ListScriptsCommand implements SubCommand {

    private static final int ENTRIES_PER_PAGE = 15;

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page <= 0)
                    page = 1;
            } catch (NumberFormatException ignored) {
                sender.sendMessage(ChatColor.RED + "The page must be a number.");
            }
        }

        List<String> pages = getPage(page);
        pages.forEach(sender::sendMessage);
        return true;
    }

    private List<String> getPage(int page) {
        List<String> scripts = new ArrayList<>(ScriptManager.get().getAllScripts());
        int totalEntries = scripts.size();
        int pages = totalEntries / ENTRIES_PER_PAGE;
        int startIndex, endIndex;
        if (pages > 0) {
            startIndex = ((page * ENTRIES_PER_PAGE) - 1);
            endIndex = ((page * ENTRIES_PER_PAGE) - 1) + ENTRIES_PER_PAGE;
            if (endIndex > scripts.size())
                endIndex = scripts.size();
        } else {
            startIndex = 0;
            endIndex = scripts.size();
        }

        List<String> toReturn = new ArrayList<>();
        toReturn.add(ChatColor.YELLOW + "List of scripts, page " + page + " of " + (pages > 0 ? pages : 1) + " (" + scripts.size() + " total scripts)");
        for (int i = startIndex; i < endIndex; i++) {
            String script = scripts.get(i);
            if (ScriptManager.get().isScript(script))
                toReturn.add(ChatColor.GREEN + script);
            else
                toReturn.add(ChatColor.RED + script);
        }
        toReturn.add(ChatColor.RED + "Red =" + ChatColor.YELLOW + " script unloaded, " + ChatColor.GREEN + "Green =" + ChatColor.YELLOW + " script loaded");
        return toReturn;
    }
}
