"""
Contains some decorators related to base script functionality.
"""

import types

def start(function):
    if not isinstance(function, types.FunctionType):
        raise TypeError('@start should decorate a function')

    function.start_function = True
    return function


def stop(function):
    if not isinstance(function, types.FunctionType):
        raise TypeError('@stop should decorate a function')

    function.stop_function = True
    return function

__all__ = [
    'start',
    'stop'
]
