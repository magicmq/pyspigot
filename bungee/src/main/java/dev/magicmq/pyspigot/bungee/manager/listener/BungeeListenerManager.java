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

package dev.magicmq.pyspigot.bungee.manager.listener;

import com.google.common.collect.Multimap;
import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.exception.PluginInitializationException;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.event.EventBus;
import net.md_5.bungee.event.EventPriority;
import org.python.core.PyFunction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * The BungeeCord-specific implementation of the listener manager.
 */
public class BungeeListenerManager extends ListenerManager<BungeeScriptEventListener, Event, Byte> {

    private static BungeeListenerManager instance;

    private final Multimap<Plugin, Listener> listenersByPlugin;
    private final Map<Class<?>, Map<Byte, Map<Object, Method[]>>> byListenerAndPriority;
    private final Lock lock;
    private final EventBus eventBus;
    private final Method bakeHandlers;

    private BungeeListenerManager() {
        super();

        try {
            Class<?> pluginManagerClass = PluginManager.class;
            Field listenersByPlugin = pluginManagerClass.getDeclaredField("listenersByPlugin");
            listenersByPlugin.setAccessible(true);
            this.listenersByPlugin = (Multimap<Plugin, Listener>) listenersByPlugin.get(ProxyServer.getInstance().getPluginManager());

            Field eventBusField = pluginManagerClass.getDeclaredField("eventBus");
            eventBusField.setAccessible(true);
            this.eventBus = (EventBus) eventBusField.get(ProxyServer.getInstance().getPluginManager());

            Class<?> eventBusClass = EventBus.class;

            Field byListenerAndPriorityField = eventBusClass.getDeclaredField("byListenerAndPriority");
            byListenerAndPriorityField.setAccessible(true);
            this.byListenerAndPriority = (Map<Class<?>, Map<Byte, Map<Object, Method[]>>>) byListenerAndPriorityField.get(eventBus);

            Field lockField = eventBusClass.getDeclaredField("lock");
            lockField.setAccessible(true);
            this.lock = (Lock) lockField.get(eventBus);

            this.bakeHandlers = eventBusClass.getDeclaredMethod("bakeHandlers", Class.class);
            this.bakeHandlers.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException e) {
            //This should not happen, reflection checks done on plugin enable
            throw new PluginInitializationException("Error when initializing listener manager, event listeners will not work correctly.", e);
        }
    }

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass) {
        return registerListener(function, eventClass, EventPriority.NORMAL);
    }

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, Byte priority) {
        Script script = ScriptUtils.getScriptFromCallStack();
        BungeeScriptEventListener listener = getListener(script, eventClass);
        if (listener == null) {
            listener = new BungeeScriptEventListener(script, function, eventClass, priority);
            registerWithBungee(listener);
            addListener(script, listener);
            return listener;
        } else {
            throw new ScriptRuntimeException(script, "Script already has an event listener for '" + eventClass.getSimpleName() + "' registered");
        }
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * BungeeCord events do not support ignoreCancelled, so this method will not work. Instead, use {@link BungeeListenerManager#registerListener(PyFunction, Class)}
     * @throws UnsupportedOperationException always
     */
    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, boolean ignoreCancelled) {
        throw new UnsupportedOperationException("BungeeCord does not support ignoreCancelled for event listeners.");
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * BungeeCord events do not support ignoreCancelled, so this method will not work. Instead, use {@link BungeeListenerManager#registerListener(PyFunction, Class, Byte)}
     * @throws UnsupportedOperationException always
     */
    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, Byte priority, boolean ignoreCancelled) {
        throw new UnsupportedOperationException("BungeeCord does not support ignoreCancelled for event listeners.");
    }

    @Override
    public void unregisterListener(BungeeScriptEventListener listener) {
        ProxyServer.getInstance().getPluginManager().unregisterListener(listener);
        removeListener(listener.getScript(), listener);
        unregisterWithBungee(listener);
    }

    @Override
    public void unregisterListeners(Script script) {
        List<BungeeScriptEventListener> associatedListeners = getListeners(script);
        if (!associatedListeners.isEmpty()) {
            for (BungeeScriptEventListener eventListener : associatedListeners) {
                unregisterWithBungee(eventListener);
            }
            removeListeners(script);
        }
    }

    @Override
    public BungeeScriptEventListener getListener(Script script, Class<? extends Event> eventClass) {
        for (BungeeScriptEventListener listener : getListeners(script)) {
            if (listener.getEvent().equals(eventClass))
                return listener;
        }
        return null;
    }

    private Map<Class<?>, Map<Byte, Set<Method>>> createDummyHandler(BungeeScriptEventListener listener) {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = new HashMap<>();
        Set<Method> methods = new HashSet<>();
        try {
            methods.add(BungeeScriptEventListener.class.getDeclaredMethod("callToScript", Object.class));
        } catch (NoSuchMethodException e) {
            //This should not happen
            throw new ScriptRuntimeException(listener.getScript(), "Unhandled exception when registering listener for event '" + listener.getEvent().getSimpleName() + "'", e);
        }
        Map<Byte, Set<Method>> prioritiesMap = new HashMap<>();
        prioritiesMap.put(listener.getPriority(), methods);
        handler.put(listener.getEvent(), prioritiesMap);
        return handler;
    }

    private void registerWithBungee(BungeeScriptEventListener listener) {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = createDummyHandler(listener);
        lock.lock();
        try {
            for (Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet()) {
                Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.computeIfAbsent(e.getKey(), k -> new HashMap<>());
                for (Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet()) {
                    Map<Object, Method[]> currentPriorityMap = prioritiesMap.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
                    currentPriorityMap.put(listener, entry.getValue().toArray(new Method[0]));
                }
                try {
                    bakeHandlers.invoke(eventBus, e.getKey());
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new ScriptRuntimeException(listener.getScript(), "Unhandled exception when registering listener for event '" + listener.getEvent().getSimpleName() + "'", exception);
                }
            }
        } finally {
            lock.unlock();
        }
        this.listenersByPlugin.put(PyBungee.get(), listener);
    }

    private void unregisterWithBungee(BungeeScriptEventListener listener) {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = createDummyHandler(listener);
        lock.lock();
        try {
            for (Map.Entry<Class<?>, Map<Byte, Set<Method>>> e : handler.entrySet()) {
                Map<Byte, Map<Object, Method[]>> prioritiesMap = byListenerAndPriority.get(e.getKey());
                if (prioritiesMap != null) {
                    for (Byte priority : e.getValue().keySet()) {
                        Map<Object, Method[]> currentPriority = prioritiesMap.get(priority);
                        if (currentPriority != null) {
                            currentPriority.remove(listener);
                            if (currentPriority.isEmpty()) {
                                prioritiesMap.remove(priority);
                            }
                        }
                    }
                    if (prioritiesMap.isEmpty()) {
                        byListenerAndPriority.remove(e.getKey());
                    }
                }
                try {
                    bakeHandlers.invoke(eventBus, e.getKey());
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new ScriptRuntimeException(listener.getScript(), "Unhandled exception when unregistering listener for event '" + listener.getEvent().getSimpleName() + "'", exception);
                }
            }
        } finally {
            lock.unlock();
        }
        listenersByPlugin.values().remove(listener);
    }

    /**
     * Get the singleton instance of this BungeeListenerManager.
     * @return The instance
     */
    public static BungeeListenerManager get() {
        if (instance == null)
            instance = new BungeeListenerManager();
        return instance;
    }
}
