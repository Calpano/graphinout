package com.graphinout.foundation.pure.collections.jajson;

import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.foundation.pure.input.ContentErrorException;
import com.graphinout.foundation.pure.input.Location;
import com.graphinout.foundation.pure.input.Locator;
import com.graphinout.foundation.pure.json.writer.JsonWriter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Simple hand-written JSON parser suitable for our limited needs. */
public final class JsonParser implements Locator {

    private final String s;
    private final JsonWriter w;
    private int pos;

    JsonParser(String s, JsonWriter jsonWriter) {
        this.s = s;
        this.w = jsonWriter;
    }

    public static void parse(String string, JsonWriter jsonWriter) {
        new JsonParser(string, jsonWriter).parseValue();
    }

    @Override
    public Location location() {
        // TODO use line numbers
        return Location.of(0, pos);
    }

    public int pos() {
        return pos;
    }

    boolean isEOF() {return pos >= s.length();}

    Object parseValue() {
        skipWs();
        if (isEOF()) throw error("Unexpected end of input");
        char c = s.charAt(pos);
        switch (c) {
            case 'n'://ull
                return parseNull();
            case 't'://rue
                return parseTrue();
            case 'f'://alse
                return parseFalse();
            case '"':
                return parseString(true);
            case '[':
                return parseArray();
            case '{':
                return parseObject();
            default:// Only numbers may start with '-' or a digit. Anything else here is an error.
                if (c == '-' || isDigit(c)) {
                    return parseNumber();
                }
                throw error("Unexpected character '" + c + "' while expecting a value");
        }
    }

    void skipWs() {
        while (!isEOF()) {
            char c = s.charAt(pos);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                pos++;
            } else break;
        }
    }

    private ContentErrorException error(String msg) {
        return new ContentErrorException(ContentError.ErrorLevel.Error, msg, location());
    }

    /**
     * Eat the expected characters or fail
     * @param characters expected characters
     */
    private void expect(String characters) {
        int end = pos + characters.length();
        if (end > s.length() || !s.regionMatches(pos, characters, 0, characters.length())) {
            throw error("Expected '" + characters + "'");
        }
        pos = end;
    }

    private static int hexVal(char h) {
        if (h >= '0' && h <= '9') return h - '0';
        if (h >= 'a' && h <= 'f') return 10 + (h - 'a');
        if (h >= 'A' && h <= 'F') return 10 + (h - 'A');
        return -1;
    }

    private static boolean isDigit(char c) {return c >= '0' && c <= '9';}

    private List<Object> parseArray() {
        if (s.charAt(pos) != '[') throw error("Expected [");
        pos++;

        w.arrayStart();
        ArrayList<Object> list = new ArrayList<>();
        skipWs();
        if (!isEOF() && s.charAt(pos) == ']') {
            pos++;
            w.arrayEnd();
            return list;
        }
        while (true) {
            Object v = parseValue();
            list.add(v);
            skipWs();
            if (isEOF()) throw error("Unterminated array");
            char ch = s.charAt(pos++);
            if (ch == ']') break;
            if (ch != ',') throw error("Expected , or ] in array");
        }
        w.arrayEnd();
        return list;
    }

    private Boolean parseFalse() {
        expect("false");
        w.onBoolean(false);
        return Boolean.FALSE;
    }

    private Object parseNull() {
        expect("null");
        w.onNull();
        return null;
    }

    private Number parseNumber() {
        int start = pos;
        char c = s.charAt(pos);
        if (c == '-') pos++;
        if (isEOF()) throw error("Unexpected end in number");
        if (s.charAt(pos) == '0') {
            pos++;
        } else {
            if (!isDigit(s.charAt(pos))) throw error("Expected digit");
            while (!isEOF() && isDigit(s.charAt(pos))) pos++;
        }
        boolean isFractional = false;
        if (!isEOF() && s.charAt(pos) == '.') {
            isFractional = true;
            pos++;
            if (isEOF() || !isDigit(s.charAt(pos))) throw error("Expected digit after decimal point");
            while (!isEOF() && isDigit(s.charAt(pos))) pos++;
        }
        if (!isEOF() && (s.charAt(pos) == 'e' || s.charAt(pos) == 'E')) {
            isFractional = true;
            pos++;
            if (!isEOF() && (s.charAt(pos) == '+' || s.charAt(pos) == '-')) pos++;
            if (isEOF() || !isDigit(s.charAt(pos))) throw error("Expected digit in exponent");
            while (!isEOF() && isDigit(s.charAt(pos))) pos++;
        }
        String num = s.substring(start, pos);
        try {
            if (isFractional) {
                double d = Double.parseDouble(num);
                if (Double.isInfinite(d) || Double.isNaN(d)) {
                    BigDecimal bigDecimal = new BigDecimal(num);
                    w.onBigDecimal(bigDecimal);
                    return bigDecimal;
                }
                return d;
            } else {
                // integer path: prefer Integer, then Long, else BigDecimal
                long l = Long.parseLong(num);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                w.onLong(l);
                return l;
            }
        } catch (NumberFormatException ex) {
            // Fallback to BigDecimal for large integers or precise fractional
            BigDecimal bigDecimal = new BigDecimal(num);
            w.onBigDecimal(bigDecimal);
            return bigDecimal;
        }
    }

    private Map<String, Object> parseObject() {
        if (s.charAt(pos) != '{') throw error("Expected {");
        w.objectStart();
        pos++;
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (!isEOF() && s.charAt(pos) == '}') {
            pos++;
            w.objectEnd();
            return map;
        }
        while (true) {
            skipWs();
            if (isEOF() || s.charAt(pos) != '"') throw error("Expected string key");
            String key = parseString(false);
            w.onKey(key);
            skipWs();
            if (isEOF() || s.charAt(pos) != ':') throw error("Expected : after key");
            pos++;
            // Be tolerant to whitespace between ':' and the value token.
            // Although parseValue() also skips whitespace, doing it here
            // ensures we never accidentally enter parseNumber() while still
            // positioned on the ':' (in case future changes alter skip logic).
            skipWs();
            Object v = parseValue();
            map.put(key, v);
            skipWs();
            if (isEOF()) throw error("Unterminated object");
            char ch = s.charAt(pos++);
            if (ch == '}') break;
            if (ch != ',') throw error("Expected , or } in object");
        }
        w.objectEnd();
        return map;
    }

    private String parseString(boolean emit) {
        if (s.charAt(pos) != '"') throw error("Expected \" for string");
        pos++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        boolean closed = false;
        while (!isEOF()) {
            char c = s.charAt(pos++);
            if (c == '"') {
                closed = true;
                break;
            }
            if (c == '\\') {
                if (isEOF()) throw error("Unterminated escape sequence");
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
        String string = sb.toString();
        if(emit) {
            w.onString(string);
        }
        return string;
    }

    private Boolean parseTrue() {
        expect("true");
        w.onBoolean(true);
        return Boolean.TRUE;
    }

}
