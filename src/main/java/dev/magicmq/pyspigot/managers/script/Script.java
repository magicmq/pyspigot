package dev.magicmq.pyspigot.managers.script;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.utils.ScriptLogger;
import org.python.core.PyCode;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Script {

    private final String name;
    private final PythonInterpreter interpreter;
    private final PyCode code;
    private final File file;
    private final ScriptLogger logger;

    private PyFunction startFunction;
    private PyFunction stopFunction;

    public Script(String name, PythonInterpreter interpreter, PyCode code, File file) {
        this.name = name;
        this.interpreter = interpreter;
        this.code = code;
        this.file = file;

        this.logger = new ScriptLogger(this);
        if (PluginConfig.doLogToFile()) {
            try {
                this.logger.initFileHandler();
            } catch (IOException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when initializing log file for script " + name, e);
            }
        }
        interpreter.set("logger", logger);
    }

    public String getName() {
        return name;
    }

    public String getLogFileName() {
        return name.substring(0, name.length() - 3) + ".log";
    }

    public PythonInterpreter getInterpreter() {
        return interpreter;
    }

    public PyCode getCode() {
        return code;
    }

    public File getFile() {
        return file;
    }

    public ScriptLogger getLogger() {
        return logger;
    }

    public PyFunction getStartFunction() {
        return startFunction;
    }

    public void setStartFunction(PyFunction startFunction) {
        this.startFunction = startFunction;
    }

    public PyFunction getStopFunction() {
        return stopFunction;
    }

    public void setStopFunction(PyFunction stopFunction) {
        this.stopFunction = stopFunction;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Script))
            return false;

        return name.equals(((Script) other).name);
    }
}
