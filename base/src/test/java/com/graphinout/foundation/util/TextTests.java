package com.graphinout.foundation.util;

import org.slf4j.Logger;

import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class TextTests {

    public static final int DIFF_LIMIT_LEN = 2048;
    private static final Logger log = getLogger(TextTests.class);

    /**
     * Highlight the index i and show some context around it. E.g. for 'abcdefghijklmnopqrstuvvxyz' and index 10 show
     * '... fghi j (33)  k (73) l (33) mnop ...'
     *
     * @param s input
     * @param i index
     */
    public static String toHighlight(String s, int i) {
        int context = 10; // Number of characters to show around the index
        StringBuilder sb = new StringBuilder();

        if (i < 0 || i >= s.length()) {
            return "Index " + i + " is out of bounds for string of length " + s.length();
        }

        // Add prefix
        int prefixEnd = i;
        String prefix = "";
        if (prefixEnd > 0) {
            int prefixStart = Math.max(0, prefixEnd - context);
            if (prefixStart > 0) {
                sb.append("...");
            }
            prefix = s.substring(prefixStart, prefixEnd);
        }
        if (!prefix.isEmpty()) {
            // render last prefix char as highlight character
            int last = prefix.length() - 1;

            String newPrefix = toPrintableString( prefix.substring(0, last));
            newPrefix += " ";
            newPrefix += toHighlightCharacter(prefix.charAt(last));
            prefix = newPrefix;
        }
        sb.append(prefix);

        // Add highlighted character
        sb.append(" __");
        sb.append(toHighlightCharacter(s.charAt(i)));
        sb.append("__ ");

        // Add suffix
        int suffixStart = i + 1;
        String suffix = "";
        if (suffixStart < s.length()) {
            int suffixEnd = Math.min(suffixStart + context, s.length());
            suffix = s.substring( suffixStart,suffixEnd);
            if (suffixEnd < s.length()) {
                suffix += "...";
            }
        }
        // render first suffix character as highlight character instead
        if (!suffix.isEmpty()) {
            char first = suffix.charAt(0);
            sb.append(toHighlightCharacter(first));
            sb.append(" ");
            suffix = toPrintableString( suffix.substring(1));
            sb.append(suffix);
        }

        return sb.toString();
    }

    public static String toHighlightCharacter(char c) {
        return "'" + toPrintableCharacter(c) + "'<" + (int) c + ">";
    }

    public static String toPrintableCharacter(char c) {
        // represent non-printable charas by TWO/THREE uppercase letter codes
        return switch (c) {
            case ' ' -> "SPACE";
            case '\t' -> "TAB";
            case '\n' -> "LF";
            case '\r' -> "CR";
            case '\f' -> "FF";
            default -> String.valueOf(c);
        };
    }

    public static String toPrintableString(String s) {
        //transcode each char into printable
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            String printableCharacter = toPrintableCharacter(c);
            if (printableCharacter.equals(String.valueOf(c))) {
                sb.append(printableCharacter);
            } else {
                sb.append("<{").append(printableCharacter).append("}>");
            }
        }
        return sb.toString();

    }

    public static boolean xAssertEqual(String actual, String expected) {
        if (Objects.equals(actual, expected)) return true;

        // dump compare
        StringBuilder sb = new StringBuilder();
        if (actual == null) {
            sb.append("actual is null, but expected is not.");
        } else if (expected == null) {
            sb.append("actual is not null, but expected is null.");
        } else {
            if (actual.length() != expected.length()) {
                sb.append("actual.length()=").append(actual.length()).append(", but expected.length()=").append(expected.length()).append("\n");
            }
            // if strings are short enough, try to compute a diff
            if (actual.length() < DIFF_LIMIT_LEN && expected.length() < DIFF_LIMIT_LEN) {
                // do we have a common prefix?
                int firstDiff = -1;
                for (int i = 0; i < actual.length() && i < expected.length(); i++) {
                    if (actual.charAt(i) != expected.charAt(i)) {
                        firstDiff = i;
                        break;
                    }
                }
                if (firstDiff != -1) {
                    sb.append("First difference at index ").append(firstDiff).append(":\n");
                    sb.append("actual:   ").append(toHighlight(actual, firstDiff)).append("\n");
                    sb.append("expected: ").append(toHighlight(expected, firstDiff)).append("\n");
                }
            }
        }

        String msg = sb.toString();
        log.warn(msg);
        throw new AssertionError("Expected equal, but: " + msg);
    }

}
