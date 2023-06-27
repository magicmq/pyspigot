package dev.magicmq.pyspigot.utils;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.script.Script;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ScriptLogger extends Logger {

    public ScriptLogger(Script script) {
        super("PySpigot/" + script.getName(), null);
        this.setParent(PySpigot.get().getLogger());
        this.setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord record) {
        super.log(record);
    }
}
