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

import java.io.Serial;

/**
 * Thrown if an exception occurs during initialization of PySpigot.
 * <p>
 * This is a wrapper class for various checked exceptions associated with reflective calls ({@link java.lang.NoSuchMethodException}, {@link java.lang.NoSuchFieldException}, etc.).
 */
public class PluginInitializationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8133956334847368719L;

    /**
     *
     * @param message The detail message
     * @param cause The cause
     */
    public PluginInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
