'''

     Copyright 2023 magicmq

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

'''

# NOTE: This module is not meant to be edited. Any changes made will be overridden on plugin reload or server restart.

from dev.magicmq.pyspigot import PySpigot
from dev.magicmq.pyspigot.manager.script import ScriptManager
from dev.magicmq.pyspigot.manager.script import GlobalVariables
from dev.magicmq.pyspigot.manager.listener import ListenerManager
from dev.magicmq.pyspigot.manager.command import CommandManager
from dev.magicmq.pyspigot.manager.task import TaskManager
from dev.magicmq.pyspigot.manager.config import ConfigManager
from dev.magicmq.pyspigot.manager.protocol import ProtocolManager
from dev.magicmq.pyspigot.manager.placeholder import PlaceholderManager

def script_manager():
    """Get the script manager for loading, unloading, and reloading scripts."""
    return ScriptManager.get()

def global_vars():
    """Get the global variables manager for setting and getting global variables."""
    return GlobalVariables.get()

def listener_manager():
    """Get the listener manager for registering and unregistering event listeners."""
    return ListenerManager.get()

def event_manager():
    """Get the listener manager for registering and unregistering event listeners."""
    return ListenerManager.get()

def command_manager():
    """Get the command manager for registering and unregistering commands."""
    return CommandManager.get()

def task_manager():
    """Get the task manager for scheduling and unscheduling tasks (synchronous and asynchronous)."""
    return TaskManager.get()

def scheduler_manager():
    """Get the task manager for scheduling and unscheduling tasks (synchronous and asynchronous)."""
    return TaskManager.get()

def config_manager():
    """Get the config manager for writing to and reading from config files."""
    return ConfigManager.get()

def protocol_manager():
    """Get the protocol manager for working with ProtocolLib (registering/unregistering packet listeners, sending packets, etc.). Note: this function will return None if ProtocolLib is not available on the server."""
    if PySpigot.get().isProtocolLibavailable():
        return ProtocolManager.get()
    else: return None

def placeholder_manager():
    """get the placeholder manager for registering/unregistering PlaceholderAPI placeholders. Note: this function will return None if PlaceholderAPI is not available on the server."""
    if PySpigot.get().isPlaceholderApiAvailable():
        return PlaceholderManager.get()
    else: return None