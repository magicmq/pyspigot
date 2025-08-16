"""
Contains decorators for registering event listeners.
"""

import pyspigot as ps

from net.md_5.bungee.event import EventPriority

def event_listener(event_class, priority=EventPriority.NORMAL):
    """
    Register an event listener by decorating a function. The decorated function will be called when the event occurs.

    Note: ignore_cancelled is an option in the Bukkit version of PySpigot, but BungeeCord does not support
    ignore_cancelled for event listeners.

    :param event_class: The event class to listen to
    :param priority: The priority of the event
    """

    def _decorator(function):
        listener_manager = ps.listener_manager()
        listener = listener_manager.registerListener(function, event_class, priority)
        function.registered_listener = listener

        def _unregister():
            listener_manager.unregisterListener(function)

        function.unregister = _unregister

        return function

    return _decorator


__all__ = ['event_listener']
