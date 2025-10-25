"""
Contains decorators for registering event listeners.
"""

import pyspigot as ps

def event_listener(event_class, priority=0):
    """
    Register an event listener by decorating a function. The decorated function will be called when the event occurs.

    :param event_class: The event class to listen to
    :param priority: The priority of the event
    """

    def _decorator(function):
        listener_manager = ps.listener_manager()
        listener = listener_manager.registerListener(function, event_class, priority)
        function.registered_listener = listener

        def _unregister():
            listener_manager.unregisterListener(listener)

        function.unregister = _unregister

        return function

    return _decorator


def async_event_listener(event_class, event_task_type, priority=0):
    """
    Register an asynchronous event listener by decorating a function. The decorated function will be called when the
    event occurs.

    :param event_class: The event class to listen to
    :param event_task_type: The task type of the event. Should be EventTaskType.ASYNC, EventTskType.CONTINUATION, or
    EventTaskType.RESUME_WHEN_COMPLETE. For more information, see https://docs.papermc.io/velocity/dev/event-api/
    :param priority: The priority of the event
    """

    def _decorator(function):
        listener_manager = ps.listener_manager()
        listener = listener_manager.registerListener(function, event_class, priority, event_task_type)
        function.registered_listener = listener

        def _unregister():
            listener_manager.unregisterListener(listener)

        function.unregister = _unregister

        return function

    return _decorator


__all__ = [
    'event_listener',
    'async_event_listener',
]
