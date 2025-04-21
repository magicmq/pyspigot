/*
 *    Copyright 2025 magicmq
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

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.exception.ScriptExitException;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.ScriptUtils;
import dev.magicmq.pyspigot.util.logging.JythonLogHandler;
import org.python.core.Py;
import org.python.core.PyBaseCode;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyIndentationError;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.core.PySystemState;
import org.python.core.ThreadState;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Master manager class for PySpigot. Contains all logic to load, unload, and reload scripts.
 * <p>
 * Internally, utilizes Jython's {@link org.python.util.PythonInterpreter} to run scripts.
 * @see Script
 */
public abstract class ScriptManager {

    private static ScriptManager instance;

    private final ScriptInfo scriptInfo;
    private final Path scriptsFolder;
    private final LinkedHashMap<String, Script> scripts;

    private boolean sysInitialized;

    protected ScriptManager(ScriptInfo scriptInfo) {
        instance = this;

        this.scriptInfo = scriptInfo;
        this.scriptsFolder = PyCore.get().getDataFolderPath().resolve("scripts");
        this.scripts = new LinkedHashMap<>();

        this.sysInitialized = false;
        if (PyCore.get().getConfig().loadJythonOnStartup()) {
            initJython();
        }

        if (PyCore.get().getConfig().getScriptLoadDelay() > 0L)
            scheduleStartScriptTask();
        else
            loadScripts();
    }

    /**
     * Schedules and starts the start script task for the script load delay via a platform-specific implementation.
     */
    public abstract void scheduleStartScriptTask();

    /**
     * Cancels the start script task via a platform-specific implementation.
     */
    public abstract void cancelStartScriptTask();

    /**
     * Checks if a script plugin dependency is missing via a platform-specific implementation.
     * @param dependency The name of the dependency to check
     * @return True if the dependency is missing, false if it is present
     */
    public abstract boolean isPluginDependencyMissing(String dependency);

    /**
     * Calls a ScriptExceptionEvent via a platform-specific implementation.
     * @param script The script that threw the exception
     * @param exception The exception that was thrown
     * @return True if the exception should be reported, false if otherwise
     */
    public abstract boolean callScriptExceptionEvent(Script script, PyException exception);

    /**
     * Calls a ScriptLoadEvent via a platform-specific implementation.
     * @param script The script that was loaded
     */
    public abstract void callScriptLoadEvent(Script script);

    /**
     * Calls a ScriptUnloadEvent via a platform-specific implementation.
     * @param script The script that was unloaded
     * @param error True if the unload was due to an error, false if it was not
     */
    public abstract void callScriptUnloadEvent(Script script, boolean error);

    /**
     * Initialize a new ScriptOptions with the default values.
     * <p>
     * This is done in a platform-specific implementation, as initializing script options for Bukkit initializes permissions
     * @return The new ScriptOptions
     */
    public abstract ScriptOptions newScriptOptions();

    /**
     * Initialize a new ScriptOptions using the appropriate values in the script_options.yml file, using the script name to search for the values.
     * <p>
     * This is done in a platform-specific implementation, as initializing script options for Bukkit initializes permissions
     * @param scriptName The name of the script whose script options should be initialized
     * @return The new ScriptOptions
     */
    public abstract ScriptOptions newScriptOptions(String scriptName);

    /**
     * Initialize a new Script via a platform-specific implementation.
     * @param path The path that corresponds to the file where the script lives
     * @param name The name of this script. Should contain its extension (.py)
     * @param options The {@link ScriptOptions} for this script
     * @return The new script
     */
    public abstract Script newScript(Path path, String name, ScriptOptions options);

    /**
     * Initialize script permissions via a platform-specific implementation.
     * @param script The script whose permissions should be initialized
     */
    public abstract void initScriptPermissions(Script script);

    /**
     * Remove script permissions from the server via a platform-specific implementation.
     * @param script The script whose permissions should be removed
     */
    public abstract void removeScriptPermissions(Script script);

    /**
     * Unregisters the script from any platform-specific managers.
     * @param script The script to unregister
     */
    public abstract void unregisterFromPlatformManagers(Script script);

    /**
     * Unloads the script on the main thread by scheduling the unload operation with a platform-specific scheduler.
     * <p>
     * Used in conjunction with {@link #handleScriptException(Script, PyException, String)} to ensure if sys.exit is called from an asynchronous context, the script is unloaded synchronously.
     * @param script The script to unload
     * @param error If the script unload was due to an error, pass true. Otherwise, pass false
     */
    public abstract void unloadScriptOnMainThread(Script script, boolean error);

    /**
     * Initialize Jython. Will only initialize once; subsequent calls to this method have no effect.
     */
    public void initJython() {
        if (!sysInitialized) {
            PyCore.get().getLogger().log(Level.INFO, "Initializing Jython...");

            Logger jythonLogger = Logger.getLogger("org.python");
            jythonLogger.setLevel(Level.parse(PyCore.get().getConfig().jythonLoggingLevel()));
            jythonLogger.addHandler(new JythonLogHandler());

            PySystemState.initialize(
                    System.getProperties(),
                    PyCore.get().getConfig().getJythonProperties(),
                    PyCore.get().getConfig().getJythonArgs(),
                    LibraryManager.get().getClassLoader()
            );

            PyCore.get().getLogger().log(Level.INFO, "Jython initialized!");
            sysInitialized = true;
        }
    }

    /**
     * Called on plugin unload or server shutdown. Gracefully stops and unloads all loaded and running scripts.
     */
    public void shutdown() {
        cancelStartScriptTask();

        unloadScripts();

        Py.getSystemState().close();
    }

    /**
     * Loads and runs all scripts contained within the scripts folder. Called on plugin load (I.E. during server start). Loads in the appropriate load order (see {@link Script#compareTo(Script)}).
     */
    public void loadScripts() {
        PyCore.get().getLogger().log(Level.INFO, "Loading scripts...");

        //Init file names and paths, screen duplicate names
        HashMap<String, Path> scriptFiles = new HashMap<>();
        for (Path path : getAllScriptPaths()) {
            String fileName = path.getFileName().toString();
            Path existing = scriptFiles.putIfAbsent(fileName, path);
            if (existing != null)
                PyCore.get().getLogger().log(Level.WARNING, "Duplicate script file name '" + fileName + "' with path '" + PyCore.get().getDataFolderPath().relativize(path) + "'. Conflicts with '" + PyCore.get().getDataFolderPath().relativize(existing) + "'.");
        }

        //Init scripts and parse options
        SortedSet<Script> toLoad = new TreeSet<>();
        for (Map.Entry<String, Path> entry : scriptFiles.entrySet()) {
            ScriptOptions options = newScriptOptions(entry.getKey());
            Script script = newScript(entry.getValue(), entry.getKey(), options);
            toLoad.add(script);
        }

        //Run scripts in order with respect to load priority
        for (Script script : toLoad) {
            try {
                loadScript(script);
            } catch (IOException e) {
                PyCore.get().getLogger().log(Level.SEVERE, "Error when loading script '" + script.getName() + "': " + e.getMessage());
            }
        }

        PyCore.get().getLogger().log(Level.INFO, "Loaded " + scripts.size() + " script(s)!");
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
            return newScriptOptions(fileName);
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
            ScriptOptions options = getScriptOptions(path);
            Script script = newScript(path, fileName, options);

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
            PyCore.get().getLogger().log(Level.WARNING, "Attempted to load script '" + script.getName() + "', but there is already a loaded script with this name.");
            return RunResult.FAIL_DUPLICATE;
        }

        //Check if the script is disabled as per its options in script_options.yml
        if (!script.getOptions().isEnabled()) {
            return RunResult.FAIL_DISABLED;
        }

        //Check if the script's plugin depdendencies are all present on the server
        List<String> unresolvedPluginDependencies = new ArrayList<>();
        for (String dependency : script.getOptions().getPluginDependencies()) {
            if (isPluginDependencyMissing(dependency)) {
                unresolvedPluginDependencies.add(dependency);
            }
        }
        if (!unresolvedPluginDependencies.isEmpty()) {
            PyCore.get().getLogger().log(Level.WARNING,  "The following plugin dependencies for script '" + script.getName() + "' are missing: " + unresolvedPluginDependencies + ". This script will not be loaded.");
            return RunResult.FAIL_PLUGIN_DEPENDENCY;
        }

        if (PyCore.get().getConfig().doScriptActionLogging())
            PyCore.get().getLogger().log(Level.INFO, "Loading script '" + script.getName() + "'");

        scripts.put(script.getName(), script);

        script.prepare();
        try (FileInputStream scriptFileReader = new FileInputStream(script.getFile())) {
            initScriptPermissions(script);

            script.getInterpreter().execfile(scriptFileReader, script.getName());

            PyObject start = script.getInterpreter().get("start");
            if (start instanceof PyFunction startFunction) {
                Py.setSystemState(script.getInterpreter().getSystemState());
                ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                int args = ((PyBaseCode) startFunction.__code__).co_argcount;
                if (args == 0)
                    startFunction.__call__(threadState);
                else
                    startFunction.__call__(threadState, Py.java2py(script));
            }

            callScriptLoadEvent(script);

            if (PyCore.get().getConfig().doScriptActionLogging())
                PyCore.get().getLogger().log(Level.INFO, "Loaded script '" + script.getName() + "'");

            return RunResult.SUCCESS;
        } catch (PySyntaxError | PyIndentationError e) {
            handleScriptException(script, e, null);
            script.getLogger().log(Level.SEVERE, "Script unloaded due to a syntax/indentation error.");
            unloadScript(script, true);
            return RunResult.FAIL_ERROR;
        } catch (PyException e) {
            if (e.match(Py.SystemExit)) {
                String exitCode = getExitCode(e);
                script.getLogger().log(Level.INFO, "Script exited with exit code '" + exitCode + "'");
                unloadScript(script, false);
                return RunResult.SUCCESS;
            } else {
                handleScriptException(script, e, null);
                script.getLogger().log(Level.SEVERE, "Script unloaded due to a runtime error.");
                unloadScript(script, true);
                return RunResult.FAIL_ERROR;
            }
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
            callScriptUnloadEvent(script, false);
            stopScript(script, false);

            if (PyCore.get().getConfig().doScriptActionLogging())
                PyCore.get().getLogger().log(Level.INFO, "Unloaded script '" + script.getName() + "'");
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
     * @param error If the script unload was due to an error, pass true. Otherwise, pass false. This value will be passed on to a ScriptUnloadEvent
     * @return True if the script was successfully unloaded, false if otherwise
     */
    public boolean unloadScript(Script script, boolean error) {
        callScriptUnloadEvent(script, error);

        boolean gracefulStop = stopScript(script, error);

        scripts.remove(script.getName());

        if (PyCore.get().getConfig().doScriptActionLogging())
            PyCore.get().getLogger().log(Level.INFO, "Unloaded script '" + script.getName() + "'");

        return gracefulStop;
    }

    /**
     * Handles script errors/exceptions, particularly for script logging purposes. Also prints the traceback (and stack trace, if the exception originated in Java code) to the script's logger.
     * <p>
     * If a {@link org.python.core.Py#SystemExit} is caught, the script will be unloaded.
     * @param script The script that threw the exception
     * @param exception The PyException that was thrown
     * @param message The message associated with the exception
     */
    public void handleScriptException(Script script, PyException exception, String message) {
        boolean report = callScriptExceptionEvent(script, exception);
        if (report) {
            try {
                String toLog = "";

                if (message != null) {
                    message += ":\n";
                    toLog += message;
                }

                toLog += ScriptUtils.handleException(exception);

                script.getLogger().log(Level.SEVERE, toLog);
            } catch (ScriptExitException ignored) {
                String exitCode = getExitCode(exception);
                script.getLogger().log(Level.INFO, "Script exited with exit code '" + exitCode + "'");
                unloadScriptOnMainThread(script, false);
            } catch (InvocationTargetException | IllegalAccessException e) {
                script.getLogger().log(Level.SEVERE, "Error when attempting to handle script exception", e);
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
                PyCore.get().getLogger().log(Level.SEVERE, "Error fetching script files from scripts folder", e);
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
                PyCore.get().getLogger().log(Level.SEVERE, "Error fetching script files from scripts folder", e);
            }
        }
        return scripts;
    }

    /**
     * Get the platform-specific script info object (for printing script info via the /pyspigot info command).
     * @return The {@link ScriptInfo}
     */
    public ScriptInfo getScriptInfo() {
        return scriptInfo;
    }

    private boolean stopScript(Script script, boolean error) {
        boolean gracefulStop = true;
        if (!error) {
            PyObject stop = script.getInterpreter().get("stop");
            if (stop instanceof PyFunction stopFunction) {
                try {
                    Py.setSystemState(script.getInterpreter().getSystemState());
                    ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                    int args = ((PyBaseCode) stopFunction.__code__).co_argcount;
                    if (args == 0)
                        stopFunction.__call__(threadState);
                    else
                        stopFunction.__call__(threadState, Py.java2py(script));
                } catch (PyException e) {
                    handleScriptException(script, e, "Error when calling stop function");
                    gracefulStop = false;
                }
            }
        }

        removeScriptPermissions(script);

        ListenerManager.get().unregisterListeners(script);
        TaskManager.get().stopTasks(script);
        CommandManager.get().unregisterCommands(script);
        DatabaseManager.get().disconnectAll(script);
        RedisManager.get().closeRedisClients(script, false);

        unregisterFromPlatformManagers(script);

        script.close();

        return gracefulStop;
    }

    private String getExitCode(PyException exception) {
        PyObject value = exception.value;

        if (PyException.isExceptionInstance(exception.value)) {
            value = value.__findattr__("code");
        }

        String exitStatus;
        if (value instanceof PyInteger) {
            exitStatus = "" + value.asInt();
        } else if (value != Py.None) {
            exitStatus = value.toString();
        } else {
            exitStatus = "" + 0;
        }
        return exitStatus;
    }

    /**
     * Get the singleton instance of this ScriptManager.
     * @return The instance
     */
    public static ScriptManager get() {
        return instance;
    }
}
