/*
 *    Copyright 2023 magicmq
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

package dev.magicmq.pyspigot.manager.playground;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.util.logging.ChatPrintStream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * A manager class that handles playground mode, and interactive interpreter that can be accessed via /pyspigot playground and interacted with via in-game chat. Uses {@link org.graalvm.polyglot.Context} to interpret code along with a {@link ChatPrintStream} to redirect output to a player's chat window.
 */
public class PlaygroundManager {

    private static PlaygroundManager instance;

    private final Engine engine;
    private final ConcurrentHashMap<String, Context> activePlaygrounds;

    private PlaygroundManager() {
        this.engine = PluginConfig.getEngineBuilder().build();
        this.activePlaygrounds = new ConcurrentHashMap<>();
    }

    /**
     * Closes and removes all active playgrounds, including closing the associated contexts
     */
    public void shutdown() {
        for (Map.Entry<String, Context> playground : activePlaygrounds.entrySet()) {
            Context context = playground.getValue();
            context.close(true);
            Player player = Bukkit.getPlayer(playground.getKey());
            if (player != null)
                player.sendMessage(PluginConfig.getMessage("exit-playground-mode", true));
        }
        activePlaygrounds.clear();
    }

    /**
     * Check if a CommandSender is in playground mode.
     * @param sender The sender to check
     * @return True if the CommandSender is in playground mode, false if otherwise
     */
    public boolean isInPlayground(CommandSender sender) {
        return activePlaygrounds.containsKey(sender.getName());
    }

    /**
     * Initialize and enter playground mode for the provided CommandSender
     * @param sender The CommandSender who is entering playground mode
     */
    public void enterPlayground(CommandSender sender) {
        Context.Builder builder = PluginConfig.getContextBuilder();

        builder.hostClassLoader(LibraryManager.get().getClassLoader());

        builder.out(new ChatPrintStream(System.out, sender));
        builder.err(new ChatPrintStream(System.err, sender));

        builder.option("python.PythonPath", "./plugins/PySpigot/python-libs/");
        //We can just print all exceptions to the PrintStream (and thus to the player's chat) instead of handling and logging them
        builder.option("python.AlwaysRunExcepthook", "true");

        builder.engine(engine);

        Context context = builder.build();

        activePlaygrounds.put(sender.getName(), context);

        sender.sendMessage(PluginConfig.getMessage("enter-playground-mode", true));
    }

    /**
     * Close and exit playground mode (including the underlying context) for the provided CommandSender.
     * @param sender The CommandSender who is exiting playground mode
     */
    public void exitPlayground(CommandSender sender) {
        Context context = activePlaygrounds.remove(sender.getName());

        context.close(true);

        sender.sendMessage(PluginConfig.getMessage("exit-playground-mode", true));
    }

    /**
     * Evaluate a string of text as python code for the provided CommandSender's playground Context.
     * @param sender The sender who typed the text
     * @param text The text to be evaluated as python code
     */
    public void evaluate(CommandSender sender, String text) {
        try {
            Source source = Source.newBuilder("python", text, "<" + sender.getName() + "-playground>").build();

            Context context = activePlaygrounds.get(sender.getName());
            context.enter();
            context.eval(source);
            context.leave();
        } catch (IOException exception) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Error when evaluating text in playground mode for '" + sender.getName() + "'", exception);
            sender.sendMessage(ChatColor.RED + "There was an error when evaluating your typed text. See console for details.");
        } catch (PolyglotException exception) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Error when evaluating text in playground mode for '" + sender.getName() + "'", exception);
        }

    }

    /**
     * Get the singleton instance of this PlaygroundManager.
     * @return The instance
     */
    public static PlaygroundManager get() {
        if (instance == null)
            instance = new PlaygroundManager();
        return instance;
    }
}
