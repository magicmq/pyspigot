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
import jep.python.PyCallable;

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

    public static Runnable runnable(PyCallable function) {
        return runnable(ScriptContext.require(), function);
    }

    public static Runnable runnable(Script script, PyCallable function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call();
            }
        };
    }

    public static <T, U> BiConsumer<T, U> biConsumer(PyCallable function) {
        return biConsumer(ScriptContext.require(), function);
    }

    public static <T, U> BiConsumer<T, U> biConsumer(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1, v2);
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> biFunction(PyCallable function) {
        return biFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T, U, R> BiFunction<T, U, R> biFunction(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (R) function.call(v1, v2);
            }
        };
    }

    public static <T> BinaryOperator<T> binaryOperator(PyCallable function) {
        return binaryOperator(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T> BinaryOperator<T> binaryOperator(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (T) function.call(v1, v2);
            }
        };
    }

    public static <T, U> BiPredicate<T, U> biPredicate(PyCallable function) {
        return biPredicate(ScriptContext.require(), function);
    }

    public static <T, U> BiPredicate<T, U> biPredicate(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Boolean.class, v1, v2);
            }
        };
    }

    public static BooleanSupplier booleanSupplier(PyCallable function) {
        return booleanSupplier(ScriptContext.require(), function);
    }

    public static BooleanSupplier booleanSupplier(Script script, PyCallable function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Boolean.class);
            }
        };
    }

    public static <T> Consumer<T> consumer(PyCallable function) {
        return consumer(ScriptContext.require(), function);
    }

    public static <T> Consumer<T> consumer(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1);
            }
        };
    }

    public static DoubleBinaryOperator doubleBinaryOperator(PyCallable function) {
        return doubleBinaryOperator(ScriptContext.require(), function);
    }

    public static DoubleBinaryOperator doubleBinaryOperator(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Double.class, v1, v2);
            }
        };
    }

    public static DoubleConsumer doubleConsumer(PyCallable function) {
        return doubleConsumer(ScriptContext.require(), function);
    }

    public static DoubleConsumer doubleConsumer(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1);
            }
        };
    }

    public static <R> DoubleFunction<R> doubleFunction(PyCallable function) {
        return doubleFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <R> DoubleFunction<R> doubleFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (R) function.call(v1);
            }
        };
    }

    public static DoublePredicate doublePredicate(PyCallable function) {
        return doublePredicate(ScriptContext.require(), function);
    }

    public static DoublePredicate doublePredicate(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Boolean.class, v1);
            }
        };
    }

    public static DoubleSupplier doubleSupplier(PyCallable function) {
        return doubleSupplier(ScriptContext.require(), function);
    }

    public static DoubleSupplier doubleSupplier(Script script, PyCallable function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Double.class);
            }
        };
    }

    public static DoubleToIntFunction doubleToIntFunction(PyCallable function) {
        return doubleToIntFunction(ScriptContext.require(), function);
    }

    public static DoubleToIntFunction doubleToIntFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Integer.class, v1);
            }
        };
    }

    public static DoubleToLongFunction doubleToLongFunction(PyCallable function) {
        return doubleToLongFunction(ScriptContext.require(), function);
    }

    public static DoubleToLongFunction doubleToLongFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Long.class, v1);
            }
        };
    }

    public static DoubleUnaryOperator doubleUnaryOperator(PyCallable function) {
        return doubleUnaryOperator(ScriptContext.require(), function);
    }

    public static DoubleUnaryOperator doubleUnaryOperator(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Double.class, v1);
            }
        };
    }

    public static <T, R> Function<T, R> function(PyCallable function) {
        return function(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Function<T, R> function(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (R) function.call(v1);
            }
        };
    }

    public static IntBinaryOperator intBinaryOperator(PyCallable function) {
        return intBinaryOperator(ScriptContext.require(), function);
    }

    public static IntBinaryOperator intBinaryOperator(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Integer.class, v1, v2);
            }
        };
    }

    public static IntConsumer intConsumer(PyCallable function) {
        return intConsumer(ScriptContext.require(), function);
    }

    public static IntConsumer intConsumer(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1);
            }
        };
    }

    public static <R> IntFunction<R> intFunction(PyCallable function) {
        return intFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <R> IntFunction<R> intFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (R) function.call(v1);
            }
        };
    }

    public static IntPredicate intPredicate(PyCallable function) {
        return intPredicate(ScriptContext.require(), function);
    }

    public static IntPredicate intPredicate(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Boolean.class, v1);
            }
        };
    }

    public static IntSupplier intSupplier(PyCallable function) {
        return intSupplier(ScriptContext.require(), function);
    }

    public static IntSupplier intSupplier(Script script, PyCallable function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Integer.class);
            }
        };
    }

    public static IntToDoubleFunction intToDoubleFunction(PyCallable function) {
        return intToDoubleFunction(ScriptContext.require(), function);
    }

    public static IntToDoubleFunction intToDoubleFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Double.class, v1);
            }
        };
    }

    public static IntToLongFunction intToLongFunction(PyCallable function) {
        return intToLongFunction(ScriptContext.require(), function);
    }

    public static IntToLongFunction intToLongFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Long.class, v1);
            }
        };
    }

    public static IntUnaryOperator intUnaryOperator(PyCallable function) {
        return intUnaryOperator(ScriptContext.require(), function);
    }

    public static IntUnaryOperator intUnaryOperator(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Integer.class, v1);
            }
        };
    }

    public static LongBinaryOperator longBinaryOperator(PyCallable function) {
        return longBinaryOperator(ScriptContext.require(), function);
    }

    public static LongBinaryOperator longBinaryOperator(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Long.class, v1, v2);
            }
        };
    }

    public static LongConsumer longConsumer(PyCallable function) {
        return longConsumer(ScriptContext.require(), function);
    }

    public static LongConsumer longConsumer(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1);
            }
        };
    }

    public static <R> LongFunction<R> longFunction(PyCallable function) {
        return longFunction(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <R> LongFunction<R> longFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (R) function.call(v1);
            }
        };
    }

    public static LongPredicate longPredicate(PyCallable function) {
        return longPredicate(ScriptContext.require(), function);
    }

    public static LongPredicate longPredicate(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Boolean.class, v1);
            }
        };
    }

    public static LongSupplier longSupplier(PyCallable function) {
        return longSupplier(ScriptContext.require(), function);
    }

    public static LongSupplier longSupplier(Script script, PyCallable function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Long.class);
            }
        };
    }

    public static LongToDoubleFunction longToDoubleFunction(PyCallable function) {
        return longToDoubleFunction(ScriptContext.require(), function);
    }

    public static LongToDoubleFunction longToDoubleFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Double.class, v1);
            }
        };
    }

    public static LongToIntFunction longToIntFunction(PyCallable function) {
        return longToIntFunction(ScriptContext.require(), function);
    }

    public static LongToIntFunction longToIntFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Integer.class, v1);
            }
        };
    }

    public static LongUnaryOperator longUnaryOperator(PyCallable function) {
        return longUnaryOperator(ScriptContext.require(), function);
    }

    public static LongUnaryOperator longUnaryOperator(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Long.class, v1);
            }
        };
    }

    public static <T> ObjDoubleConsumer<T> objDoubleConsumer(PyCallable function) {
        return objDoubleConsumer(ScriptContext.require(), function);
    }

    public static <T> ObjDoubleConsumer<T> objDoubleConsumer(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1, v2);
            }
        };
    }

    public static <T> ObjIntConsumer<T> objIntConsumer(PyCallable function) {
        return objIntConsumer(ScriptContext.require(), function);
    }

    public static <T> ObjIntConsumer<T> objIntConsumer(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1, v2);
            }
        };
    }

    public static <T> ObjLongConsumer<T> objLongConsumer(PyCallable function) {
        return objLongConsumer(ScriptContext.require(), function);
    }

    public static <T> ObjLongConsumer<T> objLongConsumer(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                function.call(v1, v2);
            }
        };
    }

    public static <T> Predicate<T> predicate(PyCallable function) {
        return predicate(ScriptContext.require(), function);
    }

    public static <T> Predicate<T> predicate(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Boolean.class, v1);
            }
        };
    }

    public static <T> Supplier<T> supplier(PyCallable function) {
        return supplier(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<T> supplier(Script script, PyCallable function) {
        return () -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (T) function.call();
            }
        };
    }

    public static <T, U> ToDoubleBiFunction<T, U> toDoubleBiFunction(PyCallable function) {
        return toDoubleBiFunction(ScriptContext.require(), function);
    }

    public static <T, U> ToDoubleBiFunction<T, U> toDoubleBiFunction(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Double.class, v1, v2);
            }
        };
    }

    public static <T> ToDoubleFunction<T> toDoubleFunction(PyCallable function) {
        return toDoubleFunction(ScriptContext.require(), function);
    }

    public static <T> ToDoubleFunction<T> toDoubleFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Double.class, v1);
            }
        };
    }

    public static <T, U> ToIntBiFunction<T, U> toIntBiFunction(PyCallable function) {
        return toIntBiFunction(ScriptContext.require(), function);
    }

    public static <T, U> ToIntBiFunction<T, U> toIntBiFunction(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Integer.class, v1, v2);
            }
        };
    }

    public static <T> ToIntFunction<T> toIntFunction(PyCallable function) {
        return toIntFunction(ScriptContext.require(), function);
    }

    public static <T> ToIntFunction<T> toIntFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Integer.class, v1);
            }
        };
    }

    public static <T, U> ToLongBiFunction<T, U> toLongBiFunction(PyCallable function) {
        return toLongBiFunction(ScriptContext.require(), function);
    }

    public static <T, U> ToLongBiFunction<T, U> toLongBiFunction(Script script, PyCallable function) {
        return (v1, v2) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Long.class, v1, v2);
            }
        };
    }

    public static <T> ToLongFunction<T> toLongFunction(PyCallable function) {
        return toLongFunction(ScriptContext.require(), function);
    }

    public static <T> ToLongFunction<T> toLongFunction(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return function.callAs(Long.class, v1);
            }
        };
    }

    public static <T> UnaryOperator<T> unaryOperator(PyCallable function) {
        return unaryOperator(ScriptContext.require(), function);
    }

    @SuppressWarnings("unchecked")
    public static <T> UnaryOperator<T> unaryOperator(Script script, PyCallable function) {
        return (v1) -> {
            try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
                return (T) function.call(v1);
            }
        };
    }
}
