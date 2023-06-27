package dev.magicmq.pyspigot.managers.script;

import dev.magicmq.pyspigot.utils.ScriptLogger;
import org.python.core.PyCode;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.util.logging.Logger;

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
    }

    public String getName() {
        return name;
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
