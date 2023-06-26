package dev.magicmq.pyspigot.managers.script;

import org.python.core.PyCode;
import org.python.util.PythonInterpreter;

import java.io.File;

public class Script {

    private final String name;
    private final PythonInterpreter interpreter;
    private final PyCode code;
    private final File file;

    public Script(String name, PythonInterpreter interpreter, PyCode code, File file) {
        this.name = name;
        this.interpreter = interpreter;
        this.code = code;
        this.file = file;
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Script))
            return false;

        return name.equals(((Script) other).name);
    }
}
