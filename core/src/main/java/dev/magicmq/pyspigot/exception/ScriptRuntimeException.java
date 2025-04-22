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
 * Thrown if an error/exception is thrown or an illegal operation is performed when a script interacts with PySpigot.
 * <p>
 * This class also serves as a wrapper for some checked exceptions, including {@link java.io.IOException}.
 */
public class ScriptRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1309086963783269924L;
    private final Script script;

    /**
     *
     * @param script The script that threw or is associated with this exception
     */
    public ScriptRuntimeException(Script script) {
        this.script = script;
    }

    /**
     *
     * @param script The script that threw or is associated with this exception
     * @param message The detail message
     */
    public ScriptRuntimeException(Script script, String message) {
        super(message);
        this.script = script;
    }

    /**
     *
     * @param script The script that threw or is associated with this exception
     * @param message The detail message
     * @param cause The cause
     */
    public ScriptRuntimeException(Script script, String message, Throwable cause) {
        super(message, cause);
        this.script = script;
    }

    /**
     * Get the script that threw or is associated with this exception.
     * @return The script that threw or is associated with this exception
     */
    public Script getScript() {
        return script;
    }
}
