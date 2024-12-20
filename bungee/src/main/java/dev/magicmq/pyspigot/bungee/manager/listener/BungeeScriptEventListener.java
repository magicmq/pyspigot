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
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;

public class BungeeScriptEventListener implements Listener {

    private final Script script;
    private final PyFunction listenerFunction;
    private final Class<? extends Event> event;

    public BungeeScriptEventListener(Script script, PyFunction listenerFunction, Class<? extends Event> event) {
        this.script = script;
        this.listenerFunction = listenerFunction;
        this.event = event;
    }

    /**
     * Called internally when the event occurs.
     * @param event The event that occurred
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEvent(Event event) {
        if (this.event.isAssignableFrom(event.getClass())) {
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
        }

        try {
            PyObject parameter = Py.java2py(event);
            listenerFunction.__call__(parameter);
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
        }
    }

    /**
     * Get the script associated with this listener.
     * @return The script associated with this listener.
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the script function that should be called when the event occurs.
     * @return The script function that should be called when the event occurs
     */
    public PyFunction getListenerFunction() {
        return listenerFunction;
    }

    /**
     * Get the BungeeCord event associated with this listener.
     * <p>
     * Note: Because of the way scripts register events, this will be a {@link Class} of the BungeeCord event, which essentially represents its type.
     * @return The Bukkit event associated with this listener.
     */
    public Class<? extends Event> getEvent() {
        return event;
    }

    /**
     * Prints a representation of this BungeeScriptEventListener in string format, including the event being listened to by the listener
     * @return A string representation of the ScriptEventListener
     */
    @Override
    public String toString() {
        return String.format("ScriptEventListener[Event: %s]", event.getName());
    }
}
