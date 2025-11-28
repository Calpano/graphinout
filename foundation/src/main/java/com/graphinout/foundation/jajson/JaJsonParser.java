package com.graphinout.foundation.jajson;

import java.util.LinkedHashMap;

/** Simple hand-written JSON parser suitable for our limited needs. */
public final class JaJsonParser {

    private final String s;

    public int pos() {
        return pos;
    }

    private int pos;

    JaJsonParser(String s) {this.s = s;}

    boolean eof() {return pos >= s.length();}

    Object parseValue() {
        skipWs();
        if (eof()) throw error("Unexpected end of input");
        char c = s.charAt(pos);
        return switch (c) {
            case 'n' -> parseNull();
            case 't' -> parseTrue();
            case 'f' -> parseFalse();
            case '"' -> parseString();
            case '[' -> parseArray();
            case '{' -> parseObject();
            default -> {
                // Only numbers may start with '-' or a digit. Anything else here is an error.
                if (c == '-' || isDigit(c)) {
                    yield parseNumber();
                }
                throw error("Unexpected character '" + c + "' while expecting a value");
            }
        };
    }

    void skipWs() {
        while (!eof()) {
            char c = s.charAt(pos);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') pos++;
            else break;
        }
    }

    private IllegalArgumentException error(String msg) {
        return new IllegalArgumentException(msg + " at position " + pos);
    }

    private void expect(String lit) {
        int end = pos + lit.length();
        if (end > s.length() || !s.regionMatches(pos, lit, 0, lit.length())) {
            throw error("Expected '" + lit + "'");
        }
        pos = end;
    }

    private int hexVal(char h) {
        if (h >= '0' && h <= '9') return h - '0';
        if (h >= 'a' && h <= 'f') return 10 + (h - 'a');
        if (h >= 'A' && h <= 'F') return 10 + (h - 'A');
        return -1;
    }

    private boolean isDigit(char c) {return c >= '0' && c <= '9';}

    private java.util.List<Object> parseArray() {
        if (s.charAt(pos) != '[') throw error("Expected [");
        pos++;
        java.util.ArrayList<Object> list = new java.util.ArrayList<>();
        skipWs();
        if (!eof() && s.charAt(pos) == ']') {
            pos++;
            return list;
        }
        while (true) {
            Object v = parseValue();
            list.add(v);
            skipWs();
            if (eof()) throw error("Unterminated array");
            char ch = s.charAt(pos++);
            if (ch == ']') break;
            if (ch != ',') throw error("Expected , or ] in array");
        }
        return list;
    }

    private Boolean parseFalse() {
        expect("false");
        return Boolean.FALSE;
    }

    private Object parseNull() {
        expect("null");
        return null;
    }

    private Number parseNumber() {
        int start = pos;
        char c = s.charAt(pos);
        if (c == '-') pos++;
        if (eof()) throw error("Unexpected end in number");
        if (s.charAt(pos) == '0') {
            pos++;
        } else {
            if (!isDigit(s.charAt(pos))) throw error("Expected digit");
            while (!eof() && isDigit(s.charAt(pos))) pos++;
        }
        boolean isFractional = false;
        if (!eof() && s.charAt(pos) == '.') {
            isFractional = true;
            pos++;
            if (eof() || !isDigit(s.charAt(pos))) throw error("Expected digit after decimal point");
            while (!eof() && isDigit(s.charAt(pos))) pos++;
        }
        if (!eof() && (s.charAt(pos) == 'e' || s.charAt(pos) == 'E')) {
            isFractional = true;
            pos++;
            if (!eof() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) pos++;
            if (eof() || !isDigit(s.charAt(pos))) throw error("Expected digit in exponent");
            while (!eof() && isDigit(s.charAt(pos))) pos++;
        }
        String num = s.substring(start, pos);
        try {
            if (isFractional) {
                double d = Double.parseDouble(num);
                if (Double.isInfinite(d) || Double.isNaN(d)) {
                    return new java.math.BigDecimal(num);
                }
                return d;
            } else {
                // integer path: prefer Integer, then Long, else BigDecimal
                long l = Long.parseLong(num);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                return l;
            }
        } catch (NumberFormatException ex) {
            // Fallback to BigDecimal for large integers or precise fractional
            return new java.math.BigDecimal(num);
        }
    }

    private java.util.Map<String, Object> parseObject() {
        if (s.charAt(pos) != '{') throw error("Expected {");
        pos++;
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (!eof() && s.charAt(pos) == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWs();
            if (eof() || s.charAt(pos) != '"') throw error("Expected string key");
            String key = parseString();
            skipWs();
            if (eof() || s.charAt(pos) != ':') throw error("Expected : after key");
            pos++;
            // Be tolerant to whitespace between ':' and the value token.
            // Although parseValue() also skips whitespace, doing it here
            // ensures we never accidentally enter parseNumber() while still
            // positioned on the ':' (in case future changes alter skip logic).
            skipWs();
            Object v = parseValue();
            map.put(key, v);
            skipWs();
            if (eof()) throw error("Unterminated object");
            char ch = s.charAt(pos++);
            if (ch == '}') break;
            if (ch != ',') throw error("Expected , or } in object");
        }
        return map;
    }

    private String parseString() {
        if (s.charAt(pos) != '"') throw error("Expected \" for string");
        pos++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        boolean closed = false;
        while (!eof()) {
            char c = s.charAt(pos++);
            if (c == '"') {
                closed = true;
                break;
            }
            if (c == '\\') {
                if (eof()) throw error("Unterminated escape sequence");
                char e = s.charAt(pos++);
                switch (e) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        if (pos + 4 > s.length()) throw error("Invalid unicode escape");
                        int code = 0;
                        for (int i = 0; i < 4; i++) {
                            char h = s.charAt(pos++);
                            int v = hexVal(h);
                            if (v < 0) throw error("Invalid hex in unicode escape");
                            code = (code << 4) | v;
                        }
                        sb.append((char) code);
                        break;
                    default:
                        throw error("Invalid escape character: \\" + e + "\"");
                }
            } else {
                sb.append(c);
            }
        }
        if (!closed) throw error("Unterminated string");
        return sb.toString();
    }

    private Boolean parseTrue() {
        expect("true");
        return Boolean.TRUE;
    }

}
