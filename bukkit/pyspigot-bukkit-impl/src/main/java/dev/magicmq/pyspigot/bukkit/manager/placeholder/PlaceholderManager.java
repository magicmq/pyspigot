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

import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptContext;
import jep.python.PyCallable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Manager to interface with PlaceholderAPI. Primarily used by scripts to register and unregister placeholder expansions.
 * <p>
 * Do not call this manager if PlaceholderAPI is not loaded and enabled on the server! It will not work.
 */
public class PlaceholderManager {

    private static final String[] INVALID_CHARS = {"_", "%", "{", "}"};

    private static PlaceholderManager instance;

    private final HashMap<Script, List<ScriptPlaceholder>> registeredPlaceholders;

    private PlaceholderManager() {
        registeredPlaceholders = new HashMap<>();
    }

    /**
     * Register a new script placeholder expansion.
     * <p>
     * To register a relational placeholder expansion, see {@link #registerPlaceholder(PyCallable, PyCallable)}
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(PyCallable placeholderFunction) {
        Objects.requireNonNull(placeholderFunction);

        return registerPlaceholder(placeholderFunction, null, null, "Script Author", "1.0.0");
    }

    /**
     * Register a new script placeholder expansion, including relational placeholders.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @param relPlaceholderFunction The function that should be called when the relational placeholder
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(PyCallable placeholderFunction, PyCallable relPlaceholderFunction) {
        return registerPlaceholder(placeholderFunction, relPlaceholderFunction, null, "Script Author", "1.0.0");
    }

    /**
     * Register a new script placeholder expansion.
     * <p>
     * Note that for the identifier, invalid characters ("_", "%", "{", and "}") are automatically removed.
     * <p>
     * To register a relational placeholder expansion, see {@link #registerPlaceholder(PyCallable, PyCallable, String, String, String)}
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @param identifier The identifier of the placeholder. If this is {@code null}, then the identifier will be in the
     *                   format "script:name", where "name" is the name of the script
     * @param author The author of the placeholder
     * @param version The version of the placeholder
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(PyCallable placeholderFunction, String identifier, String author, String version) {
        return registerPlaceholder(placeholderFunction, null, identifier, author, version);
    }

    /**
     * Register a new script placeholder expansion, including relational placeholders.
     * <p>
     * Note that for the identifier, invalid characters ("_", "%", "{", and "}") are automatically removed.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @param relPlaceholderFunction The function that should be called when the relational placeholder
     * @param identifier The identifier of the placeholder. If this is {@code null}, then the identifier will be in the
     *                   format "script:name", where "name" is the name of the script
     * @param author The author of the placeholder
     * @param version The version of the placeholder
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(PyCallable placeholderFunction, PyCallable relPlaceholderFunction, String identifier, String author, String version) {
        Script script = ScriptContext.require();

        if (identifier == null)
            identifier = "script:" + script.getSimpleName();

        for (String invalid : INVALID_CHARS) {
            if (identifier.contains(invalid)) {
                script.getLogger().warn("Script placeholder identifier contains invalid character(s). Identifier will be registered as '{}'", removeInvalidCharacters(identifier));
                break;
            }
        }

        identifier = removeInvalidCharacters(identifier);

        if (getPlaceholder(script, identifier) != null)
            throw new ScriptRuntimeException(script, "Script already has a placeholder expansion registered with the identifier '" + identifier + "'.");

        ScriptPlaceholder placeholder = new ScriptPlaceholder(
                script,
                placeholderFunction,
                relPlaceholderFunction,
                identifier,
                author,
                version
        );
        placeholder.register();
        addPlaceholder(placeholder);
        return placeholder;
    }

    /**
     * Set the relational placeholder function for a placeholder expansion that was registered previously.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param relationalPlaceholderFunction The relational placeholder function to set
     * @param identifier The identifier of the placeholder expansion to register a relational function for
     */
    public void setRelationalPlaceholderFunction(PyCallable relationalPlaceholderFunction, String identifier) {
        identifier = removeInvalidCharacters(identifier);

        Script script = ScriptContext.require();
        ScriptPlaceholder placeholder = getPlaceholder(script, identifier);
        if (placeholder != null)
            placeholder.setRelationalFunction(relationalPlaceholderFunction);
        else
            throw new ScriptRuntimeException(script, "Could not find a registered placeholder expansion with the identifier '" + identifier + "'");
    }

    /**
     * Unregister a script placeholder expansion.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholder The placeholder expansion to unregister
     */
    public void unregisterPlaceholder(ScriptPlaceholder placeholder) {
        placeholder.unregister();
        removePlaceholder(placeholder);
    }

    /**
     * Unregister a script placeholder expansion. Note that multiple placeholder expansions may be unregistered, if multiple expansions are registered to the same function.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function associated with the placeholder expansion to unregister
     */
    public void unregisterPlaceholder(PyCallable placeholderFunction) {
        Script script = ScriptContext.require();
        List<ScriptPlaceholder> placeholders = getPlaceholders(script);
        for (ScriptPlaceholder placeholder : placeholders) {
            if (placeholder.getFunction().equals(placeholderFunction)) {
                unregisterPlaceholder(placeholder);
            }
        }
    }

    /**
     * Unregister all of a script's placeholder expansions.
     * @param script The script whose placeholder expansions should be unregistered
     */
    public void unregisterPlaceholders(Script script) {
        for (ScriptPlaceholder placeholder : getPlaceholders(script)) {
            placeholder.unregister();
        }
        registeredPlaceholders.remove(script);
    }

    /**
     * Get a placeholder expansion associated with a particular script by the expansion's identifier.
     * @param script The script
     * @param identifier The identifier of the placeholder
     * @return The placeholder expansion with this identifier associated with the script, or null if none was found
     */
    public ScriptPlaceholder getPlaceholder(Script script, String identifier) {
        List<ScriptPlaceholder> scriptPlaceholders = getPlaceholders(script);
        for (ScriptPlaceholder placeholder : scriptPlaceholders) {
            if (placeholder.getIdentifier().equalsIgnoreCase(identifier))
                return placeholder;
        }
        return null;
    }

    /**
     * Get all of a script's placeholder expansions.
     * @param script The script to get the placeholder expansions from
     * @return An immutable list containing the script's placeholder expansions. Will return an empty list if the script has no placeholder expansions registered
     */
    public List<ScriptPlaceholder> getPlaceholders(Script script) {
        List<ScriptPlaceholder> placeholders = registeredPlaceholders.get(script);
        return placeholders != null ? List.copyOf(placeholders) : List.of();
    }

    private void addPlaceholder(ScriptPlaceholder placeholder) {
        Script script = placeholder.getScript();
        if (registeredPlaceholders.containsKey(script))
            registeredPlaceholders.get(script).add(placeholder);
        else {
            List<ScriptPlaceholder> scriptPlaceholders = new ArrayList<>();
            scriptPlaceholders.add(placeholder);
            registeredPlaceholders.put(script, scriptPlaceholders);
        }
    }

    private void removePlaceholder(ScriptPlaceholder placeholder) {
        Script script = placeholder.getScript();
        List<ScriptPlaceholder> scriptPlaceholders = registeredPlaceholders.get(script);
        scriptPlaceholders.remove(placeholder);
        if (scriptPlaceholders.isEmpty())
            registeredPlaceholders.remove(script);
    }

    private String removeInvalidCharacters(String identifier) {
        for (String invalid : PlaceholderManager.INVALID_CHARS) {
            identifier = identifier.replace(invalid, "");
        }
        return identifier;
    }

    /**
     * Get the singleton instance of this PlaceholderManager.
     * @return The instance
     */
    public static PlaceholderManager get() {
        if (instance == null)
            instance = new PlaceholderManager();
        return instance;
    }
}
