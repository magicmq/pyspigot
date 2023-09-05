package dev.magicmq.pyspigot.manager.script;

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
     * Create a new ScriptOptions with the default values.
     */
    public ScriptOptions() {
        this(true, new ArrayList<>(), true, Level.INFO);
    }

    /**
     *
     * @param enabled Whether this script is enabled
     * @param depend A list of dependencies that this script relies on
     * @param loggingEnabled Whether this script's log messages/errors should be logged to file
     * @param loggingLevel The minimum logging level for this script's logging
     */
    public ScriptOptions(boolean enabled, List<String> depend, boolean loggingEnabled, Level loggingLevel) {
        this.enabled = enabled;
        this.depend = depend;
        this.loggingEnabled = loggingEnabled;
        this.loggingLevel = loggingLevel;
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
