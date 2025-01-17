import pyspigot as ps
from com.comphenix.protocol import PacketType
from org.bukkit import Bukkit
from org.bukkit import ChatColor

chat_notify_message = '&c%player% &atried to chat, but it was cancelled. Message: &r%message%'

def chat_packet_received(event):
    player = event.getPlayer()
    packet = event.getPacket()
    if not player.hasPermission('script.chat'):
        message = packet.getStrings().read(0)
        event.setCancelled(True)
        notify_admins(player, message)

def notify_admins(chatted, message):
    for player in Bukkit.getOnlinePlayers():
        if player.hasPermission('script.admin'):
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', chat_notify_message.replace('%player%', chatted.getName()).replace('%message%', message)))

ps.protocol.registerPacketListener(chat_packet_received, PacketType.Play.Client.CHAT)