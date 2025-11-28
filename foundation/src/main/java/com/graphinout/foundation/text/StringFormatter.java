package com.graphinout.foundation.text;

import java.util.function.Supplier;

public class StringFormatter {

    /** normalize line breaks across platforms: CR LF => \n, LF => \n, CR => \r */
    public static String normalizeLineBreaks(String raw) {
        return raw.replaceAll("\r\n", "\n").replace('\r', '\n');
    }

    public static String toStringOrNull(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static Supplier<String> toStringOrNull(Supplier<Object> supplier) {
        return () -> {
            Object o = supplier.get();
            return o == null ? null : String.valueOf(o);
        };
    }

    /**
     * Simple, hard break exactly at lineLength
     *
     * @param in         input
     * @param lineLength automatically enforced
     */
    public static String wrap(String in, int lineLength) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < in.length()) {
            if (i + lineLength < in.length()) {
                sb.append(in, i, i + lineLength).append("\n");
            } else {
                sb.append(in.substring(i));
            }
            i += lineLength;
        }
        return sb.toString();
    }

}
