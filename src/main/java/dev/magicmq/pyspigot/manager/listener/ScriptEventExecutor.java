package dev.magicmq.pyspigot.manager.listener;

import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.python.core.PyException;

/**
 * Represents an event executor for script event listeners.
 * @see org.bukkit.plugin.EventExecutor
 */
public class ScriptEventExecutor implements EventExecutor {

    private ScriptEventListener scriptEventListener;

    /**
     *
     * @param scriptEventListener The {@link ScriptEventListener} associated with this ScriptEventExecutor
     */
    public ScriptEventExecutor(ScriptEventListener scriptEventListener) {
        this.scriptEventListener = scriptEventListener;
    }

    /**
     * Called internally when the event occurs.
     * @param listener The listener associated with this EventExecutor
     * @param event The event that occurred
     */
    public void execute(Listener listener, Event event) {
        try {
            scriptEventListener.getListenerFunction()._jcall(new Object[]{event});
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(scriptEventListener.getScript(), e, "Error when executing event listener");
        } catch (Throwable t) {
            //Bukkit API catches Throwables thrown by event listeners. We need to override this for correct script error/exception handling.
            ScriptManager.get().handleScriptException(scriptEventListener.getScript(), t, "Could not pass event " + event.getEventName() + " to script");
        }
    }
}
