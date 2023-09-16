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

package dev.magicmq.pyspigot.command.subcommands;

import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SubCommandMeta(
        command = "help",
        permission = "pyspigot.command.help",
        description = "Useful information for PySpigot and how to get help",
        aliases = {"gethelp", "info", "getinfo"}
)
public class HelpCommand implements SubCommand {

    private static final String HELP_MESSAGE_CONSOLE =
            "\n" +
                    PluginConfig.getPrefix() + "&aSome helpful links:" + "\n" +
                    "&b&l- PySpigot Documentation: &b&nhttps://pyspigot-docs.magicmq.dev" + "\n" +
                    "&b&l- Spigot Plugin Page: &b&nhttps://spigotmc.org/resources/pyspigot.111006/" + "\n" +
                    "&b- Need help? Reach out on the PySpigot Discord: &b&nhttps://discord.gg/f2u7nzRwuk" + "\n" +
                    "&b- Found a bug? Please let me know on Discord (link above) and/or submit a bug report on the PySpigot Github repo: &b&nhttps://github.com/magicmq/pyspigot/issues";

    private static final BaseComponent[] HELP_MESSAGE;

    static {
        TextComponent documentation = new TextComponent("PySpigot Documentation");
        documentation.setColor(ChatColor.AQUA);
        documentation.setUnderlined(true);
        documentation.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://pyspigot-docs.magicmq.dev"));
        documentation.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GOLD + "Click to go to the documentation for PySpigot")));

        TextComponent pluginPage = new TextComponent("Spigot Plugin Page");
        pluginPage.setColor(ChatColor.AQUA);
        pluginPage.setUnderlined(true);
        pluginPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://spigotmc.org/resources/pyspigot.111006/"));
        pluginPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GOLD + "Click to go to the PySpigot plugin page")));

        TextComponent discord = new TextComponent("PySpigot Discord");
        discord.setColor(ChatColor.AQUA);
        discord.setUnderlined(true);
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/f2u7nzRwuk"));
        discord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GOLD + "Click to join the PySpigot Discord server")));

        TextComponent github = new TextComponent("PySpigot GitHub Repo");
        github.setColor(ChatColor.AQUA);
        github.setUnderlined(true);
        github.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/magicmq/pyspigot/issues"));
        github.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GOLD + "Click to go to the PySpigot GitHub Repo")));

        ComponentBuilder builder = new ComponentBuilder();
        builder
                .append("\n")
                .appendLegacy(PluginConfig.getPrefix() + ChatColor.GREEN + "Some helpful links:").append("\n").reset()
                .append("- ").color(ChatColor.AQUA).append(documentation).append("\n").reset()
                .append("- ").color(ChatColor.AQUA).append(pluginPage).append("\n").reset()
                .append("- Need help? Reach out on the ").color(ChatColor.AQUA).append(discord).append("\n").reset()
                .append("- Found a bug? Please let me know on Discord (link above) and/or submit a bug report on the ").color(ChatColor.AQUA).append(github);
        HELP_MESSAGE = builder.create();
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player)
            sender.spigot().sendMessage(HELP_MESSAGE);
        else
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', HELP_MESSAGE_CONSOLE));
        return true;
    }
}
