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

package dev.magicmq.pyspigot.bungee.event.custom;

import dev.magicmq.pyspigot.bungee.event.ScriptEvent;
import dev.magicmq.pyspigot.util.ScriptContext;
import net.md_5.bungee.api.plugin.Cancellable;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * A custom event that scripts may instantiate and call for other plugins/scripts to listen to.
 */
public class CustomEvent extends ScriptEvent implements Cancellable {

    private final String name;
    private final PyObject data;

    private boolean cancelled;

    /**
     * <p>
     * <b>Note:</b> This class should be instantiated from scripts only!
     * @param name The name of the event being created. Can be used to create subtypes of the generic custom event
     * @param data The data to attach to the event
     */
    public CustomEvent(String name, PyObject data) {
        super(ScriptContext.require());
        this.name = name;
        this.data = data;

        cancelled = false;
    }

    /**
     * Get the name of this event.
     * @return The name of this event
     */
    public String getName() {
        return name;
    }

    /**
     * Get the data attached to this event.
     * @return The data attached to this event
     */
    public PyObject getData() {
        return data;
    }

    /**
     * Attempt to convert the data attached to this event to a provided type.
     * @param clazz The type that the data should be converted to
     * @return An object of the specified type representing the converted data
     * @throws org.python.core.PyException If the data could not be converted to the provided type
     */
    public Object getDataAsType(String clazz) {
        return Py.tojava(data, clazz);
    }

    /**
     * Attempt to convert the data attached to this event to a provided type.
     * @param clazz The type that the data should be converted to
     * @return An object of the specified type representing the converted data
     * @param <T> The type to which the data should be converted
     * @throws org.python.core.PyException If the data could not be converted to the provided type
     */
    public <T> T getDataAsType(Class<T> clazz) {
        return Py.tojava(data, clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

