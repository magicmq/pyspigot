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
import dev.magicmq.pyspigot.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.scheduler.BukkitTask;
import org.python.core.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Master manager class for PySpigot. Contains all logic to load, unload, and reload scripts.
 * <p>
 * Internally, utilizes Jython's {@link org.python.util.PythonInterpreter} to run scripts.
 * @see Script
 */
public class ScriptManager {

    private static ScriptManager manager;

    private final Path scriptsFolder;
    private final LinkedHashMap<String, Script> scripts;

    private BukkitTask startScriptTask;

    private ScriptManager() {
        scriptsFolder = PySpigot.get().getDataFolderPath().resolve("scripts");
        this.scripts = new LinkedHashMap<>();

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

        Py.getSystemState().close();
    }

    /**
     * Loads and runs all scripts contained within the scripts folder. Called on plugin load (I.E. during server start). Loads in the appropriate load order (see {@link Script#compareTo(Script)}).
     */
    public void loadScripts() {
        PySpigot.get().getLogger().log(Level.INFO, "Loading scripts...");

        //Init file names and paths, screen duplicate names
        HashMap<String, Path> scriptFiles = new HashMap<>();
        for (Path path : getAllScriptPaths()) {
            String fileName = path.getFileName().toString();
            Path existing = scriptFiles.putIfAbsent(fileName, path);
            if (existing != null)
                PySpigot.get().getLogger().log(Level.WARNING, "Duplicate script file name '" + fileName + "' with path '" + PySpigot.get().getDataFolderPath().relativize(path) + "'. Conflicts with '" + PySpigot.get().getDataFolderPath().relativize(existing) + "'.");
        }

        //Init scripts and parse options
        SortedSet<Script> toLoad = new TreeSet<>();
        for (Map.Entry<String, Path> entry : scriptFiles.entrySet()) {
            ScriptOptions options;
            try {
                options = new ScriptOptions(entry.getKey());
            } catch (InvalidConfigurationException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when initializing script options for script '" + entry.getKey() + "', the default values will be used for this script.", e);
                options = new ScriptOptions();
            }
            Script script = new Script(entry.getValue(), entry.getKey(), options);
            toLoad.add(script);
        }

        //Run scripts in order with respect to load priority
        for (Script script : toLoad) {
            try {
                loadScript(script);
            } catch (IOException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when loading script '" + script.getName() + "': " + e.getMessage());
            }
        }

        PySpigot.get().getLogger().log(Level.INFO, "Loaded " + scripts.size() + " script(s)!");
    }

    /**
     * Get the {@link ScriptOptions} for a particular script from the script file name.
     * @param name The name of the script to fetch options for. Name should contain the file extension (.py)
     * @return A ScriptOptions object representing the options beloinging to the script, or null if no script file was found that matched the given name
     */
    public ScriptOptions getScriptOptions(String name) {
        Path scriptPath = getScriptPath(name);
        return getScriptOptions(scriptPath);
    }

    /**
     * Get the {@link ScriptOptions} for a particular script from the path pointing to the script file.
     * @param path The path pointing to the script file to get script options for
     * @return A ScriptOptions object representing the options beloinging to the script, or null if no script file was found that matched the given path
     */
    public ScriptOptions getScriptOptions(Path path) {
        if (path != null) {
            String fileName = path.getFileName().toString();
            ScriptOptions options;
            try {
                options = new ScriptOptions(fileName);
            } catch (InvalidConfigurationException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when initializing script options for script '" + fileName + "', the default values will be used for this script.", e);
                options = new ScriptOptions();
            }
            return options;
        } else
            return null;
    }

    /**
     * Load a script with the given name.
     * @param name The file name of the script to load. Name should contain the file extension (.py)
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws IOException If there was an IOException related to loading the script file
     */
    public RunResult loadScript(String name) throws IOException {
        Path scriptPath = getScriptPath(name);
        return loadScript(scriptPath);
    }

    /**
     * Load a script with the given path.
     * @param path The absolute path pointing to the script file to load.
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws IOException If there was an IOException related to loading the script file
     */
    public RunResult loadScript(Path path) throws IOException {
        if (path != null) {
            String fileName = path.getFileName().toString();
            ScriptOptions options;
            try {
                options = new ScriptOptions(fileName);
            } catch (InvalidConfigurationException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when initializing script options for script '" + fileName + "', the default values will be used for this script.", e);
                options = new ScriptOptions();
            }
            Script script = new Script(path, fileName, options);

            return loadScript(script);
        } else
            return RunResult.FAIL_SCRIPT_NOT_FOUND;
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
            PySpigot.get().getLogger().log(Level.WARNING, "Attempted to load script '" + script.getName() + "', but there is already a loaded script with this name.");
            return RunResult.FAIL_DUPLICATE;
        }

        //Check if the script is disabled as per its options in script_options.yml
        if (!script.getOptions().isEnabled()) {
            return RunResult.FAIL_DISABLED;
        }

        //Check if the script's plugin depdendencies are all present on the server
        List<String> unresolvedPluginDependencies = new ArrayList<>();
        for (String dependency : script.getOptions().getPluginDependencies()) {
            if (Bukkit.getPluginManager().getPlugin(dependency) == null) {
                unresolvedPluginDependencies.add(dependency);
            }
        }
        if (!unresolvedPluginDependencies.isEmpty()) {
            PySpigot.get().getLogger().log(Level.WARNING,  "The following plugin dependencies for script '" + script.getName() + "' are missing: " + unresolvedPluginDependencies + ". This script will not be loaded.");
            return RunResult.FAIL_PLUGIN_DEPENDENCY;
        }

        if (PluginConfig.doScriptActionLogging())
            PySpigot.get().getLogger().log(Level.INFO, "Loading script '" + script.getName() + "'");

        scripts.put(script.getName(), script);

        script.prepare();
        try (FileInputStream scriptFileReader = new FileInputStream(script.getFile())) {
            script.initPermissions();

            script.getInterpreter().execfile(scriptFileReader, script.getName());

            PyObject start = script.getInterpreter().get("start");
            if (start instanceof PyFunction)
                start.__call__();

            ScriptLoadEvent eventLoad = new ScriptLoadEvent(script);
            Bukkit.getPluginManager().callEvent(eventLoad);

            if (PluginConfig.doScriptActionLogging())
                PySpigot.get().getLogger().log(Level.INFO, "Loaded script '" + script.getName() + "'");

            return RunResult.SUCCESS;
        } catch (PySyntaxError | PyIndentationError e) {
            handleScriptException(script, e, "Syntax/indentation error");
            script.getLogger().log(Level.SEVERE, "Script unloaded due to a syntax/indentation error.");
            unloadScript(script, true);
            return RunResult.FAIL_ERROR;
        } catch (PyException e) {
            handleScriptException(script, e, "Runtime error");
            script.getLogger().log(Level.SEVERE, "Script unloaded due to a runtime error.");
            unloadScript(script, true);
            return RunResult.FAIL_ERROR;
        } catch (IOException e) {
            scripts.remove(script.getName());
            script.close();
            throw e;
        }
    }

    /**
     * Unload all currently loaded scripts. Unloads in the reverse order that they were loaded (I.E. the opposite of the load order).
     */
    public void unloadScripts() {
        List<Script> toUnload = new ArrayList<>(scripts.values());
        Collections.reverse(toUnload);
        for (Script script : toUnload) {
            ScriptUnloadEvent event = new ScriptUnloadEvent(script, false);
            Bukkit.getPluginManager().callEvent(event);
            stopScript(script, false);

            if (PluginConfig.doScriptActionLogging())
                PySpigot.get().getLogger().log(Level.INFO, "Unloaded script '" + script.getName() + "'");
        }
        scripts.clear();
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

        boolean gracefulStop = stopScript(script, error);

        scripts.remove(script.getName());

        if (PluginConfig.doScriptActionLogging())
            PySpigot.get().getLogger().log(Level.INFO, "Unloaded script '" + script.getName() + "'");

        return gracefulStop;
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
        ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception, !Bukkit.isPrimaryThread());
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

    /**
     * Check if a script with the given name is currently loaded.
     * @param name The name of the script to check. Name should contain the script file extension (.py)
     * @return True if the script is running, false if otherwise
     */
    public boolean isScriptRunning(String name) {
        return scripts.containsKey(name);
    }

    /**
     * Attempts to resolve the absolute path for a script in the scripts folder based on the file name by searching through the scripts folder. Subfolders are also searched. If there are multiple matching files in different subfolders, the first match will be returned.
     * @param name The name of the script file to search for
     * @return The absolute path of the matching file, or null if no matching file was found
     */
    public Path getScriptPath(String name) {
        for (Path path : getAllScriptPaths()) {
            if (path.getFileName().toString().equalsIgnoreCase(name))
                return path;
        }
        return null;
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
     * Get a set of absolute paths corresponding to all script files in the scripts folder (including in subfolders).
     * @return An immutable {@link java.util.SortedSet} of Paths representing the absolute paths of all script files
     */
    public SortedSet<Path> getAllScriptPaths() {
        SortedSet<Path> scripts = new TreeSet<>();

        if (Files.exists(scriptsFolder) && Files.isDirectory(scriptsFolder)) {
            try (Stream<Path> stream = Files.walk(scriptsFolder)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".py"))
                        .forEach(scripts::add);
            } catch (IOException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error fetching script files from scripts folder", e);
            }
        }
        return scripts;
    }

    /**
     * Get a set of file names corresponding to all script files in the scripts folder (including in subfolders). This only returns the names of the files, and does not include the subfolder.
     * @return An immutable {@link java.util.SortedSet} of Strings representing the names of all script files (including in subfolders)
     */
    public SortedSet<String> getAllScriptNames() {
        SortedSet<String> scripts = new TreeSet<>();

        if (Files.exists(scriptsFolder) && Files.isDirectory(scriptsFolder)) {
            try (Stream<Path> stream = Files.walk(scriptsFolder)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".py"))
                        .map(path -> path.getFileName().toString())
                        .forEach(scripts::add);
            } catch (IOException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error fetching script files from scripts folder", e);
            }
        }
        return scripts;
    }

    private boolean stopScript(Script script, boolean error) {
        boolean gracefulStop = true;
        if (!error) {
            PyObject stop = script.getInterpreter().get("stop");
            if (stop instanceof PyFunction) {
                try {
                    stop.__call__();
                } catch (PyException e) {
                    handleScriptException(script, e, "Error when calling stop function");
                    gracefulStop = false;
                }
            }
        }

        script.removePermissions();

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
