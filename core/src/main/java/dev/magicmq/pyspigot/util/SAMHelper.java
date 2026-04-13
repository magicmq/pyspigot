/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.util;


import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

/**
 * A class that contains methods which wrap Java SAMs (Single Object Methods), also known as functional interfaces, for
 * ease of use within the Python scripting environment.
 * <p>
 * Meant to be used via the {@code functionals.py} module. Scripts should never call methods in this class directly.
 */
public final class SAMHelper {

    public static Runnable runnable(PyFunction function) {
        return runnable(ScriptContext.require(), function);
    }

    public static Runnable runnable(Script script, PyFunction function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.__call__();
            }
        };
    }

    public static <T, U> BiConsumer<T, U> biConsumer(PyFunction function) {
        return biConsumer(ScriptContext.require(), function);
    }

    public static <T, U> BiConsumer<T, U> biConsumer(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                function.__call__(p1, p2);
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> biFunction(PyFunction function) {
        return biFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T, U, R> BiFunction<T, U, R> biFunction(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return (R) function.__call__(p1, p2);
            }
        };
    }

    public static <T> BinaryOperator<T> binaryOperator(PyFunction function) {
        return binaryOperator(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T> BinaryOperator<T> binaryOperator(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return (T) function.__call__(p1, p2);
            }
        };
    }

    public static <T, U> BiPredicate<T, U> biPredicate(PyFunction function) {
        return biPredicate(ScriptContext.require(), function);
    }

    public static <T, U> BiPredicate<T, U> biPredicate(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return function.__call__(p1, p2).__nonzero__();
            }
        };
    }

    public static BooleanSupplier booleanSupplier(PyFunction function) {
        return booleanSupplier(ScriptContext.require(), function);
    }

    public static BooleanSupplier booleanSupplier(Script script, PyFunction function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.__call__().__nonzero__();
            }
        };
    }

    public static <T> Consumer<T> consumer(PyFunction function) {
        return consumer(ScriptContext.require(), function);
    }

    public static <T> Consumer<T> consumer(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                function.__call__(p1);
            }
        };
    }

    public static DoubleBinaryOperator doubleBinaryOperator(PyFunction function) {
        return doubleBinaryOperator(ScriptContext.require(), function);
    }

    public static DoubleBinaryOperator doubleBinaryOperator(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return function.__call__(p1, p2).asDouble();
            }
        };
    }

    public static DoubleConsumer doubleConsumer(PyFunction function) {
        return doubleConsumer(ScriptContext.require(), function);
    }

    public static DoubleConsumer doubleConsumer(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                function.__call__(p1);
            }
        };
    }

    public static <R> DoubleFunction<R> doubleFunction(PyFunction function) {
        return doubleFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <R> DoubleFunction<R> doubleFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return (R) function.__call__(p1);
            }
        };
    }

    public static DoublePredicate doublePredicate(PyFunction function) {
        return doublePredicate(ScriptContext.require(), function);
    }

    public static DoublePredicate doublePredicate(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).__nonzero__();
            }
        };
    }

    public static DoubleSupplier doubleSupplier(PyFunction function) {
        return doubleSupplier(ScriptContext.require(), function);
    }

    public static DoubleSupplier doubleSupplier(Script script, PyFunction function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.__call__().asDouble();
            }
        };
    }

    public static DoubleToIntFunction doubleToIntFunction(PyFunction function) {
        return doubleToIntFunction(ScriptContext.require(), function);
    }

    public static DoubleToIntFunction doubleToIntFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asInt();
            }
        };
    }

    public static DoubleToLongFunction doubleToLongFunction(PyFunction function) {
        return doubleToLongFunction(ScriptContext.require(), function);
    }

    public static DoubleToLongFunction doubleToLongFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asLong();
            }
        };
    }

    public static DoubleUnaryOperator doubleUnaryOperator(PyFunction function) {
        return doubleUnaryOperator(ScriptContext.require(), function);
    }

    public static DoubleUnaryOperator doubleUnaryOperator(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asDouble();
            }
        };
    }

    public static <T, R> Function<T, R> function(PyFunction function) {
        return function(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Function<T, R> function(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return (R) function.__call__(p1);
            }
        };
    }

    public static IntBinaryOperator intBinaryOperator(PyFunction function) {
        return intBinaryOperator(ScriptContext.require(), function);
    }

    public static IntBinaryOperator intBinaryOperator(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return function.__call__(p1, p2).asInt();
            }
        };
    }

    public static IntConsumer intConsumer(PyFunction function) {
        return intConsumer(ScriptContext.require(), function);
    }

    public static IntConsumer intConsumer(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                function.__call__(p1);
            }
        };
    }

    public static <R> IntFunction<R> intFunction(PyFunction function) {
        return intFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <R> IntFunction<R> intFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return (R) function.__call__(p1);
            }
        };
    }

    public static IntPredicate intPredicate(PyFunction function) {
        return intPredicate(ScriptContext.require(), function);
    }

    public static IntPredicate intPredicate(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).__nonzero__();
            }
        };
    }

    public static IntSupplier intSupplier(PyFunction function) {
        return intSupplier(ScriptContext.require(), function);
    }

    public static IntSupplier intSupplier(Script script, PyFunction function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.__call__().asInt();
            }
        };
    }

    public static IntToDoubleFunction intToDoubleFunction(PyFunction function) {
        return intToDoubleFunction(ScriptContext.require(), function);
    }

    public static IntToDoubleFunction intToDoubleFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asDouble();
            }
        };
    }

    public static IntToLongFunction intToLongFunction(PyFunction function) {
        return intToLongFunction(ScriptContext.require(), function);
    }

    public static IntToLongFunction intToLongFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asLong();
            }
        };
    }

    public static IntUnaryOperator intUnaryOperator(PyFunction function) {
        return intUnaryOperator(ScriptContext.require(), function);
    }

    public static IntUnaryOperator intUnaryOperator(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asInt();
            }
        };
    }

    public static LongBinaryOperator longBinaryOperator(PyFunction function) {
        return longBinaryOperator(ScriptContext.require(), function);
    }

    public static LongBinaryOperator longBinaryOperator(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return function.__call__(p1, p2).asLong();
            }
        };
    }

    public static LongConsumer longConsumer(PyFunction function) {
        return longConsumer(ScriptContext.require(), function);
    }

    public static LongConsumer longConsumer(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                function.__call__(p1);
            }
        };
    }

    public static <R> LongFunction<R> longFunction(PyFunction function) {
        return longFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <R> LongFunction<R> longFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return (R) function.__call__(p1);
            }
        };
    }

    public static LongPredicate longPredicate(PyFunction function) {
        return longPredicate(ScriptContext.require(), function);
    }

    public static LongPredicate longPredicate(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).__nonzero__();
            }
        };
    }

    public static LongSupplier longSupplier(PyFunction function) {
        return longSupplier(ScriptContext.require(), function);
    }

    public static LongSupplier longSupplier(Script script, PyFunction function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.__call__().asLong();
            }
        };
    }

    public static LongToDoubleFunction longToDoubleFunction(PyFunction function) {
        return longToDoubleFunction(ScriptContext.require(), function);
    }

    public static LongToDoubleFunction longToDoubleFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asDouble();
            }
        };
    }

    public static LongToIntFunction longToIntFunction(PyFunction function) {
        return longToIntFunction(ScriptContext.require(), function);
    }

    public static LongToIntFunction longToIntFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asInt();
            }
        };
    }

    public static LongUnaryOperator longUnaryOperator(PyFunction function) {
        return longUnaryOperator(ScriptContext.require(), function);
    }

    public static LongUnaryOperator longUnaryOperator(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asLong();
            }
        };
    }

    public static <T> ObjDoubleConsumer<T> objDoubleConsumer(PyFunction function) {
        return objDoubleConsumer(ScriptContext.require(), function);
    }

    public static <T> ObjDoubleConsumer<T> objDoubleConsumer(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                function.__call__(p1, p2);
            }
        };
    }

    public static <T> ObjIntConsumer<T> objIntConsumer(PyFunction function) {
        return objIntConsumer(ScriptContext.require(), function);
    }

    public static <T> ObjIntConsumer<T> objIntConsumer(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                function.__call__(p1, p2);
            }
        };
    }

    public static <T> ObjLongConsumer<T> objLongConsumer(PyFunction function) {
        return objLongConsumer(ScriptContext.require(), function);
    }

    public static <T> ObjLongConsumer<T> objLongConsumer(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                function.__call__(p1, p2);
            }
        };
    }

    public static <T> Predicate<T> predicate(PyFunction function) {
        return predicate(ScriptContext.require(), function);
    }

    public static <T> Predicate<T> predicate(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).__nonzero__();
            }
        };
    }

    public static <T> Supplier<T> supplier(PyFunction function) {
        return supplier(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> supplier(Script script, PyFunction function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (T) function.__call__();
            }
        };
    }

    public static <T, U> ToDoubleBiFunction<T, U> toDoubleBiFunction(PyFunction function) {
        return toDoubleBiFunction(ScriptContext.require(), function);
    }

    public static <T, U> ToDoubleBiFunction<T, U> toDoubleBiFunction(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return function.__call__(p1, p2).asDouble();
            }
        };
    }

    public static <T> ToDoubleFunction<T> toDoubleFunction(PyFunction function) {
        return toDoubleFunction(ScriptContext.require(), function);
    }

    public static <T> ToDoubleFunction<T> toDoubleFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asDouble();
            }
        };
    }

    public static <T, U> ToIntBiFunction<T, U> toIntBiFunction(PyFunction function) {
        return toIntBiFunction(ScriptContext.require(), function);
    }

    public static <T, U> ToIntBiFunction<T, U> toIntBiFunction(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return function.__call__(p1, p2).asInt();
            }
        };
    }

    public static <T> ToIntFunction<T> toIntFunction(PyFunction function) {
        return toIntFunction(ScriptContext.require(), function);
    }

    public static <T> ToIntFunction<T> toIntFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asInt();
            }
        };
    }

    public static <T, U> ToLongBiFunction<T, U> toLongBiFunction(PyFunction function) {
        return toLongBiFunction(ScriptContext.require(), function);
    }

    public static <T, U> ToLongBiFunction<T, U> toLongBiFunction(Script script, PyFunction function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                PyObject p2 = Py.java2py(v2);
                return function.__call__(p1, p2).asLong();
            }
        };
    }

    public static <T> ToLongFunction<T> toLongFunction(PyFunction function) {
        return toLongFunction(ScriptContext.require(), function);
    }

    public static <T> ToLongFunction<T> toLongFunction(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return function.__call__(p1).asLong();
            }
        };
    }

    public static <T> UnaryOperator<T> unaryOperator(PyFunction function) {
        return unaryOperator(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T> UnaryOperator<T> unaryOperator(Script script, PyFunction function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                PyObject p1 = Py.java2py(v1);
                return (T) function.__call__(p1);
            }
        };
    }
}
