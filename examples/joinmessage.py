from dev.magicmq.pyspigot import PySpigot as ps
from org.bukkit.event.player import PlayerJoinEvent
from org.bukkit import Bukkit
from org.bukkit import ChatColor

message_delay = 20
join_message = 'aYou joined the server!'
join_notify_message = '&c%player% &ahas joined the server!'

def join_event(event):
    player = event.getPlayer()
    if player.hasPermission('joinmessage.permission'):
        notify_admins(player)
        if message_delay > 0:
            ps.scheduler.runTaskLater(lambda: player.sendMessage(ChatColor.translateAlternateColorCodes('&', join_message)), message_delay)
        else:
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', join_message))

def notify_admins(joined):
    for player in Bukkit.getOnlinePlayers():
        if player.hasPermission('joinmessage.admin'):
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', join_notify_message.replace('%player%', joined.getName())))

ps.listener.registerListener(join_event, PlayerJoinEvent)