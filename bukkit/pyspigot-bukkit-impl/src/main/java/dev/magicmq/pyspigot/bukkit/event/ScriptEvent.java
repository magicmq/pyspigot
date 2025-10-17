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

package dev.magicmq.pyspigot.bukkit.event;

import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Script event superclass. All script events inherit from this class.
 */
public class ScriptEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Script script;

    /**
     *
     * @param script The script associated with this event
     * @param async Whether the event is asynchronous
     */
    public ScriptEvent(Script script, boolean async) {
        super(async);
        this.script = script;
    }

    /**
     * Get the script associated with this event.
     * @return The script associated with this event
     */
    public Script getScript() {
        return script;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
