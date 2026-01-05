package com.graphinout.foundation.pure.text;

import com.graphinout.foundation.pure.bridge.Java9;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;

/**
 * A string-to-string formatter which also works with invalid JSON input.
 */
public class JsonFormatting {

    /**
     * @param json must be valid JSON
     */
    public static String formatCompact(String json) {
        Object o = JaJson.parse(json);
        return JsonCompactFormatter.formatCompact(o);
    }

    /**
     * Wrap valid or invalid JSON into multiple lines for easier debugging. Each line has a max length of 60 characters.
     * Quoted strings cannot be wrapped. They are the only exception and might exceed the max line length. Lines are
     * wrapped, if the next token would go over the limit.
     */
    public static String formatDebug(String source) {
        StringBuilder wrapped = new StringBuilder();
        StringBuilder line = new StringBuilder();
        StringBuilder quotedString = new StringBuilder();

        int sourcePos = 0;
        boolean inString = false;
        int maxLineLength = 60;

        while (sourcePos < source.length()) {
            char c = source.charAt(sourcePos);
            if (c == '"') {
                quotedString.append(c);
                inString = !inString;
                if (!inString) {
                    // where to emit the token?
                    if (line.length() + quotedString.length() > maxLineLength) {
                        wrapped.append(line).append("\n");
                        line = new StringBuilder();
                        if (quotedString.length() > maxLineLength) {
                            wrapped.append(quotedString).append("\n");
                        } else {
                            line.append(quotedString);
                        }
                    } else {
                        line.append(quotedString);
                    }
                    quotedString = new StringBuilder();
                }
            } else if (inString) {
                quotedString.append(c);
            } else //noinspection StatementWithEmptyBody
                if (Character.isWhitespace(c)) {
                    // Ignore whitespace outside of strings
                } else
                    // Check if adding the current character would exceed the max line length
                    // and if it's not the very beginning of a line
                    if (!Java9.String.isEmpty(line) && line.length() + 1 > maxLineLength) {
                        wrapped.append(line).append("\n");
                        line = new StringBuilder();
                        line.append(c);
                    } else {
                        // just append
                        line.append(c);
                        if (c == ':') {
                            line.append("\n");
                            wrapped.append(line);
                            line = new StringBuilder();
                        }
                    }
            sourcePos++;
        }
        wrapped.append(line);
        return wrapped.toString();
    }

    /**
     * Removes all JSON whitespace (space, tab, CR, LF) that is outside string literals. This produces a canonical
     * compact form suitable for comparing in tests.
     */
    public static String normalizeJsonWhitespace(String json) {
        StringBuilder sb = new StringBuilder(json.length());
        boolean inString = false;
        boolean escaping = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                sb.append(c);
                if (escaping) {
                    // whatever the char is, we just consumed an escape sequence character
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
            } else {
                if (c == '"') {
                    inString = true;
                    sb.append(c);
                } else //noinspection StatementWithEmptyBody
                    if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                        // drop insignificant whitespace outside strings
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Correctly remove whitespace between tokens, but not within strings.
     */
    public static String removeWhitespace(String json) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                inString = !inString;
                sb.append(c);
            } else if (inString) {
                sb.append(c);
            } else if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
