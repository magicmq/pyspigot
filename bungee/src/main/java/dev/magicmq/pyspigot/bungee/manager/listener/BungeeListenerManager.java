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

import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.python.core.PyFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

public class BungeeListenerManager extends ListenerManager<BungeeScriptEventListener, Event, Byte> {

    private static BungeeListenerManager manager;

    private BungeeListenerManager() {
        super();
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
            try {
                modifyEventPriority(priority);
            } catch (Exception e) {
                throw new RuntimeException("Exception occured when registering event '" + eventClass.getSimpleName() + "'", e);
            }
            listener = new BungeeScriptEventListener(script, function, eventClass);
            ProxyServer.getInstance().getPluginManager().registerListener(PyBungee.get(), listener);
            return listener;
        } else {
            throw new RuntimeException("Script already has an event listener for '" + eventClass.getSimpleName() + "' registered");
        }
    }

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, boolean ignoreCancelled) {
        throw new UnsupportedOperationException("BungeeCord does not support ignoreCancelled for event listeners.");
    }

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, Byte priority, boolean ignoreCancelled) {
        throw new UnsupportedOperationException("BungeeCord does not support ignoreCancelled for event listeners.");
    }

    @Override
    public void unregisterListener(BungeeScriptEventListener listener) {
        ProxyServer.getInstance().getPluginManager().unregisterListener(listener);
        removeListener(listener.getScript(), listener);
    }

    @Override
    public void unregisterListeners(Script script) {
        List<BungeeScriptEventListener> associatedListeners = getListeners(script);
        if (associatedListeners != null) {
            for (BungeeScriptEventListener eventListener : associatedListeners) {
                ProxyServer.getInstance().getPluginManager().unregisterListener(eventListener);
            }
            removeListeners(script);
        }
    }

    @Override
    public BungeeScriptEventListener getListener(Script script, Class<? extends Event> eventClass) {
        List<BungeeScriptEventListener> scriptListeners = getListeners(script);
        if (scriptListeners != null) {
            for (BungeeScriptEventListener listener : scriptListeners) {
                if (listener.getEvent().equals(eventClass))
                    return listener;
            }
        }
        return null;
    }

    private void modifyEventPriority(Byte priority) throws Exception {
        Method method = BungeeScriptEventListener.class.getMethod("onEvent");
        final EventHandler annotation = method.getAnnotation(EventHandler.class);
        Object handler = Proxy.getInvocationHandler(annotation);
        Field field = handler.getClass().getDeclaredField("memberValues");
        field.setAccessible(true);
        Map<String, Object> memberValues = (Map<String, Object>) field.get(handler);
        memberValues.put("priority", priority);
    }

    /**
     * Get the singleton instance of this BungeeListenerManager.
     * @return The instance
     */
    public static BungeeListenerManager get() {
        if (manager == null)
            manager = new BungeeListenerManager();
        return manager;
    }
}
