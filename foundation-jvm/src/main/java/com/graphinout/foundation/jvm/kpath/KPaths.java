package com.graphinout.foundation.jvm.kpath;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses and builds path strings for {@link PathResolver}.
 * <p>
 * Syntax: steps are separated by {@code /}; a single leading {@code /} is ignored. A step that starts with a single
 * quote is a <em>quoted step</em>: it ends at the next single quote that is followed by {@code /} or the end of the
 * path, and may contain any character, including {@code /}. Inside a quoted step, {@code ''} stands for one
 * {@code '}. A quoted step is always a literal map key, so {@code '*'} or {@code '[0]'} address keys named
 * {@code *} or {@code [0]}. For the meaning of unquoted steps see {@link Step}.
 * <p>
 * Example: {@code /foo/'bar/baz'/'it''s'} has the steps {@code foo}, {@code 'bar/baz'}, {@code 'it''s'}, which
 * address the keys {@code foo}, {@code bar/baz} and {@code it's}.
 */
public class KPaths {

    static final char QUOTE = '\'';

    /**
     * Splits a path string into steps. Quoted steps keep their quotes, so that {@link Step} treats them as literal
     * keys.
     *
     * @param pathString e.g. {@code a/b/c} or {@code /foo/'bar/baz'/boingo}
     * @return the steps, e.g. {@code [a, b, c]} or {@code [foo, 'bar/baz', boingo]}
     * @throws IllegalArgumentException if a quoted step is not closed, or the closing quote is not followed by
     *                                  {@code /} or the end of the path
     */
    public static List<String> of(String pathString) {
        List<String> steps = new ArrayList<>();
        int i = pathString.startsWith("/") ? 1 : 0;
        int n = pathString.length();
        while (true) {
            int start = i;
            if (i < n && pathString.charAt(i) == QUOTE) {
                // quoted step: find the closing quote, skipping '' escapes
                i++;
                while (true) {
                    if (i >= n) {
                        throw new IllegalArgumentException("Unclosed quote in path step starting at index " + start + ": " + pathString);
                    }
                    if (pathString.charAt(i) == QUOTE) {
                        if (i + 1 < n && pathString.charAt(i + 1) == QUOTE) {
                            i += 2;
                            continue;
                        }
                        i++;
                        break;
                    }
                    i++;
                }
                if (i < n && pathString.charAt(i) != '/') {
                    throw new IllegalArgumentException("Expected '/' or end of path after closing quote at index " + (i - 1) + ": " + pathString);
                }
            } else {
                while (i < n && pathString.charAt(i) != '/') {
                    i++;
                }
            }
            steps.add(pathString.substring(start, i));
            if (i >= n) {
                return steps;
            }
            i++; // skip '/'
        }
    }

    /**
     * Quotes a map key so that it is addressed literally, even if it contains {@code /}, {@code '} or looks like a
     * wildcard.
     *
     * @param key any map key
     * @return the key as a quoted step, e.g. {@code it's} becomes {@code 'it''s'}
     */
    public static String quote(String key) {
        return QUOTE + key.replace("'", "''") + QUOTE;
    }

    /**
     * @return true if the step is a quoted step, i.e. starts and ends with a single quote
     */
    static boolean isQuoted(String step) {
        return step.length() >= 2 && step.charAt(0) == QUOTE && step.charAt(step.length() - 1) == QUOTE;
    }

    /**
     * @param quotedStep a step for which {@link #isQuoted(String)} is true
     * @return the literal key: quotes removed, {@code ''} replaced by {@code '}
     */
    static String unquote(String quotedStep) {
        return quotedStep.substring(1, quotedStep.length() - 1).replace("''", "'");
    }

}
