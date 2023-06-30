/*
 *    Copyright 2023 magicmq
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

package dev.magicmq.pyspigot.event;

import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.event.HandlerList;
import org.python.core.PyException;

/**
 * Called when a script throws an unhandled error/exception.
 * <p>
 * The exception could be a Java exception or a Python error/exception, depending on its source. Use {@link ScriptExceptionEvent#getExceptionType()} to get the type of exception that was thrown.
 * <p>
 * The throwable associated with this event may or may not be a PyException. If {@link #getExceptionType()} returns PYTHON, the throwable will always be a PyException.
 * @see PyException
 */
public class ScriptExceptionEvent extends ScriptEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Throwable throwable;
    private final ExceptionType exceptionType;
    private boolean reportException;

    /**
     *
     * @param script The script that caused the error/exception
     * @param throwable The exception that was thrown
     * @param exceptionType The type of exception that was thrown
     */
    public ScriptExceptionEvent(Script script, Throwable throwable, ExceptionType exceptionType) {
        super(script);
        this.throwable = throwable;
        this.exceptionType = exceptionType;
        this.reportException = true;
    }

    /**
     * Get the throwable that was thrown. This may or may not be a PyException. If {@link #getExceptionType()} returns PYTHON, the throwable will always be a PyException.
     * @return The throwable that was thrown
     * @see PyException
     */
    public Throwable getException() {
        return throwable;
    }

    /**
     * Get the type of exception that was thrown
     * @return The type of exception that was thrown.
     */
    public ExceptionType getExceptionType() {
        return exceptionType;
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Enum that represents the type of exception that was thrown, PYTHON for python errors/exceptions, JAVA for Java exceptions
     * <p>
     * For Java exceptions, use {@link PyException#getCause} to get the underlying Java exception. Will have an associated stack trace.
     * <p>
     * For Python exceptions, they may or may not have an associated Python traceback. See {@link PyException#traceback}
     */
    public enum ExceptionType {

        /**
         * A Java exception.
         */
        JAVA,

        /**
         * A Python error/exception.
         */
        PYTHON

    }
}
