import pyspigot as ps
from org.bukkit.event.player import AsyncPlayerChatEvent
from org.bukkit.event import EventPriority
from org.bukkit import Bukkit
from org.bukkit import ChatColor
import urllib2
import re

admin_notify_message = '&cPlayer %player% sent a message that was censored! Message: &r%message%'
swear_word_pattern = ''

# Normally, any I/O operations (such as fetching web pages) should be done asynchronously, but it's okay to do it sync in this case,
# since script loading is done when the server first starts. Doing this at any other time would cause noticeable server lag.
def init_swear_words():
    swear_words = []
    swear_words_url = 'http://raw.githubusercontent.com/LDNOOBW/List-of-Dirty-Naughty-Obscene-and-Otherwise-Bad-Words/master/en'
    data = urllib2.urlopen(swear_words_url)
    for line in data:
        swear_words.append(line.replace('\n', ''))
    data.close()

    global swear_word_pattern
    swear_word_pattern = '|'.join(re.escape(word) for word in swear_words)

def on_chat(event):
    player = event.getPlayer()
    if player.hasPermission('swearfilter.bypass'):
        return

    chat_message = event.getMessage()
    censored_message = censor_string(chat_message)

    if censored_message != chat_message:
        event.setMessage(censored_message)
        notify_admins(player, chat_message)

def censor_string(input):
    def replace(match):
        return '*' * len(match.group())

    censored_string = re.sub(swear_word_pattern, replace, input, flags=re.IGNORECASE)
    return censored_string

def notify_admins(offender, message):
    for player in Bukkit.getOnlinePlayers():
        if player.hasPermission('swearfilter.admin'):
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', admin_notify_message
                                                                      .replace('%player%', offender.getName())
                                                                      .replace('%message%', message)))

init_swear_words()

ps.listener.registerListener(on_chat, AsyncPlayerChatEvent, EventPriority.HIGHEST)