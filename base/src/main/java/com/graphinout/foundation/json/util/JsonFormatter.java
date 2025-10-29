package com.graphinout.foundation.json.util;

public class JsonFormatter {

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
            } else if (Character.isWhitespace(c)) {
                // Ignore whitespace outside of strings
            } else
                // Check if adding the current character would exceed the max line length
                // and if it's not the very beginning of a line
                if (!line.isEmpty() && line.length() + 1 > maxLineLength) {
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
