"""
Contains decorators for registering plugin message listeners.
"""

import pyspigot as ps

def plugin_message_listener(channel):
    """
    Register a new plugin message listener to listen on the given channel by decorating a function. The decorated
    function will be called when a plugin message is received on the listening channel.

    :param channel: The channel to listen on
    """

    def _decorator(function):
        message_manager = ps.message_manager()
        listener = message_manager.registerListener(function, channel)
        function.registered_listener = listener

        def _unregister():
            message_manager.unregisterListener(listener)

        function.unregister = _unregister

        return function
    return _decorator


__all__ = ['plugin_message_listener']
