"""
Contains decorators to more easily register Java functional interfaces.
"""

from dev.magicmq.pyspigot.util import SAMHelper

def _attach(fn, java_object, kind):
    fn.java = java_object
    fn.j = java_object

    def _to_java():
        return java_object
    fn.to_java = _to_java

    def _into(registrar, *args, **kwargs):
        return registrar(java_object, *args, **kwargs)
    fn.into = _into

    fn._sam_kind = kind

    return fn


def runnable(fn):               return _attach(fn, SAMHelper.runnable(fn), 'Runnable')
def bi_consumer(fn):            return _attach(fn, SAMHelper.biConsumer(fn), 'BiConsumer')
def bi_function(fn):            return _attach(fn, SAMHelper.biFunction(fn), 'BiFunction')
def bi_predicate(fn):           return _attach(fn, SAMHelper.biPredicate(fn), 'BiPredicate')
def binary_operator(fn):        return _attach(fn, SAMHelper.binaryOperator(fn), 'BinaryOperator')
def boolean_supplier(fn):       return _attach(fn, SAMHelper.booleanSupplier(fn), 'BooleanSupplier')
def consumer(fn):               return _attach(fn, SAMHelper.consumer(fn), 'Consumer')
def double_binary_operator(fn): return _attach(fn, SAMHelper.doubleBinaryOperator(fn), 'DoubleBinaryOperator')
def double_consumer(fn):        return _attach(fn, SAMHelper.doubleConsumer(fn), 'DoubleConsumer')
def double_function(fn):        return _attach(fn, SAMHelper.doubleFunction(fn), 'DoubleFunction')
def double_predicate(fn):       return _attach(fn, SAMHelper.doublePredicate(fn), 'DoublePredicate')
def double_supplier(fn):        return _attach(fn, SAMHelper.doubleSupplier(fn), 'DoubleSupplier')
def double_to_int_function(fn): return _attach(fn, SAMHelper.doubleToIntFunction(fn), 'DoubleToIntFunction')
def double_to_long_function(fn):return _attach(fn, SAMHelper.doubleToLongFunction(fn), 'DoubleToLongFunction')
def double_unary_operator(fn):  return _attach(fn, SAMHelper.doubleUnaryOperator(fn), 'DoubleUnaryOperator')
def function(fn):               return _attach(fn, SAMHelper.function(fn), 'Function')
def int_binary_operator(fn):    return _attach(fn, SAMHelper.intBinaryOperator(fn), 'IntBinaryOperator')
def int_consumer(fn):           return _attach(fn, SAMHelper.intConsumer(fn), 'IntConsumer')
def int_function(fn):           return _attach(fn, SAMHelper.intFunction(fn), 'IntFunction')
def int_predicate(fn):          return _attach(fn, SAMHelper.intPredicate(fn), 'IntPredicate')
def int_supplier(fn):           return _attach(fn, SAMHelper.intSupplier(fn), 'IntSupplier')
def int_to_double_function(fn): return _attach(fn, SAMHelper.intToDoubleFunction(fn), 'IntToDoubleFunction')
def int_to_long_function(fn):   return _attach(fn, SAMHelper.intToLongFunction(fn), 'IntToLongFunction')
def int_unary_operator(fn):     return _attach(fn, SAMHelper.intUnaryOperator(fn), 'IntUnaryOperator')
def long_binary_operator(fn):   return _attach(fn, SAMHelper.longBinaryOperator(fn), 'LongBinaryOperator')
def long_consumer(fn):          return _attach(fn, SAMHelper.longConsumer(fn), 'LongConsumer')
def long_function(fn):          return _attach(fn, SAMHelper.longFunction(fn), 'LongFunction')
def long_predicate(fn):         return _attach(fn, SAMHelper.longPredicate(fn), 'LongPredicate')
def long_supplier(fn):          return _attach(fn, SAMHelper.longSupplier(fn), 'LongSupplier')
def long_to_double_function(fn):return _attach(fn, SAMHelper.longToDoubleFunction(fn), 'LongToDoubleFunction')
def long_to_int_function(fn):   return _attach(fn, SAMHelper.longToIntFunction(fn), 'LongToIntFunction')
def long_unary_operator(fn):    return _attach(fn, SAMHelper.longUnaryOperator(fn), 'LongUnaryOperator')
def obj_double_consumer(fn):    return _attach(fn, SAMHelper.objDoubleConsumer(fn), 'ObjDoubleConsumer')
def obj_int_consumer(fn):       return _attach(fn, SAMHelper.objIntConsumer(fn), 'ObjIntConsumer')
def obj_long_consumer(fn):      return _attach(fn, SAMHelper.objLongConsumer(fn), 'ObjLongConsumer')
def predicate(fn):              return _attach(fn, SAMHelper.predicate(fn), 'Predicate')
def supplier(fn):               return _attach(fn, SAMHelper.supplier(fn), 'Supplier')
def to_double_bi_function(fn):  return _attach(fn, SAMHelper.toDoubleBiFunction(fn), 'ToDoubleBiFunction')
def to_double_function(fn):     return _attach(fn, SAMHelper.toDoubleFunction(fn), 'ToDoubleFunction')
def to_int_bi_function(fn):     return _attach(fn, SAMHelper.toIntBiFunction(fn), 'ToIntBiFunction')
def to_int_function(fn):        return _attach(fn, SAMHelper.toIntFunction(fn), 'ToIntFunction')
def to_long_bi_function(fn):    return _attach(fn, SAMHelper.toLongBiFunction(fn), 'ToLongBiFunction')
def to_long_function(fn):       return _attach(fn, SAMHelper.toLongFunction(fn), 'ToLongFunction')
def unary_operator(fn):         return _attach(fn, SAMHelper.unaryOperator(fn), 'UnaryOperator')

# --------- "into" decorator factories (register at import time) ---------

def _into_factory(base_decorator, registrar, *rargs, **rkwargs):
    def _deco(fn):
        fn = base_decorator(fn)           # attach .java etc.
        registrar(fn.java, *rargs, **rkwargs)
        return fn
    return _deco


def bi_consumer_into(registrar, *a, **k):            return _into_factory(bi_consumer,            registrar, *a, **k)
def bi_function_into(registrar, *a, **k):            return _into_factory(bi_function,            registrar, *a, **k)
def bi_predicate_into(registrar, *a, **k):           return _into_factory(bi_predicate,           registrar, *a, **k)
def binary_operator_into(registrar, *a, **k):        return _into_factory(binary_operator,        registrar, *a, **k)
def boolean_supplier_into(registrar, *a, **k):       return _into_factory(boolean_supplier,       registrar, *a, **k)
def consumer_into(registrar, *a, **k):               return _into_factory(consumer,               registrar, *a, **k)
def double_binary_operator_into(registrar, *a, **k): return _into_factory(double_binary_operator, registrar, *a, **k)
def double_consumer_into(registrar, *a, **k):        return _into_factory(double_consumer,        registrar, *a, **k)
def double_function_into(registrar, *a, **k):        return _into_factory(double_function,        registrar, *a, **k)
def double_predicate_into(registrar, *a, **k):       return _into_factory(double_predicate,       registrar, *a, **k)
def double_supplier_into(registrar, *a, **k):        return _into_factory(double_supplier,        registrar, *a, **k)
def double_to_int_function_into(registrar, *a, **k): return _into_factory(double_to_int_function, registrar, *a, **k)
def double_to_long_function_into(registrar, *a, **k):return _into_factory(double_to_long_function,registrar, *a, **k)
def double_unary_operator_into(registrar, *a, **k):  return _into_factory(double_unary_operator,  registrar, *a, **k)
def function_into(registrar, *a, **k):               return _into_factory(function,               registrar, *a, **k)
def int_binary_operator_into(registrar, *a, **k):    return _into_factory(int_binary_operator,    registrar, *a, **k)
def int_consumer_into(registrar, *a, **k):           return _into_factory(int_consumer,           registrar, *a, **k)
def int_function_into(registrar, *a, **k):           return _into_factory(int_function,           registrar, *a, **k)
def int_predicate_into(registrar, *a, **k):          return _into_factory(int_predicate,          registrar, *a, **k)
def int_supplier_into(registrar, *a, **k):           return _into_factory(int_supplier,           registrar, *a, **k)
def int_to_double_function_into(registrar, *a, **k): return _into_factory(int_to_double_function, registrar, *a, **k)
def int_to_long_function_into(registrar, *a, **k):   return _into_factory(int_to_long_function,   registrar, *a, **k)
def int_unary_operator_into(registrar, *a, **k):     return _into_factory(int_unary_operator,     registrar, *a, **k)
def long_binary_operator_into(registrar, *a, **k):   return _into_factory(long_binary_operator,   registrar, *a, **k)
def long_consumer_into(registrar, *a, **k):          return _into_factory(long_consumer,          registrar, *a, **k)
def long_function_into(registrar, *a, **k):          return _into_factory(long_function,          registrar, *a, **k)
def long_predicate_into(registrar, *a, **k):         return _into_factory(long_predicate,         registrar, *a, **k)
def long_supplier_into(registrar, *a, **k):          return _into_factory(long_supplier,          registrar, *a, **k)
def long_to_double_function_into(registrar, *a, **k):return _into_factory(long_to_double_function,registrar, *a, **k)
def long_to_int_function_into(registrar, *a, **k):   return _into_factory(long_to_int_function,   registrar, *a, **k)
def long_unary_operator_into(registrar, *a, **k):    return _into_factory(long_unary_operator,    registrar, *a, **k)
def obj_double_consumer_into(registrar, *a, **k):    return _into_factory(obj_double_consumer,    registrar, *a, **k)
def obj_int_consumer_into(registrar, *a, **k):       return _into_factory(obj_int_consumer,       registrar, *a, **k)
def obj_long_consumer_into(registrar, *a, **k):      return _into_factory(obj_long_consumer,      registrar, *a, **k)
def predicate_into(registrar, *a, **k):              return _into_factory(predicate,              registrar, *a, **k)
def supplier_into(registrar, *a, **k):               return _into_factory(supplier,               registrar, *a, **k)
def to_double_bi_function_into(registrar, *a, **k):  return _into_factory(to_double_bi_function,  registrar, *a, **k)
def to_double_function_into(registrar, *a, **k):     return _into_factory(to_double_function,     registrar, *a, **k)
def to_int_bi_function_into(registrar, *a, **k):     return _into_factory(to_int_bi_function,     registrar, *a, **k)
def to_int_function_into(registrar, *a, **k):        return _into_factory(to_int_function,        registrar, *a, **k)
def to_long_bi_function_into(registrar, *a, **k):    return _into_factory(to_long_bi_function,    registrar, *a, **k)
def to_long_function_into(registrar, *a, **k):       return _into_factory(to_long_function,       registrar, *a, **k)
def unary_operator_into(registrar, *a, **k):         return _into_factory(unary_operator,         registrar, *a, **k)


__all__ = [
    'bi_consumer',
    'bi_consumer_into',
    'bi_function',
    'bi_function_into',
    'bi_predicate',
    'bi_predicate_into',
    'binary_operator',
    'binary_operator_into',
    'boolean_supplier',
    'boolean_supplier_into',
    'consumer',
    'consumer_into',
    'double_binary_operator',
    'double_binary_operator_into',
    'double_consumer',
    'double_consumer_into',
    'double_function',
    'double_function_into',
    'double_predicate',
    'double_predicate_into',
    'double_supplier',
    'double_supplier_into',
    'double_to_int_function',
    'double_to_int_function_into',
    'double_to_long_function',
    'double_to_long_function_into',
    'double_unary_operator',
    'double_unary_operator_into',
    'function',
    'function_into',
    'int_binary_operator',
    'int_binary_operator_into',
    'int_consumer',
    'int_consumer_into',
    'int_function',
    'int_function_into',
    'int_predicate',
    'int_predicate_into',
    'int_supplier',
    'int_supplier_into',
    'int_to_double_function',
    'int_to_double_function_into',
    'int_to_long_function',
    'int_to_long_function_into',
    'int_unary_operator',
    'int_unary_operator_into',
    'long_binary_operator',
    'long_binary_operator_into',
    'long_consumer',
    'long_consumer_into',
    'long_function',
    'long_function_into',
    'long_predicate',
    'long_predicate_into',
    'long_supplier',
    'long_supplier_into',
    'long_to_double_function',
    'long_to_double_function_into',
    'long_to_int_function',
    'long_to_int_function_into',
    'long_unary_operator',
    'long_unary_operator_into',
    'obj_double_consumer',
    'obj_double_consumer_into',
    'obj_int_consumer',
    'obj_int_consumer_into',
    'obj_long_consumer',
    'obj_long_consumer_into',
    'predicate',
    'predicate_into',
    'supplier',
    'supplier_into',
    'to_double_bi_function',
    'to_double_bi_function_into',
    'to_double_function',
    'to_double_function_into',
    'to_int_bi_function',
    'to_int_bi_function_into',
    'to_int_function',
    'to_int_function_into',
    'to_long_bi_function',
    'to_long_bi_function_into',
    'to_long_function',
    'to_long_function_into',
    'unary_operator',
    'unary_operator_into',
]
