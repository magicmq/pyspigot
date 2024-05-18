from dev.magicmq.pyspigot import PySpigot as ps
from org.bukkit.entity import Player
from org.bukkit import ChatColor
from org.bukkit import Material
from org.bukkit import Bukkit

save_interval = 1800
inventory_name = '&6&lCommunity Chest'

inventory_config = ps.config.loadConfig('communitychest.yml')

save_task_id = 0
inventory = None

def community_chest_command(sender, label, args):
    if isinstance(sender, Player):
        if sender.hasPermission('script.communitychest'):
            sender.openInventory(inventory)
        else:
            sender.sendMessage(ChatColor.RED + 'Insufficient permissions!')
    else:
        sender.sendMessage(ChatColor.RED + 'This command can only be executed by a player!')
    return True

ps.command.registerCommand(community_chest_command, 'communitychest', 'Open the community chest', '/communitychest',
                           ['freechest', 'cc'])

def load_inventory():
    items = {}
    for key in inventory_config.getKeys(False):
        slot = int(key)
        item = inventory_config.getItemStack(key)
        items[slot] = item

    global inventory
    inventory = Bukkit.createInventory(None, 54, ChatColor.translateAlternateColorCodes('&', inventory_name))

    for slot in items:
        inventory.setItem(slot, items[slot])

def save_inventory():
    for key in inventory_config.getKeys(False):
        inventory_config.set(key, None)

    items = inventory.getContents()
    for index, item in enumerate(items):
        if item is not None and item.getType() is not Material.AIR:
            inventory_config.set(str(index), item)

    inventory_config.save()

load_inventory()
save_task_id = ps.scheduler.scheduleAsyncRepeatingTask(save_inventory, save_interval * 20, save_interval * 20)

# Called automatically when the script is stopped (on server shutdown, script unload, etc.)
def stop():
    ps.scheduler.stopTask(save_task_id)
    save_inventory()