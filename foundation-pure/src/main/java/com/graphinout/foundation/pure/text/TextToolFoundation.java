package com.graphinout.foundation.pure.text;

public class TextToolFoundation {

    /**
     * @param s input
     * @param length desired length
     * @return first n codePoints (or whole string is s is short enough)
     * @throws IllegalArgumentException if string is null
     */
    public static String firstN(final String s, final int length) {
        if (s == null) {
            throw new IllegalArgumentException("Cannot return suffix of length " + length + " from null");
        }
        if (s.length() <= length) {
            return s;
        }
        return s.substring(0, length);
    }

    /**
     * Prefix s with copies of padding until it has exactly desiredTotalLength chars. The last copy is cut short if
     * needed, e.g. {@code padLeft("ab", "x", 4)} is {@code "abax"}.
     * <p>
     * Result "00000 x"
     *
     * @param padding non-empty padding string
     * @param s input
     * @param desiredTotalLength minimum length of the result
     * @return s, if it is already long enough; otherwise padding + s with exactly desiredTotalLength chars
     * @throws IllegalArgumentException if padding is empty and s is too short
     */
    public static String padLeft(final String padding, final String s, final int desiredTotalLength) {
        if (s.length() >= desiredTotalLength) {
            return s;
        }
        return padding(padding, desiredTotalLength - s.length()) + s;
    }

    /**
     * Append copies of padding to s until it has exactly desiredTotalLength chars. The last copy is cut short if
     * needed, e.g. {@code padRight("ab", "x", 4)} is {@code "xaba"}.
     * <p>
     * Result " x 00000"
     *
     * @param padding non-empty padding string
     * @param s input
     * @param desiredTotalLength minimum length of the result
     * @return s, if it is already long enough; otherwise s + padding with exactly desiredTotalLength chars
     * @throws IllegalArgumentException if padding is empty and s is too short
     */
    public static String padRight(final String padding, final String s, final int desiredTotalLength) {
        if (s.length() >= desiredTotalLength) {
            return s;
        }
        return s + padding(padding, desiredTotalLength - s.length());
    }

    /** @return exactly length chars of repeated padding */
    private static String padding(final String padding, final int length) {
        if (padding.isEmpty()) {
            throw new IllegalArgumentException("Padding must not be empty");
        }
        final StringBuilder buf = new StringBuilder(length + padding.length());
        while (buf.length() < length) {
            buf.append(padding);
        }
        buf.setLength(length);
        return buf.toString();
    }

    /**
     * Remove each occurrence of each crapString
     *
     * @param s
     * @param crapStrings
     * @return
     */
    public static String strip(final String s, final String... crapStrings) {
        // could be made more performant
        String result = s;
        for (final String crapString : crapStrings) {
            result = result.replace(crapString, "");
        }
        return result;
    }

    /**
     * @param s
     * @param separator
     * @return substring by removing everything including last separator and what follows
     */
    public static String stripAfterLast(final String s, final char separator) {
        final int idx = s.lastIndexOf(separator);
        if (idx >= 0) {
            return s.substring(0, idx);
        }
        return s;
    }

    /**
     * @param s
     * @param marker
     * @return all text before the first marker or all text if there is no marker
     */
    public static String stripAllAfterFirst(final String s, final String marker) {
        final int i = s.indexOf(marker);
        if (i < 0) {
            return s;
        }
        return s.substring(0, i);
    }

    /**
     * @param s
     * @param marker
     * @return all text before the last marker or all text if there is no marker
     */
    public static String stripAllAfterLast(final String s, final String marker) {
        final int i = s.lastIndexOf(marker);
        if (i < 0) {
            return s;
        }
        return s.substring(0, i);
    }

    /**
     * @param s
     * @param marker
     * @return all text after the FIRST marker (excluding marker); or all text if there is no marker
     */
    public static String stripAllUntilFirst(final String s, final String marker) {
        final int i = s.indexOf(marker);
        if (i < 0) {
            return s;
        }
        return s.substring(i + marker.length());
    }

    /**
     * @param s
     * @param marker
     * @return all text after the LAST marker (excluding marker); or all text if there is no marker
     */
    public static String stripAllUntilLast(final String s, final String marker) {
        final int i = s.lastIndexOf(marker);
        if (i < 0) {
            return s;
        }
        return s.substring(i + marker.length());
    }

}
