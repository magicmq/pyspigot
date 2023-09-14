package dev.magicmq.pyspigot.manager.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class GlobalVariables {

    private HashMap<String, Object> variables;

    public GlobalVariables() {
        variables = new HashMap<>();
    }

    public Object set(String key, Object value) {
        return set(key, value, true);
    }

    public Object set(String key, Object value, boolean override) {
        if (override)
            return variables.put(key, value);
        else
            return variables.putIfAbsent(key, value);
    }

    public Object remove(String key) {
        return variables.remove(key);
    }

    public Object get(String key) {
        return variables.get(key);
    }

    public Set<String> getKeys() {
        return variables.keySet();
    }

    public Collection<Object> getValues() {
        return variables.values();
    }

    public HashMap<String, Object> getHashMap() {
        return variables;
    }

    public boolean contains(String key) {
        return variables.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return variables.containsValue(value);
    }

    public void purge() {
        variables.clear();
    }
}
