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

package dev.magicmq.pyspigot.velocity.manager.command;


import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.ThreadState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VelocityScriptCommand implements ScriptCommand, SimpleCommand {

    private final CommandMeta commandMeta;
    private final Script script;
    private final PyFunction commandFunction;
    private final PyFunction tabFunction;
    private final boolean asyncTabComplete;
    private final String name;
    private final String permission;

    /**
     *
     * @param commandMeta The CommandMeta for this command
     * @param script The script to which this command belongs
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param asyncTabComplete Whether the tab function should be executed asynchronously
     * @param name The name of the command to register
     * @param permission The required permission node to use this command. Can be null
     */
    public VelocityScriptCommand(CommandMeta commandMeta, Script script, PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name, String permission) {
        this.commandMeta = commandMeta;
        this.script = script;
        this.commandFunction = commandFunction;
        this.tabFunction = tabFunction;
        this.asyncTabComplete = asyncTabComplete;
        this.name = name;
        this.permission = permission;
    }

    public CommandMeta getCommandMeta() {
        return commandMeta;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(Invocation invocation) {
        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject parameter = Py.java2py(invocation);
            commandFunction.__call__(threadState, parameter);
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Unhandled exception when executing command '" + getName() + "'");
            //Mimic Velocity behavior
            invocation.source().sendMessage(Component.text("An error occurred while running this command.", NamedTextColor.RED));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject parameter = Py.java2py(invocation);
            PyObject result = tabFunction.__call__(threadState, parameter);
            if (result instanceof PyList pyList) {
                ArrayList<String> toReturn = new ArrayList<>();
                for (Object object : pyList) {
                    if (object instanceof String)
                        toReturn.add((String) object);
                    else {
                        script.getLogger().warn("Script tab complete function '{}' should return a list of str", tabFunction.__name__);
                        return Collections.emptyList();
                    }
                }
                return toReturn;
            }
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Unhandled exception when tab completing command '" + getName() + "'");
        }
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        if (tabFunction != null) {
            if (!asyncTabComplete)
                return CompletableFuture.completedFuture(suggest(invocation));

            return CompletableFuture.supplyAsync(() -> suggest(invocation));
        } else {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(permission);
    }
}
