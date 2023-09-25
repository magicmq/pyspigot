from dev.magicmq.pyspigot import PySpigot as ps
from org.bukkit.entity import Player
from org.bukkit import ChatColor

player_only_message = '&cThis command can only be executed by a player!'
no_permission_message = '&cInsufficient permissions!'
clear_inventory_message = '&aYour inventory has been cleared.'

def clear_inventory_command(sender, label, args):
    if isinstance(sender, Player):
        if sender.hasPermission('script.clearinventory'):
            sender.getInventory().clear()
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', clear_inventory_message))
        else:
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', no_permission_message))
    else:
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', player_only_message))
    return True

ps.command.registerCommand(clear_inventory_command, 'clear_inventory', 'Clear your inventory', '/clear_inventory', {'cinventory', 'cleari', 'purgeinventory', 'purge_inventory', 'pi', 'purgei', 'pinventory'})