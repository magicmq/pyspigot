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


import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import dev.magicmq.pyspigot.velocity.PyVelocity;
import org.python.core.PyFunction;

public class VelocityListenerManager extends ListenerManager<VelocityScriptListener<?>, Object, Short> {

    private static VelocityListenerManager instance;

    private VelocityListenerManager() {
        super();
    }

    /**
     * Register a new asynchronous event listener with default priority and given type of {@link com.velocitypowered.api.event.EventTask}.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param eventTaskType The type of {@link com.velocitypowered.api.event.EventTask} to use in the asynchronous event executor
     * @return The ScriptEventListener that was registered
     */
    public VelocityScriptListener<?> registerAsyncListener(PyFunction function, Class<?> eventClass, EventTaskType eventTaskType) {
        return registerAsyncListener(function, eventClass, (short) 0, eventTaskType);
    }

    /**
     * Register a new asynchronous event listener with default priority and given type of {@link com.velocitypowered.api.event.EventTask}.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the event occurs
     * @param eventClass The type of event to listen to
     * @param priority The priority of the event relative to other listeners
     * @param eventTaskType The type of {@link com.velocitypowered.api.event.EventTask} to use in the asynchronous event executor
     * @return The ScriptEventListener that was registered
     */
    public VelocityScriptListener<?> registerAsyncListener(PyFunction function, Class<?> eventClass, Short priority, EventTaskType eventTaskType) {
        Script script = ScriptUtils.getScriptFromCallStack();

        return registerTypedAsyncListener(script, function, eventClass, priority, eventTaskType);
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Velocity events do not support ignoreCancelled, so this method will not work. Instead, use {@link VelocityListenerManager#registerAsyncListener(PyFunction, Class, EventTaskType)}
     * @throws UnsupportedOperationException always
     */
    public VelocityScriptListener<?> registerAsyncListener(PyFunction function, Class<?> eventClass, boolean ignoreCancelled, EventTaskType eventTaskType) {
        throw new UnsupportedOperationException("Velocity does not support ignoreCancelled for event listeners.");
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Velocity events do not support ignoreCancelled, so this method will not work. Instead, use {@link VelocityListenerManager#registerAsyncListener(PyFunction, Class, Short, EventTaskType)}
     * @throws UnsupportedOperationException always
     */
    public VelocityScriptListener<?> registerAsyncListener(PyFunction function, Class<?> eventClass, Short priority, boolean ignoreCancelled, EventTaskType eventTaskType) {
        throw new UnsupportedOperationException("Velocity does not support ignoreCancelled for event listeners.");
    }

    @Override
    public VelocityScriptListener<?> registerListener(PyFunction function, Class<?> eventClass) {
        return registerListener(function, eventClass, (short) 0);
    }

    @Override
    public VelocityScriptListener<?> registerListener(PyFunction function, Class<?> eventClass, Short priority) {
        Script script = ScriptUtils.getScriptFromCallStack();

        return registerTypedListener(script, function, eventClass, priority);
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Velocity events do not support ignoreCancelled, so this method will not work. Instead, use {@link VelocityListenerManager#registerListener(PyFunction, Class)}
     * @throws UnsupportedOperationException always
     */
    @Override
    public VelocityScriptListener<?> registerListener(PyFunction function, Class<?> eventClass, boolean ignoreCancelled) {
        throw new UnsupportedOperationException("Velocity does not support ignoreCancelled for event listeners.");
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Velocity events do not support ignoreCancelled, so this method will not work. Instead, use {@link VelocityListenerManager#registerListener(PyFunction, Class, Short)}
     * @throws UnsupportedOperationException always
     */
    @Override
    public VelocityScriptListener<?> registerListener(PyFunction function, Class<?> eventClass, Short priority, boolean ignoreCancelled) {
        throw new UnsupportedOperationException("Velocity does not support ignoreCancelled for event listeners.");
    }

    @Override
    public void unregisterListener(VelocityScriptListener<?> listener) {
        PyVelocity.get().getProxy().getEventManager().unregisterListener(PyVelocity.get(), listener);
        removeListener(listener.getScript(), listener);
    }

    @Override
    public void unregisterListeners(Script script) {
        for (VelocityScriptListener<?> listener : getListeners(script)) {
            PyVelocity.get().getProxy().getEventManager().unregisterListener(PyVelocity.get(), listener);
        }
        removeListeners(script);
    }

    private <T> VelocitySyncScriptListener<T> registerTypedListener(Script script, PyFunction function, Class<T> eventClass, Short priority) {
        VelocitySyncScriptListener<T> listener = new VelocitySyncScriptListener<>(script, function, eventClass);
        PyVelocity.get().getProxy().getEventManager().register(PyVelocity.get(), eventClass, priority, listener);
        addListener(script, listener);
        return listener;
    }

    private <T> VelocityAsyncScriptListener<T> registerTypedAsyncListener(Script script, PyFunction function, Class<T> eventClass, Short priority, EventTaskType eventTaskType) {
        VelocityAsyncScriptListener<T> listener = new VelocityAsyncScriptListener<>(script, function, eventClass, eventTaskType);
        PyVelocity.get().getProxy().getEventManager().register(PyVelocity.get(), eventClass, priority, listener);
        addListener(script, listener);
        return listener;
    }

    /**
     * Get the singleton instance of this VelocityListenerManager.
     * @return The instance
     */
    public static VelocityListenerManager get() {
        if (instance == null)
            instance = new VelocityListenerManager();
        return instance;
    }
}
