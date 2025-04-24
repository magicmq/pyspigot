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

package dev.magicmq.pyspigot.util;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.exception.PluginInitializationException;
import dev.magicmq.pyspigot.exception.ScriptExitException;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.Options;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.StdoutWrapper;
import org.python.core.ThreadState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * A collection of utility methods related to scripts.
 */
public final class ScriptUtils {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    private static final Method EXCEPTION_TO_STRING;

    static {
        try {
            EXCEPTION_TO_STRING = Py.class.getDeclaredMethod("exceptionToString", PyObject.class, PyObject.class, PyObject.class);
            EXCEPTION_TO_STRING.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new PluginInitializationException("Error when initializing script exception handler", e);
        }
    }

    private ScriptUtils() {}

    /**
     * Attempts to get the script involved in a Java method call by analyzing the call stack.
     * @return The script associated with the method call, or null if no script was found in the call stack
     */
    public static Script getScriptFromCallStack() {
        Optional<StackWalker.StackFrame> callingScript = STACK_WALKER.walk(stream -> stream.filter(frame -> {
            String className = frame.getClassName();
            String methodName = frame.getMethodName();
            return (className.contains("org.python.pycode") || className.contains("$py")) && methodName.equals("call_function");
        }).findFirst());
        if (callingScript.isPresent()) {
            String scriptFile = callingScript.get().getFileName();
            return ScriptManager.get().getScriptByPath(Paths.get(scriptFile));
        } else {
            return null;
        }
    }

    /**
     * Initializes a new PySystemState for a new {@link org.python.util.PythonInterpreter} when a script is loaded. This method will also initialize Jython if it hasn't been initialized previously.
     * <p>
     * This method will also do the following with the new PySystemState: set its class loader to the class loader provided by the {@link LibraryManager}, and add "./plugins/PySpigot/python-libs/" to the path.
     * @param projectPath The project's path to append to sys.path, if initializing a new PySystemState for a multi-file project. Pass null if initializing a new PySystemState for a single-file script
     * @return The PySystemState that was created
     */
    public static PySystemState initPySystemState(Path projectPath) {
        ScriptManager.get().initJython();

        PySystemState sys = new PySystemState();
        sys.setClassLoader(LibraryManager.get().getClassLoader());
        sys.path.append(new PyString(PyCore.get().getDataFolderPath().resolve("python-libs").toString()));
        if (projectPath != null) {
            sys.path.append(new PyString(projectPath.toString()));
        }
        return sys;
    }

    /**
     * Interacts with Jython to handle a caught Throwable when running script code.
     * <p>
     * Also uses reflection to call the {@code exceptionToString} method in the {@link org.python.core.Py} class to fetch and return a string representation of the throwable (including traceback and/or stack trace) for logging purposes.
     * <p>
     * If the {@code python.options.showJavaExceptions} property is {@code true}, then this method will print the Java stack trace to the script's stderr in addition to handling the exception and returning the exception string.
     * @param script The script that threw or is associated with the exception
     * @param throwable The exception that was thrown
     * @return A String representing the exception, including a traceback and/or stack trace
     * @throws ScriptExitException If the caught exception is a {@link org.python.core.Py#SystemExit}
     * @throws InvocationTargetException If there was an error when accessing the {@code exceptionToString} method in the {@link org.python.core.Py} class (via reflection)
     * @throws IllegalAccessException If there was an error when accessing the {@code exceptionToString} method in the {@link org.python.core.Py} class (via reflection)
     */
    public static synchronized String handleException(Script script, Throwable throwable) throws ScriptExitException, InvocationTargetException, IllegalAccessException {
        StdoutWrapper stderr = Py.stderr;

        if (Options.showJavaExceptions) {
            stderr.println("Java Traceback:");
            java.io.CharArrayWriter buf = new java.io.CharArrayWriter();
            if (throwable instanceof PyException) {
                ((PyException) throwable).super__printStackTrace(new java.io.PrintWriter(buf));
            } else {
                throwable.printStackTrace(new java.io.PrintWriter(buf));
            }
            stderr.print(buf.toString());
        }

        PyException exception = Py.JavaError(throwable);

        if (exception.match(Py.SystemExit))
            throw new ScriptExitException(script);

        //Sets ThreadState.exception
        Py.setException(exception, null);

        ThreadState threadState = Py.getThreadState();
        PySystemState sys = threadState.getSystemState();
        sys.last_type = exception.type;
        sys.last_value = exception.value;
        sys.last_traceback = exception.traceback;

        String exceptionString = (String) EXCEPTION_TO_STRING.invoke(null, exception.type, exception.value, exception.traceback);

        threadState.exception = null;

        return exceptionString;
    }
}
