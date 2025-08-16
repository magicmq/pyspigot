"""
Contains decorators for registering event listeners.
"""

import pyspigot as ps

from org.bukkit.event import EventPriority

def event_listener(event_class, priority=EventPriority.NORMAL, ignore_cancelled=False):
    """
    Register an event listener by decorating a function. The decorated function will be called when the event occurs.

    :param event_class: The event class to listen to
    :param priority: The priority of the event
    :param ignore_cancelled: Whether to ignore calling the event listener if the event was cancelled previously
    :return:
    """

    def _decorator(function):
        listener_manager = ps.listener_manager()
        listener = listener_manager.registerListener(function, event_class, priority, ignore_cancelled)
        function.registered_listener = listener

        def _unregister():
            listener_manager.unregisterListener(listener)

        function.unregister = _unregister

        return function

    return _decorator


__all__ = ['event_listener']
