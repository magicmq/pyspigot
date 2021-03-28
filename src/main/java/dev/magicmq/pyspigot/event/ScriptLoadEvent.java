package dev.magicmq.pyspigot.event;

import dev.magicmq.pyspigot.managers.script.Script;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScriptLoadEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Script script;
    private boolean cancelled;

    public ScriptLoadEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
