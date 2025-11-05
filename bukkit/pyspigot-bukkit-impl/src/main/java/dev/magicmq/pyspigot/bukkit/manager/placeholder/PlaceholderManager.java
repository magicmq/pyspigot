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
import org.python.core.PyFunction;

import java.util.HashMap;
import java.util.Objects;

/**
 * Manager to interface with PlaceholderAPI. Primarily used by scripts to register and unregister placeholder expansions.
 * <p>
 * Do not call this manager if PlaceholderAPI is not loaded and enabled on the server! It will not work.
 */
public class PlaceholderManager {

    private static final String[] INVALID_CHARS = {"_", "%", "{", "}"};

    private static PlaceholderManager instance;

    private final HashMap<Script, ScriptPlaceholder> registeredPlaceholders;

    private PlaceholderManager() {
        registeredPlaceholders = new HashMap<>();
    }

    /**
     * Register a new script placeholder expansion.
     * <p>
     * To register a relational placeholder expansion, see {@link #registerPlaceholder(PyFunction, PyFunction)}
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(PyFunction placeholderFunction) {
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
    public ScriptPlaceholder registerPlaceholder(PyFunction placeholderFunction, PyFunction relPlaceholderFunction) {
        return registerPlaceholder(placeholderFunction, relPlaceholderFunction, null, "Script Author", "1.0.0");
    }

    /**
     * Register a new script placeholder expansion.
     * <p>
     * Note that for the identifier, invalid characters ("_", "%", "{", and "}") are automatically removed.
     * <p>
     * To register a relational placeholder expansion, see {@link #registerPlaceholder(PyFunction, PyFunction, String, String, String)}
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @param identifier The identifier of the placeholder. If this is {@code null}, then the identifier will be in the
     *                   format "script:name", where "name" is the name of the script
     * @param author The author of the placeholder
     * @param version The version of the placeholder
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(PyFunction placeholderFunction, String identifier, String author, String version) {
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
    public ScriptPlaceholder registerPlaceholder(PyFunction placeholderFunction, PyFunction relPlaceholderFunction, String identifier, String author, String version) {
        Script script = ScriptContext.require();
        if (!registeredPlaceholders.containsKey(script)) {
            if (identifier == null)
                identifier = "script:" + script.getSimpleName();

            for (String invalid : INVALID_CHARS) {
                if (identifier.contains(invalid)) {
                    script.getLogger().warn("Script placeholder identifier contains invalid character(s). Identifier will be registered as '{}'", removeInvalidCharacters(identifier));
                    break;
                }
            }

            ScriptPlaceholder placeholder = new ScriptPlaceholder(
                    script,
                    placeholderFunction,
                    relPlaceholderFunction,
                    removeInvalidCharacters(identifier),
                    author,
                    version
            );
            placeholder.register();
            registeredPlaceholders.put(script, placeholder);
            return placeholder;
        } else
            throw new ScriptRuntimeException(script, "Script already has a placeholder expansion registered");
    }

    /**
     * Set the relational placeholder function for a placeholder that was registered previously.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param relationalPlaceholderFunction The relational placeholder function to set
     */
    public void setRelationalPlaceholderFunction(PyFunction relationalPlaceholderFunction) {
        Script script = ScriptContext.require();
        ScriptPlaceholder placeholder = registeredPlaceholders.get(script);
        if (placeholder != null)
            placeholder.setRelationalFunction(relationalPlaceholderFunction);
        else
            throw new ScriptRuntimeException(script, "Script does not have a placeholder expansion registered");
    }

    /**
     * Unregister a script placeholder expansion.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholder The placeholder expansion to unregister
     */
    public void unregisterPlaceholder(ScriptPlaceholder placeholder) {
        placeholder.unregister();
        registeredPlaceholders.remove(placeholder.getScript());
    }

    /**
     * Unregister a script's placeholder expansion.
     * @param placeholderFunction The function associated with the placeholder expansion to unregister
     */
    public void unregisterPlaceholder(PyFunction placeholderFunction) {
        Script script = ScriptContext.require();
        ScriptPlaceholder placeholder = registeredPlaceholders.get(script);
        if (placeholder != null)
            unregisterPlaceholder(placeholder);
    }

    /**
     * Unregister a script's placeholder expansion.
     * <p>
     * Similar to {@link #unregisterPlaceholder(ScriptPlaceholder)}, except this method can be called from outside a script to unregister a script's placeholder expansion (for example when the script is unloaded and stopped).
     * @param script The script whose placeholder should be unregistered
     */
    public void unregisterPlaceholder(Script script) {
        ScriptPlaceholder placeholder = registeredPlaceholders.get(script);
        if (placeholder != null)
            unregisterPlaceholder(placeholder);
    }

    /**
     * Get a script's placeholder expansion.
     * @param script The script to get the placeholder expansion from
     * @return The script's placeholder expansion, or null if the script does not have a placeholder expansion registered
     */
    public ScriptPlaceholder getPlaceholder(Script script) {
        return registeredPlaceholders.get(script);
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
