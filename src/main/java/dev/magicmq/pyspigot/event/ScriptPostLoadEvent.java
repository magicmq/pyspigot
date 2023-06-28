package dev.magicmq.pyspigot.event;

import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.event.HandlerList;

public class ScriptPostLoadEvent extends ScriptEvent {

    private static final HandlerList handlers = new HandlerList();

    public ScriptPostLoadEvent(Script script) {
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
