package com.calpano.graphinout.foundation.json.json5;

public class Json5Preprocessor {

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
        int i = 0;
        while (i < json5.length()) {
            char c = json5.charAt(i);
            if (c == '"') {
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
                            sb.append('\n');
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
                        sb.append(c);
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '}') {
                if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ',') {
                    sb.setCharAt(sb.length() - 1, c);
                } else {
                    sb.append(c);
                }
            } else if (c == ']') {
                if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == ',') {
                    sb.setCharAt(sb.length() - 1, c);
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
            i++;
        }

        String result = sb.toString();
        // still use regex for things that are not context-sensitive
        result = result.replaceAll("([{,]\\s*)([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*:", "$1\"$2\":");
        result = result.replaceAll(",\\s*([}\\]])", "$1");

        return result;
    }

}
