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

import dev.magicmq.pyspigot.command.PySpigotCommand;
import dev.magicmq.pyspigot.command.SubCommand;
import dev.magicmq.pyspigot.command.SubCommandMeta;
import dev.magicmq.pyspigot.util.player.CommandSenderAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@SubCommandMeta(
        command = "help",
        aliases = {"gethelp"},
        permission = "pyspigot.command.help",
        description = "Useful information for PySpigot and how to get help"
)
public class HelpCommand implements SubCommand {

    private static final Component HELP_MESSAGE_CONSOLE = Component.text()
            .append(Component.text().append(PySpigotCommand.PLUGIN_PREFIX).append(Component.text("Some helpful links:", NamedTextColor.GREEN)))
            .appendNewline()
            .append(Component.text().append(Component.text("- PySpigot Documentation: ", NamedTextColor.AQUA, TextDecoration.BOLD)).append(Component.text("https://pyspigot-docs.magicmq.dev/", NamedTextColor.AQUA, TextDecoration.UNDERLINED)))
            .appendNewline()
            .append(Component.text().append(Component.text("- Spigot Plugin Page: ", NamedTextColor.AQUA, TextDecoration.BOLD)).append(Component.text("https://spigotmc.org/resources/pyspigot.111006/", NamedTextColor.AQUA, TextDecoration.UNDERLINED)))
            .appendNewline()
            .append(Component.text().append(Component.text("- Need help? Reach out on the PySpigot Discord: ", NamedTextColor.AQUA)).append(Component.text("https://discord.gg/f2u7nzRwuk", NamedTextColor.AQUA, TextDecoration.UNDERLINED)))
            .appendNewline()
            .append(Component.text().append(Component.text("- Found a bug? Please let me know on Discord (link above) and/or submit a bug report on the PySpigot Github repo: ", NamedTextColor.AQUA)).append(Component.text("https://github.com/magicmq/pyspigot/issues", NamedTextColor.AQUA, TextDecoration.UNDERLINED)))
            .build();
    private static final Component HELP_MESSAGE;

    static {
        Component documentation = Component.text("PySpigot Documentation")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://pyspigot-docs.magicmq.dev/"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to go to the documentation for PySpigot", NamedTextColor.GOLD)));

        Component pluginPage = Component.text("Spigot Plugin Page")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://spigotmc.org/resources/pyspigot.111006/"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to go to the PySpigot plugin page", NamedTextColor.GOLD)));

        Component discord = Component.text("PySpigot Discord")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://discord.gg/f2u7nzRwuk"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to join the PySpigot Discord server", NamedTextColor.GOLD)));

        Component github = Component.text("PySpigot GitHub Repo")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://github.com/magicmq/pyspigot/issues"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to go to the PySpigot GitHub Repository", NamedTextColor.GOLD)));

        HELP_MESSAGE = Component.text()
                .append(Component.text().append(PySpigotCommand.PLUGIN_PREFIX).append(Component.text("Some helpful links:", NamedTextColor.GREEN)))
                .appendNewline()
                .append(Component.text().append(Component.text("- ", NamedTextColor.AQUA)).append(documentation))
                .appendNewline()
                .append(Component.text().append(Component.text("- ", NamedTextColor.AQUA)).append(pluginPage))
                .appendNewline()
                .append(Component.text().append(Component.text("- Need help? Reah out on the ", NamedTextColor.AQUA)).append(discord))
                .appendNewline()
                .append(Component.text().append(Component.text("- Found a bug? Please let me know on Discord (link above) and/or submit a bug report on the ", NamedTextColor.AQUA)).append(github))
                .build();
    }

    @Override
    public boolean onCommand(CommandSenderAdapter sender, String[] args) {
        if (sender.isPlayer())
            sender.sendMessage(HELP_MESSAGE);
        else
            sender.sendMessage(HELP_MESSAGE_CONSOLE);
        return true;
    }
}
