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

package dev.magicmq.pyspigot.bukkit.manager.placeholder;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.ThreadState;

/**
 * A class that represents a script placeholder expansion.
 * <p>
 * A ScriptPlaceholder can have multiple individual placeholders. For example, a script with the name "test.py" could have "%script:test_placeholder1%" and "%script:test_placeholder2%". It will be up to the script to handle each individual placeholder.
 * @see me.clip.placeholderapi.expansion.PlaceholderExpansion
 */
public class ScriptPlaceholder extends PlaceholderExpansion implements Relational {

    private final Script script;
    private final PyFunction function;
    private final String author;
    private final String version;

    private PyFunction relFunction;

    /**
     *
     * @param script The script associated with this ScriptPlaceholder
     * @param function The function to call when the placeholder is used
     * @param relFunction The function to call when the relational placeholder is used
     * @param author The author of this ScriptPlaceholder
     * @param version The version of this ScriptPlaceholder
     */
    public ScriptPlaceholder(Script script, PyFunction function, PyFunction relFunction, String author, String version) {
        this.script = script;
        this.function = function;
        this.relFunction = relFunction;
        this.author = author;
        this.version = version;
    }

    /**
     * Set the relational placeholder function for this placeholder.
     * @param relFunction The relational function
     */
    public void setRelationalFunction(PyFunction relFunction) {
        this.relFunction = relFunction;
    }

    /**
     * Get the script associated with this ScriptPlaceholder.
     * @return The script associated with this ScriptPlaceholder
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the author of this ScriptPlaceholder.
     * @return The author of this ScriptPlaceholder
     */
    @Override
    public String getAuthor() {
        return author;
    }

    /**
     * Get the version of this ScriptPlaceholder.
     * @return The version of this ScriptPlaceholder
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Get the identifier of this ScriptPlaceholder.
     * <p>
     * This is used to identify the script's placeholder. It will be in the format "script:name", where "name" is the name of the script (without the file extension, .py). For example, for a script named "test.py", the placeholder identifier will be "script:test".
     * @return The identifier of this ScriptPlaceholder
     */
    @Override
    public String getIdentifier() {
        return "script:" + script.getSimpleName();
    }

    /**
     * Indicates that the ScriptPlaceholder should persist when PlaceholderAPI is reloaded.
     * @return True
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Called internally when the ScriptPlaceholder is used.
     * @param player The {@link org.bukkit.OfflinePlayer} associated with the placeholder, or null if there is none
     * @param params The specific placeholder that was used (the ScriptPlaceholder expansion can have multiple individual placeholders. Scripts will handle each specific placeholder on their own)
     * @return The replaced text
     */
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (function == null) {
            return null;
        }

        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject[] parameters = Py.javas2pys(player, params);
            PyObject result = function.__call__(threadState, parameters[0], parameters[1]);
            if (result instanceof PyString) {
                return ((PyString) result).getString();
            }
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when executing placeholder '" + getIdentifier() + "'");
        }
        return null;
    }

    /**
     * Called internally when the ScriptPlaceholder is used in a relational fashion.
     * @param playerOne The first {@link org.bukkit.entity.Player} used for the placeholder.
     * @param playerTwo The second {@link org.bukkit.entity.Player} used for the placeholder.
     * @param identifier The specific placeholder that was used, right after the relational aspect.
     * @return The replaced text
     */
    @Override
    public String onPlaceholderRequest(Player playerOne, Player playerTwo, String identifier) {
        if (relFunction == null) {
            return null;
        }

        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject[] parameters = Py.javas2pys(playerOne, playerTwo, identifier);
            PyObject result = relFunction.__call__(threadState, parameters[0], parameters[1], parameters[2]);
            if (result instanceof PyString) {
                return ((PyString) result).getString();
            }
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when executing relational placeholder '" + getIdentifier() + "'");
        }
        return null;
    }
}
