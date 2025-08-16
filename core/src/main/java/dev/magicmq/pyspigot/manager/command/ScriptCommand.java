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

package dev.magicmq.pyspigot.manager.command;


import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyFunction;

/**
 * A command registered by a script. Meant to be implemented by platform-specific classes that also implement or extend an API's command executor class/interface.
 */
public interface ScriptCommand {

    /**
     * Get the script associated with this command.
     * @return The script associated with this command
     */
    Script getScript();

    /**
     * Get the command function associated with this command.
     * @return The command function associated with this command
     */
    PyFunction getCommandFunction();

    /**
     * Get the name of this command.
     * @return The name of this command
     */
    String getName();

    /**
     * Set the tab completion function for this command.
     * @param tabFunction The tab function to set
     */
    void setTabFunction(PyFunction tabFunction);

}
