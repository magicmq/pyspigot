package dev.magicmq.pyspigot.manager.placeholder;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.python.core.*;

/**
 * A class that represents a script placeholder expansion.
 * <p>
 * A ScriptPlaceholder can have multiple individual placeholders. For example, a script with the name "test.py" could have "%script:test_placeholder1%" and "%script:test_placeholder2%". It will be up to the script to handle each individual placeholder.
 * @see me.clip.placeholderapi.expansion.PlaceholderExpansion
 */
public class ScriptPlaceholder extends PlaceholderExpansion {

    private Script script;
    private PyFunction function;
    private String author;
    private String version;

    /**
     *
     * @param script The script associated with this ScriptPlaceholder
     * @param function The function to call when the placeholder is used
     * @param author The author of this ScriptPlaceholder
     * @param version The version of this ScriptPlaceholder
     */
    public ScriptPlaceholder(Script script, PyFunction function, String author, String version) {
        this.script = script;
        this.function = function;
        this.author = author;
        this.version = version;
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
        try {
            PyObject[] parameters = Py.javas2pys(player, params);
            PyObject result = function.__call__(parameters[0], parameters[1]);
            if (result instanceof PyString) {
                return ((PyString) result).getString();
            }
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when executing placeholder '" + getIdentifier() + "'");
        }
        return null;
    }
}
