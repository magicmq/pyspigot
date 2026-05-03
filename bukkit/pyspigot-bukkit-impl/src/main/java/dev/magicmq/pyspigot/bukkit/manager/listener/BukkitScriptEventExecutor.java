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

package dev.magicmq.pyspigot.bukkit.manager.listener;

import dev.magicmq.pyspigot.bukkit.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.ScriptContext;
import jep.JepException;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

/**
 * Represents a Bukkit event executor for script event listeners.
 * @see org.bukkit.plugin.EventExecutor
 */
public class BukkitScriptEventExecutor implements EventExecutor {

    private final BukkitScriptEventListener scriptEventListener;
    private final Class<? extends Event> eventClass;

    /**
     *
     * @param scriptEventListener The {@link BukkitScriptEventListener} associated with this ScriptEventExecutor
     * @param eventClass The Bukkit event associated with this ScriptEventExecutor. Should be a {@link Class} of the Bukkit event
     */
    public BukkitScriptEventExecutor(BukkitScriptEventListener scriptEventListener, Class<? extends Event> eventClass) {
        this.scriptEventListener = scriptEventListener;
        this.eventClass = eventClass;
    }

    /**
     * Called internally when the event occurs.
     * @param listener The listener associated with this EventExecutor
     * @param event The event that occurred
     */
    public void execute(Listener listener, Event event) {
        if (eventClass.isAssignableFrom(event.getClass())) {
            if (event instanceof ScriptExceptionEvent scriptExceptionEvent) {
                Script script = scriptExceptionEvent.getScript();
                if (scriptEventListener.getScript().equals(script)) {
                    //TODO Handle if ScriptExceptionEvent fired as a result of an exception in this listener
                }
            }

            try {
                //TODO Async
                ScriptContext.runWith(scriptEventListener.getScript(), () -> scriptEventListener.getListenerFunction().call(event));
            } catch (JepException exception) {
                ScriptManager.get().handleScriptException(scriptEventListener.getScript(), exception, "Error when executing event listener");
            }
        }
    }
}
