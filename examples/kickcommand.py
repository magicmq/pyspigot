#Bukkit Imports
from org.bukkit import Bukkit
from org.bukkit import ChatColor

kick_message = '&cYou have been kicked by %player%'

def kick_command(sender, command, label, args):
    if len(args) > 0:
        to_kick = Bukkit.getPlayer(args[0])
        if to_kick is not None:
            to_kick.kickPlayer(ChatColor.translateAlternateColorCodes('&', kick_message.replace('%player%', sender.getName())))
        else:
            sender.sendMessage('Player ' + args[0] + ' not found')
    else:
        sender.sendMessage('Usage: /kickplayer <player>')
    return True

commands.registerCommand(kick_command, 'kickplayer')