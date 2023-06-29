/*
 *    Copyright 2023 magicmq
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
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.python.core.PyFunction;

import java.util.Collection;
import java.util.HashMap;

public class DummyListener implements Listener {

    private final Script script;
    private final HashMap<PyFunction, Class<? extends Event>> events;

    public DummyListener(Script script) {
        this.script = script;
        this.events = new HashMap<>();
    }

    public Script getScript() {
        return script;
    }

    public void addEvent(PyFunction function, Class<? extends Event> event) {
        events.put(function, event);
    }

    public Class<? extends Event> removeEvent(PyFunction function) {
        return events.remove(function);
    }

    public boolean containsEvent(Class<? extends Event> event) {
        return events.containsValue(event);
    }

    public Collection<Class<? extends Event>> getEvents() {
        return events.values();
    }

    public boolean isEmpty() {
        return events.size() == 0;
    }
}
