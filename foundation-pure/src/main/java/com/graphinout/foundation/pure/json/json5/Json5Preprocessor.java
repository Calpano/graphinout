package com.graphinout.foundation.pure.json.json5;

import java.math.BigInteger;

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

    /**
     * Convert JSON5 hex numbers ({@code 0x1F}, {@code -0XfF}) to decimal. A sign stays where it is. Hex inside
     * identifiers such as {@code a0x1} is left alone. Uses no regex, so it works the same under j2cl.
     *
     * @param s non-string JSON5 content
     */
    static String hexToDecimal(String s) {
        if (s.indexOf('x') < 0 && s.indexOf('X') < 0) {
            return s;
        }
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            boolean startsHex = c == '0' && i + 2 < s.length() //
                    && (s.charAt(i + 1) == 'x' || s.charAt(i + 1) == 'X') //
                    && isHexDigit(s.charAt(i + 2)) //
                    && (i == 0 || !isIdentifierOrNumberChar(s.charAt(i - 1)));
            if (!startsHex) {
                out.append(c);
                i++;
                continue;
            }
            int end = i + 2;
            while (end < s.length() && isHexDigit(s.charAt(end))) {
                end++;
            }
            if (end < s.length() && isIdentifierOrNumberChar(s.charAt(end))) {
                // e.g. 0x1g: not a hex number, keep it as it is
                out.append(s, i, end);
            } else {
                out.append(new BigInteger(s.substring(i + 2, end), 16).toString());
            }
            i = end;
        }
        return out.toString();
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isIdentifierOrNumberChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$' || c == '.';
    }

    /** Normalize a run of non-string JSON5 content and append it to {@code sb}, then reset {@code chunk}. */
    private static void flushChunk(StringBuilder sb, StringBuilder chunk) {
        if (chunk.length() == 0) {
            return;
        }
        String normalized = hexToDecimal(chunk.toString())
                .replaceAll(UNQUOTED_KEY, "$1\"$2\":")
                .replaceAll(TRAILING_COMMA, "$1");
        sb.append(normalized);
        chunk.setLength(0);
    }

}
