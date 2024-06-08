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
import org.graalvm.polyglot.Value;

/**
 * A dummy listener object that represents all events a script is currently listening to.
 * @see org.bukkit.event.Listener
 */
public class ScriptEventListener implements Listener {

    private final Script script;
    private final Value listenerFunction;
    private final Class<? extends Event> event;
    private final ScriptEventExecutor eventExecutor;

    /**
     *
     * @param script The script listening to events within this listener
     * @param listenerFunction The script function that should be called when the event occurs
     * @param event The Bukkit event associated with this listener. Should be a {@link Class} of the Bukkit event
     */
    public ScriptEventListener(Script script, Value listenerFunction, Class<? extends Event> event) {
        this.script = script;
        this.listenerFunction = listenerFunction;
        this.event = event;
        this.eventExecutor = new ScriptEventExecutor(this, event);
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
    public Value getListenerFunction() {
        return listenerFunction;
    }

    /**
     * Get the Bukkit event associated with this listener.
     * <p>
     * Note: Because of the way scripts register events, this will be a {@link Class} of the Bukkit event, which essentially represents its type.
     * @return The Bukkit event associated with this listener.
     */
    public Class<? extends Event> getEvent() {
        return event;
    }

    /**
     * Get the {@link ScriptEventExecutor} associated with this script event listener.
     * @return The {@link ScriptEventExecutor} associated with this script event listener
     */
    public ScriptEventExecutor getEventExecutor() {
        return eventExecutor;
    }

    /**
     * Prints a representation of this ScriptEventListener in string format, including the event being listened to by the listener
     * @return A string representation of the ScriptEventListener
     */
    @Override
    public String toString() {
        return String.format("ScriptEventListener[Event: %s]", event.getName());
    }
}
