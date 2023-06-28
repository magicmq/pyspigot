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
