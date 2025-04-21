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

package dev.magicmq.pyspigot.exception;

/**
 * Thrown if a {@link org.python.core.Py#SystemExit} is handled in {@link dev.magicmq.pyspigot.util.ScriptUtils#handleException(Throwable)}.
 * <p>
 * Used to signal to the ScriptManager that the script should be unloaded.
 */
@SuppressWarnings("serial")
public class ScriptExitException extends Exception {

    public ScriptExitException() {}

}
