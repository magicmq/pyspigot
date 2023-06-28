package dev.magicmq.pyspigot.util;

public class StringUtils {

    public static String replaceLastOccurrence(String string, String toReplace, String replaceWith) {
        return string.replaceFirst("(?s)" + toReplace + "(?!.*?" + toReplace + ")", replaceWith);
    }
}