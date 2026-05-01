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

/**
 * A custom resulted event that scripts may instantiate and call for other plugins/scripts to listen to.
 * <p>
 * This event is an extension of Velocity's {@link com.velocitypowered.api.event.ResultedEvent}, and should be used
 * instead of {@link dev.magicmq.pyspigot.velocity.event.custom.CustomEvent} in cases when an event has some sort of
 * actionable result or outcome.
 */
public class CustomResultedEvent extends ScriptEvent implements ResultedEvent<ResultedEvent.GenericResult> {

    private final String name;
    private final Object data;

    private GenericResult result = GenericResult.allowed();

    /**
     * <p>
     * <b>Note:</b> This class should be instantiated from scripts only!
     * @param name The name of the event being created. Can be used to create subtypes of the generic custom event
     * @param data The data to attach to the event
     */
    public CustomResultedEvent(String name, Object data) {
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
    public Object getData() {
        return data;
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
