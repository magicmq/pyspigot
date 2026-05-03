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
import dev.magicmq.pyspigot.exception.ScriptInitializationException;
import dev.magicmq.pyspigot.jep.BukkitClassEnquirer;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.packetevents.PacketEventsManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.ScriptContext;
import dev.magicmq.pyspigot.util.logging.ScriptAwareOutputStream;
import jep.Jep;
import jep.JepConfig;
import jep.JepException;
import jep.SharedInterpreter;
import jep.python.PyCallable;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Master manager class for PySpigot. Contains all logic to load, unload, and reload scripts.
 * <p>
 * Internally drives a single shared {@link jep.SharedInterpreter} (created and used on a single
 * thread, per JEP's threading rules) and delegates per-script load/unload bookkeeping to the
 * {@code pyspigot_loader} Python module shipped under {@code site-packages}.
 * @see Script
 */
public abstract class ScriptManager {

    private static ScriptManager instance;

    private final ScriptInfo scriptInfo;
    private final Path scriptsFolder;
    private final Path projectsFolder;
    private final LinkedHashMap<Path, Script> scripts;
    private final LinkedHashMap<String, Script> scriptNames;
    private final HashMap<Path, Path> moduleMap;
    private final ThreadLocal<Jep> threadInterpreter;
    private final Set<Jep> attachedInterpreters;

    private SharedInterpreter mainThreadInterpreter;
    private PyCallable defLoad;
    private PyCallable defStop;
    private PyCallable defUnload;
    private PyCallable defIsLoaded;
    private PyCallable defLoadedScripts;

    protected ScriptManager(ScriptInfo scriptInfo) {
        instance = this;

        this.scriptInfo = scriptInfo;
        this.scriptsFolder = PyCore.get().getDataFolderPath().resolve("scripts");
        this.projectsFolder = PyCore.get().getDataFolderPath().resolve("projects");
        this.scripts = new LinkedHashMap<>();
        this.scriptNames = new LinkedHashMap<>();
        this.moduleMap = new HashMap<>();
        this.threadInterpreter = new ThreadLocal<>();
        this.attachedInterpreters = ConcurrentHashMap.newKeySet();

        //TODO Load on startup config option
        initInterpreter();

        if (PyCore.get().getConfig().getScriptLoadDelay() > 0L)
            scheduleStartScriptTask();
        else
            loadScripts();
    }

    /**
     * Schedules and starts the start script task for the script load delay via a platform-specific implementation.
     */
    protected abstract void scheduleStartScriptTask();

    /**
     * Cancels the start script task via a platform-specific implementation.
     */
    protected abstract void cancelStartScriptTask();

    /**
     * Schedules and starts the script load service via a platform-specific implementation.
     * @param service The ScriptLoadService instance to schedule with the platform's scheduler
     */
    protected abstract void scheduleScriptLoadService(ScriptLoadService service);

    /**
     * Cancels the script load service task via a platform-specific implementation.
     */
    protected abstract void cancelScriptLoadService();

    /**
     * Checks if a script plugin dependency is missing via a platform-specific implementation.
     * @param dependency The name of the dependency to check
     * @return True if the dependency is missing, false if it is present
     */
    protected abstract boolean isPluginDependencyMissing(String dependency);

    /**
     * Calls a ScriptExceptionEvent via a platform-specific implementation.
     * @param script The script that threw the exception
     * @param exception The exception that was thrown
     * @return True if the exception should be reported, false if otherwise
     */
    protected abstract boolean callScriptExceptionEvent(Script script, JepException exception);

    /**
     * Calls a ScriptLoadEvent via a platform-specific implementation.
     * @param script The script that was loaded
     */
    protected abstract void callScriptLoadEvent(Script script);

    /**
     * Calls a ScriptUnloadEvent via a platform-specific implementation.
     * @param script The script that was unloaded
     * @param error True if the unload was due to an error, false if it was not
     */
    protected abstract void callScriptUnloadEvent(Script script, boolean error);

    /**
     * Initialize a new ScriptOptions for a single-file script via a platform-specific implementation, using the appropriate values in the script_options.yml file.
     * @param scriptPath The path of the script file whose script options should be initialized
     * @return The new ScriptOptions
     */
    protected abstract ScriptOptions newScriptOptions(Path scriptPath);

    /**
     * Initialize a new ScriptOptions for a multi-file project via a platform-specific implementation, using the appropriate values in the project's project.yml file.
     * @param projectConfigPath The path of the project.yml file to parse that belongs to the project.
     *                          If the project.yml. If the project does not have a project.yml file, pass null, and the default values will be used
     * @return The new ScriptOptions
     */
    protected abstract ScriptOptions newProjectOptions(Path projectConfigPath);

    /**
     * Initialize a new Script via a platform-specific implementation.
     * @param path The path that corresponds to the file where the script lives
     * @param name The name of this script. Should contain its extension (.py)
     * @param options The {@link ScriptOptions} for this script
     * @param project True if the script is a multi-file project, false if it is a single-file script
     * @return The new script
     */
    protected abstract Script newScript(Path path, String name, ScriptOptions options, boolean project);

    /**
     * Initialize script permissions via a platform-specific implementation.
     * @param script The script whose permissions should be initialized
     */
    protected abstract void initScriptPermissions(Script script);

    /**
     * Remove script permissions from the server via a platform-specific implementation.
     * @param script The script whose permissions should be removed
     */
    protected abstract void removeScriptPermissions(Script script);

    /**
     * Unregisters the script from any platform-specific managers.
     * @param script The script to unregister
     */
    protected abstract void unregisterFromPlatformManagers(Script script);

    /**
     * Unloads the script on the main thread by scheduling the unload operation with a platform-specific scheduler.
     * <p>
     * Used in conjunction with {@link #handleScriptException(Script, JepException, String)} to ensure if sys.exit is called from an asynchronous context, the script is unloaded synchronously.
     * @param script The script to unload
     * @param error If the script unload was due to an error, pass true. Otherwise, pass false
     */
    protected abstract void unloadScriptOnMainThread(Script script, boolean error);

    /**
     * Initialize the shared JEP interpreter and import the Python loader module.
     * <p>
     * Called once during construction. Per JEP's threading rules the resulting
     * {@link SharedInterpreter} must only be used on the same thread that created it; on Bukkit
     * that is the primary server thread.
     */
    public void initInterpreter() {
        PyCore.get().getLogger().info("Creating JEP shared interpreter...");

        try (InputStream is = PyCore.get().getResourceAsStream("loader_module.py")) {
            JepConfig jepConfig = new JepConfig();
            jepConfig.setClassEnquirer(new BukkitClassEnquirer());
            jepConfig.redirectStdout(new ScriptAwareOutputStream(false));
            jepConfig.redirectStdErr(new ScriptAwareOutputStream(true));
            SharedInterpreter.setConfig(jepConfig);

            mainThreadInterpreter = new SharedInterpreter();
            this.threadInterpreter.set(mainThreadInterpreter);

            String loaderCode = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            mainThreadInterpreter.exec(loaderCode);
        } catch (JepException | IOException e) {
            throw new RuntimeException("Failed to initialize JEP shared interpreter", e);
        }
    }

    /**
     * Initialize callable functions from the loader module so the ScriptManager can call them later from Java.
     * <p>
     * This should be called once from the loader module only.
     * @param defLoad {@code def load(script_id, java_script, java_logger, main_path, source, project_path=None):}
     * @param defStop {@code def stop(script_id):}
     * @param defUnload {@code def unload(script_id):}
     * @param defIsLoaded {@code def is_loaded(script_id):}
     * @param defLoadedScripts {@code def loaded_scripts():}
     */
    public void initFunctions(PyCallable defLoad, PyCallable defStop, PyCallable defUnload, PyCallable defIsLoaded, PyCallable defLoadedScripts) {
        if (this.defLoad != null)
            throw new UnsupportedOperationException("Loader module functions have already been initialized");

        this.defLoad = defLoad;
        this.defStop = defStop;
        this.defUnload = defUnload;
        this.defIsLoaded = defIsLoaded;
        this.defLoadedScripts = defLoadedScripts;
    }

    /**
     * Get the shared JEP interpreter that backs every script, corresponding to the thread calling this method, via a
     * {@link ThreadLocal} object.
     * <p>
     * Callers must observe JEP's threading constraint: the interpreter is bound to the thread that
     * created it (see {@link #initInterpreter()}).
     * <p>
     * If there is no SharedInterpreter bound to the thread from which this method is called, one will be created via
     * the {@link jep.Interpreter#attach} method from the {@link jep.SharedInterpreter} bound to the main server thread.
     * @return The shared interpreter
     */
    public Jep getThreadInterpreter() {
        Jep interpreter = threadInterpreter.get();
        if (interpreter != null)
            return interpreter;

        Jep attached = (Jep) mainThreadInterpreter.attach(true);
        threadInterpreter.set(attached);
        attachedInterpreters.add(attached);
        return attached;
    }

    /**
     * Called on plugin unload or server shutdown. Gracefully stops and unloads all loaded and running scripts.
     */
    public void shutdown() {
        cancelStartScriptTask();
        cancelScriptLoadService();

        unloadScripts();

        for (Jep jep : attachedInterpreters) {
            try {
                jep.close();
            } catch (JepException e) {
                PyCore.get().getLogger().warn("Error closing attached JEP interpreter on shutdown", e);
            }
        }

        attachedInterpreters.clear();

        if (mainThreadInterpreter != null) {
            try {
                mainThreadInterpreter.close();
            } catch (JepException e) {
                PyCore.get().getLogger().warn("Error closing main JEP interpreter on shutdown", e);
            }
        }
    }

    /**
     * Loads and runs all scripts contained within the scripts folder. Called on plugin load (I.E. during server start). Loads in the appropriate load order (see {@link Script#compareTo(Script)}).
     */
    public void loadScripts() {
        loadScripts(List.of());
    }

    /**
     * Loads and runs all scripts contained within the scripts folder. Called on plugin load (I.E. during server start). Loads in the appropriate load order (see {@link Script#compareTo(Script)}).
     * <p>
     * Takes into account scripts which were previously loaded (via the {@code loadedBefore} parameter) in order to load scripts which have the {@code auto-load} option set to false but were running beforehand.
     * @param loadedBefore A list of script names that represent previously loaded scripts
     */
    public void loadScripts(List<String> loadedBefore) {
        PyCore.get().getLogger().info("Loading scripts/projects...");

        SortedSet<Path> scriptPaths = getAllScriptPaths();
        scriptPaths.addAll(getAllProjectPaths());

        //Init file names and paths, screen duplicate names
        HashMap<String, Path> scriptFiles = new HashMap<>();
        for (Path path : scriptPaths) {
            String fileName = path.getFileName().toString();
            Path existing = scriptFiles.putIfAbsent(fileName, path);
            if (existing != null)
                PyCore.get().getLogger().warn("Duplicate script/project '{}' conflicts with '{}'.", PyCore.get().getDataFolderPath().relativize(path), PyCore.get().getDataFolderPath().relativize(existing));
        }

        //Init scripts and parse options
        SortedSet<Script> toLoad = new TreeSet<>();
        for (Map.Entry<String, Path> entry : scriptFiles.entrySet()) {
            boolean project = Files.isDirectory(entry.getValue());

            ScriptOptions options;
            if (project) {
                options = getProjectOptions(entry.getValue());
            } else
                options = getScriptOptions(entry.getValue());

            //Skip script loading if autoload option is set to false, and it was not previously loaded
            if (!options.isAutoLoad()) {
                if (!loadedBefore.contains(entry.getKey().toLowerCase())) {
                    continue;
                }
            }

            Script script = newScript(entry.getValue(), entry.getKey(), options, project);
            toLoad.add(script);
        }

        //Run scripts in order with respect to load priority
        if (PyCore.get().getConfig().getScriptLoadInterval() > 0L) {
            ScriptLoadService service = new ScriptLoadService(toLoad);
            scheduleScriptLoadService(service);
        } else {
            for (Script script : toLoad) {
                try {
                    if (script.isProject())
                        loadProject(script);
                    else
                        loadScript(script);
                } catch (ScriptInitializationException e) {
                    PyCore.get().getLogger().error("Error when loading script/project '{}'", script.getName(), e);
                }
            }
        }
    }

    /**
     * Get the {@link ScriptOptions} for a particular script from the path pointing to the script file.
     * @param path The path pointing to the script file to get script options for
     * @return A ScriptOptions object representing the options beloinging to the script, or null if no script file was found that matched the given path
     */
    public ScriptOptions getScriptOptions(Path path) {
        return newScriptOptions(path);
    }

    /**
     * Get the {@link ScriptOptions} for a particular project from the path pointing to the project folder.
     * @param path The path pointing to the project folder to get script options for
     * @return A ScriptOptions object representing the options beloinging to the project, or null if no project folder was found that matched the given path
     */
    public ScriptOptions getProjectOptions(Path path) {
        Path projectConfigPath = path.resolve("project.yml");
        if (Files.exists(projectConfigPath)) {
            return newProjectOptions(projectConfigPath);
        } else
            return newProjectOptions(null);
    }

    /**
     * Load a script with the given name.
     * @param name The file name of the script to load. Name should contain the file extension (.py)
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws ScriptInitializationException If there was an error when initializing the script
     */
    public RunResult loadScript(String name) throws ScriptInitializationException {
        Path scriptPath = getScriptPath(name);
        if (scriptPath != null)
            return loadScript(scriptPath);
        else
            return RunResult.FAIL_SCRIPT_NOT_FOUND;
    }

    /**
     * Load a project with the given name.
     * @param name The folder name of the project to load.
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws ScriptInitializationException If there was an error when initializing the project
     */
    public RunResult loadProject(String name) throws ScriptInitializationException {
        Path projectPath = getProjectPath(name);
        if (projectPath != null)
            return loadProject(projectPath);
        else
            return RunResult.FAIL_SCRIPT_NOT_FOUND;
    }

    /**
     * Load a script with the given path.
     * @param path The absolute path pointing to the script file to load.
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws ScriptInitializationException If there was an error when initializing the script
     */
    public RunResult loadScript(Path path) throws ScriptInitializationException {
        String fileName = path.getFileName().toString();
        ScriptOptions options = getScriptOptions(path);
        Script script = newScript(path, fileName, options, false);

        return loadScript(script);
    }

    /**
     * Load a project with the given path.
     * @param path The absolute path pointing to the project folder to load.
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws ScriptInitializationException If there was an error when initializing the project
     */
    public RunResult loadProject(Path path) throws ScriptInitializationException {
        String folderName = path.getFileName().toString();
        ScriptOptions options = getProjectOptions(path);
        Script script = newScript(path, folderName, options, true);

        if (!Files.exists(script.getMainScriptPath()))
            return RunResult.FAIL_NO_MAIN;

        return loadProject(script);
    }

    /**
     * Load the given script.
     * @param script The script that should be loaded
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws ScriptInitializationException If there was an error when initializing the script
     */
    public RunResult loadScript(Script script) throws ScriptInitializationException {
        //Check if another script/project is already running with the same name
        if (scriptNames.containsKey(script.getName().toLowerCase())) {
            PyCore.get().getLogger().warn("Attempted to load script '{}', but there is another loaded script/project with this name.", script.getName());
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
            PyCore.get().getLogger().warn("The following plugin dependencies for script '{}' are missing: {}. This script will not be loaded.", script.getName(), unresolvedPluginDependencies);
            return RunResult.FAIL_PLUGIN_DEPENDENCY;
        }

        if (PyCore.get().getConfig().doScriptActionLogging())
            PyCore.get().getLogger().info("Loading script '{}'", script.getName());

        RunResult result = startScript(script);

        if (result == RunResult.SUCCESS) {
            if (PyCore.get().getConfig().doScriptActionLogging())
                PyCore.get().getLogger().info("Loaded script '{}'", script.getName());
        }

        return result;
    }

    /**
     * Load the given project.
     * @param script The script that should be loaded
     * @return A {@link RunResult} describing the outcome of the load operation
     * @throws ScriptInitializationException If there was an error when initializing the script
     */
    public RunResult loadProject(Script script) throws ScriptInitializationException {
        //Check if another script/project is already running with the same name
        if (scriptNames.containsKey(script.getName().toLowerCase())) {
            PyCore.get().getLogger().warn("Attempted to load project '{}', but there is another loaded script/project with this name.", script.getName());
            return RunResult.FAIL_DUPLICATE;
        }

        //Check if the project is disabled as per its options in its project.yml
        if (!script.getOptions().isEnabled()) {
            return RunResult.FAIL_DISABLED;
        }

        //Check if the project's plugin depdendencies are all present on the server
        List<String> unresolvedPluginDependencies = new ArrayList<>();
        for (String dependency : script.getOptions().getPluginDependencies()) {
            if (isPluginDependencyMissing(dependency)) {
                unresolvedPluginDependencies.add(dependency);
            }
        }
        if (!unresolvedPluginDependencies.isEmpty()) {
            PyCore.get().getLogger().warn("The following plugin dependencies for project '{}' are missing: {}. This project will not be loaded.", script.getName(), unresolvedPluginDependencies);
            return RunResult.FAIL_PLUGIN_DEPENDENCY;
        }

        //Check if the project's main script exists
        if (!Files.exists(script.getMainScriptPath())) {
            PyCore.get().getLogger().warn("Attempted to load project '{}', but the main script file '{}' was not found in the project folder.", script.getName(), script.getMainScriptPath().toString());
            return RunResult.FAIL_NO_MAIN;
        }

        if (PyCore.get().getConfig().doScriptActionLogging())
            PyCore.get().getLogger().info("Loading project '{}'", script.getName());

        RunResult result = startScript(script);

        if (result == RunResult.SUCCESS) {
            if (PyCore.get().getConfig().doScriptActionLogging())
                PyCore.get().getLogger().info("Loaded project '{}'", script.getName());
        }

        return result;
    }

    /**
     * Unload all currently loaded scripts and projects. Unloads in the reverse order that they were loaded (I.E. the opposite of the load order).
     * @return A list of the names of scripts that were unloaded
     */
    public List<String> unloadScripts() {
        List<String> loaded = new ArrayList<>();
        List<Script> toUnload = new ArrayList<>(scripts.values());
        Collections.reverse(toUnload);
        for (Script script : toUnload) {
            callScriptUnloadEvent(script, false);
            stopScript(script, false);

            loaded.add(script.getName().toLowerCase());

            if (PyCore.get().getConfig().doScriptActionLogging()) {
                if (script.isProject())
                    PyCore.get().getLogger().info("Unloaded project '{}'", script.getName());
                else
                    PyCore.get().getLogger().info("Unloaded script '{}'", script.getName());
            }
        }
        scripts.clear();
        scriptNames.clear();
        moduleMap.clear();

        return loaded;
    }

    /**
     * Unload a script/project with the given name.
     * @param name The name of the script/project to unload. Name should contain the script file extension (.py) if unloading a single-file script.
     * @return True if the script was successfully unloaded, false if otherwise
     */
    public boolean unloadScript(String name) {
        return unloadScript(getScriptByName(name), false);
    }

    /**
     * Unload a given script/project.
     * @param script The script/project to unload
     * @param error If the script/project unload was due to an error, pass true. Otherwise, pass false. This value will be passed on to a ScriptUnloadEvent
     * @return True if the script/project was successfully unloaded, false if otherwise
     */
    public boolean unloadScript(Script script, boolean error) {
        callScriptUnloadEvent(script, error);

        boolean gracefulStop = stopScript(script, error);

        scripts.remove(script.getMainScriptPath());
        scriptNames.remove(script.getName().toLowerCase());
        script.getModules().forEach(moduleMap::remove);

        if (PyCore.get().getConfig().doScriptActionLogging()) {
            if (script.isProject())
                PyCore.get().getLogger().info("Unloaded project '{}'", script.getName());
            else
                PyCore.get().getLogger().info("Unloaded script '{}'", script.getName());
        }

        return gracefulStop;
    }

    /**
     * Handles script errors/exceptions, particularly for script logging purposes.
     * <p>
     * The JEP exception's {@link Throwable#getMessage()} carries the formatted Python traceback,
     * which is what we surface to the script's logger.
     * @param script The script that threw the exception
     * @param exception The {@link JepException} that was thrown
     * @param message An optional context message to prefix the traceback with
     */
    public void handleScriptException(Script script, JepException exception, String message) {
        boolean report = callScriptExceptionEvent(script, exception);
        if (report) {
            StringBuilder toLog = new StringBuilder();
            if (message != null) {
                toLog.append(message).append(":\n");
            }

            StringWriter trace = new StringWriter();
            exception.printStackTrace(new PrintWriter(trace));
            toLog.append(trace);

            script.getLogger().error(toLog.toString());
        }
    }

    /**
     * Check if a script or project with the given name is currently loaded. Names are case-insensitive.
     * @param name The name of the script to check. For single-file scripts, the name should contain the script file extension (.py)
     * @return True if the script is running, false if otherwise
     */
    public boolean isScriptRunning(String name) {
        return scriptNames.containsKey(name.toLowerCase());
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
     * Attempts to resolve the absolute path for a project in the projects folder based on the project folder name.
     * @param name The name of the project to search for
     * @return The absolute path of the matching project folder, or null if no matching folder was found
     */
    public Path getProjectPath(String name) {
        for (Path path : getAllProjectPaths()) {
            if (path.getFileName().toString().equalsIgnoreCase(name))
                return path;
        }
        return null;
    }

    /**
     * Get a {@link Script} object for a loaded and running script (or module, if the module belongs to a project).
     * @param path The path of the script/module to get
     * @return The Script object for the script/module, or null if there is no script
     */
    public Script getScriptByPath(Path path) {
        //First just try searching scripts
        Script script = scripts.get(path);

        //Path is a non-main module in a project. Need to fetch the project's main module from the moduleMap
        if (script == null) {
            Path mainPath = moduleMap.get(path);
            if (mainPath != null)
                return scripts.get(mainPath);
            else
                return null;
        } else
            return script;
    }

    /**
     * Get a {@link Script} object for a loaded and running script.
     * @param name The name of the script to get. Name should contain the script file extension (.py)
     * @return The Script object for the script, null if no script is loaded and running with the given name
     */
    public Script getScriptByName(String name) {
        return scriptNames.get(name.toLowerCase());
    }

    /**
     * Get all loaded scripts.
     * @return An immutable set containing all loaded and running scripts
     */
    public Set<Script> getLoadedScripts() {
        return Set.copyOf(scripts.values());
    }

    /**
     * Get the names of all loaded scripts.
     * @return An immutable set containing the names of all loaded and running scripts
     */
    public Set<String> getLoadedScriptNames() {
        return scripts.values()
                .stream()
                .map(Script::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Get a set of absolute paths corresponding to all script files in the scripts folder (including in subfolders).
     * @return A {@link java.util.SortedSet} of Paths representing the absolute paths of all script files. Sorting is based on the {@link Comparable} implementation of Path.
     */
    public SortedSet<Path> getAllScriptPaths() {
        SortedSet<Path> scripts = new TreeSet<>();

        if (Files.exists(scriptsFolder) && Files.isDirectory(scriptsFolder)) {
            try (Stream<Path> stream = Files.walk(scriptsFolder)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".py"))
                        .map(Path::toAbsolutePath)
                        .forEach(scripts::add);
            } catch (IOException e) {
                PyCore.get().getLogger().error("Error fetching script files from scripts folder", e);
            }
        }
        return scripts;
    }

    /**
     * Get a set of absolute paths corresponding to all script projects in the projects folder.
     * @return An {@link java.util.SortedSet} of Paths representing the absolute paths of all project folders. Sorting is based on the {@link Comparable} implementation of Path.
     */
    public SortedSet<Path> getAllProjectPaths() {
        SortedSet<Path> projects = new TreeSet<>();

        if (Files.exists(projectsFolder) && Files.isDirectory(projectsFolder)) {
            try (Stream<Path> stream = Files.list(projectsFolder)) {
                projects.addAll(stream.filter(Files::isDirectory).map(Path::toAbsolutePath).toList());
            } catch (IOException e) {
                PyCore.get().getLogger().error("Error fetching project folders", e);
            }
        }
        return projects;
    }

    /**
     * Get a set of script names corresponding to all script files in the scripts folder (including in subfolders).
     * <p>
     * This method only returns the names of the files, and does not include the subfolder.
     * @return An {@link java.util.SortedSet} of Strings representing the names of all script files (including in subfolders). Sorting is performed in alphabetical order.
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
                PyCore.get().getLogger().error("Error fetching script files from scripts folder", e);
            }
        }
        return scripts;
    }

    /**
     * Get a set of project names corresponding to all project folders in the projects folder.
     * @return An {@link java.util.SortedSet} of Strings representing the names of all project folders. Sorting is performed in alphabetical order.
     */
    public SortedSet<String> getAllProjectNames() {
        SortedSet<String> projects = new TreeSet<>();

        if (Files.exists(projectsFolder) && Files.isDirectory(projectsFolder)) {
            try (Stream<Path> stream = Files.list(projectsFolder)) {
                stream.filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .forEach(projects::add);
            } catch (IOException e) {
                PyCore.get().getLogger().error("Error fetching project folders", e);
            }
        }
        return projects;
    }

    /**
     * Get the platform-specific script info object (for printing script info via the /pyspigot info command).
     * @return The {@link ScriptInfo}
     */
    public ScriptInfo getScriptInfo() {
        return scriptInfo;
    }

    protected void finishScriptLoading() {
        cancelScriptLoadService();
        PyCore.get().getLogger().info("Loaded {} scripts/projects!", scripts.size());
    }

    private RunResult startScript(Script script) throws ScriptInitializationException {
        scripts.put(script.getMainScriptPath(), script);
        scriptNames.put(script.getName().toLowerCase(), script);

        try {
            script.prepare();
        } catch (ScriptInitializationException e) {
            scripts.remove(script.getMainScriptPath(), script);
            scriptNames.remove(script.getName().toLowerCase(), script);
            throw e;
        }

        script.getModules().forEach(module -> moduleMap.put(module, script.getMainScriptPath()));

        String source;
        try {
            source = Files.readString(script.getMainScriptPath());
        } catch (IOException e) {
            scripts.remove(script.getMainScriptPath());
            scriptNames.remove(script.getName().toLowerCase());
            script.getModules().forEach(moduleMap::remove);
            script.close();
            throw new ScriptInitializationException(script, "Error when reading script file", e);
        }

        initScriptPermissions(script);

        Object[] loaderArgs = new Object[]{
                script.getName(),
                script,
                script.getLogger(),
                script.getMainScriptPath().toString(),
                source,
                script.isProject() ? script.getPath().toString() : null
        };

        try {
            ScriptContext.runWith(script, () -> {
                try {
                    defLoad.call(loaderArgs);
                } catch (JepException e) {
                    throw new LoaderInvocationException(e);
                }
            });
            callScriptLoadEvent(script);
            return RunResult.SUCCESS;
        } catch (LoaderInvocationException wrapped) {
            JepException e = wrapped.cause;
            if (isPythonSystemExit(e)) {
                String exitCode = extractSystemExitCode(e);
                script.getLogger().info("Script exited with exit code '{}'", exitCode);
                unloadScript(script, !"0".equals(exitCode));
                return "0".equals(exitCode) ? RunResult.SUCCESS : RunResult.FAIL_ERROR;
            }
            handleScriptException(script, e, null);
            script.getLogger().error("Script unloaded due to a runtime error.");
            unloadScript(script, true);
            return RunResult.FAIL_ERROR;
        }
    }

    private boolean stopScript(Script script, boolean error) {
        boolean gracefulStop = true;

        if (!error) {
            try {
                Object failures = ScriptContext.supplyWith(script, () -> {
                    try {
                        return defStop.call(script.getName());
                    } catch (JepException e) {
                        throw new LoaderInvocationException(e);
                    }
                });
                if (failures instanceof List<?> failureList && !failureList.isEmpty()) {
                    gracefulStop = false;
                    for (Object failure : failureList) {
                        script.getLogger().error("Error when calling stop function: {}", failure);
                    }
                }
            } catch (LoaderInvocationException wrapped) {
                handleScriptException(script, wrapped.cause, "Error when running stop hooks");
                gracefulStop = false;
            }
        }

        removeScriptPermissions(script);

        ListenerManager.get().unregisterListeners(script);
        TaskManager.get().stopTasks(script);
        CommandManager.get().unregisterCommands(script);
        DatabaseManager.get().disconnectAll(script);
        RedisManager.get().closeRedisClients(script, false);

        if (PyCore.get().isPacketEventsAvailable())
            PacketEventsManager.get().unregisterPacketListeners(script);

        unregisterFromPlatformManagers(script);

        try {
            defUnload.call(script.getName());
        } catch (JepException e) {
            PyCore.get().getLogger().warn("Error tearing down loader state for script '{}'", script.getName(), e);
            gracefulStop = false;
        }

        script.close();

        return gracefulStop;
    }

    /**
     * Best-effort check that a {@link JepException} is wrapping a Python {@code SystemExit}.
     * <p>
     * JEP surfaces this in the message as a {@code SystemExit} prefix; there is no direct typed
     * accessor on the exception, so we match on the type name.
     */
    private boolean isPythonSystemExit(JepException exception) {
        String message = exception.getMessage();
        return message != null && message.contains("SystemExit");
    }

    /**
     * Extract a numeric exit code from a Python {@code SystemExit} surfaced via JEP.
     * Returns "0" when no usable code can be parsed (matches the legacy default).
     */
    private String extractSystemExitCode(JepException exception) {
        String message = exception.getMessage();
        if (message == null)
            return "0";

        int colon = message.indexOf(':');
        if (colon < 0)
            return "0";

        String rest = message.substring(colon + 1).trim();
        int newline = rest.indexOf('\n');
        if (newline >= 0)
            rest = rest.substring(0, newline).trim();

        return rest.isEmpty() ? "0" : rest;
    }

    /**
     * Get the singleton instance of this ScriptManager.
     * @return The instance
     */
    public static ScriptManager get() {
        return instance;
    }

    /**
     * Unchecked carrier so we can pop a {@link JepException} back out of the
     * {@link ScriptContext} lambdas without polluting their {@code Runnable}/{@code Supplier}
     * signatures with a checked throws clause.
     */
    private static final class LoaderInvocationException extends RuntimeException {
        private final JepException cause;

        LoaderInvocationException(JepException cause) {
            super(cause);
            this.cause = cause;
        }
    }
}
