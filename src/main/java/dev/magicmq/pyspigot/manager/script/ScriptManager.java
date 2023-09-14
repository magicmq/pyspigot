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
import dev.magicmq.pyspigot.event.ScriptRunEvent;
import dev.magicmq.pyspigot.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.ScriptSorter;
import org.bukkit.Bukkit;
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

    private PySystemState systemState;
    private final Set<Script> scripts;
    private final HashMap<String, PyObject> globalVariables;

    private final BukkitTask startScriptTask;

    private ScriptManager() {
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
     * Load a script with the given name. This only loads the script (compiles script code and reads options); it does not run the script.
     * @param name The file name of the script to load. Name should contain the file extension (.py)
     * @return A script object representing the script that was loaded, or null if there was an error when loading the script
     * @throws IOException If there was an IOException related to loading the script file
     */
    public Script loadScript(String name) throws IOException {
        if (getScript(name) != null) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Attempted to load script " + name + ", but there is already a loaded script with this name. Script names must be unique.");
            return null;
        }

        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        File scriptFile = new File(scriptsFolder, name);
        try (FileReader reader = new FileReader(scriptFile)) {
            PythonInterpreter interpreter = initNewInterpreter();
            try {
                ScriptOptions options = new ScriptOptions(PySpigot.get().getScriptOptionsConfig().getConfigurationSection(name));
                Script script = new Script(scriptFile.getName(), options, interpreter, interpreter.compile(reader, scriptFile.getName()), scriptFile);

                ScriptLoadEvent eventLoad = new ScriptLoadEvent(script);
                Bukkit.getPluginManager().callEvent(eventLoad);

                return script;
            } catch (PySyntaxError | PyIndentationError e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when parsing script " + scriptFile.getName() + ": " + e.getMessage());
                interpreter.close();
                return null;
            }
        }
    }

    /**
     * Run a script. This will also check if the script's dependencies are loaded and running prior to running the script.
     * @param script The script to run
     * @return True if the script ran successfully, or false if the script was not run, which could be due to missing dependencies, runtime error, or script disabled as per script options
     */
    public RunResult runScript(Script script) {
        if (!script.getOptions().isEnabled()) {
            script.close();
            return RunResult.FAIL_DISABLED;
        }

        List<String> unresolvedDependencies = getUnresolvedDependencies(script);
        if (unresolvedDependencies.size() > 0) {
            PySpigot.get().getLogger().log(Level.SEVERE,  "The following dependencies for script '" + script.getName() + "' are not running: " + unresolvedDependencies + ". This script will not load.");
            script.close();
            return RunResult.FAIL_DEPENDENCY;
        }

        this.scripts.add(script);

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

            ScriptRunEvent event = new ScriptRunEvent(script);
            Bukkit.getPluginManager().callEvent(event);
        } catch (PyException e) {
            handleScriptException(script, e, "Runtime error");
            script.getLogger().log(Level.SEVERE, "Script unloaded due to a runtime error.");
            unloadScript(script, true);
            return RunResult.FAIL_ERROR;
        }
        return RunResult.SUCCESS;
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
        if (!unloadScript(name))
            return false;

        Script newScript = loadScript(name);
        if (newScript != null)
            return runScript(newScript) == RunResult.SUCCESS;
        else
            return false;
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
     * Check if a script with the given name is currently running.
     * @param name The name of the script to check. Name should contain the script file extension (.py)
     * @return True if the script is running, false if otherwise
     */
    public boolean isScriptRunning(String name) {
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
     * Attempts to get the script involved in a Java method call by analyzing the call stack.
     * @return The script associated with the method call, or null if no script was found in th call stack
     */
    public Script getScriptFromCallStack() {
        Optional<StackWalker.StackFrame> callingScript = StackWalker.getInstance().walk(stream -> stream.filter(frame -> frame.getClassName().contains("org.python.pycode") && frame.getMethodName().equals("call_function")).findFirst());
        if (callingScript.isPresent()) {
            String scriptName = callingScript.get().getFileName();
            return getScript(scriptName);
        } else {
            return null;
        }
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

        List<Script> loadedScripts = new ArrayList<>();
        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        if (scriptsFolder.isDirectory()) {
            SortedSet<File> toLoad = new TreeSet<>();
            toLoad.addAll(Arrays.asList(scriptsFolder.listFiles()));
            for (File script : toLoad) {
                if (script.getName().endsWith(".py")) {
                    try {
                        Script loaded = loadScript(script.getName());
                        if (loaded != null)
                            loadedScripts.add(loaded);
                    } catch (IOException e) {
                        PySpigot.get().getLogger().log(Level.SEVERE, "IOException when reading script file '" + script.getName() + "': " + e.getMessage() + ". Does the file exist?");
                    }
                }
            }
        }

        checkDependencies(loadedScripts);

        PySpigot.get().getLogger().log(Level.INFO, "Found and loaded " + loadedScripts.size() + " script(s)!");

        runScripts(loadedScripts);
    }

    private void runScripts(List<Script> scripts) {
        PySpigot.get().getLogger().log(Level.INFO, "Running scripts...");

        int running = 0;
        ScriptSorter sorter = new ScriptSorter(scripts);
        LinkedList<Script> loadOrder = sorter.getOptimalLoadOrder();
        for (Script script : loadOrder) {
            if (runScript(script) == RunResult.SUCCESS)
                running++;
        }

        PySpigot.get().getLogger().log(Level.INFO, running + " script(s) are now running!");
    }

    private void checkDependencies(List<Script> scripts) {
        List<String> scriptNames = scripts.stream().map(Script::getName).collect(Collectors.toList());
        for (Iterator<Script> scriptIterator = scripts.iterator(); scriptIterator.hasNext();) {
            Script script = scriptIterator.next();
            List<String> dependencies = script.getOptions().getDependencies();
            for (String dependency : dependencies) {
                if (!scriptNames.contains(dependency)) {
                    PySpigot.get().getLogger().log(Level.SEVERE, "Script '" + script.getName() + "' has an unknown dependency '" + dependency + "'. This script will not be loaded.");
                    scriptIterator.remove();
                    script.close();
                    break;
                }
            }
        }
    }

    private List<String> getUnresolvedDependencies(Script script) {
        List<String> unresolved = new ArrayList<>();
        for (String dependency : script.getOptions().getDependencies()) {
            if (getScript(dependency) == null) {
                unresolved.add(dependency);
            }
        }
        return unresolved;
    }

    private PythonInterpreter initNewInterpreter() {
        PythonInterpreter interpreter = new PythonInterpreter(null, systemState);

        interpreter.set("global", globalVariables);

        return interpreter;
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

        if (!error) {
            try {
                if (script.getStopFunction() != null)
                    script.getStopFunction().__call__();
            } catch (PyException e) {
                handleScriptException(script, e, "Error when executing stop function");
                script.close();
                return false;
            }
        }

        script.close();

        return true;
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
