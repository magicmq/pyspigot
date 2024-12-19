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

package dev.magicmq.pyspigot.config;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PluginConfig {

    void reload();

    boolean getMetricsEnabled();

    long getScriptLoadDelay();

    HashMap<String, String> getLibraryRelocations();

    DateTimeFormatter getLogTimestamp();

    boolean doScriptActionLogging();

    boolean doVerboseRedisLogging();

    boolean doScriptUnloadOnPluginDisable();

    boolean scriptOptionEnabled();

    int scriptOptionLoadPriority();

    List<String> scriptOptionPluginDepend();

    boolean scriptOptionFileLoggingEnabled();

    String scriptOptionMinLoggingLevel();

    String scriptOptionPermissionDefault();

    Map<?, ?> scriptOptionPermissions();

    boolean shouldPrintStackTraces();

    boolean shouldShowUpdateMessages();

    boolean shouldUpdatePySpigotLib();

    String getMessage(String key, boolean withPrefix);

    String getPrefix();
}
