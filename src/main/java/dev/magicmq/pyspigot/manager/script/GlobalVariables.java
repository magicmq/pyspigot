package dev.magicmq.pyspigot.manager.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * A wrapper class for a HashMap that contains global variables that can be shared across scripts.
 * @see java.util.HashMap
 */
public class GlobalVariables {

    private HashMap<String, Object> variables;

    public GlobalVariables() {
        variables = new HashMap<>();
    }

    /**
     * Set a global variable. Will override an existing global variable with the same name.
     * @param key The name of the variable to set
     * @param value The value of the variable
     * @return The value that was previously associated with the given key, or null if there was none
     */
    public Object set(String key, Object value) {
        return set(key, value, true);
    }

    /**
     * Set a global variable, with the option to override an existing global variable with the same name.
     * @param key The name of the variable to set
     * @param value The value of the variable
     * @param override Whether an existing value should be overridden
     * @return If override is true, will return the value that was previously associated with the key, or null if there was none. If override is false, will return the existing value, or null if there was none
     */
    public Object set(String key, Object value, boolean override) {
        if (override)
            return variables.put(key, value);
        else
            return variables.putIfAbsent(key, value);
    }

    /**
     * Remove/delete a global variable.
     * @param key The name of the variable to remove
     * @return The value of the variable that was removed, or null if there was none
     */
    public Object remove(String key) {
        return variables.remove(key);
    }

    /**
     * Get a global variable
     * @param key The name of the variable to get
     * @return The variable, or null if there is no variable associated with the given key
     */
    public Object get(String key) {
        return variables.get(key);
    }

    /**
     * Get a set of all global variable names.
     * @return An immutable {@link java.util.Set} containing the names of all global variables. Will return an empty set if there are no global variables
     */
    public Set<String> getKeys() {
        return variables.keySet();
    }

    /**
     * Get a set of all global variable values.
     * @return An immutable {@link java.util.Collection} containing the values of all global variables. Will return an empty collection if there are no global variables
     */
    public Collection<Object> getValues() {
        return variables.values();
    }

    /**
     * Get the underlying {@link java.util.HashMap} wherein global variables are cached.
     * @return The underlying HashMap, which is mutable
     */
    public HashMap<String, Object> getHashMap() {
        return variables;
    }

    /**
     * Check if a global variable exists with the given name.
     * @param key The name to check
     * @return True if there is a global variable with the given name, false if there is not
     */
    public boolean contains(String key) {
        return variables.containsKey(key);
    }

    /**
     * Check if a global variable exists with the given value.
     * @param value The value to check
     * @return True if there is a global variable with the given value, false if there is not
     */
    public boolean containsValue(Object value) {
        return variables.containsValue(value);
    }

    /**
     * Clear all global variables.
     */
    public void purge() {
        variables.clear();
    }
}
