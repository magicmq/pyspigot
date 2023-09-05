package dev.magicmq.pyspigot.manager.script;

public enum RunResult {

    /**
     * Returned if the script ran successfully.
     */
    SUCCESS,

    /**
     * Returned if the script was not run because it was disabled as per its script options in script_options.yml
     */
    FAIL_DISABLED,

    /**
     * Returned if the script was not run because it has missing dependencies.
     */
    FAIL_DEPENDENCY,

    /**
     * Returned if the script was run but failed during runtime due to an error.
     */
    FAIL_ERROR

}
