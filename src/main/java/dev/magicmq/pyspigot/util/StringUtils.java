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

package dev.magicmq.pyspigot.util;

import org.jetbrains.annotations.NotNull;

/**
 * A utility class for various methods/classes related to Strings.
 */
public final class StringUtils {

    private StringUtils() {}

    public static String replaceLastOccurrence(String string, String toReplace, String replaceWith) {
        return string.replaceFirst("(?s)" + toReplace + "(?!.*?" + toReplace + ")", replaceWith);
    }

    public static class Version implements Comparable<Version> {

        private String version;

        public Version(String version) {
            if (version.contains("SNAPSHOT"))
                version = version.substring(0, version.indexOf("-"));
            this.version = version;
        }

        public final String getVersion() {
            return version;
        }

        @Override
        public int compareTo(@NotNull StringUtils.Version that) {
            String[] thisParts = this.getVersion().split("\\.");
            String[] thatParts = that.getVersion().split("\\.");
            int length = Math.max(thisParts.length, thatParts.length);
            for (int i = 0; i < length; i++) {
                int thisPart = i < thisParts.length ?
                        Integer.parseInt(thisParts[i]) : 0;
                int thatPart = i < thatParts.length ?
                        Integer.parseInt(thatParts[i]) : 0;
                if (thisPart < thatPart)
                    return -1;
                if (thisPart > thatPart)
                    return 1;
            }
            return 0;
        }
    }
}