#Imports
from dev.magicmq.pyspigot import PySpigot as ps
from org.bukkit import Bukkit
from org.bukkit import ChatColor

message = '&aThis is a test message!'

def task():
    for player in Bukkit.getOnlinePlayers():
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message))

task_id = ps.scheduler.scheduleRepeatingTask(task, 0, 100)