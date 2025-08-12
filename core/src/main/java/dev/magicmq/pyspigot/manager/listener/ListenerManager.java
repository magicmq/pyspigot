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

package dev.magicmq.pyspigot.manager.listener;

import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract manager to interface with a server-specific event framework. Primarily used by scripts to register and unregister event listeners.
 * @param <T> The platform-specific Listener class
 * @param <S> The platform-specific Event class
 * @param <U> The platform-specific EventPriority class
 */
public abstract class ListenerManager<T, S, U> {

    private static ListenerManager<?, ?, ?> instance;

    private final HashMap<Script, List<T>> registeredListeners;

    protected ListenerManager() {
        instance = this;

        registeredListeners = new HashMap<>();
    }

    /**
     * Register a new event listener with default priority.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @return The ScriptEventListener that was registered
     */
    public abstract T registerListener(PyFunction function, Class<? extends S> eventClass);

    /**
     * Register a new event listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param priority The priority of the event relative to other listeners
     * @return The ScriptEventListener that was registered
     */
    public abstract T registerListener(PyFunction function, Class<? extends S> eventClass, U priority);

    /**
     * Register a new event listener with default priority.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param ignoreCancelled If true, the event listener will not be called if the event has been previously cancelled by another listener.
     * @return The ScriptEventListener that was registered
     */
    public abstract T registerListener(PyFunction function, Class<? extends S> eventClass, boolean ignoreCancelled);

    /**
     * Register a new event listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param priority The priority of the event relative to other listeners
     * @param ignoreCancelled If true, the event listener will not be called if the event has been previously cancelled by another listener.
     * @return The ScriptEventListener that was registered
     */
    public abstract T registerListener(PyFunction function, Class<? extends S> eventClass, U priority, boolean ignoreCancelled);

    /**
     * Unregister an event listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param listener The listener to unregister
     */
    public abstract void unregisterListener(T listener);

    /**
     * Unregister all event listeners belonging to a script.
     * @param script The script whose event listeners should be unregistered
     */
    public abstract void unregisterListeners(Script script);

    /**
     * Get all event listeners for a particular event associated with a script
     * @param script The script
     * @param eventClass The event
     * @return An immutable List of listeners associated with the script and event. Will return an empty list if there are no event listeners for the particular event associated with the script
     */
    public abstract List<T> getListeners(Script script, Class<? extends S> eventClass);

    /**
     * Get all event listeners associated with a script
     * @param script The script to get event listeners from
     * @return An immutable List of listeners containing all event listeners associated with the script. Will return an empty list if there are no event listeners associated with the script
     */
    public List<T> getListeners(Script script) {
        List<T> scriptListeners = registeredListeners.get(script);
        return scriptListeners != null ? List.copyOf(scriptListeners) : List.of();
    }

    protected void addListener(Script script, T listener) {
        if (registeredListeners.containsKey(script))
            registeredListeners.get(script).add(listener);
        else {
            List<T> scriptListeners = new ArrayList<>();
            scriptListeners.add(listener);
            registeredListeners.put(script, scriptListeners);
        }
    }

    protected void removeListener(Script script, T listener) {
        List<T> scriptListeners = registeredListeners.get(script);
        scriptListeners.remove(listener);
        if (scriptListeners.isEmpty())
            registeredListeners.remove(script);
    }

    protected void removeListeners(Script script) {
        registeredListeners.remove(script);
    }

    /**
     * Get the singleton instance of this ListenerManager.
     * @return The instance
     */
    public static ListenerManager<?, ?, ?> get() {
        return instance;
    }

}
