package dev.magicmq.pyspigot.managers.script;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.managers.command.CommandManager;
import dev.magicmq.pyspigot.managers.config.ConfigManager;
import dev.magicmq.pyspigot.managers.libraries.LibraryManager;
import dev.magicmq.pyspigot.managers.listener.ListenerManager;
import dev.magicmq.pyspigot.managers.protocol.ProtocolManager;
import dev.magicmq.pyspigot.managers.task.TaskManager;
import org.bukkit.Bukkit;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScriptManager {

    private static ScriptManager manager;

    private PySystemState systemState;
    private final List<Script> scripts;
    private final HashMap<String, PyObject> globalVariables;

    private ScriptManager() {
        this.systemState = new PySystemState();
        systemState.setClassLoader(LibraryManager.get().getClassLoader());

        this.scripts = new ArrayList<>();
        this.globalVariables = new HashMap<>();

        File scripts = new File(PySpigot.get().getDataFolder(), "scripts");
        if (!scripts.exists())
            scripts.mkdir();

        Bukkit.getScheduler().runTaskLater(PySpigot.get(), this::loadScripts, PluginConfig.getLoadScriptDelay());
    }

    public void shutdown() {
        scripts.forEach(script -> script.getInterpreter().close());
    }

    private void loadScripts() {
        PySpigot.get().getLogger().log(Level.INFO, "Loading scripts...");
        int numOfScripts = 0;
        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        if (scriptsFolder.isDirectory()) {
            SortedSet<File> toLoad = new TreeSet<>();
            toLoad.addAll(Arrays.asList(scriptsFolder.listFiles()));
            for (File script : toLoad) {
                if (script.getName().endsWith(".py")) {
                    try {
                        if (loadScript(script.getName()))
                            numOfScripts++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        PySpigot.get().getLogger().log(Level.INFO, "Found and loaded " + numOfScripts + " scripts!");
    }

    public boolean loadScript(String name) throws IOException {
        if (getScript(name) != null)
            throw new IllegalArgumentException("Attempted to load script " + name + ", but there is already a loaded script with this name. Script names must be unique.");

        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        File scriptFile = new File(scriptsFolder, name);
        try (FileReader reader = new FileReader(scriptFile)) {
            try {
                PythonInterpreter interpreter = initNewInterpreter();

                Script script = new Script(scriptFile.getName(), interpreter, interpreter.compile(reader, scriptFile.getName()), scriptFile);

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

        return loadScript(script.getName());
    }

    public void handleScriptException(Script script, PyException exception, String message) {
        ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception);
        Bukkit.getPluginManager().callEvent(event);
        if (event.doReportException()) {
            if (exception.getCause() != null && !(exception.getCause() instanceof PyException))
                exception.getCause().printStackTrace();
            else {
                if (exception.traceback != null)
                    PySpigot.get().getLogger().log(Level.SEVERE, message + " " + script.getName() + ": " + exception.getMessage() + "\n\n" + exception.traceback.dumpStack());
                else
                    PySpigot.get().getLogger().log(Level.SEVERE, message + " " + script.getName() + ": " + exception.getMessage());
            }
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

    public SortedSet<String> getAllScripts() {
        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        SortedSet<String> scripts = new TreeSet<>();
        if (scriptsFolder.isDirectory()) {
            for (File file : scriptsFolder.listFiles()) {
                if (file.getName().endsWith(".py"))
                    scripts.add(file.getName());
            }
        }
        return scripts;
    }

    private PythonInterpreter initNewInterpreter() {
        PythonInterpreter interpreter = new PythonInterpreter(null, systemState);

        interpreter.set("listener", ListenerManager.get());
        interpreter.set("command", CommandManager.get());
        interpreter.set("config", ConfigManager.get());
        interpreter.set("scheduler", TaskManager.get());

        if (PySpigot.get().isProtocolLibAvailable())
            interpreter.set("protocol", ProtocolManager.get());

        interpreter.set("global", globalVariables);

        if (PluginConfig.doAutoImportBukkit()) {
            interpreter.set("bukkit", Bukkit.class);
        }

        return interpreter;
    }

    private boolean runScript(String name) {
        Script script = getScript(name);

        try {
            script.getInterpreter().exec(script.getCode());
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

        if (PySpigot.get().isProtocolLibAvailable())
            ProtocolManager.get().stopScript(script);

        script.getInterpreter().close();

        return true;
    }

    public static ScriptManager get() {
        if (manager == null)
            manager = new ScriptManager();
        return manager;
    }
}
