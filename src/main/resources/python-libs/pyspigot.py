###
# NOTE: This module is not meant to be edited. Any changes made will be overridden on plugin reload or server restart.
###

from dev.magicmq.pyspigot import PySpigot
from dev.magicmq.pyspigot.manager.script import ScriptManager
from dev.magicmq.pyspigot.manager.script import GlobalVariables
from dev.magicmq.pyspigot.manager.listener import ListenerManager
from dev.magicmq.pyspigot.manager.command import CommandManager
from dev.magicmq.pyspigot.manager.task import TaskManager
from dev.magicmq.pyspigot.manager.config import ConfigManager

def script_manager():
    """Get the script manager for loading, unloading, and reloading scripts."""
    return ScriptManager.get()

def global_variables():
    """Get the global variables manager for setting and getting global variables."""
    return GlobalVariables.get()

def listener_manager():
    """Get the listener manager for registering and unregistering event listeners."""
    return ListenerManager.get()

def command_manager():
    """Get the command manager for registering and unregistering commands."""
    return CommandManager.get()

def task_manager():
    """Get the task manager for scheduling and unscheduling tasks (synchronous and asynchronous)."""
    return TaskManager.get()

def config_manager():
    """Get the config manager for writing to and reading from config files."""
    return ConfigManager.get()

def protocol_manager():
    """Get the protocol manager for working with ProtocolLib (registering/unregistering packet listeners, sending packets, etc.). Note: this function will return None if ProtocolLib is not available on the server."""
    if PySpigot.get().isProtocolLibAvailable():
        from dev.magicmq.pyspigot.manager.protocol import ProtocolManager
        return ProtocolManager.get()
    else: return None

def placeholder_manager():
    """get the placeholder manager for registering/unregistering PlaceholderAPI placeholders. Note: this function will return None if PlaceholderAPI is not available on the server."""
    if PySpigot.get().isPlaceholderApiAvailable():
        from dev.magicmq.pyspigot.manager.placeholder import PlaceholderManager
        return PlaceholderManager.get()
    else: return None

# Convenience variables for ease of access

script = script_manager()
scripts = script_manager()
sm = script_manager()

global_vars = global_variables()
gv = global_variables()

listener = listener_manager()
listeners = listener_manager()
lm = listener_manager()
event = listener_manager()
events = listener_manager()
em = listener_manager()

command = command_manager()
commands = command_manager()
cm = command_manager()

scheduler = task_manager()
scm = task_manager()
tasks = task_manager()
tm = task_manager()

config = config_manager()
configs = config_manager()
com = config_manager()

protocol = protocol_manager()
protocol_lib = protocol_manager()
protocols = protocol_manager()
pm = protocol_manager()

placeholder = placeholder_manager()
placeholder_api = placeholder_manager()
placeholders = placeholder_manager()
plm = placeholder_manager()