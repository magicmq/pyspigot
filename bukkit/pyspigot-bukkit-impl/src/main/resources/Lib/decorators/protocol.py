"""
Contains decorators for registering ProtocolLib packet listeners.
"""

import pyspigot as ps

from dev.magicmq.pyspigot.bukkit import PySpigot
import dev.magicmq.pyspigot.exception.ScriptRuntimeException

if not PySpigot.get().isProtocolLibAvailable():
    raise dev.magicmq.pyspigot.exception.ScriptRuntimeException('Attempted to initialize ProtocolLib decorators, but ProtocolLib was not found on the server')

from com.comphenix.protocol.events import ListenerPriority

def packet_listener(packet_type, priority=ListenerPriority.NORMAL):
    """
    Register a packet listener by decorating a function. The decorated function will be called when the packet is sent/received.

    :param packet_type: The packet type to listen for
    :param priority: The priority of the listener
    """

    def _decorator(function):
        protocol_manager = ps.protocol_manager()
        registered_listener = protocol_manager.registerPacketListener(function, packet_type, priority)

        function.registered_listener = registered_listener

        def _unregister():
            protocol_manager.unregisterPacketListener(registered_listener)

        function.unregister = _unregister

        return function
    return _decorator


def async_packet_listener(packet_type, priority=ListenerPriority.NORMAL):
    """
    Register an asynchronous packet listener by decorating a function. The decorated function will be called when the packet is sent/received.

    :param packet_type: The packet type to listen for
    :param priority: The priority of the listener
    """

    def _decorator(function):
        async_protocol_manager = ps.protocol_manager().asyncManager()
        registered_listener = async_protocol_manager.registerAsyncPacketListener(function, packet_type, priority)

        function.registered_listener = registered_listener

        def _unregister():
            async_protocol_manager.unregisterAsyncPacketListener(registered_listener)

        function.unregister = _unregister

        return function
    return _decorator


def timeout_packet_listener(packet_type, priority=ListenerPriority.NORMAL):
    """
    Register a timeout packet listener by decorating a function. The decorated function will be called when the packet is sent/received.

    :param packet_type: The packet type to listen for
    :param priority: The priority of the listener
    """

    def _decorator(function):
        async_protocol_manager = ps.protocol_manager().asyncManager()
        registered_listener = async_protocol_manager.registerTimeoutPacketListener(function, packet_type, priority)

        function.registered_listener = registered_listener

        def _unregister():
            async_protocol_manager.unregisterAsyncPacketListener(registered_listener)

        function.unregister = _unregister

        return function
    return _decorator


__all__ = [
    'packet_listener',
    'async_packet_listener',
    'timeout_packet_listener'
]
