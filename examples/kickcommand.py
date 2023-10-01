from dev.magicmq.pyspigot import PySpigot as ps
from org.bukkit import Bukkit
from org.bukkit import ChatColor

kick_message = '&cYou have been kicked by %player%'

def kick_command(sender, label, args):
    if len(args) > 0:
        to_kick = Bukkit.getPlayer(args[0])
        if to_kick is not None:
            to_kick.kickPlayer(ChatColor.translateAlternateColorCodes('&', kick_message.replace('%player%', sender.getName())))
        else:
            sender.sendMessage('Player ' + args[0] + ' not found')
    else:
        sender.sendMessage('Usage: /kickplayer <player>')
    return True

def kick_command_tab(sender, alias, args):
    if len(args) > 0:
        return [player.getName() for player in Bukkit.getOnlinePlayers()]

ps.command.registerCommand(kick_command, kick_command_tab, 'kickplayer')