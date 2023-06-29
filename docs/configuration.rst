.. _configuration:

Configuration Files
===================

With PySpigot, your scripts can load, access, and save to configuration files. All configuration files that scripts access using the config manager are automatically stored in the ``config`` folder located within PySpigot's plugin folder.

See `PySpigot's Managers`_ for instructions on how to import the config manager into your script.

.. note:: This is not a comprehensive guide to working with config files. For more complete documentation on available methods/functions, see the Javadocs: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/MemorySection.html. All methods listed here can be called from within your script.

Configuration File Code Example
###############################

Let's take a look at the following code that loads a config, reads a number and string from it, writes to it, then saves it.

.. code-block:: python
    :linenos:

    from dev.magicmq.pyspigot import PySpigot as ps

    script_config = ps.config.loadConfig('test.yml')

    a_number = script_config.getInt('test-number')
    a_string = script_config.getString('test-string')

    script_config.set('test-set', 1337)
    script_config.save()

On line 1, we import PySpigot as ``ps`` to utilize the config manager (``config``).

On line 3, we load the config using the config manager. Fortunately, the config manager is loaded into the local namespace at runtime as a variable called ``config``, so you do not need to access it manually. The ``loadConfig`` function takes a string representing the name of the config file to load. If the file does not exist, it will create it automatically.

On lines 5 and 6, we read a number and a string from the config, respectively, by using ``getInt`` and ``getString``.

Finally, on lines 7 and 8, we first set the value 1337 to a config key called ``test-set``. Then, we save the config with ``script_config.save()``.

.. warning:: Configuration files are not unique to each script! Any script can access any config file. Make sure that when you load a config, the name of the config file you are loading is the name you want to load. Try to use unique names for each script so that the same config file isn't accidentally loaded/saved on multiple different scripts.

To summrize:

* Scripts can load and save to config files that are automatically stored in PySpigot's plugin folder in the ``configs`` folder.
* To load a config, use ``config.load(name)``. The ``name`` parameter is the name of the config file you wish to load (including the ``.yml`` extension). If the config file does not exist, it will be created for you automatically. This returns a ``ScriptConfig`` object that is used to access the contents of the config and write to the config.
* For all available functions/methods to get values from a loaded config, see the `Javadocs <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/MemorySection.html>`__.
* To set a value in a config, use ``script_config.set(key, value)``, where ``key`` is the key you wish to write to and ``value`` is the value to write.
* Finally, to save a config, use ``script_config.save()``.

Config Manager Usage
####################

The following are methods/functions that you can use from the config manager:

* ``config.loadConfig(name)``: This loads/creates the config, as described above. Takes the name of the file you wish to load or create. Returns a ``ScriptConfig`` object representing the config that was loaded/created.
* ``config.reloadConfig(config)``: This reloads a config in case there any changes to the file that need to be loaded in. Takes the config (a ``ScriptConfig``) to reload. Returns another ``ScriptConfig`` object representing the config that was reloaded.

ScriptConfig Usage
##################

Like described above, loading/reloading a config returns a ``ScriptConfig`` object. This object has many methods/functions that you can use:

* ``script_config.set(key, value)``: Set a value in the config at the given key. Takes a key representing the key to write to and value which is the value to write.
* ``script_config.save()``: This saves the config so that any values you set will be persistent.
* All methods present `here <https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/MemorySection.html>`__ can also be used.