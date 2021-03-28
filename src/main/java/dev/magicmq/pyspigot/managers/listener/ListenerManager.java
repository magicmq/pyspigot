package dev.magicmq.pyspigot.managers.listener;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.script.Script;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.python.core.PyBaseCode;
import org.python.core.PyFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ListenerManager {

    private static ListenerManager manager;

    private final List<DummyListener> registeredScripts;

    private ListenerManager() {
        registeredScripts = new ArrayList<>();
    }

    public void registerEvent(PyFunction function, Class<? extends Event> eventClass, String priorityString, boolean ignoreCancelled) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.getFuncCode()).co_filename);
        DummyListener listener = get().getListener(script);
        if (listener == null) {
            listener = new DummyListener(script);
            registeredScripts.add(listener);
        } else {
            if (listener.containsEvent(eventClass))
                throw new UnsupportedOperationException("Tried to register an event, but " + script.getName() + " already has a/an " + eventClass.getSimpleName() + " registered!");
        }
        EventPriority priority = EventPriority.valueOf(priorityString);
        EventExecutor executor = (listenerInner, event) -> {
            try {
                if (!eventClass.isAssignableFrom(event.getClass())) {
                    return;
                }
                function._jcall(new Object[]{event});
            } catch (Throwable t) {
                throw new EventException(t);
            }
        };

        listener.addEvent(function, eventClass);
        Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, executor, PySpigot.get(), ignoreCancelled);
    }

    public void registerEvent(PyFunction function, Class<? extends Event> eventClass, String priorityString) {
        registerEvent(function, eventClass, priorityString, false);
    }

    public void registerEvent(PyFunction function, Class<? extends Event> eventClass, boolean ignoreCancelled) {
        registerEvent(function, eventClass, "NORMAL", ignoreCancelled);
    }

    public void registerEvent(PyFunction function, Class<? extends Event> eventClass) {
        registerEvent(function, eventClass, "NORMAL", false);
    }

    public void unregisterEvent(PyFunction function) {
        String scriptName = ((PyBaseCode) function.getFuncCode()).co_filename;
        DummyListener dummyListener = getListener(scriptName);
        Class<? extends Event> event = dummyListener.removeEvent(function);
        try {
            Method method = event.getMethod("getHandlerList", null);
            HandlerList list = (HandlerList) method.invoke(null, null);
            list.unregister(dummyListener);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (dummyListener.isEmpty()) {
            registeredScripts.remove(dummyListener);
        }
    }

    public DummyListener getListener(String scriptName) {
        for (DummyListener listener : registeredScripts) {
            if (listener.getScript().getName().equals(scriptName))
                return listener;
        }

        return null;
    }

    public DummyListener getListener(Script script) {
        for (DummyListener listener : registeredScripts) {
            if (listener.getScript().equals(script))
                return listener;
        }

        return null;
    }

    public void stopScript(Script script) {
        DummyListener listener = getListener(script);
        if (listener != null) {
            for (Class<? extends Event> event : listener.getEvents()) {
                try {
                    Method method = event.getMethod("getHandlerList", null);
                    HandlerList list = (HandlerList) method.invoke(null, null);
                    list.unregister(listener);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            registeredScripts.remove(listener);
        }
    }

    public static ListenerManager get() {
        if (manager == null)
            manager = new ListenerManager();
        return manager;
    }
}
