/*
 *    Copyright 2023 magicmq
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

package dev.magicmq.pyspigot.manager.listener;

import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

/**
 * Represents an event executor for script event listeners.
 * @see org.bukkit.plugin.EventExecutor
 */
public class ScriptEventExecutor implements EventExecutor {

    private final ScriptEventListener scriptEventListener;

    /**
     *
     * @param scriptEventListener The {@link ScriptEventListener} associated with this ScriptEventExecutor
     */
    public ScriptEventExecutor(ScriptEventListener scriptEventListener) {
        this.scriptEventListener = scriptEventListener;
    }

    /**
     * Called internally when the event occurs.
     * @param listener The listener associated with this EventExecutor
     * @param event The event that occurred
     */
    public void execute(Listener listener, Event event) {
        try {
            PyObject parameter = Py.java2py(event);
            scriptEventListener.getListenerFunction().__call__(parameter);
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(scriptEventListener.getScript(), exception, "Error when executing event listener");
        }
    }
}
