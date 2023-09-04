package dev.magicmq.pyspigot.manager.script;

import java.util.*;

public class ScriptSorter {

    private HashMap<String, Script> scripts;
    private Set<Script> visited;
    private LinkedList<Script> loadOrder;

    public ScriptSorter(List<Script> scripts) {
        this.scripts = new HashMap<>();
        scripts.forEach(script -> this.scripts.put(script.getName(), script));
        this.visited = new HashSet<>();
        this.loadOrder = new LinkedList<>();
    }

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
