#Bukkit Imports
from org.bukkit import Bukkit
from org.bukkit import ChatColor

message = '&aThis is a test message!'

def task():
    for player in Bukkit.getOnlinePlayers():
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message))

task_id = scheduler.scheduleRepeatingTask(task, 0, 100)