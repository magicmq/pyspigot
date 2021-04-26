#Bukkit Imports
from org.bukkit.event.player import AsyncPlayerChatEvent

config = configs.loadConfig('swearfilter.yml')

swear_words = config.getStringList('swear-words')

def player_chat(event):
    message = event.getMessage().lower()
    for word in message.split():
        if word in swear_words:
            event.setCancelled(True)

listeners.registerEvent(player_chat, AsyncPlayerChatEvent)