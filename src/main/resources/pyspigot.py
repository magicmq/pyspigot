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

# Helper functions to fetch managers
from dev.magicmq.pyspigot import PySpigot
from dev.magicmq.pyspigot.manager.script import ScriptManager
from dev.magicmq.pyspigot.manager.script import GlobalVariables
from dev.magicmq.pyspigot.manager.listener import ListenerManager
from dev.magicmq.pyspigot.manager.command import CommandManager
from dev.magicmq.pyspigot.manager.scheduler import TaskManager
from dev.magicmq.pyspigot.manager.config import ConfigManager
from dev.magicmq.pyspigot.manager.protocol import ProtocolManager
from dev.magicmq.pyspigot.manager.placeholder import PlaceholderManager

def script_manager():
    return ScriptManager.get()

def global_vars():
    return GlobalVariables.get()

def listener_manager():
    return ListenerManager.get()

def command_manager():
    return CommandManager.get()

def task_manager():
    return TaskManager.get()

def scheduler_manager():
    return TaskManager.get()

def config_manager():
    return ConfigManager.get()

def protocol_manager():
    if PySpigot.get().isProtocolLibavailable():
        return ProtocolManager.get()
    else: return None

def placeholder_manager():
    if PySpigot.get().isPlaceholderApiAvailable():
        return PlaceholderManager.get()
    else: return None