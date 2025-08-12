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

/**
 * A listener registered by a script. Meant to be implemented by platform-specific classes that make use of a platform API's listener framework.
 * @param <T> The platform-specific Event class
 */
public interface ScriptEventListener<T> {

    /**
     * Get the script associated with this listener.
     * @return The script associated with this listener
     */
    Script getScript();

    /**
     * Get the script function that should be called when the event occurs.
     * @return The script function that should be called when the event occurs
     */
    PyFunction getListenerFunction();

    /**
     * Get the platform-specific event class associated with this listener.
     * <p>
     * Note: Because of the way scripts register events, this will be a {@link Class} object, which essentially represents the type of event being listened to.
     * @return The platform-specific event class associated with this listener
     */
    Class<? extends T> getEvent();

}
