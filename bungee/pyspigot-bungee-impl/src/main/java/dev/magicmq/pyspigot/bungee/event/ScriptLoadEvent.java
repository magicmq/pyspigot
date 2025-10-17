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

/**
 * Called when a script is loaded. This event fires at the end of a load operation on a script. The event will not fire for scripts that fail to load. Therefore, it is safe to assume the script within this event is currently running.
 */
public class ScriptLoadEvent extends ScriptEvent {

    /**
     *
     * @param script The script that was loaded
     */
    public ScriptLoadEvent(Script script) {
        super(script);
    }
}
