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

public enum RunResult {

    /**
     * Returned if the script ran successfully.
     */
    SUCCESS,

    /**
     * Returned if the script was not run because it was disabled as per its script options in script_options.yml
     */
    FAIL_DISABLED,

    /**
     * Returned if the script was not run because it has missing dependencies.
     */
    FAIL_DEPENDENCY,

    /**
     * Returned if the script was run but failed during runtime due to an error.
     */
    FAIL_ERROR

}
