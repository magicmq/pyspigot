"""
Contains decorators for registering PlaceholderAPI placeholder expansions.
"""

import pyspigot as ps

from dev.magicmq.pyspigot.bukkit import PySpigot
import dev.magicmq.pyspigot.exception.ScriptRuntimeException

if not PySpigot.get().isPlaceholderApiAvailable():
    raise dev.magicmq.pyspigot.exception.ScriptRuntimeException('Attempted to initialize PlaceholderAPI decorators, but PlaceholderAPI was not found on the server')

def placeholder(author='Script Author', version='1.0.0'):
    """
    Register a new placeholder expansion by decorating a function. The decorated function will be called when the
    placeholder is used.

    This decorator also adds an attribute to the function that was decorated called `relational_function`, which allows
    for setting a relational placeholder function.

    :param author: The author of the placeholder
    :param version: The version of the placeholder
    """

    def _decorator(function):
        placeholder_manager = ps.placeholder_manager()
        placeholder = placeholder_manager.registerPlaceholder(function, author, version)

        def _relational(relational_function):
            placeholder.setRelationalFunction(relational_function)
            return relational_function

        function.relational_function = _relational

        def _unregister():
            placeholder_manager.unregisterPlaceholder(placeholder)

        function.unregister = _unregister

        return function
    return _decorator


def relational_placehodler(function):
    """
    Add a relational placeholder to a placeholder expansion that was registered previously by decorating a function. The
    decorated function will be called when the relational placeholder is used.

    :param function: The function corresponding to the placeholder that was previously registered
    """

    placeholder_manager = ps.placeholder_manager()
    placeholder_manager.setRelationalPlaceholderFunction(function)
    return function


__all__ = [
    'placeholder',
    'relational_placehodler',
]
