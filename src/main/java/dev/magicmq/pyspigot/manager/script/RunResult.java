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

package dev.magicmq.pyspigot.manager.script;

/**
 * An enum class representing various outcomes of running a script, including run failures.
 */
public enum RunResult {

    /**
     * Returned if the script loaded successfully.
     */
    SUCCESS,

    /**
     * Returned if the script was not loaded because it was disabled as per its script options in script_options.yml
     */
    FAIL_DISABLED,

    /**
     * Returned if the script was not loaded because it has one or more missing plugin dependencies.
     */
    FAIL_PLUGIN_DEPENDENCY,

    /**
     * Returned if the script was loaded but failed during runtime due to an error.
     */
    FAIL_ERROR,

    /**
     * Returned if a script is already loaded with the same name.
     */
    FAIL_DUPLICATE,

    /**
     * Returned if a script was not found with the given name.
     */
    FAIL_SCRIPT_NOT_FOUND
}
