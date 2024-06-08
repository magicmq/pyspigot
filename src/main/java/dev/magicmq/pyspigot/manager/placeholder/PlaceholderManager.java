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

package dev.magicmq.pyspigot.manager.placeholder;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.HashMap;

/**
 * Manager to interface with PlaceholderAPI. Primarily used by scripts to register and unregister placeholder expansions.
 * <p>
 * Do not call this manager if PlaceholderAPI is not loaded and enabled on the server! It will not work.
 */
public class PlaceholderManager {

    private static PlaceholderManager instance;

    private final HashMap<Script, ScriptPlaceholder> registeredPlaceholders;

    private PlaceholderManager() {
        registeredPlaceholders = new HashMap<>();
    }

    /**
     * Register a new script placeholder expansion.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(Value placeholderFunction) {
        return registerPlaceholder(placeholderFunction, "Script Author", "1.0.0");
    }

    /**
     * Register a new script placeholder expansion.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param placeholderFunction The function that should be called when the placeholder is used
     * @param author The author of the placeholder
     * @param version The version of the placeholder
     * @return A {@link ScriptPlaceholder} representing the placeholder expansion that was registered
     */
    public ScriptPlaceholder registerPlaceholder(Value placeholderFunction, String author, String version) {
        Script script = ScriptManager.get().getScript(Context.getCurrent());
        if (!registeredPlaceholders.containsKey(script)) {
            if (!placeholderFunction.canExecute())
                throw new RuntimeException("placeholderFunction must be a function (callable)");

            ScriptPlaceholder placeholder = new ScriptPlaceholder(script, placeholderFunction, author, version);
            placeholder.register();
            registeredPlaceholders.put(script, placeholder);
            return placeholder;
        } else
            throw new RuntimeException("Script already has a placeholder expansion registered");
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
     * <p>
     * Similar to {@link #unregisterPlaceholder(ScriptPlaceholder)}, except this method can be called from outside a script to unregister a script's placeholder expansion (for example when the script is unloaded and stopped).
     * @param script The script whose placeholder should be unregistered
     * @return True if a placeholder was unregistered, false if the script did not have a placeholder registered previously
     */
    public boolean unregisterPlaceholder(Script script) {
        ScriptPlaceholder placeholder = registeredPlaceholders.get(script);
        if (placeholder != null) {
            unregisterPlaceholder(placeholder);
            return true;
        }
        return false;
    }

    /**
     * Get a script's placeholder expansion.
     * @param script The script to get the placeholder expansion from
     * @return The script's placeholder expansion, null if the script does not have a placeholder expansion registered
     */
    public ScriptPlaceholder getPlaceholder(Script script) {
        return registeredPlaceholders.get(script);
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
