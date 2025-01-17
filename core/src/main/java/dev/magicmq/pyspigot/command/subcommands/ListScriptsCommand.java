/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.command.subcommands;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import net.md_5.bungee.api.ChatColor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SubCommandMeta(
        command = "listscripts",
        aliases = {"list", "scriptslist", "ls"},
        permission = "pyspigot.command.listscripts",
        description = "List all scripts",
        usage = "[page]"
)
public class ListScriptsCommand implements SubCommand {

    private static final int ENTRIES_PER_PAGE = 15;

    @Override
    public boolean onCommand(CommandSenderAdapter sender, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page <= 0)
                    page = 1;
            } catch (NumberFormatException ignored) {
                sender.sendMessage("&cThe page must be a number.");
            }
        }

        List<String> pages = getPage(page);
        pages.forEach(sender::sendMessage);
        return true;
    }

    private List<String> getPage(int page) {
        List<Path> scripts = new ArrayList<>(ScriptManager.get().getAllScriptPaths());
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
        toReturn.add("&eList of scripts, page " + page + " of " + (pages > 0 ? pages : 1) + " (" + scripts.size() + " total scripts)");
        for (int i = startIndex; i < endIndex; i++) {
            Path script = scripts.get(i);
            String fileName = script.getFileName().toString();
            if (ScriptManager.get().isScriptRunning(fileName))
                toReturn.add(ChatColor.GREEN + fileName + " (" + PyCore.get().getDataFolderPath().relativize(script) + ")");
            else
                toReturn.add(ChatColor.RED + fileName + " (" + PyCore.get().getDataFolderPath().relativize(script) + ")");
        }
        toReturn.add("&cRed = &escript unloaded, &aGreen = &escript loaded");
        return toReturn;
    }
}
