package dev.magicmq.pyspigot.event;

import dev.magicmq.pyspigot.managers.script.Script;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScriptEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Script script;

    public ScriptEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
