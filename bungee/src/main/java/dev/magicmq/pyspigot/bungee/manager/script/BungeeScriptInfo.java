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

package dev.magicmq.pyspigot.bungee.manager.script;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptInfo;

/**
 * A utility class that fetches and returns a script's info (for the /pyspigot info command).
 */
public class BungeeScriptInfo extends ScriptInfo {

    private static BungeeScriptInfo instance;

    private BungeeScriptInfo() {
        super();
    }

    /**
     * {@inheritDoc}
     * @param script The script whose information should be printed
     * @param appendTo The info StringBuilder that platform-specific manager info should be appended to
     */
    @Override
    public void printPlatformManagerInfo(Script script, StringBuilder appendTo) {
        //Don't need to add any additional managers
    }

    /**
     * Get the singleton instance of this BungeeScriptInfo.
     * @return The instance
     */
    public static BungeeScriptInfo get() {
        if (instance == null)
            instance = new BungeeScriptInfo();
        return instance;
    }
}

