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

import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@SubCommandMeta(
        command = "loadlibrary",
        aliases = {"loadlib"},
        permission = "pyspigot.command.loadlibrary",
        description = "Load a library as the specified file name in the java-libs folder.",
        usage = "<filename>"
)
public class LoadLibraryCommand implements SubCommand {

    @Override
    public boolean onCommand(CommandSenderAdapter sender, String[] args) {
        if (args.length > 0) {
            LibraryManager.LoadResult result = LibraryManager.get().loadLibrary(args[0]);
            if (result == LibraryManager.LoadResult.FAILED_FILE)
                sender.sendMessage(Component.text("File '" + args[0] + "' not found. Did you make sure to include the extension (.jar)?", NamedTextColor.RED));
            else if (result == LibraryManager.LoadResult.FAILED_LOADED)
                sender.sendMessage(Component.text("This library appears to be already loaded.", NamedTextColor.RED));
            else if (result == LibraryManager.LoadResult.FAILED_ERROR)
                sender.sendMessage(Component.text("Loading library failed unexpectedly. Please see console for details.", NamedTextColor.RED));
            else
                sender.sendMessage(Component.text("Successfully loaded library '" + args[0] + "'. Scripts can now use this library.", NamedTextColor.GREEN));
            return true;
        }
        return false;
    }
}
