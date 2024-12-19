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

import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.script.Script;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.event.EventPriority;
import org.python.core.PyFunction;

/**
 * Manager to interface with BungeeCord's event framework. Primarily used by scripts to register and unregister event listeners.
 */
public class BungeeListenerManager extends ListenerManager<BungeeScriptEventListener, Event, EventPriority> {

    private static BungeeListenerManager manager;

    private BungeeListenerManager() {
        super();
    }

    //TODO

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass) {
        return null;
    }

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, EventPriority priority) {
        return null;
    }

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, boolean ignoreCancelled) {
        return null;
    }

    @Override
    public BungeeScriptEventListener registerListener(PyFunction function, Class<? extends Event> eventClass, EventPriority priority, boolean ignoreCancelled) {
        return null;
    }

    @Override
    public void unregisterListener(BungeeScriptEventListener listener) {

    }

    @Override
    public void unregisterListeners(Script script) {

    }

    @Override
    public BungeeScriptEventListener getListener(Script script, Class<? extends Event> eventClass) {
        return null;
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
