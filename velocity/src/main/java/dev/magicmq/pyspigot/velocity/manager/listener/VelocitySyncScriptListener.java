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

package dev.magicmq.pyspigot.velocity.manager.listener;


import com.velocitypowered.api.event.EventHandler;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.velocity.event.ScriptExceptionEvent;
import jep.JepException;
import jep.python.PyCallable;

/**
 * A synchronous Velocity listener registered by a script.
 * @param <E> The Velocity Event class, representing the event being listened to
 */
public class VelocitySyncScriptListener<E> extends VelocityScriptListener<E> implements EventHandler<E> {

    public VelocitySyncScriptListener(Script script, PyCallable listenerFunction, Class<E> event) {
        super(script, listenerFunction, event);
    }

    @Override
    public void execute(E event) {
        if (event instanceof ScriptExceptionEvent scriptExceptionEvent) {
            Script eventScript = scriptExceptionEvent.getScript();
            if (eventScript.equals(script)) {
                //TODO Handle if ScriptExceptionEvent fired as a result of an exception in this listener
            }
        }

        try {
            ScriptManager.get().getInterpreter().call(script, () -> listenerFunction.call(event));
        } catch (JepException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when executing event listener");
        }
    }

    /**
     * Prints a representation of this VelocitySyncScriptListener in string format, including the event being listened to by the listener
     * @return A string representation of the VelocityAsyncScriptListener
     */
    @Override
    public String toString() {
        return String.format("VelocitySyncScriptListener[Event: %s]", event.getName());
    }
}
