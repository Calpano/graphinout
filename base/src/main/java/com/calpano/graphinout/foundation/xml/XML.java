package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.json.JSON;
import com.calpano.graphinout.foundation.xml.element.XmlElement;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.regex.Pattern;

public interface XML {

    enum XmlSpace {
        default_(XML_SPACE__DEFAULT), preserve(XML_SPACE__PRESERVE);

        /** the XML attribute value to use in 'xml:space' attribute */
        public final String xmlAttValue;

        XmlSpace(String xmlAttValue) {
            this.xmlAttValue = xmlAttValue;
        }

        public static XmlSpace fromAttributesValue(@Nullable String xmlSpaceValue) {
            if (xmlSpaceValue == null) return default_;
            return switch (xmlSpaceValue) {
                case XML_SPACE__DEFAULT -> default_;
                case XML_SPACE__PRESERVE -> preserve;
                default -> throw new IllegalArgumentException("Unknown xml:space value: " + xmlSpaceValue);
            };
        }

        public static XmlSpace fromElement(XmlElement element) {
            return fromAttributesValue(element.attribute(XML_SPACE));
        }

        public JSON.XmlSpace toJson_XmlSpace() {
            return switch (this) {
                case preserve -> JSON.XmlSpace.preserve;
                case default_ -> JSON.XmlSpace.auto;
            };
        }

    }

    enum AttributeOrderPerElement {
        /** which is random, when coming from a SAX parser */
        AsWritten, Lexicographic
    }

    /**
     * XML Schema Instance "xmlns:xsi" is <code>http://www.w3.org/2001/XMLSchema-instance</code>.
     */
    @SuppressWarnings("JavadocLinkAsPlainText") String XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    /** Attribute key 'xmlns' */
    String ATT_XMLNS = "xmlns";
    /** Attribute key 'xmlns:xsi' */
    String ATT_XMLNS_XSI = "xmlns:xsi";
    /** Attribute key 'xsi:schemaLocation' */
    String ATT_XSI_SCHEMA_LOCATION = "xsi:schemaLocation";

    String XML_SPACE = "xml:space";

    String XML_VERSION_1_0_ENCODING_UTF_8 = """
            <?xml version="1.0" encoding="UTF-8"?>""";

    /** XML whitespace: SPACE, TAB, CR, LF */
    Pattern P_WHITESPACE = Pattern.compile("[ \t\r\n]*");
    String XML_SPACE__DEFAULT = "default";
    String XML_SPACE__PRESERVE = "preserve";
    String CDATA_START = "<![CDATA[";
    String CDATA_END = "]]>";
    /**
     * Space ({@code  }) - Unicode character #x20
     * <p>
     * Tab (\t) - Unicode character #x09
     * <p>
     * Carriage Return (\r) - Unicode character #x0D
     * <p>
     * Line Feed (\n) - Unicode character #x0A
     */
    Set<Integer> WHITE_SPACE_CHARACTERS = Set.of(0x20, 0x09, 0x0D, 0x0A);

    static boolean isWhitespace(String text) {
        return P_WHITESPACE.matcher(text).matches();
    }

    static String normalizeWhitespace(String text) {
        // IMPROVE java is trimming slightly too many character types here
        return text.trim();
    }

}
