package dev.magicmq.pyspigot.event;

import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.event.HandlerList;

public class ScriptUnloadEvent extends ScriptEvent {

    private static final HandlerList handlers = new HandlerList();

    private final boolean error;

    public ScriptUnloadEvent(Script script, boolean error) {
        super(script);
        this.error = error;
    }

    public boolean isError() {
        return error;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
