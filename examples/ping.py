import pyspigot as ps
from org.bukkit.event.player import AsyncPlayerChatEvent
from org.bukkit.entity import Player
from org.bukkit import NamespacedKey
from org.bukkit import ChatColor
from org.bukkit.persistence import PersistentDataType
from org.bukkit import Bukkit
from org.bukkit import Sound

ping_toggle_key = NamespacedKey('script_ping', 'toggled')

# Configurable variables
toggle_on_message = '&aChat pings toggled on.'
toggle_off_message = '&cChat pings toggled off.'
notification_sound = Sound.BLOCK_NOTE_BLOCK_BELL
notification_volume = 0.5
notification_pitch = 1.122

# Listen for chat event
def on_chat(event):
    player = event.getPlayer()
    message = event.getMessage()
    for to_notify in Bukkit.getOnlinePlayers():
        if to_notify.hasPermission('ping.nonotify'):
            return

        data_container = to_notify.getPersistentDataContainer()
        if data_container.has(ping_toggle_key, PersistentDataType.STRING):
            value = data_container.get(ping_toggle_key, PersistentDataType.STRING)
            if value == 'off': return

        for word in message.split(' '):
            if levenshtein_distance(to_notify.getName(), word) <= 3 and to_notify is not player:
                to_notify.playSound(to_notify.getLocation(), notification_sound, notification_volume,
                                    notification_pitch)

# Command to toggle chat pings
def toggle_ping_command(sender, label, args):
    if sender.hasPermission('ping.toggle'):
        if isinstance(sender, Player):
            toggle_ping(sender)
        else:
            sender.sendMessage(ChatColor.RED + 'This command can only be executed by a player!')
    else:
        sender.sendMessage(ChatColor.RED + 'Insufficient permissions!')
    return True

def toggle_ping(player):
    data_container = player.getPersistentDataContainer()
    if data_container.has(ping_toggle_key, PersistentDataType.STRING):
        value = data_container.get(ping_toggle_key, PersistentDataType.STRING)
        if value == 'on':
            data_container.set(ping_toggle_key, PersistentDataType.STRING, 'off')
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', toggle_off_message))
        elif value == 'off':
            data_container.set(ping_toggle_key, PersistentDataType.STRING, 'on')
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', toggle_on_message))
    else:
        data_container.set(ping_toggle_key, PersistentDataType.STRING, 'off')
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', toggle_off_message))

# See https://en.wikipedia.org/wiki/Levenshtein_distance for an in-depth explanation of Levenshtein distance
def levenshtein_distance(str1, str2):
    matrix = [[0] * (len(str2) + 1) for _ in range(len(str1) + 1)]

    for i in range(len(str1) + 1):
        matrix[i][0] = i
    for j in range(len(str2) + 1):
        matrix[0][j] = j

    for i in range(1, len(str1) + 1):
        for j in range(1, len(str2) + 1):
            cost = 0 if str1[i - 1] == str2[j - 1] else 1
            matrix[i][j] = min(
                matrix[i - 1][j] + 1,
                matrix[i][j - 1] + 1,
                matrix[i - 1][j - 1] + cost
            )

    return matrix[len(str1)][len(str2)]

ps.listener.registerListener(on_chat, AsyncPlayerChatEvent)
ps.command.registerCommand(toggle_ping_command, 'toggleping', 'Toggle chat pings on/off.', '/toggleping',
                           ['tp', 'togglenotify', 'tn', 'togglechatping', 'togglechatnotify'])