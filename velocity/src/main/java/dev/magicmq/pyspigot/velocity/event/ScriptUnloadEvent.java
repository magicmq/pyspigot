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

package dev.magicmq.pyspigot.velocity.event;


import dev.magicmq.pyspigot.manager.script.Script;

/**
 * Called when a script is unloaded.
 */
public class ScriptUnloadEvent extends ScriptEvent {

    private final boolean error;

    /**
     *
     * @param script The script that was unloaded
     * @param error Whether the script was unloaded due to an error
     */
    public ScriptUnloadEvent(Script script, boolean error) {
        super(script);
        this.error = error;
    }

    /**
     * Get if this unload event was due to a script error.
     * @return True if the script was unloaded due to an error/exception, false if otherwise
     */
    public boolean isError() {
        return error;
    }
}
