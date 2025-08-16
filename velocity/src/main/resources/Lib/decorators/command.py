"""
Contains decorators for registering commands.
"""

import pyspigot as ps

def command(name, async_tab_complete=True, description='', usage='', aliases=[], permission=None):
    """
    Register a new command by decorating a function. The decorated function will be called when the command is executed.

    :param name: The name of the command to register
    :param async_tab_complete: Whether the tab completion function should be executed asynchronously or not
    :param description: The description of the command
    :param usage: The usage message for the command
    :param aliases: A list of str containing all the aliases for this command
    :param permission: The required permission node to use this command
    """

    def _decorator(function):
        command_manager = ps.command_manager()
        registered_command = command_manager.registerCommand(function, None, async_tab_complete, name, description, usage, aliases, permission)

        function.registered_command = registered_command

        def _tab(tab_function):
            registered_command.setTabFunction(tab_function)
            return tab_function

        function.tab = _tab

        def _unregister():
            command_manager.unregisterCommand(registered_command)

        function.unregister = _unregister

        return function
    return _decorator


def tab(name):
    """
    Register a tab completion function for a command that was registered previously by decorating a function. The decorated
    function will be called when the command is tab completed.

    :param name: The name of the command that was previously registered
    """

    def _decorator(function):
        command_manager = ps.command_manager()
        command_manager.registerTabFunction(function, name)
        return function
    return _decorator


__all__ = [
    'command',
    'tab',
]
