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

import dev.magicmq.pyspigot.manager.listener.ScriptEventListener;
import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.python.core.PyFunction;

/**
 * A dummy Bukkit Listener that holds an event a script is currently listening to.
 * @see org.bukkit.event.Listener
 */
public class BukkitScriptEventListener implements Listener, ScriptEventListener<Event> {

    private final Script script;
    private final PyFunction listenerFunction;
    private final Class<? extends Event> event;
    private final BukkitScriptEventExecutor eventExecutor;

    /**
     *
     * @param script The script listening to events within this listener
     * @param listenerFunction The script function that should be called when the event occurs
     * @param event The Bukkit event associated with this listener. Should be a {@link Class} of the Bukkit event
     */
    public BukkitScriptEventListener(Script script, PyFunction listenerFunction, Class<? extends Event> event) {
        this.script = script;
        this.listenerFunction = listenerFunction;
        this.event = event;
        this.eventExecutor = new BukkitScriptEventExecutor(this, event);
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
     * Get the {@link BukkitScriptEventExecutor} associated with this script event listener.
     * @return The {@link BukkitScriptEventExecutor} associated with this script event listener
     */
    public BukkitScriptEventExecutor getEventExecutor() {
        return eventExecutor;
    }

    /**
     * Prints a representation of this BukkitScriptEventListener in string format, including the event being listened to by the listener
     * @return A string representation of the ScriptEventListener
     */
    @Override
    public String toString() {
        return String.format("BukkitScriptEventListener[Event: %s]", event.getName());
    }
}
