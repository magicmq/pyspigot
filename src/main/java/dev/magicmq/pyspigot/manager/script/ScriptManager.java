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
import dev.magicmq.pyspigot.config.ScriptOptions;
import dev.magicmq.pyspigot.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.ScriptSorter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.graalvm.polyglot.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Master manager class for PySpigot. Contains all logic to load, unload, and reload scripts.
 * <p>
 * Internally, utilizes Graal's Polyglot API via a {@link org.graalvm.polyglot.Context} to run scripts. This manager class also creates an {@link org.graalvm.polyglot.Engine} when the plugin loads that is used to build contexts.
 * @see Script
 */
public class ScriptManager {

    private static ScriptManager manager;

    private final File scriptsFolder;
    private final HashMap<String, Script> scripts;
    private final HashMap<Context, Script> scriptContexts;
    private final Engine engine;

    private BukkitTask startScriptTask;

    private ScriptManager() {
        scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        this.scripts = new HashMap<>();
        this.scriptContexts = new HashMap<>();
        this.engine = PluginConfig.getEngineBuilder().build();

        if (PluginConfig.getScriptLoadDelay() > 0L)
            startScriptTask = Bukkit.getScheduler().runTaskLater(PySpigot.get(), this::loadScripts, PluginConfig.getScriptLoadDelay());
        else
            loadScripts();
    }

    /**
     * Called on plugin unload or server shutdown. Gracefully stops and unloads all loaded and running scripts.
     */
    public void shutdown() {
        if (startScriptTask != null)
            startScriptTask.cancel();

        unloadScripts();

        engine.close(true);
    }

    /**
     * Loads and runs all scripts contained within the scripts folder. Called on plugin load (I.E. during server start).
     */
    public void loadScripts() {
        PySpigot.get().getLogger().log(Level.INFO, "Loading scripts...");

        //Init scripts and parse options
        List<Script> toLoad = new ArrayList<>();
        if (this.scriptsFolder.isDirectory()) {
            for (File file : scriptsFolder.listFiles()) {
                if (file.getName().endsWith(".py")) {
                    ScriptOptions options = new ScriptOptions(PySpigot.get().getScriptOptionsConfig().getConfigurationSection(file.getName()));
                    Script script = new Script(file.getName(), file, options);
                    toLoad.add(script);
                }
            }
        }

        //Check that all dependencies are available
        List<String> scriptNames = toLoad.stream().map(Script::getName).collect(Collectors.toList());
        for (Iterator<Script> scriptIterator = toLoad.iterator(); scriptIterator.hasNext();) {
            Script script = scriptIterator.next();
            List<String> dependencies = script.getOptions().getDependencies();
            for (String dependency : dependencies) {
                if (!scriptNames.contains(dependency)) {
                    PySpigot.get().getLogger().log(Level.SEVERE, "Script '" + script.getName() + "' has an unknown dependency '" + dependency + "'. This script will not be loaded.");
                    scriptIterator.remove();
                    scriptNames.remove(script.getName());
                    break;
                }
            }
        }

        //Sort scripts with respect to dependencies
        ScriptSorter sorter = new ScriptSorter(toLoad);
        LinkedList<Script> sorted = sorter.getOptimalLoadOrder();

        //Run scripts in the proper load order
        for (Script script : sorted) {
            try {
                loadScript(script);
            } catch (IOException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when interacting with script file for script '" + script.getName() + "': " + e.getMessage());
            }
        }

        PySpigot.get().getLogger().log(Level.INFO, "Loaded " + scripts.size() + " script(s)!");
    }

    /**
     * Get the {@link ScriptOptions} for a particular script.
     * @param name The name of the script to fetch options for. Name should contain the file extension (.py)
     * @return A ScriptOptions object representing the options beloinging to the specified script
     * @throws FileNotFoundException If a script file was not found in the scripts folder with the given name
     */
    public ScriptOptions getScriptOptions(String name) throws FileNotFoundException {
        File scriptFile = new File(scriptsFolder, name);
        if (scriptFile.exists()) {
            return new ScriptOptions(PySpigot.get().getScriptOptionsConfig().getConfigurationSection(scriptFile.getName()));
        } else
            throw new FileNotFoundException("Script file not found in the scripts folder with the name '" + name + "'");

    }

    /**
     * Load a script with the given name.
     * @param name The file name of the script to load. Name should contain the file extension (.py)
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws FileNotFoundException If a script file was not found in the scripts folder with the given name
     * @throws IOException If there was another IOException related to loading the script file
     */
    public RunResult loadScript(String name) throws IOException {
        File scriptFile = new File(scriptsFolder, name);
        if (scriptFile.exists()) {
            ScriptOptions options = new ScriptOptions(PySpigot.get().getScriptOptionsConfig().getConfigurationSection(scriptFile.getName()));
            Script script = new Script(scriptFile.getName(), scriptFile, options);

            return loadScript(script);
        } else
            throw new FileNotFoundException("Script file not found in the scripts folder with the name '" + name + "'");
    }

    /**
     * Load the given script.
     * @param script The script that should be loaded
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws IOException If there was an IOException related to loading the script file
     */
    public RunResult loadScript(Script script) throws IOException {
        //Check if another script is already running with the same name
        if (scripts.containsKey(script.getName())) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Attempted to load script '" + script.getName() + "', but there is already a loaded script with this name.");
            return RunResult.FAIL_DUPLICATE;
        }

        //Check if the script is disabled as per its options in script_options.yml
        if (!script.getOptions().isEnabled()) {
            return RunResult.FAIL_DISABLED;
        }

        //Check if the script's dependencies are all running
        List<String> unresolvedDependencies = new ArrayList<>();
        for (String dependency : script.getOptions().getDependencies()) {
            if (getScript(dependency) == null) {
                unresolvedDependencies.add(dependency);
            }
        }
        if (!unresolvedDependencies.isEmpty()) {
            PySpigot.get().getLogger().log(Level.SEVERE,  "The following dependencies for script '" + script.getName() + "' are not loaded: " + unresolvedDependencies + ". This script will not load.");
            return RunResult.FAIL_DEPENDENCY;
        }

        if (PluginConfig.doScriptActionLogging())
            PySpigot.get().getLogger().log(Level.INFO, "Loading script '" + script.getName() + "'");

        script.prepare(engine);

        scripts.put(script.getName(), script);
        scriptContexts.put(script.getContext(), script);

        try {
            Source source = Source.newBuilder("python", script.getFile()).name(script.getName()).build();

            try {
                script.getContext().parse(source);
            } catch (PolyglotException e) {
                if (e.isSyntaxError()) {
                    handleScriptException(script, e, "Syntax/indentation error");
                    script.getLogger().log(Level.SEVERE, "Script unloaded due to a syntax/indentation error.");
                } else {
                    script.getLogger().log(Level.SEVERE, "Script unloaded due to an error.");
                }

                unloadScript(script, true);
                return RunResult.FAIL_ERROR;
            }

            try {
                script.getContext().enter();

                script.getContext().eval(source);

                Value start = script.getContext().getBindings("python").getMember("start");
                if (start != null && start.canExecute())
                    start.executeVoid();

                script.getContext().leave();
            } catch (PolyglotException e) {
                handleScriptException(script, e, "Runtime error");
                if (e.isGuestException()) {
                    script.getLogger().log(Level.SEVERE, "Script unloaded due to a runtime error that originated in the script.");
                } else {
                    script.getLogger().log(Level.SEVERE, "Script unloaded due to a runtime error that originated in Java.");
                }

                unloadScript(script, true);
                return RunResult.FAIL_ERROR;
            }

            ScriptLoadEvent eventLoad = new ScriptLoadEvent(script);
            Bukkit.getPluginManager().callEvent(eventLoad);

            if (PluginConfig.doScriptActionLogging())
                PySpigot.get().getLogger().log(Level.INFO, "Loaded script '" + script.getName() + "'");

            return RunResult.SUCCESS;
        } catch (IOException e) {
            scripts.remove(script.getName());
            scriptContexts.remove(script.getContext());
            script.close();
            throw e;
        }
    }

    /**
     * Unload all currently loaded scripts.
     */
    public void unloadScripts() {
        for (Script script : scripts.values()) {
            ScriptUnloadEvent event = new ScriptUnloadEvent(script, false);
            Bukkit.getPluginManager().callEvent(event);
            stopScript(script, false);

            if (PluginConfig.doScriptActionLogging())
                PySpigot.get().getLogger().log(Level.INFO, "Unloaded script '" + script.getName() + "'");
        }
        scripts.clear();
        scriptContexts.clear();
    }

    /**
     * Unload a script with the given name.
     * @param name The name of the script to unload. Name should contain the script file extension (.py)
     * @return True if the script was successfully unloaded, false if otherwise
     */
    public boolean unloadScript(String name) {
        return unloadScript(getScript(name), false);
    }

    /**
     * Unload a given script.
     * @param script The script to unload
     * @param error If the script unload was due to an error, pass true. Otherwise, pass false. This value will be passed on to {@link ScriptUnloadEvent}
     * @return True if the script was successfully unloaded, false if otherwise
     */
    public boolean unloadScript(Script script, boolean error) {
        ScriptUnloadEvent event = new ScriptUnloadEvent(script, error);
        Bukkit.getPluginManager().callEvent(event);

        scripts.remove(script.getName());
        scriptContexts.remove(script.getContext());

        boolean gracefulStop = stopScript(script, error);

        if (PluginConfig.doScriptActionLogging())
            PySpigot.get().getLogger().log(Level.INFO, "Unloaded script '" + script.getName() + "'");

        return gracefulStop;
    }

    /**
     * Handles script errors/exceptions, particularly for script logging purposes. Will also print the python traceback associated with the exception (if the exception originated from a script's code).
     * <p>
     * <b>Note:</b> This method may run asynchronously, if the exception was thrown in an asynchronous context (I.E. during an asynchronous task).
     * @param script The script that threw the error
     * @param exception The PolyglotException that was thrown
     * @param message The message associated with the exception
     */
    public void handleScriptException(Script script, PolyglotException exception, String message) {
        ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);

        if (exception.isInterrupted())
            return;

        if (event.doReportException()) {
            script.getLogger().log(Level.SEVERE, message + ": ");

            if (exception.getGuestObject() != null) {
                Value guestObject = exception.getGuestObject();
                script.getContext().eval("python", "import traceback;traceback.print_exception").executeVoid(guestObject);
            }

            if (exception.isHostException()) {
                script.getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            }
        }
    }

    /**
     * Check if a script with the given name is currently loaded.
     * @param name The name of the script to check. Name should contain the script file extension (.py)
     * @return True if the script is running, false if otherwise
     */
    public boolean isScriptRunning(String name) {
        return scripts.containsKey(name);
    }

    /**
     * Get a {@link Script} object for a loaded and running script
     * @param name The name of the script to get. Name should contain the script file extension (.py)
     * @return The Script object for the script, null if no script is loaded and running with the given name
     */
    public Script getScript(String name) {
        return scripts.get(name);
    }

    /**
     * Gets a script from its {@link org.graalvm.polyglot.Context}. Primarily used for resolving scripts that call PySpigot methods.
     * @param context The context that was entered previously (I.E. method call from a script)
     * @return The script associated with the context
     */
    public Script getScript(Context context) {
        return scriptContexts.get(context);
    }

    /**
     * Get all loaded scripts.
     * @return An immutable set containing all loaded and running scripts
     */
    public Set<Script> getLoadedScripts() {
        return new HashSet<>(scripts.values());
    }

    /**
     * Get the names of all loaded scripts.
     * @return An immutable list containing the names of all loaded and running scripts
     */
    public Set<String> getLoadedScriptNames() {
        return new HashSet<>(scripts.keySet());
    }

    /**
     * Get the engine used for running script contexts. The engine is initialized on plugin load.
     * @return The engine
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Get a set of all script files in the scripts folder.
     * @return An immutable {@link java.util.SortedSet} of Strings containing all script files, sorted in alphabetical order
     */
    public SortedSet<String> getAllScriptNames() {
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

    private boolean stopScript(Script script, boolean error) {
        boolean gracefulStop = true;
        if (!error) {
            try {
                Value stop = script.getContext().getBindings("python").getMember("stop");
                if (stop != null && stop.canExecute())
                    stop.executeVoid();
            } catch (PolyglotException e) {
                if (e.isGuestException()) {
                    script.getLogger().log(Level.SEVERE, "Runtime error occurred that originated in the script when calling stop function.");
                } else {
                    script.getLogger().log(Level.SEVERE, "Runtime error occurred that originated in Java when calling stop function.");
                }
                gracefulStop = false;
            }
        }

        ListenerManager.get().unregisterListeners(script);
        TaskManager.get().stopTasks(script);
        CommandManager.get().unregisterCommands(script);
        DatabaseManager.get().disconnectAll(script);
        RedisManager.get().closeRedisClients(script, false);

        if (PySpigot.get().isProtocolLibAvailable()) {
            ProtocolManager.get().unregisterPacketListeners(script);
            ProtocolManager.get().async().unregisterAsyncPacketListeners(script);
        }

        if (PySpigot.get().isPlaceholderApiAvailable()) {
            PlaceholderManager.get().unregisterPlaceholder(script);
        }

        script.close();

        return gracefulStop;
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
