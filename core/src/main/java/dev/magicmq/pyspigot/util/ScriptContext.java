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

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.function.Supplier;

public final class ScriptContext {

    private static final ThreadLocal<Deque<Script>> threadLocal = ThreadLocal.withInitial(ArrayDeque::new);

    private ScriptContext() {}

    public static Script current() {
        Deque<Script> stack = threadLocal.get();
        return stack.peek();
    }

    public static Script require() {
        Script script = current();
        if (script == null)
            throw new IllegalArgumentException("No script context available on this thread. Was PySpigot called from a thread that it is unaware of?");
        return script;
    }

    public static Scope enter(Script script) {
        threadLocal.get().push(script);
        return new Scope();
    }

    public static void runWith(Script script, Runnable runnable) {
        try (Scope ignored = enter(script)) {
            runnable.run();
        }
    }

    public static <V> V supplyWith(Script script, Supplier<V> supplier) {
        try (Scope ignored = enter(script)) {
            return supplier.get();
        }
    }

    public static final class Scope implements AutoCloseable {

        private boolean closed;

        @Override
        public void close() {
            if (!closed) {
                Deque<Script> stack = threadLocal.get();
                stack.pop();
                closed = true;
            }
        }
    }
}
