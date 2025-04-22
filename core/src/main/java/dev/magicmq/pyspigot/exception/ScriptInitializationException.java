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

package dev.magicmq.pyspigot.exception;

import dev.magicmq.pyspigot.manager.script.Script;

import java.io.Serial;

/**
 * Thrown if an exception occurs when loading or running a script.
 * <p>
 * This is a wrapper class for checked exceptions such as {@link java.io.IOException}.
 */
public class ScriptInitializationException extends Exception {

    @Serial
    private static final long serialVersionUID = 3601572161262479369L;
    private final Script script;

    /**
     *
     * @param script The script that was being loaded
     * @param message The detail message
     * @param cause The cause
     */
    public ScriptInitializationException(Script script, String message, Throwable cause) {
        super(message, cause);
        this.script = script;
    }

    /**
     * Get the script that was being loaded.
     * @return The script that was being loaded
     */
    public Script getScript() {
        return script;
    }
}
