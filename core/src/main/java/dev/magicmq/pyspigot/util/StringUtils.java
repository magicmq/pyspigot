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

package dev.magicmq.pyspigot.util;

import java.time.Duration;

/**
 * A utility class for various methods/classes related to Strings.
 */
public final class StringUtils {

    private StringUtils() {}

    public static String replaceLastOccurrence(String string, String toReplace, String replaceWith) {
        return string.replaceFirst("(?s)" + toReplace + "(?!.*?" + toReplace + ")", replaceWith);
    }

    public static String formatDuration(Duration duration) {
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        return days + "d" + hours + "h" + minutes + "m" + seconds + "s";
    }

    public static class Version implements Comparable<Version> {

        private final String version;

        public Version(String version) {
            this.version = version;
        }

        @Override
        public int compareTo(StringUtils.Version that) {
            String thisClean = this.version.replace("-SNAPSHOT", "");
            String thatClean = that.version.replace("-SNAPSHOT", "");

            String[] thisParts = thisClean.split("\\.");
            String[] thatParts = thatClean.split("\\.");

            int length = Math.max(thisParts.length, thatParts.length);
            for (int i = 0; i < length; i++) {
                int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
                int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;

                if (thisPart < thatPart)
                    return -1;
                if (thisPart > thatPart)
                    return 1;
            }

            boolean thisIsSnapshot = this.version.endsWith("-SNAPSHOT");
            boolean thatIsSnapshot = that.version.endsWith("-SNAPSHOT");
            if (thisIsSnapshot && !thatIsSnapshot)
                return -1;
            if (!thisIsSnapshot && thatIsSnapshot)
                return 1;

            return 0;
        }
    }
}