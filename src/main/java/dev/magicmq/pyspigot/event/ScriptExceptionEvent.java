package dev.magicmq.pyspigot.event;

import dev.magicmq.pyspigot.managers.script.Script;
import org.bukkit.event.HandlerList;
import org.python.core.PyException;

public class ScriptExceptionEvent extends ScriptEvent {

    private static final HandlerList handlers = new HandlerList();
    private final PyException exception;
    private boolean reportException;

    public ScriptExceptionEvent(Script script, PyException exception) {
        super(script);
        this.exception = exception;
        this.reportException = true;
    }

    public PyException getException() {
        return exception;
    }

    public boolean doReportException() {
        return reportException;
    }

    public void setReportException(boolean reportException) {
        this.reportException = reportException;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
