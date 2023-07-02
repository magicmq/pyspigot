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

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.python.core.PyBaseCode;
import org.python.core.PyFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager to interface with Bukkit's event framework. Primarily used by scripts to register and unregister event listeners.
 */
public class ListenerManager {

    private static ListenerManager manager;

    private final List<ScriptEventListener> registeredListeners;

    private ListenerManager() {
        registeredListeners = new ArrayList<>();
    }

    /**
     * Register a new event listener with default priority.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @return The ScriptEventListener that was registered
     */
    public ScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass) {
        return registerListener(function, eventClass, "NORMAL", false);
    }

    /**
     * Register a new event listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param priorityString The priority of the event relative to other listeners, should be a string of {@link org.bukkit.event.EventPriority}
     * @return The ScriptEventListener that was registered
     */
    public ScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, String priorityString) {
        return registerListener(function, eventClass, priorityString, false);
    }

    /**
     * Register a new event listener with default priority.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param ignoreCancelled If true, the event listener will not be called if the event has been previously cancelled by another listener.
     * @return The ScriptEventListener that was registered
     */
    public ScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, boolean ignoreCancelled) {
        return registerListener(function, eventClass, "NORMAL", ignoreCancelled);
    }

    /**
     * Register a new event listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param priorityString The priority of the event relative to other listeners, should be a string of {@link org.bukkit.event.EventPriority}
     * @param ignoreCancelled If true, the event listener will not be called if the event has been previously cancelled by another listener.
     * @return The ScriptEventListener that was registered
     */
    public ScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, String priorityString, boolean ignoreCancelled) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        ScriptEventListener listener = getEventListener(script, eventClass);
        if (listener == null) {
            listener = new ScriptEventListener(script, function, eventClass);
            EventPriority priority = EventPriority.valueOf(priorityString);
            Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, listener.getEventExecutor(), PySpigot.get(), ignoreCancelled);
            registeredListeners.add(listener);
            return listener;
        } else {
            throw new UnsupportedOperationException("Script " + script.getName() + " already has an event listener for " + eventClass.getSimpleName() + " registered");
        }
    }

    /**
     * Unregister an event listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param listener The listener to unregister
     */
    public void unregisterListener(ScriptEventListener listener) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        removeFromHandlers(listener);
        registeredListeners.remove(listener);
    }

    /**
     * Get all event listeners associated with a script
     * @param script The script to get event listeners from
     * @return A List of {@link ScriptEventListener} containing all events associated with the script. Will return an empty list if there are no event listeners associated with the script
     */
    public List<ScriptEventListener> getListeners(Script script) {
        List<ScriptEventListener> toReturn = new ArrayList<>();
        for (ScriptEventListener listener : registeredListeners) {
            if (listener.getScript().equals(script))
                toReturn.add(listener);
        }
        return toReturn;
    }

    /**
     * Get the event listener for a particular event associated with a script
     * @param script The script
     * @param eventClass The event
     * @return The {@link ScriptEventListener} associated with the script and event, null if there is none
     */
    public ScriptEventListener getEventListener(Script script, Class<? extends Event> eventClass) {
        for (ScriptEventListener listener : registeredListeners) {
            if (listener.getScript().equals(script) && listener.getEvent().equals(eventClass))
                return listener;
        }
        return null;
    }

    /**
     * Unregister all event listeners belonging to a script.
     * @param script The script whose event listeners should be unregistered
     */
    public void unregisterListeners(Script script) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<ScriptEventListener> associatedListeners = getListeners(script);
        for (ScriptEventListener eventListener : associatedListeners) {
            unregisterListener(eventListener);
        }
    }

    private void removeFromHandlers(ScriptEventListener listener) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = listener.getEvent().getDeclaredMethod("getHandlerList");
        HandlerList list = (HandlerList) method.invoke(null);
        list.unregister(listener);
    }

    /**
     * Get the singleton instance of this ListenerManager.
     * @return The instance
     */
    public static ListenerManager get() {
        if (manager == null)
            manager = new ListenerManager();
        return manager;
    }
}
