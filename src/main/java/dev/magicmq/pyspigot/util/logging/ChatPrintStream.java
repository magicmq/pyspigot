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

package dev.magicmq.pyspigot.util.logging;

import org.bukkit.command.CommandSender;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A wrapper class that captures print statements and errors/exceptions from scripts and redirects them to the script's logger.
 */
public class ChatPrintStream extends PrintStream {

    private final CommandSender sender;

    /**
     *
     * @param out The parent OutputStream, usually System.out or System.err
     * @param sender The CommandSender whose chat messages sent to this PrintStream should be redirected
     */
    public ChatPrintStream(OutputStream out, CommandSender sender) {
        super(out);
        this.sender = sender;
    }

    /**
     * Captures writes to the PrintStream, converts the bytes into readable text (truncating according to the specified length and offset), and sends the text to the CommandSender's chat (or console). This method also strips carriage returns/new line characters from the end of the text, because sendMessage implies a new line.
     * @param buf A byte array
     * @param off Offset from which to start taking bytes
     * @param len Number of bytes to write
     */
    @Override
    public void write(byte[] buf, int off, int len) {
        byte[] toLog = Arrays.copyOfRange(buf, off, len);
        String string = new String(toLog, StandardCharsets.UTF_8);
        string = string.replaceAll("\\R$", "");
        sender.sendMessage(string);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] buf) {
        this.write(buf, 0, buf.length);
    }
}
