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

package dev.magicmq.pyspigot.manager.task;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.PyException;
import org.python.core.PyFunction;

/**
 * Represents a repeating task defined by a script.
 */
public class RepeatingTask extends Task {

    /**
     *
     * @param script The script associated with this repeating task
     * @param function The script function that should be called every time the repeating task executes
     */
    public RepeatingTask(Script script, PyFunction function) {
        super(script, function);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            function.__call__();
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error when executing task");
        }
    }
}
