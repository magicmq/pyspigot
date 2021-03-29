package dev.magicmq.pyspigot.managers.script;

import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.event.ScriptRunEvent;
import dev.magicmq.pyspigot.event.ScriptStopEvent;
import dev.magicmq.pyspigot.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.managers.command.CommandManager;
import dev.magicmq.pyspigot.managers.config.ConfigManager;
import dev.magicmq.pyspigot.managers.listener.ListenerManager;
import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.task.TaskManager;
import org.bukkit.Bukkit;
import org.python.core.PyException;
import org.python.core.PySyntaxError;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScriptManager {

    private static ScriptManager manager;

    private final PythonInterpreter interpreter;
    private final List<Script> scripts;

    private ScriptManager() {
        this.interpreter = new PythonInterpreter();
        this.scripts = new ArrayList<>();

        interpreter.set("listener_manager", ListenerManager.get());
        interpreter.set("command_manager", CommandManager.get());
        interpreter.set("config_manager", ConfigManager.get());
        interpreter.set("task_manager", TaskManager.get());
        interpreter.set("global_variables", interpreter);

        File scripts = new File(PySpigot.get().getDataFolder(), "scripts");
        if (!scripts.exists())
            scripts.mkdir();
        File autorunScripts = new File(PySpigot.get().getDataFolder(), "autorun_scripts");
        if (!autorunScripts.exists())
            autorunScripts.mkdir();

        Bukkit.getScheduler().runTaskLater(PySpigot.get(), () -> {
            if (PluginConfig.preloadScripts()) {
                loadAllScripts();
            }

            if (PluginConfig.autorunScripts()) {
                loadAutorunScripts();
            }
        }, PluginConfig.getLoadScriptDelay());
    }

    public void shutdown() {
        interpreter.close();
    }

    private void loadAllScripts() {
        PySpigot.get().getLogger().log(Level.INFO, "Initializing scripts...");
        int numOfScripts = 0;
        File scripts = new File(PySpigot.get().getDataFolder(), "scripts");
        if (scripts.isDirectory()) {
            File[] files = scripts.listFiles();
            for (File file : files) {
                if (!file.getName().endsWith(".py"))
                    continue;

                try {
                    if (loadScript(file.getName()))
                        numOfScripts++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        PySpigot.get().getLogger().log(Level.INFO, "Found and initialized " + numOfScripts + " scripts!");
    }

    public boolean loadScript(String name) throws IOException {
        if (getScript(name) != null)
            throw new IllegalArgumentException("Attempted to load " + name + ", but there is already a loaded script with this name. Script names must be unique.");

        if (!name.endsWith(".py")) {
            name += ".py";
        }

        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        File scriptFile = new File(scriptsFolder, name);
        try (FileReader reader = new FileReader(scriptFile)) {
            try {
                Script script = new Script(scriptFile.getName(), interpreter.compile(reader, scriptFile.getName()), scriptFile, ScriptType.NORMAL);

                ScriptLoadEvent event = new ScriptLoadEvent(script);
                Bukkit.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    this.scripts.add(script);
                    return true;
                } else
                    return false;
            } catch (PySyntaxError e) {
                PySpigot.get().getLogger().log(Level.SEVERE, e.getMessage());
                return false;
            }
        }
    }

    private void loadAutorunScripts() {
        PySpigot.get().getLogger().log(Level.INFO, "Initializing autorun scripts...");
        int numOfScripts = 0;
        File scripts = new File(PySpigot.get().getDataFolder(), "autorun_scripts");
        if (scripts.isDirectory()) {
            File[] files = scripts.listFiles();
            for (File file : files) {
                if (!file.getName().endsWith(".py"))
                    continue;

                try {
                    if (loadAutorunScript(file.getName()))
                        numOfScripts++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        PySpigot.get().getLogger().log(Level.INFO, "Found and initialized " + numOfScripts + " autorun scripts!");
    }

    private boolean loadAutorunScript(String name) throws IOException {
        if (getScript(name) != null)
            throw new IllegalArgumentException("Attempted to load " + name + ", but there is already a loaded script with this name. Script names must be unique.");

        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "autorun_scripts");
        File scriptFile = new File(scriptsFolder, name);
        try (FileReader reader = new FileReader(scriptFile)) {
            try {
                Script script = new Script(scriptFile.getName(), interpreter.compile(reader, scriptFile.getName()), scriptFile, ScriptType.AUTORUN);

                ScriptLoadEvent eventLoad = new ScriptLoadEvent(script);
                Bukkit.getPluginManager().callEvent(eventLoad);
                if (!eventLoad.isCancelled()) {
                    this.scripts.add(script);
                } else
                    return false;

                return runScript(script.getName());
            } catch (PySyntaxError e) {
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
            if (script.isRunning()) {
                if (!stopScript(name))
                    return false;
            }
            scripts.remove(script);
            return true;
        } else
            return false;
    }

    public boolean runScript(String name) {
        Script script = getScript(name);

        ScriptRunEvent event = new ScriptRunEvent(script);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            try {
                interpreter.exec(script.getCode());
                script.setRunning(true);
            } catch (PyException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when running " + script.getName() + ": " + e.getMessage() + "\n\n" + e.traceback.dumpStack());
                return false;
            }
            return true;
        } else
            return false;
    }

    public boolean stopScript(String name) {
        Script script = getScript(name);

        ScriptStopEvent event = new ScriptStopEvent(script);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            ListenerManager.get().stopScript(script);
            TaskManager.get().stopScript(script);
            CommandManager.get().stopScript(script);
            script.setRunning(false);
            return true;
        } else
            return false;
    }

    public boolean reloadScript(String name) throws IOException {
        Script script = getScript(name);

        if (script.isRunning()) {
            if (!stopScript(name))
                return false;
        }
        scripts.remove(script);

        if (script.getType() == ScriptType.AUTORUN)
            return loadAutorunScript(script.getName());
        else
            if (!loadScript(script.getName()))
                return false;

        if (script.isRunning()) {
            if (runScript(script.getName()))
                return true;
            else
                return false;
        } else
            return true;
    }

    public boolean isScript(String name) {
        return getScript(name) != null;
    }

    public boolean isScriptRunning(String name) {
        Script script = getScript(name);
        return script.isRunning();
    }

    public Script getScript(String name) {
        if (!name.endsWith(".py")) {
            name += ".py";
        }

        for (Script script : scripts) {
            if (script.getName().equals(name))
                return script;
        }
        return null;
    }

    public List<String> getLoadedScripts() {
        return scripts.stream().map(Script::getName).collect(Collectors.toList());
    }

    public static ScriptManager get() {
        if (manager == null)
            manager = new ScriptManager();
        return manager;
    }
}
