.. _protocollib:

ProtocolLib Manager
===================

PySpigot includes a manager that interfaces with ProtocolLib if you would like to work with packets in your script.

See `PySpigot's Managers`_ for instructions on how to import the protocol manager into your script.

Packet Listener Code Example
############################

Let's look at the following code that defines and registers a chat packet listener:

.. code-block:: python
    :linenos:

    from dev.magicmq.pyspigot import PySpigot as ps
    from com.comphenix.protocol import PacketType

    def chat_packet_event(event):
        packet = event.getPacket()
        message = packet.getStrings().read(0)
        print('Player sent a chat! Their message was: ' + event.getMessage())

    ps.protocol.registerPacketListener(chat_packet_event, PacketType.Play.Client.CHAT)
