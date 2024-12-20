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
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.python.core.PyFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class SpigotListenerManager extends ListenerManager<SpigotScriptEventListener, Event, EventPriority> {

    private static SpigotListenerManager manager;

    private SpigotListenerManager() {
        super();
    }

    @Override
    public SpigotScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass) {
        return registerListener(function, eventClass, EventPriority.NORMAL, false);
    }

    @Override
    public SpigotScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, EventPriority priority) {
        return registerListener(function, eventClass, priority, false);
    }

    @Override
    public SpigotScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, boolean ignoreCancelled) {
        return registerListener(function, eventClass, EventPriority.NORMAL, ignoreCancelled);
    }

    @Override
    public SpigotScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, EventPriority priority, boolean ignoreCancelled) {
        Script script = ScriptUtils.getScriptFromCallStack();
        SpigotScriptEventListener listener = getListener(script, eventClass);
        if (listener == null) {
            listener = new SpigotScriptEventListener(script, function, eventClass);
            Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, listener.getEventExecutor(), PySpigot.get(), ignoreCancelled);
            addListener(script, listener);
            return listener;
        } else {
            throw new RuntimeException("Script already has an event listener for '" + eventClass.getSimpleName() + "' registered");
        }
    }

    @Override
    public void unregisterListener(SpigotScriptEventListener listener) {
        removeFromHandlers(listener);
        removeListener(listener.getScript(), listener);
    }

    @Override
    public void unregisterListeners(Script script) {
        List<SpigotScriptEventListener> associatedListeners = getListeners(script);
        if (associatedListeners != null) {
            for (SpigotScriptEventListener eventListener : associatedListeners) {
                removeFromHandlers(eventListener);
            }
            removeListeners(script);
        }
    }

    @Override
    public SpigotScriptEventListener getListener(Script script, Class<? extends Event> eventClass) {
        List<SpigotScriptEventListener> scriptListeners = getListeners(script);
        if (scriptListeners != null) {
            for (SpigotScriptEventListener listener : scriptListeners) {
                if (listener.getEvent().equals(eventClass))
                    return listener;
            }
        }
        return null;
    }

    private void removeFromHandlers(SpigotScriptEventListener listener) {
        try {
            Method method = getRegistrationClass(listener.getEvent()).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            HandlerList list = (HandlerList) method.invoke(null);
            list.unregister(listener);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            //This should not happen, because all events *should* have getHandlerList defined
            throw new RuntimeException("Unhandled exception when unregistering listener '" + listener.getEvent().getSimpleName() + "'", e);
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
                throw new RuntimeException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    /**
     * Get the singleton instance of this SpigotListenerManager.
     * @return The instance
     */
    public static SpigotListenerManager get() {
        if (manager == null)
            manager = new SpigotListenerManager();
        return manager;
    }
}
