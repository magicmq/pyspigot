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

package dev.magicmq.pyspigot.command;

import net.md_5.bungee.api.chat.BaseComponent;

public abstract class AbstractCommandSender<T> {

    protected T sender;

    public AbstractCommandSender(T sender) {
        this.sender = sender;
    }

    public abstract boolean hasPermission(String permission);

    public abstract void sendMessage(String message);

    public abstract void sendMessage(BaseComponent[] message);

    public abstract boolean isPlayer();
}
