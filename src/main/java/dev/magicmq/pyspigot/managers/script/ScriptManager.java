package dev.magicmq.pyspigot.managers.script;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.managers.command.CommandManager;
import dev.magicmq.pyspigot.managers.config.ConfigManager;
import dev.magicmq.pyspigot.managers.libraries.LibraryManager;
import dev.magicmq.pyspigot.managers.listener.ListenerManager;
import dev.magicmq.pyspigot.managers.task.TaskManager;
import org.bukkit.Bukkit;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScriptManager {

    private static ScriptManager manager;

    private final PythonInterpreter interpreter;
    private final List<Script> scripts;
    private final HashMap<String, PyObject> globalVariables;

    private ScriptManager() {
        PySystemState state = new PySystemState();
        state.setClassLoader(LibraryManager.get().getClassLoader());
        this.interpreter = new PythonInterpreter(null, state);

        this.scripts = new ArrayList<>();
        this.globalVariables = new HashMap<>();

        interpreter.set("listeners", ListenerManager.get());
        interpreter.set("commands", CommandManager.get());
        interpreter.set("configs", ConfigManager.get());
        interpreter.set("tasks", TaskManager.get());
        interpreter.set("globals", globalVariables);

        File scripts = new File(PySpigot.get().getDataFolder(), "scripts");
        if (!scripts.exists())
            scripts.mkdir();

        Bukkit.getScheduler().runTaskLater(PySpigot.get(), () -> {
            if (PluginConfig.autorunScriptsEnabled()) {
                loadAutorunScripts();
            }
        }, PluginConfig.getLoadScriptDelay());
    }

    public void shutdown() {
        interpreter.close();
    }

    private void loadAutorunScripts() {
        PySpigot.get().getLogger().log(Level.INFO, "Initializing autorun scripts...");
        int numOfScripts = 0;
        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        if (scriptsFolder.isDirectory()) {
            for (String script : PluginConfig.getAutorunScripts()) {
                File file = new File(scriptsFolder, script);
                if (file.exists()) {
                    try {
                        if (loadScript(script, ScriptType.AUTORUN))
                            numOfScripts++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    PySpigot.get().getLogger().log(Level.WARNING, "Could not find script " + script + " in the scripts folder! Did you make sure to include the extension (.py)?");
            }
        }
        PySpigot.get().getLogger().log(Level.INFO, "Found and initialized " + numOfScripts + " autorun scripts!");
    }

    public boolean loadScript(String name, ScriptType type) throws IOException {
        if (getScript(name) != null)
            throw new IllegalArgumentException("Attempted to load script " + name + ", but there is already a loaded script with this name. Script names must be unique.");

        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        File scriptFile = new File(scriptsFolder, name);
        try (FileReader reader = new FileReader(scriptFile)) {
            try {
                Script script = new Script(scriptFile.getName(), interpreter.compile(reader, scriptFile.getName()), scriptFile, type);

                ScriptLoadEvent eventLoad = new ScriptLoadEvent(script);
                Bukkit.getPluginManager().callEvent(eventLoad);
                if (!eventLoad.isCancelled()) {
                    this.scripts.add(script);
                } else
                    return false;

                return runScript(script.getName());
            } catch (PySyntaxError | PyIndentationError e) {
                PySpigot.get().getLogger().log(Level.SEVERE, e.getMessage());
                return false;
            }
        }
    }

    public boolean unloadScript(String name) {
        Script script = getScript(name);

        ScriptUnloadEvent event = new ScriptUnloadEvent(script);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            if (!stopScript(name))
                return false;
            scripts.remove(script);
            return true;
        } else
            return false;
    }

    public boolean reloadScript(String name) throws IOException {
        Script script = getScript(name);

        if (!unloadScript(name))
            return false;

        return loadScript(script.getName(), script.getType());
    }

    public void handleScriptException(Script script, PyException exception, String message) {
        if (exception.getCause() != null && !(exception.getCause() instanceof PyException))
            exception.getCause().printStackTrace();
        else {
            if (exception.traceback != null)
                PySpigot.get().getLogger().log(Level.SEVERE, message + " " + script.getName() + ": " + exception.getMessage() + "\n\n" + exception.traceback.dumpStack());
            else
                PySpigot.get().getLogger().log(Level.SEVERE, message + " " + script.getName() + ": " + exception.getMessage());
        }
    }

    public boolean isScript(String name) {
        return getScript(name) != null;
    }

    public Script getScript(String name) {
        for (Script script : scripts) {
            if (script.getName().equals(name))
                return script;
        }
        return null;
    }

    public List<String> getLoadedScripts() {
        return scripts.stream().map(Script::getName).collect(Collectors.toList());
    }

    private boolean runScript(String name) {
        Script script = getScript(name);

        try {
            interpreter.exec(script.getCode());
        } catch (PyException e) {
            handleScriptException(script, e, "Error when running script");
            PySpigot.get().getLogger().log(Level.SEVERE, "Script " + script.getName() + " has been unloaded due to a crash.");
            unloadScript(name);
            return false;
        }
        return true;
    }

    private boolean stopScript(String name) {
        Script script = getScript(name);

        ListenerManager.get().stopScript(script);
        TaskManager.get().stopScript(script);
        CommandManager.get().stopScript(script);
        return true;
    }

    public static ScriptManager get() {
        if (manager == null)
            manager = new ScriptManager();
        return manager;
    }
}
