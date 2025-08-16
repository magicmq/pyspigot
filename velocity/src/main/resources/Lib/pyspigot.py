"""
A helper module for more easy access to PySpigot's managers.
"""
from dev.magicmq.pyspigot.velocity import PyVelocity
from dev.magicmq.pyspigot.manager.script import ScriptManager
from dev.magicmq.pyspigot.manager.script import GlobalVariables
from dev.magicmq.pyspigot.manager.listener import ListenerManager
from dev.magicmq.pyspigot.manager.command import CommandManager
from dev.magicmq.pyspigot.manager.task import TaskManager
from dev.magicmq.pyspigot.manager.config import ConfigManager
from dev.magicmq.pyspigot.manager.database import DatabaseManager
from dev.magicmq.pyspigot.manager.redis import RedisManager

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

def database_manager():
    """Get the database manager for connecting to and interacting with databases."""
    return DatabaseManager.get()

def redis_manager():
    """Get the redis manager for connecting to and interacting with redis servers."""
    return RedisManager.get()

def packet_events_manager():
    """Get the packet events manager for registering packet event listeners. Note: this function will return None if PacketEvents is not available on the server."""
    if PyVelocity.get().isPacketEventsAvailable():
        from dev.magicmq.pyspigot.manager.packetevents import PacketEventsManager
        return PacketEventsManager.get()
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

database = database_manager()

redis = redis_manager()

packet_events = packet_events_manager()
pe = packet_events_manager()