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
import org.graalvm.polyglot.PolyglotException;

/**
 * Called when a script throws an unhandled error/exception. This event could be called asynchronously if the exception occurred in an asynchronous context. To check if the event is asynchronous, call {@link org.bukkit.event.Event#isAsynchronous()}
 * <p>
 * The exception will be a {@link org.graalvm.polyglot.PolyglotException}, which will include both exceptions that originate from script code and exceptions that originate from PySpigot Java code. Use {@link org.graalvm.polyglot.PolyglotException#isHostException()} or {@link org.graalvm.polyglot.PolyglotException#isGuestException()} to determine if the exception originated from a script or from Java.
 */
public class ScriptExceptionEvent extends ScriptEvent {

    private static final HandlerList handlers = new HandlerList();
    private final PolyglotException exception;
    private boolean reportException;

    /**
     *
     * @param script The script that caused the error/exception
     * @param exception The {@link org.graalvm.polyglot.PolyglotException} that was thrown
     * @param async Whether the exception occurred in an asychronous context
     */
    public ScriptExceptionEvent(Script script, PolyglotException exception, boolean async) {
        super(script, async);
        this.exception = exception;
        this.reportException = true;
    }

    /**
     * Get the {@link org.graalvm.polyglot.PolyglotException} that was thrown.
     * @return The {@link org.graalvm.polyglot.PolyglotException} that was thrown
     */
    public PolyglotException getException() {
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
