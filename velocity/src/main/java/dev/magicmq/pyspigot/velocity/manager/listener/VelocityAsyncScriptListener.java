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

package dev.magicmq.pyspigot.velocity.manager.listener;


import com.velocitypowered.api.event.AwaitingEventExecutor;
import com.velocitypowered.api.event.EventTask;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.velocity.event.ScriptExceptionEvent;
import jep.JepException;
import jep.python.PyCallable;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * An asynchronous Velocity listener registered by a script.
 * @param <E> The Velocity Event class, representing the event being listened to
 */
public class VelocityAsyncScriptListener<E> extends VelocityScriptListener<E> implements AwaitingEventExecutor<E> {

    private final EventTaskType eventTaskType;

    public VelocityAsyncScriptListener(Script script, PyCallable listenerFunction, Class<E> event, EventTaskType eventTaskType) {
        super(script, listenerFunction, event);
        this.eventTaskType = eventTaskType;
    }

    @Override
    public @Nullable EventTask executeAsync(E event) {
        if (event instanceof ScriptExceptionEvent scriptExceptionEvent) {
            Script eventScript = scriptExceptionEvent.getScript();
            if (eventScript.equals(script)) {
                //TODO Handle if ScriptExceptionEvent fired as a result of an exception in this listener
            }
        }

        if (eventTaskType == EventTaskType.ASYNC) {
            return EventTask.async(() -> {
                try {
                    ScriptManager.get().getInterpreter().call(script, () -> listenerFunction.call(event));
                } catch (JepException exception) {
                    ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
                }
            });
        } else if (eventTaskType == EventTaskType.CONTINUATION) {
            return EventTask.withContinuation((continuation) -> {
                try {
                    ScriptManager.get().getInterpreter().call(script, () -> listenerFunction.call(event, continuation));
                } catch (JepException exception) {
                    ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
                    continuation.resumeWithException(exception);
                }
            });
        } else if (eventTaskType == EventTaskType.RESUME_WHEN_COMPLETE) {
            return EventTask.resumeWhenComplete(CompletableFuture.runAsync(() -> {
                try {
                    ScriptManager.get().getInterpreter().call(script, () -> listenerFunction.call(event));
                } catch (JepException exception) {
                    ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
                    throw new CompletionException(exception);
                }
            }));
        } else {
            return null;
        }
    }

    /**
     * Prints a representation of this VelocityAsyncScriptListener in string format, including the event being listened to by the listener
     * @return A string representation of the VelocityAsyncScriptListener
     */
    @Override
    public String toString() {
        return String.format("VelocityAsyncScriptListener[Event: %s, Task Type: %s]", event.getName(), eventTaskType.name());
    }
}
