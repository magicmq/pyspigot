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

package dev.magicmq.pyspigot.velocity.manager.listener;


import dev.magicmq.pyspigot.manager.listener.ScriptEventListener;
import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyFunction;

public class VelocityScriptListener<E> implements ScriptEventListener<E> {

    protected final Script script;
    protected final PyFunction listenerFunction;
    protected final Class<? extends E> event;

    public VelocityScriptListener(Script script, PyFunction listenerFunction, Class<? extends E> event) {
        this.script = script;
        this.listenerFunction = listenerFunction;
        this.event = event;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public PyFunction getListenerFunction() {
        return listenerFunction;
    }

    @Override
    public Class<? extends E> getEvent() {
        return event;
    }
}
