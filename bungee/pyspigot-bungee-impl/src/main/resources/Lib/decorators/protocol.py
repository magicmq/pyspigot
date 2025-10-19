"""
Contains decorators for registering Protocolize packet listeners.
"""

import pyspigot as ps

from dev.magicmq.pyspigot.bungee import PyBungee
import dev.magicmq.pyspigot.exception.ScriptRuntimeException

if not PyBungee.get().isProtocolizeAvailable():
    raise dev.magicmq.pyspigot.exception.ScriptRuntimeException('Attempted to initialize Protocolize decorators, but Protocolize was not found on the server')

def packet_listener_receive(packet_class, direction, priority=0):
    """
    Register a new packet listener by decorating the receive function for the listener. The send function can subsequently
    be decorated with @<function>.send, where <function> is the name of the receive function.

    Usage:

        from decorators.protocol import protocol
        from dev.simplix.protocolize.api import Direction
        from net.md_5.bungee.protocol.packet import Chat

        @packet_listener_receive(Chat, Direction.UPSTREAM, priority=10)
        def on_receive(player, packet): ...

        @on_receive.send
        def on_send(player, packet): ...

    :param packet_class: The class of the packet to listen for
    :param direction: The direction of the packet listener, either Direction.UPSTREAM or Direction.DOWNSTREAM
    :param priority: The priority of the packet listener
    """

    priority = int(priority or 0)
    def _deco(recv_fn):
        pm = ps.protocol_manager()
        state = {'handle': None}

        def _unregister():
            h = state['handle']
            if h is not None:
                pm.unregisterPacketListener(h)
                state['handle'] = None

        def send(send_fn):
            if state['handle'] is not None:
                _unregister()
            h = pm.registerPacketListener(recv_fn, send_fn, packet_class, direction, priority)
            state['handle'] = h
            # expose helpers on both functions
            recv_fn.unregister = _unregister
            send_fn.unregister = _unregister
            recv_fn.listener = h
            send_fn.listener = h
            return send_fn

        # attach complementary decorator to the function (legal: @recv_fn.send)
        recv_fn.send = send
        return recv_fn
    return _deco


def packet_listener_send(packet_class, direction, priority=0):
    """
    Register a new packet listener by decorating the send function for the listener. The receive function can subsequently
    be decorated with @<function>.send, where <function> is the name of the send function.

        Usage:

        from decorators.protocol import protocol
        from dev.simplix.protocolize.api import Direction
        from net.md_5.bungee.protocol.packet import Chat

        @packet_listener_send(Chat, Direction.UPSTREAM, priority=10)
        def on_send(player, packet): ...

        @on_send.receive
        def on_receive(player, packet): ...

    :param packet_class: The class of the packet to listen for
    :param direction: The direction of the packet listener, either Direction.UPSTREAM or Direction.DOWNSTREAM
    :param priority: The priority of the packet listener
    """

    priority = int(priority or 0)
    def _deco(send_fn):
        pm = ps.protocol_manager()
        state = {'handle': None}

        def _unregister():
            h = state['handle']
            if h is not None:
                pm.unregisterPacketListener(h)
                state['handle'] = None

        def receive(recv_fn):
            if state['handle'] is not None:
                _unregister()
            h = pm.registerPacketListener(recv_fn, send_fn, packet_class, direction, priority)
            state['handle'] = h
            send_fn.unregister = _unregister
            recv_fn.unregister = _unregister
            send_fn.listener = h
            recv_fn.listener = h
            return recv_fn

        # attach complementary decorator to the function (legal: @send_fn.receive)
        send_fn.receive = receive
        return send_fn
    return _deco


__all__ = [
    'packet_listener_receive',
    'packet_listener_send'
]
