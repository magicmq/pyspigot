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

package dev.magicmq.pyspigot.util.player;


import net.kyori.adventure.text.Component;

/**
 * A utility class that wraps a platform-specific player object.
 */
public interface PlayerAdapter {

    /**
     * Check if the player has a permission via a platform-specific implementation.
     * @param permission The permission to check
     * @return True if the player has the permission, false if they do not
     */
    boolean hasPermission(String permission);

    /**
     * Send a message to the player via a platform-specific implementation.
     * @param message The message to send
     */
    void sendMessage(Component message);

}
