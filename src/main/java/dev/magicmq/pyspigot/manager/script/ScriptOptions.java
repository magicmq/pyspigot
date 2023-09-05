package dev.magicmq.pyspigot.manager.script;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A class representing various runtime options belonging to a certain script.
 */
public class ScriptOptions {

    private final boolean enabled;
    private final List<String> depend;
    private final boolean loggingEnabled;
    private final Level loggingLevel;

    /**
     * Initialize a new ScriptOptions using values from the provided ConfigurationSection. If this constructor is passed a null value for the config parameter, then the default script options will be used.
     * @param config The configuration section from which script options should be read, or null if the default script options should be used
     */
    public ScriptOptions(ConfigurationSection config) {
        if (config != null) {
            this.enabled = config.getBoolean("enabled", true);
            this.depend = config.getStringList("depend");
            this.loggingEnabled = config.getBoolean("logging-enabled", true);
            this.loggingLevel = Level.parse(config.getString("logging-level", "INFO"));
        } else {
            this.enabled = true;
            this.depend = new ArrayList<>();
            this.loggingEnabled = true;
            this.loggingLevel = Level.INFO;
        }
    }

    /**
     * Get if this script is enabled.
     * @return True if the script is enabled, false if otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get a list of dependencies for this script.
     * @return A list of dependencies for this script. Will return an empty list if this script has no dependencies
     */
    public List<String> getDependencies() {
        return depend;
    }

    /**
     * Get if file logging is enabled for this script.
     * @return True if file logging is enabled, false if otherwise
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Get the minimum logging level for this script, represented as a {@link java.util.logging.Level}
     * @return The minimum logging level at which messages should be logged
     */
    public Level getLoggingLevel() {
        return loggingLevel;
    }
}
