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

package dev.magicmq.pyspigot.velocity.event.custom;


import com.velocitypowered.api.event.ResultedEvent;
import dev.magicmq.pyspigot.util.ScriptContext;
import dev.magicmq.pyspigot.velocity.event.ScriptEvent;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 * A custom resulted event that scripts may instantiate and call for other plugins/scripts to listen to.
 * <p>
 * This event is an extension of Velocity's {@link com.velocitypowered.api.event.ResultedEvent}, and should be used
 * instead of {@link dev.magicmq.pyspigot.velocity.event.custom.CustomEvent} in cases when an event has some sort of
 * actionable result or outcome.
 */
public class CustomResultedEvent extends ScriptEvent implements ResultedEvent<ResultedEvent.GenericResult> {

    private final String name;
    private final PyObject data;

    private GenericResult result = GenericResult.allowed();

    /**
     * <p>
     * <b>Note:</b> This class should be instantiated from scripts only!
     * @param name The name of the event being created. Can be used to create subtypes of the generic custom event
     * @param data The data to attach to the event
     */
    public CustomResultedEvent(String name, PyObject data) {
        super(ScriptContext.require());
        this.name = name;
        this.data = data;
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

    @Override
    public GenericResult getResult() {
        return result;
    }

    @Override
    public void setResult(GenericResult result) {
        this.result = result;
    }
}
