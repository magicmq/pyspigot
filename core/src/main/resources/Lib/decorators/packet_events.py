"""
Contains decorators for registering PacketEvents packet listeners.
"""

import pyspigot as ps

from dev.magicmq.pyspigot import PyCore
import dev.magicmq.pyspigot.exception.ScriptRuntimeException

if not PyCore.get().isProtocolizeAvailable():
    raise dev.magicmq.pyspigot.exception.ScriptRuntimeException('Attempted to initialize PacketEvents decorators, but PacketEvents was not found on the server')

from com.github.retrooper.packetevents.event import PacketListenerPriority

def packet_listener(packet_type, priority=PacketListenerPriority.NORMAL):
    """
    Register a PacketEvents packet listener by decorating a function. The decorated function will be called when the
    packet is sent/received.

    :param packet_type: The packet type to listen for
    :param priority: The priority of the packet listener
    """

    def _decorator(function):
        packet_events_manager = ps.packet_events_manager()
        listener = packet_events_manager.registerPacketListener(function, packet_type, priority)
        function.registered_listener = listener

        def _unregister():
            packet_events_manager.unregisterPacketListener(listener)

        function.unregister = _unregister

        return function
    return _decorator


__all__ = ['packet_listener']