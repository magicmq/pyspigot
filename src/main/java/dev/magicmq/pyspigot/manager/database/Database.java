package dev.magicmq.pyspigot.manager.database;

import dev.magicmq.pyspigot.manager.script.Script;

public abstract class Database {

    private final Script script;

    public Database(Script script) {
        this.script = script;
    }

    public abstract boolean open();

    public abstract boolean close();

    public Script getScript() {
        return script;
    }
}
