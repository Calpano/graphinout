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
     * Format a string s so that it has the desired length. Do this by prefixing the string with copies of a padding
     * string.
     * <p>
     * Result "00000 x"
     * <p>
     * TODO weird behavior depending on length of padding string
     *
     * @param padding
     * @param s
     * @param desiredTotalLength
     * @return
     */
    public static String padLeft(final String padding, final String s, final int desiredTotalLength) {
        if (s.length() >= desiredTotalLength) {
            return s;
        }
        final int missing = desiredTotalLength - s.length();
        final int adding = missing / padding.length();
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < adding; i++) {
            buf.append(padding);
        }
        buf.append(s);
        return buf.toString();
    }

    /**
     * Result " x 00000"
     *
     * @param padding
     * @param s
     * @param desiredTotalLength (minimum)
     * @return
     */
    public static String padRight(final String padding, final String s, final int desiredTotalLength) {
        if (s.length() >= desiredTotalLength) {
            return s;
        }
        final int missing = desiredTotalLength - s.length();
        final int adding = missing / padding.length();
        final StringBuilder buf = new StringBuilder();
        buf.append(s);
        for (int i = 0; i < adding; i++) {
            buf.append(padding);
        }
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
