.. _eventlisteners:

Event Listeners
===============

With PySpigot's ListenerManager, you have the ability to register event listeners. Like Bukkit's listener system, when an event fires, its respective function in your script will be called, and you may work with the event in your script.

See `PySpigot's Managers`_ for instructions on how to import the listener manager into your script.

.. note:: This is not a comprehensive guide to events in Bukkit. For a more complete guide on events, see Spigot's tutorial on using the event API: https://www.spigotmc.org/wiki/using-the-event-api/. Note that much of this information will not be useful because it pertains to Java, but the background information is nevertheless helpful.

Listener Code Example
#####################

Let's look at the following code that defines and registers an event listener:

.. code-block:: python
    :linenos:

    from dev.magicmq.pyspigot import PySpigot as ps
    from org.bukkit.event.player import AsyncPlayerChatEvent

    def player_chat(event):
        print('Player sent a chat! Their message was: ' + event.getMessage())

    ps.listener.registerEvent(player_chat, AsyncPlayerChatEvent)

On line 1, we import PySpigot as ``ps`` to utilize the listener manager (``listener``).

On line 2, there is an appropriate import statement for Bukkit's ``AsyncPlayerChatEvent``. All events that you wish to listen to *must* be imported!

On line 4, we define a function called ``player_chat`` that takes an event as a parameter (an AsyncPlayerChatEvent in this case). This is the function that will be called when an AsyncPlayerChatEvent occurs. On line 5, we print a simple message to the console that contains the message that was sent in chat.

All event listeners must be registered with PySpigot's listener manager. Event listeners are registered using ``registerEvent(function, event)``. The ``registerEvent`` function takes two arguments:

* The first argument accepts the function that should be called when the event fires.
* The second argument accepts the event that should be listened for.

Therefore, on line 7, we call the listener manager to register our event, passing the function we defined on line 5, ``player_chat``, and the event we want to listen for, ``AsyncPlayerChatEvent``.

For complete documentation on available listeners and functions/methods available to use from each, see the `Spigot Javadocs <https://hub.spigotmc.org/javadocs/spigot/index.html?overview-summary.html>`__.

To summarize:

* All events that you wish to use should be imported using Python's import syntax.
* All event listeners should be defined as functions in your script that accept a single parameter, the event (the parameter name can be whatever you like).
* All event listeners must be registered with PySpigot's listener manager using ``listener.registerEvent(function, event)``.

Listener Manager Usage
######################

There are five functions available for you to use in your script in the listener manager if you would like greater control over events or need more advanced event handling:

* ``listener.registerListener(function, event)``: Explained above, takes the function to call when event fires as well as the event to listen to.
* ``listener.registerListener(function, event, priority)``: Same as above, except also allows you to define an event priority (how early/late your event listener should fire relative to other listeners for the same event). The priority is a string and
   * Event priorities are the same as the priorities found in Bukkit's `EventPriority class <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html>`__.
* ``listener.registerListener(function, event, ignoreCancelled)``: Allows you to "ignore" the event if it has been cancelled by another event listener. The listener in your script will not be called if it is cancelled elsewhere.
   * This will only work with events that are `cancellable <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/Cancellable.html>`__.
* ``listener.registerListener(function, event, priority, ignoreCancelled)``: Allows you to register an event that is ignored if cancelled *and* that has a priority (a combination of the previous two functions).
* ``listener.unregisterEvent(function)``: Allows you to unregister an event listener from your script. Takes the function you want to unregister as an argument.