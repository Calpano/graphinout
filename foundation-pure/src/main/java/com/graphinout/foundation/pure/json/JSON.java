package com.graphinout.foundation.pure.json;


import com.graphinout.foundation.pure.text.Texts;
import com.graphinout.foundation.pure.xml.XML;

public class JSON {

    public enum XmlSpace {
        preserve("preserve"), ignore("ignore"), auto("default");

        public final String jsonStringValue;

        XmlSpace(String jsonStringValue) {
            this.jsonStringValue = jsonStringValue;
        }

        public static XmlSpace parseJson(String jsonStringValue) {
            if (jsonStringValue == null) return XmlSpace.auto;
            switch (jsonStringValue) {
                case "preserve":
                    return XmlSpace.preserve;
                case "ignore":
                    return XmlSpace.ignore;
                case "default":
                case "":
                    return XmlSpace.auto;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public XML.XmlSpace toXml_XmlSpace() {
            switch (this) {
                case preserve:
                    return XML.XmlSpace.preserve;
                // XML has no 'ignore' option
                case ignore:
                case auto:
                    return XML.XmlSpace.default_;
                default:
                    throw new IllegalArgumentException();
            }
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
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ' || (c >= '\u007f' && c <= '\u009f') || (c >= '\u2000' && c <= '\u200f')) {
                        sb.append(Texts.asUnicodeEscape( (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

}
