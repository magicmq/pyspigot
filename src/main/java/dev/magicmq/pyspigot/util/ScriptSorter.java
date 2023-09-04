package dev.magicmq.pyspigot.util;

import dev.magicmq.pyspigot.manager.script.Script;

import java.util.*;

/**
 * Utility class that places scripts into the proper loading order, taking into account dependencies. This class uses script dependencies as defined in {@link dev.magicmq.pyspigot.manager.script.ScriptOptions}.
 * <p>
 * Under the hood, utilizes depth-first search algorithm to order scripts.
 */
public class ScriptSorter {

    private HashMap<String, Script> scripts;
    private Set<Script> visited;
    private LinkedList<Script> loadOrder;

    /**
     *
     * @param scripts An unordered list of scripts
     */
    public ScriptSorter(List<Script> scripts) {
        this.scripts = new HashMap<>();
        scripts.forEach(script -> this.scripts.put(script.getName(), script));
        this.visited = new HashSet<>();
        this.loadOrder = new LinkedList<>();
    }

    /**
     * Get a {@link LinkedList}, in the order that the scripts should be loaded. Ordering is done with respect to script dependencies. The first script in this list should load first, and the last script in this list should load last.
     * @return An ordered list of scripts
     */
    public LinkedList<Script> getOptimalLoadOrder() {
        for (Script script : scripts.values()) {
            if (!visited.contains(script)) {
                dfs(script);
            }
        }

        return loadOrder;
    }

    private void dfs(Script script) {
        visited.add(script);

        for (String dependency : script.getOptions().getDependencies()) {
            if (!visited.contains(dependency)) {
                dfs(scripts.get(dependency));
            }
        }

        loadOrder.offer(script);
    }
}
