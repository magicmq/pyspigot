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

import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptContext;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.python.core.PyFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The Bukkit-specific implementation of the listener manager.
 */
public class BukkitListenerManager extends ListenerManager<BukkitScriptEventListener, Event, EventPriority> {

    private static BukkitListenerManager instance;

    private BukkitListenerManager() {
        super();
    }

    @Override
    public BukkitScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass) {
        return registerListener(function, eventClass, EventPriority.NORMAL, false);
    }

    @Override
    public BukkitScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, EventPriority priority) {
        return registerListener(function, eventClass, priority, false);
    }

    @Override
    public BukkitScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, boolean ignoreCancelled) {
        return registerListener(function, eventClass, EventPriority.NORMAL, ignoreCancelled);
    }

    @Override
    public BukkitScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, EventPriority priority, boolean ignoreCancelled) {
        Script script = ScriptContext.require();

        BukkitScriptEventListener listener = new BukkitScriptEventListener(script, function, eventClass);
        Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, listener.getEventExecutor(), PySpigot.get().getPlugin(), ignoreCancelled);
        addListener(script, listener);
        return listener;
    }

    @Override
    public void unregisterListener(BukkitScriptEventListener listener) {
        removeFromHandlers(listener);
        removeListener(listener.getScript(), listener);
    }

    public void unregisterListener(PyFunction function, Class<? extends Event> eventClass) {
        Script script = ScriptContext.require();
        List<BukkitScriptEventListener> listeners = getListeners(script, function, eventClass);
        for (BukkitScriptEventListener listener : listeners) {
            unregisterListener(listener);
        }
    }

    @Override
    public void unregisterListeners(Script script) {
        for (BukkitScriptEventListener eventListener : getListeners(script)) {
            removeFromHandlers(eventListener);
        }
        removeListeners(script);
    }

    private void removeFromHandlers(BukkitScriptEventListener listener) {
        try {
            Method method = getRegistrationClass(listener.getEvent()).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            HandlerList list = (HandlerList) method.invoke(null);
            list.unregister(listener);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ScriptRuntimeException(listener.getScript(), "Unhandled exception when unregistering listener for event '" + listener.getEvent().getSimpleName() + "'", e);
        }
    }

    //Copied from org.bukkit.plugin.SimplePluginManager#getRegistrationClass. Resolves getHandlerList for events, including those where getHandlerList is defined in a superclass (such as BlockBreakEvent)
    private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event '" + clazz.getName() + "'. Static getHandlerList method required!");
            }
        }
    }

    /**
     * Get the singleton instance of this BukkitListenerManager.
     * @return The instance
     */
    public static BukkitListenerManager get() {
        if (instance == null)
            instance = new BukkitListenerManager();
        return instance;
    }
}
