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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class VelocityAsyncScriptListener<E> extends VelocityScriptListener<E> implements AwaitingEventExecutor<E> {

    private final EventTaskType eventTaskType;

    public VelocityAsyncScriptListener(Script script, PyFunction listenerFunction, Class<E> event, EventTaskType eventTaskType) {
        super(script, listenerFunction, event);
        this.eventTaskType = eventTaskType;
    }

    @Override
    public @Nullable EventTask executeAsync(E event) {
        if (event instanceof ScriptExceptionEvent scriptExceptionEvent) {
            Script eventScript = scriptExceptionEvent.getScript();
            if (eventScript.equals(script)) {
                String listenerFunctionName = listenerFunction.__code__.co_name;
                String exceptionFunctionName = scriptExceptionEvent.getException().traceback.tb_frame.f_code.co_name;
                if (listenerFunctionName.equals(exceptionFunctionName)) {
                    return null;
                }
            }
        }

        if (eventTaskType == EventTaskType.ASYNC) {
            return EventTask.async(() -> {
                try {
                    Py.setSystemState(script.getInterpreter().getSystemState());
                    ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                    PyObject parameter = Py.java2py(event);
                    listenerFunction.__call__(threadState, parameter);
                } catch (PyException exception) {
                    ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
                }
            });
        } else if (eventTaskType == EventTaskType.CONTINUATION) {
            return EventTask.withContinuation((continuation) -> {
                try {
                    Py.setSystemState(script.getInterpreter().getSystemState());
                    ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                    PyObject[] parameters = Py.javas2pys(event, continuation);
                    listenerFunction.__call__(threadState, parameters[0], parameters[1]);
                } catch (PyException exception) {
                    ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
                    continuation.resumeWithException(exception);
                }
            });
        } else if (eventTaskType == EventTaskType.RESUME_WHEN_COMPLETE) {
            return EventTask.resumeWhenComplete(CompletableFuture.runAsync(() -> {
                try {
                    Py.setSystemState(script.getInterpreter().getSystemState());
                    ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                    PyObject parameter = Py.java2py(event);
                    listenerFunction.__call__(threadState, parameter);
                } catch (PyException exception) {
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
