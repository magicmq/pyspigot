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

import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.python.core.PyFunction;

import java.util.Collection;
import java.util.HashMap;

/**
 * A dummy listener object that represents all events a script is currently listening to.
 */
public class DummyListener implements Listener {

    private final Script script;
    private final HashMap<PyFunction, Class<? extends Event>> events;

    /**
     *
     * @param script The script listening to events within this listener
     */
    public DummyListener(Script script) {
        this.script = script;
        this.events = new HashMap<>();
    }

    /**
     * Get the script associated with this listener.
     * @return The script associated with this listener.
     */
    public Script getScript() {
        return script;
    }

    /**
     * Add a new event that should be listened for
     * @param function The function that should be called when the event occurs
     * @param event The type of event being listened to
     */
    public void addEvent(PyFunction function, Class<? extends Event> event) {
        events.put(function, event);
    }

    /**
     * Stop listening to an event.
     * @param function The function for the event listener that should be removed
     * @return The type of event that is no longer being listened to, null if no listener was stopped
     */
    public Class<? extends Event> removeEvent(PyFunction function) {
        return events.remove(function);
    }

    /**
     * Check if this listener contains a type of event
     * @param event The type of event to check
     * @return True if this listener is listening to the type of event, false if otherwise
     */
    public boolean containsEvent(Class<? extends Event> event) {
        return events.containsValue(event);
    }

    /**
     * Get all types of events this listener is currently listening to.
     * @return An immutable Collection containing all events this listener is currently listening to
     */
    public Collection<Class<? extends Event>> getEvents() {
        return events.values();
    }

    /**
     * Check if this listener is currently listening to zero events.
     * @return True if this listener is listening to zero events, false if otherwise
     */
    public boolean isEmpty() {
        return events.size() == 0;
    }
}
