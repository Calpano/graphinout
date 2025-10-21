package com.graphinout.foundation.json;

import com.graphinout.foundation.xml.XML;

public class JSON {

    public enum XmlSpace {
        preserve("preserve"), ignore("ignore"), auto("default");

        public final String jsonStringValue;

        XmlSpace(String jsonStringValue) {
            this.jsonStringValue = jsonStringValue;
        }

        public XML.XmlSpace toXml_XmlSpace() {
            return switch (this) {
                case preserve -> XML.XmlSpace.preserve;
                // XML has no 'ignore' option
                case ignore, auto -> XML.XmlSpace.default_;
            };
        }
    }

    /**
     * Escape all necessary codepoints in javaString to make it a valid JSON string
     *
     * @param javaString to escape
     * @return escaped
     */
    public static String jsonEscape(String javaString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < javaString.length(); i++) {
            char c = javaString.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ' || (c >= '\u007f' && c <= '\u009f') || (c >= '\u2000' && c <= '\u200f')) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

}
