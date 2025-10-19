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

package dev.magicmq.pyspigot.util.logging;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.manager.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.util.logging.Level;

/**
 * A wrapper class that contains a {@link java.util.logging.Logger} to handle logging script log messages to file, as well as a {@link org.slf4j.Logger} for logging to console and the console log file.
 * <p>
 * This class contains methods which pass through to SLF4J's logger, but these methoods additionally log messages to the script's log file.
 * @see Logger
 */
public class ScriptLogger {

    private final Logger logger;
    private ScriptFileLogger fileLogger;

    /**
     *
     * @param script The script associated with this ScriptLogger
     */
    public ScriptLogger(Script script) {
        this.logger = LoggerFactory.getLogger(PyCore.get().getPluginIdentifier() + "/" + script.getName());

        if (script.getOptions().isFileLoggingEnabled()) {
            try {
                this.fileLogger = new ScriptFileLogger(script);
                this.fileLogger.setLevel(script.getOptions().getMinLoggingLevel());
            } catch (IOException e) {
                this.logger.error("Error when initializing the log file, skipping file logging.", e);
                this.fileLogger = null;
            }
        }
    }

    /**
     * Closes the ScriptFileLogger by closing its FileHandler.
     */
    public void close() {
        if (fileLogger != null)
            fileLogger.closeFileHandler();
    }

    /**
     * Get the underlying SLF4J logger which backs this ScriptLogger.
     * @return The SLF4JLogger
     */
    public Logger getSLF4JLogger() {
        return logger;
    }

    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     */
    public void trace(String msg) {
        logger.trace(msg);
        logToFileLogger(Level.FINEST, msg);
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and argument.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
        logToFileLogger(Level.FINEST, format, arg);
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the TRACE level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
        logToFileLogger(Level.FINEST, format, arg1, arg2);
    }

    /**
     * Log a message at the TRACE level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the TRACE level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for TRACE. The variants taking {@link #trace(String, Object) one} and
     * {@link #trace(String, Object, Object) two} arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
        logToFileLogger(Level.FINEST, format, arguments);
    }

    /**
     * Log an exception (throwable) at the TRACE level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
        logToFileLogger(Level.FINEST, msg, t);
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    public void debug(String msg) {
        logger.debug(msg);
        logToFileLogger(Level.FINE, msg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
        logToFileLogger(Level.FINE, format, arg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
        logToFileLogger(Level.FINE, format, arg1, arg2);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for DEBUG. The variants taking
     * {@link #debug(String, Object) one} and {@link #debug(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
        logToFileLogger(Level.FINE, format, arguments);
    }

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
        logToFileLogger(Level.FINE, msg, t);
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    public void info(String msg) {
        logger.info(msg);
        logToFileLogger(Level.INFO, msg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the INFO level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void info(String format, Object arg) {
        logger.info(format, arg);
        logToFileLogger(Level.INFO, format, arg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the INFO level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format);
        logToFileLogger(Level.INFO, format, arg1, arg2);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the INFO level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for INFO. The variants taking
     * {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
        logToFileLogger(Level.INFO, format, arguments);
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
        logToFileLogger(Level.INFO, msg, t);
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    public void warn(String msg) {
        logger.warn(msg);
        logToFileLogger(Level.WARNING, msg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the WARN level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
        logToFileLogger(Level.WARNING, format, arg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the WARN level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for WARN. The variants taking
     * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
        logToFileLogger(Level.WARNING, format, arguments);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the WARN level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
        logToFileLogger(Level.WARNING, format, arg1, arg2);
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
        logToFileLogger(Level.WARNING, msg, t);
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    public void error(String msg) {
        logger.error(msg);
        logToFileLogger(Level.SEVERE, msg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level.
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void error(String format, Object arg) {
        logger.error(format, arg);
        logToFileLogger(Level.SEVERE, format, arg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level.
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
        logToFileLogger(Level.SEVERE, format, arg1, arg2);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p>
     * This form avoids superfluous string concatenation when the logger
     * is disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for ERROR. The variants taking
     * {@link #error(String, Object) one} and {@link #error(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
        logToFileLogger(Level.SEVERE, format, arguments);
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
        if (fileLogger != null)
            logToFileLogger(Level.SEVERE, msg, t);
    }

    private void logToFileLogger(Level level, String message) {
        if (fileLogger != null && fileLogger.getLevel().intValue() >= level.intValue())
            fileLogger.log(level, message);
    }

    private void logToFileLogger(Level level, String format, Object arg) {
        if (fileLogger != null && fileLogger.getLevel().intValue() >= level.intValue()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg);
            if (tuple.getThrowable() != null)
                fileLogger.log(level, tuple.getMessage(), tuple.getThrowable());
            else
                fileLogger.log(level, tuple.getMessage());
        }
    }

    private void logToFileLogger(Level level, String format, Object arg1, Object arg2) {
        if (fileLogger != null && fileLogger.getLevel().intValue() >= level.intValue()) {
            FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            if (tuple.getThrowable() != null)
                fileLogger.log(level, tuple.getMessage(), tuple.getThrowable());
            else
                fileLogger.log(level, tuple.getMessage());
        }
    }

    private void logToFileLogger(Level level, String format, Object[] argArray) {
        if (fileLogger != null && fileLogger.getLevel().intValue() >= level.intValue()) {
            FormattingTuple tuple = MessageFormatter.arrayFormat(format, argArray);
            if (tuple.getThrowable() != null)
                fileLogger.log(level, tuple.getMessage(), tuple.getThrowable());
            else
                fileLogger.log(level, tuple.getMessage());
        }
    }
}
