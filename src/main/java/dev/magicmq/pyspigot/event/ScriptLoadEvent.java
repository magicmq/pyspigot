package dev.magicmq.pyspigot.event;

import dev.magicmq.pyspigot.managers.script.Script;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScriptLoadEvent extends ScriptEvent {

    private static final HandlerList handlers = new HandlerList();

    public ScriptLoadEvent(Script script) {
        super(script);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
