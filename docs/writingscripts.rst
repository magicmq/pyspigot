.. _writingscripts:

Scripts
=======

This page contains all information related to writing scripts.

.. note:: This tutorial *does not* explain how to use the Bukkit/Spigot API or how to use

There are a few guidelines that must be followed when writing PySpigot scripts. Please make sure to adhere to these guidelines at all times:

* Scripts must by Python scripts and end in .py. Files that do not end in .py will not load.
* Scripts must be placed in the ``scripts`` folder
* Do not assign variables with the name ``listener``, ``command``, ``scheduler``, ``config``, ``bukkit``, or ``global``. These variables are inserted to the local namespace automatically at runtime and correspond to the managers that you will use to register listeners, tasks, etc.

Basic Syntax
############

The basic syntax is identical to that of Python 2. Jython does not support Python 3, so Python 2 syntax needs to be used for the time being. It is not known when Jython will update to Python 3. For most cases, the Python 2 syntax will be the exact same as that of Python 3.

Any questions concerning Python syntax or writing Python code in general should be redirected to the appropriate forum, as this tutorial will not provide an intoroduction to writing basic Python code.

Additionally, this tutorial will not explain how to use the Bukkit/Spigot API. Please see `Spigot's Website <https://www.spigotmc.org/>`__ and the `Spigot Javadocs <https://hub.spigotmc.org/javadocs/spigot/index.html?overview-summary.html>`__ for complete information on this.

Basic Script Information
########################

All PySpigot scripts are designed to be *self-contained* files. This means that each script should, at most, consist of one file only. This does not mean that scripts cannot interact with each other, but PySpigot is designed to treat each .py file in the ``scripts`` folder as a separate script.

PySpigot scripts should be placed into the ``scripts`` folder in PySpigot's main plugin folder.

PySpigot scripts should also have a ``.py`` extension. Any files in the ``scripts`` folder that does not end in ``.py`` will not be parsed and loaded.

Script Loading
***************

PySpigot loads and runs all scripts in the scripts folder automatically and in alphabetical order. This means that if a script depends on another script, then you should name it such that it falls after the script it depends on alphabetically (so it loads after the script it depends on).

There is only one config option related to loading scripts:

* ``script-load-delay``: This is the delay, in ticks, that PySpigot will wait **after server loading is completed** to load scripts. For example, if the value is 20, then PySpigot will wait 20 ticks (or 1 second) after the server finishes loading to load scripts.

Of course, scripts can also be manually loaded using /pyspigot load <scriptname> if you want to load/enable a script after server start/plugin load.

Script Errors and Crashes
*************************

If a script happens to generate an unhandled error or exception while it is running, the script will be automatically unloaded so as not to cause any further issues. Additionally, any information related to the error will be printed to the console.

If the error originated in a script's code (and not Java code), then a traceback will be provided along with the message indicating that a script produced an error. If the error originated in Java code, then a stack trace will be provided along with the error message.

Event Listeners
###############

With PySpigot, you have the ability to register event listeners. Like Bukkit's listener system, when an event fires, its respective function in your script will be called.

.. note:: This is not a comprehensive guide to events in Bukkit. For a more complete guide to commands, see Spigot's tutorial on using the event API: https://www.spigotmc.org/wiki/using-the-event-api/. Note that much of this information will not be useful because it pertains to Java, but the background information is nevertheless helpful.

Listener Code Example
*********************

Let's look at the following code that defines and registers an event listener:

.. code-block:: python
    :linenos:

    from org.bukkit.event.player import AsyncPlayerChatEvent

    def player_chat(event):
        print('Player sent a chat! Their message was: ' + event.getMessage())

    listener.registerEvent(player_chat, AsyncPlayerChatEvent)

First, on line 1, there is an appropriate import statement for Bukkit's ``AsyncPlayerChatEvent``. All events that you wish to listen to *must* be imported!

On line 3, we define a function called ``player_chat`` that takes an event as a parameter (an AsyncPlayerChatEvent in this case). This is the function that will be called when an AsyncPlayerChatEvent occurs. On line 5, we print a simple message to the console that contains the message that was sent in chat.

All event listeners must be registered with PySpigot's listener manager. Fortunately, the listener manager is loaded into the local namespace at runtime as a variable called ``listener``, so you do not need to access it manually. Event listeners are registered using ``listener.registerEvent(function, event)`` The ``registerEvent`` function takes two arguments:

* The first argument accepts the function that should be called when the event fires.
* The second argument accepts the event that should be listened for.

Therefore, on line 6, we call the listener manager to register our event, passing the function we defined on line 5, ``player_chat``, and the event we want to listen for, ``AsyncPlayerChatEvent``.

For complete documentation on available listeners and functions/methods available to use from each, see the `Spigot Javadocs <https://hub.spigotmc.org/javadocs/spigot/index.html?overview-summary.html>`__.

To summarize:

* All events that you wish to use should be imported using Python's import syntax.
* All event listeners should be defined as functions in your script that accept a single parameter (the parameter name can be whatever you like).
* All event listeners must be registered with PySpigot's listener manager using ``listener.registerEvent(function, event)``.

Listener Manager Usage
**********************

There are five functions available for you to use in your script in the listener manager if you would like greater control over events or need more advanced event handling:

* ``listener.registerListener(function, event)``: Explained above, takes the function to call when event fires as well as the event to listen to.
* ``listener.registerListener(function, event, priority)``: Same as above, except also allows you to define an event priority (how early/late your event listener should fire relative to other listeners for the same event). The priority is a string and
   * Event priorities are the same as the priorities found in Bukkit's `EventPriority class <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/EventPriority.html>`__.
* ``listener.registerListener(function, event, ignoreCancelled)``: Allows you to "ignore" the event if it has been cancelled. This means that the event will not fire in your script if it has been previously cancelled by another event listener.
   * This will only work with events that are `cancellable <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/Cancellable.html>`__.
* ``listener.registerListener(function, event, priority, ignoreCancelled)``: Allows you to register an event that is ignored if cancelled *and* that has a priority (a combination of the previous two functions).
* ``listener.unregisterEvent(function)``: Allows you to unregister an event listener from your script. Takes the function you want to unregister as an argument.

Defining Commands
#################

PySpigot allows you to define and register new commands from scripts. These commands function in the same way as any command would in-game.

.. note:: This is not a comprehensive guide to commands in Bukkit. For a more complete guide to commands, see Spigot's tutorial on commands: https://www.spigotmc.org/wiki/create-a-simple-command/. Note that much of this information will not be useful because it pertains to Java, but the background information is nevertheless helpful.

Command Code Example
********************

Let's look at the following code that defines and registers a command:

.. code-block:: python
    :linenos:

    def kick_command(sender, command, label, args):
        #Do something...
        return True

    command.registerCommand(kick_command, 'kickplayer')

On line 1, we define a function called ``kick_command`` that takes four arguments, a sender, command, label, and args. Sender is who executed the command, command is a `command <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/command/Command.html>`__ object. The label is exactly the command that the player typed in (if the command had aliases, then this would be the alias that the command sender used if they did). Finally, args is a string array representing each argument that the command sender typed after the label.

On line 3, we return a boolean value from the function. This is a requirement for all command functions! They must return either true or false.

Like listeners, all commands must be registered with PySpigot's command manager. Fortunately, the command manager is loaded into the local namespace at runtime as a variable called ``command``, so you do not need to access it manually. Commands are registered using ``command.registerCommand(function, name)`` The ``registerCommand`` function takes two arguments:

* The first argument accepts the function that should be called when a player executes the command.
* The second argument is the name of the command, a string.

Therefore, on line 5, we register the command by calling ``command.registerCommand``, passing it our ``kick_command`` function as well as the string ``kickplayer``, the name the we want our command to be.

To summarize:

* Like listeners, commands are defined as functions in your script. Command functions *must* take four parameters: a sender, command, label, and args (the names of these can be whatever you like).
* All commands must be registered with PySpigot's command manager using ``command.registerCommand(function, name)``.

Command Manager Usage
*********************

In addition to the most basic function explained above, the command manager has other methods in case you need greater flexibility or control over commands you define:

* ``command.registerCommand(function, name)``: Explained above, takes the function to call when the command is executed as well as the name of the command to register.
* ``command.registerCommand(function, name, usage, description, aliases)``: In addition to the same arguments as the above function, this one also takes a usage, description, and aliases. Usage is what to send to the player if the command function returns false (if it did not complete successfully). This is usually something like "/command <args>", where you show someone how to execute the command. Description is a description of what the command does, and aliases is a list of strings that someone could use to execute the command (that isn't the command name itself).
* ``command.unregisterCommand(function)``: Allows you to unregister a command from your script. Takes the function you want to unregister as an argument.

Tasks
#####

Through PySpigot, you can interact with Bukkit's task scheduler and schedule/run synchronous and asynchronous tasks. These allow you to run code on a thread other than the main thread as well as run code repeatedly at a fixed interval.

.. note:: This is not a comprehensive guide to scheduling tasks. For a more complete guide to tasks and scheduler programming, see Bukkit's tutorial on using the scheduler: https://bukkit.fandom.com/wiki/Scheduler_Programming. Note that much of this information will not be useful because it pertains to Java, but the background information is nevertheless helpful.

Task Code Example
*****************

Let's take a look at the following code that defines and starts a task:

.. code-block:: python
    :linenos:

    def run_task():
        #Do something...

    task_id = scheduler.scheduleRepeatingTask(run_task, 0, 100)

On line 1, we define a function called ``run_task`` that takes no arguments.

Like listeners, all tasks must be registered and run with PySpigot's task manager. Fortunately, the task manager is loaded into the local namespace at runtime as a variable called ``tasks``, so you do not need to access it manually. There are many different ways to start tasks depending on if we want it to be synchronous, ascynchronous, and/or repeating, but here we want our task to be synchronous and repeating, so we use ``tasks.scheduleRepeatingTask(function, delay, interval)``, which takes three arguments:

* The first argument accepts the function that should be called when the task runs (either once or repeatedly at a fixed interval).
* The second argument is the delay (in ticks) that the scheduler should wait before starting the task when it is registered.
* The third argument is the interval (in ticks) that the task should be run.

Therefore, on line 4, we register the task as a synchronous repeating task using ``scheduler.scheduleRepeatingTask``. This will return a task id, which we then store as a variable called ``task_id``. We store this task id in case we want to cancel our task later. Cancelling a task requires the task ID.

To summarize:

* Like listeners, tasks are defined as functions in your script. Task functions do not take any arguments and do not return anything.
* All tasks must be registered with PySpigot's command manager. To schedule and run a synchronous repeating task, use ``scheduler.scheduleRepeatingTask(function, delay, interval)``.

Task Manager Usage
******************

In addition to scheduling synchronous repeating tasks, the task manager has many other functions to schedule other types of tasks as well as stop tasks:

* ``scheduler.runTask(function)``: Run a synchronous task as soon as possible. Takes the function to call when the task runs.
* ``scheduler.runTaskAsync(function)``: Run an asychronous task (a task on a thread other than the main server thread). Takes the function to call when the task runs.
* ``scheduler.runTaskLater(function, delay)``: Run a synchronous task at some point in the future after the specified delay. Takes the function to call when the task runs and the delay to wait (in ticks) before running the task.
* ``scheduler.runTaskLaterAsync(function, delay)``: Run an asynchronous task at some point in the future after the specified delay. Takes the function to call when the task runs and the delay to wait (in ticks) before running the task.
* ``scheduler.scheduleRepeatingTask(function, delay, interval)``: Run a synchronous repeating task that repeats every specified interval. Takes the function to call each time the task runs, the delay to wait (in ticks) before running the task, and the interval (in ticks) at which the task should be run.
* ``scheduler.scheduleAsyncRepeatingTask(function, delay, interval)``: Run an asynchronous repeating task that repeats every specified interval. Takes the function to call each time the task runs, the delay to wait (in ticks) before running the task, and the interval (in ticks) at which the task should be run.
* ``scheduler.stopTask(id)``: Stop/Cancel a task. Takes the id of the task to stop.

Configuration Files
###################

With PySpigot, your scripts can load, access, and save to configuration files. All configuration files that scripts access using the config manager are automatically stored in the ``config`` folder located within PySpigot's plugin folder.

.. note:: This is not a comprehensive guide to working with config files. For more complete documentation on available methods/functions, see the Javadocs: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/MemorySection.html. All methods listed here can be called from within your script.

Configuration File Code Example
*******************************

Let's take a look at the following code that loads a config, reads a number and string from it, writes to it, then saves it.

.. code-block:: python
    :linenos:

    script_config = config.loadConfig('test.yml')

    a_number = config.getInt('test-number')
    a_string = config.getString('test-string')

    script_config.set('test-set', 1337)
    script_config.save()

On line 1, we load the config using the config manager. Fortunately, the config manager is loaded into the local namespace at runtime as a variable called ``config``, so you do not need to access it manually. The ``loadConfig`` function takes a string representing the name of the config file to load. If the file does not exist, it will create it automatically.

On lines 3 and 4, we read a number and a string from the config, respectively, by using ``getInt`` and ``getString``.

Finally, on lines 5 and 6, we first set the value 1337 to a config key called ``test-set``. Then, we save the config with ``script_config.save()``.

.. warning:: Configuration files are not unique to each script! Any script can access any config file. Make sure that when you load a config, the name of the config file you are loading is the name you want to load. Try to use unique names for each script so that the same config file isn't accidentally loaded/saved on multiple different scripts.

To summrize:

* Scripts can load and save to config files that are automatically stored in PySpigot's plugin folder in the ``configs`` folder.
* To load a config, use ``config.load(name)``. The ``name`` parameter is the name of the config file you wish to load (including the ``.yml`` extension). If the config file does not exist, it will be created for you automatically. This returns a ``ScriptConfig`` object that is used to access the contents of the config and write to the config.
* For all available functions/methods to get values from a loaded config, see the `Javadocs <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/MemorySection.html>`__.
* To set a value in a config, use ``script_config.set(key, value)``, where ``key`` is the key you wish to write to and ``value`` is the value to write.
* Finally, to save a config, use ``script_config.save()``.

Config Manager Usage
********************

The following are methods/functions that you can use from the config manager:

* ``config.loadConfig(name)``: This loads/creates the config, as described above. Takes the name of the file you wish to load or create. Returns a ``ScriptConfig`` object representing the config that was loaded/created.
* ``config.reloadConfig(config)``: This reloads a config in case there any changes to the file that need to be loaded in. Takes the config (a ``ScriptConfig``) to reload. Returns another ``ScriptConfig`` object representing the config that was reloaded.

ScriptConfig Usage
******************

Like described above, loading/reloading a config returns a ``ScriptConfig`` object. This object has many methods/functions that you can use:

* ``script_config.set(key, value)``: Set a value in the config at the given key. Takes a key representing the key to write to and value which is the value to write.
* ``script_config.save()``: This saves the config so that any values you set will be persistent.
* All methods present `here <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/MemorySection.html>`__ can also be used.