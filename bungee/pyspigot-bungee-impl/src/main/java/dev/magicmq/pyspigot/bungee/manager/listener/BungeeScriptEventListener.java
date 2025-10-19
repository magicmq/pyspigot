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

package dev.magicmq.pyspigot.bungee.manager.listener;

import dev.magicmq.pyspigot.bungee.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.manager.listener.ScriptEventListener;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.ScriptContext;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

/**
 * A dummy BungeeCord Listener that holds an event a script is currently listening to.
 * @see net.md_5.bungee.api.plugin.Listener
 */
public class BungeeScriptEventListener implements Listener, ScriptEventListener<Event> {

    private final Script script;
    private final PyFunction listenerFunction;
    private final Class<? extends Event> event;
    private final byte priority;

    /**
     *
     * @param script The script listening to events within this listener
     * @param listenerFunction The script function that should be called when the event occurs
     * @param event The BungeeCord event associated with this listener. Should be a {@link Class} of the BungeeCord event
     * @param priority The priority of this event listener
     */
    public BungeeScriptEventListener(Script script, PyFunction listenerFunction, Class<? extends Event> event, byte priority) {
        this.script = script;
        this.listenerFunction = listenerFunction;
        this.event = event;
        this.priority = priority;
    }

    /**
     * Called internally when the event occurs.
     * @param event The event that occurred
     */
    public void callToScript(Object event) {
        if (event instanceof ScriptExceptionEvent scriptExceptionEvent) {
            Script eventScript = scriptExceptionEvent.getScript();
            if (eventScript.equals(script)) {
                String listenerFunctionName = listenerFunction.__code__.co_name;
                String exceptionFunctionName = scriptExceptionEvent.getException().traceback.tb_frame.f_code.co_name;
                if (listenerFunctionName.equals(exceptionFunctionName)) {
                    return;
                }
            }
        }

        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject parameter = Py.java2py(event);
            ScriptContext.runWith(script, () -> listenerFunction.__call__(threadState, parameter));
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
        }
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public PyFunction getListenerFunction() {
        return listenerFunction;
    }

    @Override
    public Class<? extends Event> getEvent() {
        return event;
    }

    /**
     * Get the priority of this listener.
     * @return The priority of this listener
     */
    public byte getPriority() {
        return priority;
    }

    /**
     * Prints a representation of this BungeeScriptEventListener in string format, including the event being listened to by the listener
     * @return A string representation of the ScriptEventListener
     */
    @Override
    public String toString() {
        return String.format("BungeeScriptEventListener[Event: %s]", event.getName());
    }
}
