/*
 *    Copyright 2023 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.manager.script;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.event.ScriptPostLoadEvent;
import dev.magicmq.pyspigot.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Master manager class for loading and unloading scripts.
 * <p>
 * Scripts should not access this manager under normal circumstances. Other plugins may use this class to work with scripts.
 * <p>
 * Internally, utilizes Jython's {@link org.python.util.PythonInterpreter} to run scripts.
 * @see Script
 */
public class ScriptManager {

    private static ScriptManager manager;

    private FileConfiguration scriptOptions;

    private PySystemState systemState;
    private final Set<Script> scripts;
    private final HashMap<String, PyObject> globalVariables;

    private final BukkitTask startScriptTask;

    private ScriptManager() {
        File file = new File(PySpigot.get().getDataFolder(), "script_options.yml");
        if (file.exists()) {
            scriptOptions = YamlConfiguration.loadConfiguration(file);
        }

        this.systemState = new PySystemState();
        systemState.setClassLoader(LibraryManager.get().getClassLoader());

        this.scripts = new HashSet<>();
        this.globalVariables = new HashMap<>();

        File scripts = new File(PySpigot.get().getDataFolder(), "scripts");
        if (!scripts.exists())
            scripts.mkdir();

        File logs = new File(PySpigot.get().getDataFolder(), "logs");
        if (!logs.exists())
            logs.mkdir();

        startScriptTask = Bukkit.getScheduler().runTaskLater(PySpigot.get(), this::loadScripts, PluginConfig.getLoadScriptDelay());
    }

    /**
     * Called on plugin unload or server shutdown. Gracefully stops and unloads all loaded and running scripts.
     */
    public void shutdown() {
        startScriptTask.cancel();

        for (Script script : scripts) {
            ScriptUnloadEvent event = new ScriptUnloadEvent(script, false);
            Bukkit.getPluginManager().callEvent(event);
            stopScript(script, false);
        }
    }

    /**
     * Load a script with the given name.
     * @param name The file name of the script to load. Name should contain the file extension (.py)
     * @return True if the script was successfully loaded, false if otherwise
     * @throws IOException If there was an IOException related to loading the script file
     */
    public boolean loadScript(String name) throws IOException {
        if (getScript(name) != null) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Attempted to load script " + name + ", but there is already a loaded script with this name. Script names must be unique.");
            return false;
        }

        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        File scriptFile = new File(scriptsFolder, name);
        try (FileReader reader = new FileReader(scriptFile)) {
            PythonInterpreter interpreter = initNewInterpreter();
            try {
                ScriptOptions options = getScriptOptions(name);
                Script script = new Script(scriptFile.getName(), options, interpreter, interpreter.compile(reader, scriptFile.getName()), scriptFile);

                ScriptLoadEvent eventLoad = new ScriptLoadEvent(script);
                Bukkit.getPluginManager().callEvent(eventLoad);

                this.scripts.add(script);

                return runScript(script);
            } catch (PySyntaxError | PyIndentationError e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when parsing script " + scriptFile.getName() + ": " + e.getMessage());
                interpreter.close();
                return false;
            }
        }
    }

    /**
     * Unload a script with the given name
     * @param name The name of the script to unload. Name should contain the script file extension (.py)
     * @return True if the script was successfully unloaded, false if otherwise
     */
    public boolean unloadScript(String name) {
        return unloadScript(getScript(name), false);
    }

    /**
     * Unload a given script.
     * @param script The script to unload
     * @param error If the script unload was due to an error, pass true. Otherwise, pass false. This value will be passed to {@link ScriptUnloadEvent}
     * @return True if the script was successfully unloaded, false if otherwise
     */
    public boolean unloadScript(Script script, boolean error) {
        ScriptUnloadEvent event = new ScriptUnloadEvent(script, error);
        Bukkit.getPluginManager().callEvent(event);
        scripts.remove(script);
        return stopScript(script, error);
    }

    /**
     * Reload a loaded and runnnig script.
     * @param name The name of the script to reload. Name should contain the script file extension (.py)
     * @return True if the script was successfully reloaded, false if otherwise
     * @throws IOException If there was an IOException related to loading the script file
     */
    public boolean reloadScript(String name) throws IOException {
        Script script = getScript(name);

        if (!unloadScript(name))
            return false;

        return loadScript(script.getName());
    }

    /**
     * Handles script errors/exceptions, particularly for script logging purposes. Will also attempt to get the traceback of the {@link org.python.core.PyException} that was thrown and print it (if it exists).
     * <p>
     * <b>Note:</b> This method will always run synchronously. If it is called from an asynchronous context, it will run inside a synchronous task.
     * @param script The script that threw the error
     * @param exception The PyException that was thrown
     * @param message The message associated with the exception
     */
    public void handleScriptException(Script script, PyException exception, String message) {
        if (!Bukkit.isPrimaryThread()) {
            //This method could be called asynchronously (i.e. from a ProtocolLib listener). We need to run it synchronously to call ScriptExceptionEvent.
            Bukkit.getScheduler().runTask(PySpigot.get(), () -> ScriptManager.this.handleScriptException(script, exception, message));
        } else {
            ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception);
            Bukkit.getPluginManager().callEvent(event);
            if (event.doReportException()) {
                String toLog = "";
                toLog += message + ": ";

                if (exception.getCause() != null) {
                    Throwable cause = exception.getCause();
                    toLog += cause;

                    if (cause.getCause() != null) {
                        Throwable causeOfCause = cause.getCause();
                        toLog += "\n" + "Caused by: " + causeOfCause;
                    }
                } else {
                    toLog += exception.getMessage();
                }

                if (exception.traceback != null) {
                    toLog += "\n\n" + exception.traceback.dumpStack();
                }

                script.getLogger().log(Level.SEVERE, toLog);
            }

            if (PluginConfig.shouldPrintStackTraces())
                exception.printStackTrace();
        }
    }

    /**
     * Check if a script with the given name is currently loaded and running.
     * @param name The name of the script to check. Name should contain the script file extension (.py)
     * @return True if the script is loaded and running, false if otherwise
     */
    public boolean isScriptLoaded(String name) {
        return getScript(name) != null;
    }

    /**
     * Get a {@link Script} object for a loaded and running script
     * @param name The name of the script to get. Name should contain the script file extension (.py)
     * @return The Script object for the script, null if no script is loaded and running with the given name
     */
    public Script getScript(String name) {
        for (Script script : scripts) {
            if (script.getName().equals(name))
                return script;
        }
        return null;
    }

    /**
     * Get all loaded scripts.
     * @return An immutable set containing all loaded and running scripts
     */
    public Set<Script> getLoadedScripts() {
        return new HashSet<>(scripts);
    }

    /**
     * Get the names of all loaded scripts.
     * @return An immutable list containing the names of all loaded and running scripts
     */
    public Set<String> getLoadedScriptNames() {
        return scripts.stream().map(Script::getName).collect(Collectors.toSet());
    }

    /**
     * Get a set of all script files in the scripts folder.
     * @return An immutable {@link SortedSet} of Strings containing all script files, sorted in alphabetical order
     */
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

    private void loadScripts() {
        PySpigot.get().getLogger().log(Level.INFO, "Loading scripts...");
        int numOfScripts = 0;
        int errorScripts = 0;
        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        if (scriptsFolder.isDirectory()) {
            SortedSet<File> toLoad = new TreeSet<>();
            toLoad.addAll(Arrays.asList(scriptsFolder.listFiles()));
            for (File script : toLoad) {
                if (script.getName().endsWith(".py")) {
                    try {
                        if (loadScript(script.getName()))
                            numOfScripts++;
                        else
                            errorScripts++;
                    } catch (IOException e) {
                        PySpigot.get().getLogger().log(Level.SEVERE, "Error when loading script file " + script.getName() + ". Does the file exist?", e);
                        errorScripts++;
                    }
                }
            }
        }
        PySpigot.get().getLogger().log(Level.INFO, "Found and loaded " + numOfScripts + " scripts!");
        if (errorScripts > 0)
            PySpigot.get().getLogger().log(Level.INFO, errorScripts + " scripts were not loaded due to errros.");
    }

    private PythonInterpreter initNewInterpreter() {
        PythonInterpreter interpreter = new PythonInterpreter(null, systemState);

        interpreter.set("global", globalVariables);

        return interpreter;
    }

    private boolean runScript(Script script) {
        try {
            script.getInterpreter().exec(script.getCode());

            PyObject start = script.getInterpreter().get("start");
            if (start != null) {
                if (start instanceof PyFunction)
                    script.setStartFunction((PyFunction) start);
                else {
                    script.getLogger().log(Level.WARNING, "'start' is defined, but it is not a function. Is this a mistake?");
                }
            }

            PyObject stop = script.getInterpreter().get("stop");
            if (stop != null) {
                if (stop instanceof PyFunction)
                    script.setStopFunction((PyFunction) stop);
                else {
                    script.getLogger().log(Level.WARNING, "'stop' is defined, but it is not a function. Is this a mistake?");
                }
            }

            if (script.getStartFunction() != null)
                script.getStartFunction().__call__();

            ScriptPostLoadEvent event = new ScriptPostLoadEvent(script);
            Bukkit.getPluginManager().callEvent(event);
        } catch (PyException e) {
            handleScriptException(script, e, "Runtime error");
            script.getLogger().log(Level.SEVERE, "Script unloaded due to a runtime error.");
            unloadScript(script, true);
            return false;
        }
        return true;
    }

    private boolean stopScript(Script script, boolean error) {
        ListenerManager.get().unregisterListeners(script);
        TaskManager.get().stopTasks(script);
        CommandManager.get().unregisterCommands(script);

        if (PySpigot.get().isProtocolLibAvailable()) {
            ProtocolManager.get().unregisterPacketListeners(script);
            ProtocolManager.get().async().unregisterAsyncPacketListeners(script);
        }

        if (PySpigot.get().isPlaceholderApiAvailable()) {
            PlaceholderManager.get().unregisterPlaceholder(script);
        }

        script.getInterpreter().close();

        if (!error) {
            try {
                if (script.getStopFunction() != null)
                    script.getStopFunction().__call__();
            } catch (PyException e) {
                handleScriptException(script, e, "Error when executing stop function");
                return false;
            }
        }

        script.closeLogger();

        return true;
    }

    private ScriptOptions getScriptOptions(String name) {
        ConfigurationSection options = scriptOptions.getConfigurationSection(name);
        if (options != null) {
            boolean enabled = options.getBoolean("enabled", true);
            long loadDelay = options.getLong("load-delay", 20L);
            List<String> depend = options.getStringList("depend");
            boolean loggingEnabled = options.getBoolean("logging-enabled", true);
            Level loggingLevel = Level.parse(options.getString("logging-level", "INFO"));
            return new ScriptOptions(enabled, loadDelay, depend, loggingEnabled, loggingLevel);
        } else
            return new ScriptOptions();
    }

    /**
     * Get the singleton instance of this ScriptManager.
     * @return The instance
     */
    public static ScriptManager get() {
        if (manager == null)
            manager = new ScriptManager();
        return manager;
    }
}
