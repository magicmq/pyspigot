"""
This is a helper module that wraps a variety of functional interfaces in the java.util.function package, for easier use in Python.

This module should be particularly useful when working with some libraries that make use of functional interfaces (such as NBT-API).
"""
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

"""
Represents an operation that accepts two input arguments and returns no result. This is the two-arity specialization of Consumer. Unlike most other functional interfaces, BiConsumer is expected to operate via side-effects.
"""
class BiConsumer(java.util.function.BiConsumer):

    """
    Initialize a new BiConsumer.

    Arguments:
        function (callable): A function that accepts two arguments and returns nothing.
    """
    def __init__(self, function):
        self.accept = function


"""
Represents a function that accepts two arguments and produces a result. This is the two-arity specialization of Function. 
"""
class BiFunction(java.util.function.BiFunction):

    """
    Initializes a new BiFunction.

    Arguments:
        function (callable): A function that accepts two arguments and returns a value.
    """
    def __init__(self, function):
        self.apply = function


"""
Represents a predicate (boolean-valued function) of two arguments. This is the two-arity specialization of Predicate. 
"""
class BiPredicate(java.util.function.BiPredicate):

    """
    Initializes a new BiPredicate.

    Arguments:
        function (callable): A function that accepts two arguments and returns either True or False.
    """
    def __init__(self, function):
        self.test = function


"""
Represents an operation that accepts a single input argument and returns no result. Unlike most other functional interfaces, Consumer is expected to operate via side-effects.
"""
class Consumer(java.util.function.Consumer):

    """
    Initializes a new Consumer.

    Arguments:
        function (callable): A function that accepts one argument and returns nothing.
    """
    def __init__(self, function):
        self.accept = function


"""
Represents a function that accepts one argument and produces a result.
"""
class Function(java.util.function.Function):

    """
    Initializes a new Function.

    Arguments:
        function (callable): A function that accepts one argument and returns a value.
    """
    def __init__(self, function):
        self.apply = function


"""
Represents a predicate (boolean-valued function) of one argument.
"""
class Predicate(java.util.function.Predicate):

    """
    Initializes a new Predicate.

    Arguments:
        function (callable): A function that accepts one argument and returns either True or False.
    """
    def __init__(self, function):
        self.test = function


"""
Represents a supplier of results.

There is no requirement that a new or distinct result be returned each time the supplier is invoked.
"""
class Supplier(java.util.function.Supplier):

    """
    Initializes a new Supplier.

    Arguments:
        function (callable): A function that accepts no arguments and returns a value.
    """
    def __init__(self, function):
        self.get = function