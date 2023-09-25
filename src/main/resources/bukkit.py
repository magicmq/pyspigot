#     Copyright 2023 magicmq
#
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.

# Wrapper functions for org.bukkit.Bukkit
from org.bukkit import Bukkit

def ban_ip(address=None, inet_address=None):
    """Takes either an IP address (str) or InetAddress (Java object) and bans the address from the server."""
    if address:
        Bukkit.banIP(address)
    elif inet_address:
        Bukkit.banIP(inet_address)

def broadcast(message, permission=None):
    """Broadcast a message. If permission is none, then all players will see the message. If permission is specified, then only players with the permission will see the message"""
    if permission:
        return Bukkit.broadcast(message, permission)
    else:
        Bukkit.broadcastMessage(message)

def craft_item(crafting_matrix, world, player):
    """Craft an item. crafting_matrix is a 3x3 list of ItemStack (Java object) representing the recipe in the crafting crid, world is the world the crafting takes place in, and player is the player to imitate the crafting event on"""
    return Bukkit.craftitem(crafting_matrix, world, player)

def create_block_data(data=None, material=None):
    """Creates a new BlockData instance (Spigot object) with either data (str), material (Spigot object), or both."""
    if data and not material:
        return Bukkit.createBlockData(data)
    elif material and not data:
        return Bukkit.createBlockData(material)
    elif data and material:
        return Bukkit.createBlockData(material, data)

def create_boss_bar(title, color, style, *flags):
    return Bukkit.createBossBar(title, color, style, flags)

def create_keyed_boss_bar(key, title, color, style, *flags):
    return Bukkit.createBossBar(key, title, color, style, *flags)

def create_inventory(owner, size=27, type=None, title=None):
    if type:
        if title:
            return Bukkit.createInventory(owner, type, title)
        else:
            return Bukkit.createInventory(owner, type)
    elif size:
        if title:
            return Bukkit.createInventory(owner, size, title)
        else:
            return Bukkit.createInventory(owner, size)

def create_player_profile(name=None, uuid=None):
    if name and not uuid:
        return Bukkit.createPlayerProfile(name)
    elif uuid and not name:
        return Bukkit.createPlayerProfile(uuid)
    elif name and uuid:
        return Bukkit.createPlayerProfile(name, uuid)

def dispatch_command(sender, command):
    return Bukkit.dispatchCommand(sender, command)

def get_allow_end():
    return Bukkit.getAllowEnd()

def get_allow_flight():
    return Bukkit.getAllowFlight()

def get_allow_nether():
    return Bukkit.getAllowNether()

def get_bukkit_version():
    return Bukkit.getBukkitVersion()

def get_console_sender():
    return Bukkit.getConsoleSender()

def get_offline_player(name=None, uuid=None):
    if name:
        return Bukkit.getOfflinePlayer(name)
    elif uuid:
        return Bukkit.getOfflinePlayer(uuid)

def get_player(name=None, uuid=None, exact=False):
    if name:
        if exact:
            return Bukkit.getPlayerExact(name)
        else:
            return Bukkit.getPlayer(name)
    elif uuid:
        return Bukkit.getPlayer(uuid)

def get_server():
    return Bukkit.getServer()

def get_world(name):
    return Bukkit.getWorld(name)

def get_worlds():
    return Bukkit.getWorlds()

def match_player(name):
    return Bukkit.matchPlayer(name)

def unban_ip(address=None, inet_address=None):
    if address:
        Bukkit.unbanIP(address)
    elif inet_address:
        Bukkit.unbanIP(inet_address)