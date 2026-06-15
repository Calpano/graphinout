package com.graphinout.foundation.pure.json.json5;

public class Json5Preprocessor {

    /** Quote unquoted object keys: {@code {key:} / ,key:} -> {@code {"key":} / ,"key":}. */
    private static final String UNQUOTED_KEY = "([{,]\\s*)([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*:";
    /** Drop a trailing comma before a closing brace/bracket. */
    private static final String TRAILING_COMMA = ",\\s*([}\\]])";

    /**
     * Convert all relaxed JSON5 specialties to their stricter JSON versions.
     *
     * @param json5 to convert
     * @return pure JSON
     */
    public static String toJson(String json5) {
        if (json5 == null || json5.isEmpty()) {
            return json5;
        }

        StringBuilder sb = new StringBuilder();
        // Non-string content pending regex normalization. We accumulate it separately from string literals so
        // that the context-insensitive regexes (key quoting, trailing-comma removal) never touch string values.
        StringBuilder chunk = new StringBuilder();
        int i = 0;
        while (i < json5.length()) {
            char c = json5.charAt(i);
            if (c == '"') {
                flushChunk(sb, chunk);
                sb.append(c);
                i++;
                while (i < json5.length()) {
                    char inner = json5.charAt(i);
                    sb.append(inner);
                    if (inner == '\\') {
                        if (i + 1 < json5.length()) {
                            sb.append(json5.charAt(i + 1));
                            i++;
                        }
                    } else if (inner == '"') {
                        break;
                    }
                    i++;
                }
            } else if (c == '\'') {
                flushChunk(sb, chunk);
                sb.append('"');
                i++;
                while (i < json5.length()) {
                    char inner = json5.charAt(i);
                    if (inner == '\\') {
                        sb.append(inner);
                        if (i + 1 < json5.length()) {
                            sb.append(json5.charAt(i + 1));
                            i++;
                        }
                    } else if (inner == '\'') {
                        sb.append('"');
                        break;
                    } else {
                        sb.append(inner);
                    }
                    i++;
                }
            } else if (c == '/') {
                if (i + 1 < json5.length()) {
                    char next = json5.charAt(i + 1);
                    if (next == '/') {
                        i += 2;
                        while (i < json5.length() && json5.charAt(i) != '\n') {
                            i++;
                        }
                        if (i < json5.length()) {
                            chunk.append('\n');
                        } else {
                            // end of string
                            continue;
                        }
                    } else if (next == '*') {
                        i += 2;
                        while (i + 1 < json5.length() && !(json5.charAt(i) == '*' && json5.charAt(i + 1) == '/')) {
                            i++;
                        }
                        i++;
                    } else {
                        chunk.append(c);
                    }
                } else {
                    chunk.append(c);
                }
            } else {
                chunk.append(c);
            }
            i++;
        }
        flushChunk(sb, chunk);

        return sb.toString();
    }

    /** Normalize a run of non-string JSON5 content and append it to {@code sb}, then reset {@code chunk}. */
    private static void flushChunk(StringBuilder sb, StringBuilder chunk) {
        if (chunk.length() == 0) {
            return;
        }
        String normalized = chunk.toString()
                .replaceAll(UNQUOTED_KEY, "$1\"$2\":")
                .replaceAll(TRAILING_COMMA, "$1");
        sb.append(normalized);
        chunk.setLength(0);
    }

}
