import pyspigot as ps

from org.bukkit import Bukkit
from org.bukkit import Location
from org.bukkit.inventory import ItemStack
from org.bukkit import Material
from org.bukkit import ChatColor
from org.bukkit.enchantments import Enchantment
from org.bukkit.inventory import ItemFlag
from org.bukkit.event.inventory import InventoryClickEvent
from org.bukkit.entity import Player


def load_location(config):
    world = Bukkit.getWorld(config.getString('world'))
    x = config.getDouble('x')
    y = config.getDouble('y')
    z = config.getDouble('z')
    yaw = config.getDouble('yaw', 0.0)
    pitch = config.getDouble('pitch', 0.0)

    return Location(world, x, y, z, yaw, pitch)


def load_item(config):
    material = Material.matchMaterial(config.getString('material'))

    if config.contains('amount'):
        item = ItemStack(material, config.getInt('amount'))
    else:
        item = ItemStack(material)

    item_meta = item.getItemMeta()

    if config.contains('name'):
        item_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString('name')))

    if config.contains('lore'):
        lore_translated = [ChatColor.translateAlternateColorCodes('&', line) for line in config.getStringList('lore')]
        item_meta.setLore(lore_translated)

    if config.contains('glowing'):
        if config.getBoolean('glowing'):
            item_meta.addEnchant(Enchantment.SHARPNESS, 1, True)
            item_meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

    item.setItemMeta(item_meta)

    return item


def construct_items(config):
    items = {}

    for key in config.getKeys(False):
        slot = int(key)
        config_section = config.getConfigurationSection(key)

        location = load_location(config_section.getConfigurationSection('location'))
        location_name = config_section.getString('location-name')
        item = load_item(config_section.getConfigurationSection('item'))

        items[item] = (slot, location, location_name)

    return items


def create_inventory(number_of_slots, title, items):
    inventory = Bukkit.createInventory(None, number_of_slots, title)

    for item, data in items.items():
        inventory.setItem(data[0], item)

    return inventory


config = ps.config.loadConfig('teleport.yml')

misc_section = config.getConfigurationSection('misc')
teleport_message = ChatColor.translateAlternateColorCodes('&', misc_section.getString('teleport-message'))

items_section = config.getConfigurationSection('items')
items = construct_items(items_section)

inventory_section = config.getConfigurationSection('inventory')
inventory_size = inventory_section.getInt('size')
inventory_title = ChatColor.translateAlternateColorCodes('&', inventory_section.getString('title'))

inventory = create_inventory(inventory_size, inventory_title, items)


def on_inventory_click(event):
    if event.getClickedInventory() is not None:
        if event.getClickedInventory().getTitle() == inventory.getTitle():
            event.setCancelled(True)

            item = event.getCurrentItem()
            if item != None and item.getType() != Material.AIR:
                player = event.getWhoClicked()

                data = items[item]

                if data != None:
                    location = data[1]

                    player.closeInventory()
                    player.teleport(location)
                    player.sendMessage(teleport_message.replace('%name%', data[2]))
ps.listener_manager().registerListener(on_inventory_click, InventoryClickEvent)


def on_command(sender, label, args):
    if isinstance(sender, Player):
        if sender.hasPermission('teleportgui.command'):
            sender.openInventory(inventory)
        else:
            sender.sendMessage(ChatColor.RED + 'You do not have permission to execute this command!')
    else:
        sender.sendMessage(ChatColor.RED + 'This command can only be executed by players.')

    return True
ps.command_manager().registerCommand(on_command, 'teleportgui', 'Open the teleport GUI', '/teleportgui', ['tgui', 'teleport_gui'])


def stop():
    for player in Bukkit.getOnlinePlayers():
        if player.getOpenInventory() is not None:
            if player.getOpenInventory().getTitle() == inventory.getTitle():
                player.closeInventory()
