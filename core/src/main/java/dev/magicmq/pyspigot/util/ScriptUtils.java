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

import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.PyString;
import org.python.core.PySystemState;

import java.util.Optional;

/**
 * A collection of utility methods related to scripts.
 */
public final class ScriptUtils {

    private static final StackWalker STACK_WALKER;

    static {
        STACK_WALKER = StackWalker.getInstance();
    }

    private ScriptUtils() {}

    /**
     * Attempts to get the script involved in a Java method call by analyzing the call stack.
     * @return The script associated with the method call, or null if no script was found in the call stack
     */
    public static Script getScriptFromCallStack() {
        Optional<StackWalker.StackFrame> callingScript = STACK_WALKER.walk(stream -> stream.filter(frame -> frame.getClassName().contains("org.python.pycode") && frame.getMethodName().equals("call_function")).findFirst());
        if (callingScript.isPresent()) {
            String scriptName = callingScript.get().getFileName();
            return ScriptManager.get().getScript(scriptName);
        } else {
            return null;
        }
    }

    /**
     * Initializes a new PySystemState for a new {@link org.python.util.PythonInterpreter} when a script is loaded.
     * <p>
     * This method will also do the following with the new PySystemState: set its class loader to the class loader provided by the {@link LibraryManager}, and add "./plugins/PySpigot/python-libs/" and "./plugins/PySpigot/scripts/" to the path.
     * @return The PySystemState that was created
     */
    public static PySystemState initPySystemState() {
        PySystemState sys = new PySystemState();
        sys.setClassLoader(LibraryManager.get().getClassLoader());
        sys.path.append(new PyString("./plugins/PySpigot/python-libs/"));
        sys.path.append(new PyString("./plugins/PySpigot/scripts/"));
        return sys;
    }
}
