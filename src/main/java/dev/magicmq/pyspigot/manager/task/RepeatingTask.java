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
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

/**
 * Represents a repeating task defined by a script.
 */
public class RepeatingTask extends Task {

    private final long interval;

    /**
     *
     * @param script The script associated with this repeating task
     * @param function The script function that should be called every time the repeating task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @param async True if the task is asynchronous, false if otherwise
     * @param delay The delay, in ticks, to wait until running the task
     * @param interval The interval, in ticks, between each repeat of the task
     */
    public RepeatingTask(Script script, Value function, Object[] functionArgs, boolean async, long delay, long interval) {
        super(script, function, functionArgs, async, delay);
        this.interval = interval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            if (functionArgs != null) {
                function.executeVoid(functionArgs);
            } else {
                function.executeVoid();
            }
        } catch (PolyglotException e) {
            ScriptManager.get().handleScriptException(script, e, "Error when executing task #" + getTaskId());
        }
    }

    /**
     * Prints a representation of this RepeatingTask in string format, including the task ID, if it is async, delay (if applicable), and interval (if applicable)
     * @return A string representation of the RepeatingTask
     */
    @Override
    public String toString() {
        return String.format("RepeatingTask[Task ID: %d, Async: %b, Delay: %d, Interval: %d]", getTaskId(), async, (int) delay, (int) interval);
    }
}
