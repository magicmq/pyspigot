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
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
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

        File logs = new File(PySpigot.get().getDataFolder(), "logs");
        if (!logs.exists())
            logs.mkdir();

        Bukkit.getScheduler().runTaskLater(PySpigot.get(), this::loadScripts, PluginConfig.getLoadScriptDelay());
    }

    public void shutdown() {
        for (Script script : scripts) {
            ScriptUnloadEvent event = new ScriptUnloadEvent(script, false);
            Bukkit.getPluginManager().callEvent(event);
            stopScript(script, false);
        }
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
                        e.printStackTrace();
                        errorScripts++;
                    }
                }
            }
        }
        PySpigot.get().getLogger().log(Level.INFO, "Found and loaded " + numOfScripts + " scripts!");
        if (errorScripts > 0)
            PySpigot.get().getLogger().log(Level.INFO, errorScripts + " scripts were not loaded due to errros.");
    }

    public boolean loadScript(String name) throws IOException {
        if (getScript(name) != null)
            throw new IllegalArgumentException("Attempted to load script " + name + ", but there is already a loaded script with this name. Script names must be unique.");

        File scriptsFolder = new File(PySpigot.get().getDataFolder(), "scripts");
        File scriptFile = new File(scriptsFolder, name);
        try (FileReader reader = new FileReader(scriptFile)) {
            PythonInterpreter interpreter = initNewInterpreter();
            try {
                Script script = new Script(scriptFile.getName(), interpreter, interpreter.compile(reader, scriptFile.getName()), scriptFile);

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

    public boolean unloadScript(String name) {
        return unloadScript(getScript(name), false);
    }

    public boolean unloadScript(Script script, boolean error) {
        ScriptUnloadEvent event = new ScriptUnloadEvent(script, error);
        Bukkit.getPluginManager().callEvent(event);
        scripts.remove(script);
        return stopScript(script, error);
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
            if (exception.getCause() != null && !(exception.getCause() instanceof PyException)) {
                script.getLogger().log(Level.SEVERE, message + ":", exception.getCause());
            } else {
                if (exception.traceback != null)
                    script.getLogger().log(Level.SEVERE, message + ": " + exception.getMessage() + "\n\n" + exception.traceback.dumpStack());
                else
                    script.getLogger().log(Level.SEVERE, message + ": " + exception.getMessage());
            }
        }
    }

    public boolean isScriptLoaded(String name) {
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

        interpreter.set("global", globalVariables);

        if (PluginConfig.doAutoImportBukkit()) {
            interpreter.set("bukkit", Bukkit.class);
        }

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
            unloadScript(script, true);
            script.getLogger().log(Level.SEVERE, "Script unloaded due to a runtime error.");
            return false;
        }
        return true;
    }

    private boolean stopScript(Script script, boolean error) {
        ListenerManager.get().unregisterEvents(script);
        TaskManager.get().stopTasks(script);
        CommandManager.get().unregisterCommands(script);

        if (PySpigot.get().isProtocolLibAvailable())
            ProtocolManager.get().unregisterListeners(script);

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

    public static ScriptManager get() {
        if (manager == null)
            manager = new ScriptManager();
        return manager;
    }
}
