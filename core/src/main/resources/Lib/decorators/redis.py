"""
Contains decorators for registering redis pub/sub listeners.
"""

import pyspigot as ps

def sync_pub_sub_listener(client, channel):
    """
    Register a pub sub listener by decorating a function. The function will be called when a message is received on the
    specified channel.

    An alias for this decorator is @pub_sub_listener.

    :param client: The pub/sub redis client that was previously registered and opened
    :param channel: The channel to listen on
    """

    def _decorator(function):
        listener = client.registerSyncListener(function, channel)

        function.registered_listener = listener

        def _unregister():
            client.unregisterListener(listener)

        function.unregister = _unregister

        return function
    return _decorator


def async_pub_sub_listener(client, channel):
    """
    Register an asynchronous pub sub listener by decorating a function. The function will be called when a message is
    received on the specified channel.

    :param client: The pub/sub redis client that was previously registered and opened
    :param channel: The channel to listen on
    """

    def _decorator(function):
        listener = client.registerAsyncListener(function, channel)

        function.registered_listener = listener

        def _unregister():
            client.unregisterListener(listener)

        function.unregister = _unregister

        return function
    return _decorator


pub_sub_listener = sync_pub_sub_listener

__all__ = [
    'sync_pub_sub_listener',
    'async_pub_sub_listener',
    'pub_sub_listener',
]
