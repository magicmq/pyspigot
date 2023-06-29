.. _commands:

Defining Commands
=================

PySpigot allows you to define and register new commands from scripts. These commands function in the same way as any command would in-game.

See `PySpigot's Managers`_ for instructions on how to import the command manager into your script.

.. note:: This is not a comprehensive guide to commands in Bukkit. For a more complete guide to commands, see Spigot's tutorial on commands: https://www.spigotmc.org/wiki/create-a-simple-command/. Note that much of this information will not be useful because it pertains to Java, but the background information is nevertheless helpful.

Command Code Example
####################

Let's look at the following code that defines and registers a command:

.. code-block:: python
    :linenos:

    from dev.magicmq.pyspigot import PySpigot as ps

    def kick_command(sender, label, args):
        #Do something...
        return True

    def tab_kick_command(sender, alias, args):
        #Do something...
        return ['',]

    registered_command = ps.command.registerCommand(kick_command, 'kickplayer')

On line 1, we import PySpigot as ``ps`` to utilize the command manager (``command``).

On line 3, we define a function called ``kick_command`` that takes three parameters, ``sender``, ``label``, and ``args``. ``sender`` is who/what executed the command, ``label`` is exactly the command that was typed (if the command had aliases, then this would be the alias that ``sender`` used if they did). Finally, ``args`` is a list of ``str`` containing each argument that ``sender`` typed after the base command.

On line 5, we return a boolean value from the function. Command functions must return either ``True`` or ``False``. Unsure of when you should return True or False? See the `Spigot tutorial on commands <https://www.spigotmc.org/wiki/create-a-simple-command/>`__.

On line 7, we define a function called ``tab_kick_command`` that takes the same parameters as ``kick_command``. This function serves as the tab completer for the command. On line 9, we return a list of ``str`` from this function, which serves as the options that can be tab completed. Tab complete functions must return a list of ``str``.

All commands must be registered with PySpigot's command manager to work. Commands can be registered in multiple ways. In the code above, the ``registerCommand(function, name)`` function is used on line 11, which takes two arguments:

* The first argument accepts the function that should be called when the command is executed.
* The second argument is the name of the command, a ``str``.

Additionally, on line 7, we assign the returned value of ``registerCommand`` to ``registered_command``. This is a ``ScriptCommand`` object, an object that represents the command that was registered. This can be used to unregister the command if you would like to do so later.

To summarize:

* Commands are defined as functions in your script. Command functions *must* take three parameters: a sender, label, and args (the names of these parameters can be whatever you like). The function is called when the command is executed.
* All commands must be registered with PySpigot's command manager using ``command.registerCommand(function, name)``.

Command Manager Usage
#####################

In addition to the function outlined in the example code above, the command manager has other functions in case greater flexibility/control is needed over the commands you register:

* ``registerCommand(command_function, name)``: The most basic way to register a command.
* ``registerCommand(command_function, tab_function, name)``
* ``registerCommand(command_function, name, description, usage)``
* ``registerCommand(command_function, tab_function, name, description, usage)``
* ``registerCommand(command_function, name, description, usage, aliases)``
* ``registerCommand(command_function, tab_function, name, description, usage, aliases)``
* ``registerCommand(command_function, tab_function, name, description, usage, aliases, permission, permission_message)``: The most comprehensive way to register a command. ``permission_message`` is displayed if the command sender does not have permission to execute the command.
* ``unregisterCommand(name)``: Allows you to unregister a command from your script using its name.
* ``unregisterCommand(command)``: Allows you to unregister a command from your script. Accepts the ``ScriptCommand`` object returned from any of the ``registerCommand`` functions