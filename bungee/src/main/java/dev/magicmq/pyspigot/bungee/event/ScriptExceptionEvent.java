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

package dev.magicmq.pyspigot.bungee.event;

import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyException;

/**
 * Called when a script throws an unhandled error/exception.
 * <p>
 * The exception will be a {@link PyException}, which will include Java exceptions thrown by calls to Java code from scripts. Use {@link PyException#getCause} to determine if there was an underlying Java exception.
 */
public class ScriptExceptionEvent extends ScriptEvent {

    private final PyException exception;
    private boolean reportException;

    /**
     *
     * @param script The script that caused the error/exception
     * @param exception The {@link PyException} that was thrown
     */
    public ScriptExceptionEvent(Script script, PyException exception) {
        super(script);
        this.exception = exception;
        this.reportException = true;
    }

    /**
     * Get the {@link PyException} that was thrown.
     * @return The {@link PyException} that was thrown
     */
    public PyException getException() {
        return exception;
    }

    /**
     * Get if the exception should be reported to console and/or a script's log file.
     * @return True if the exception should be reported to console and/or a script's log file, false if otherwise
     */
    public boolean doReportException() {
        return reportException;
    }

    /**
     * Set if the exception should be reported to console and/or the script's log file.
     * @param reportException Whether the exception should be reported.
     */
    public void setReportException(boolean reportException) {
        this.reportException = reportException;
    }
}
