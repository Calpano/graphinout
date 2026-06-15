package com.graphinout.foundation.pure.xml;

import com.graphinout.foundation.pure.json.JSON;
import com.graphinout.foundation.pure.bridge.Java9;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * XML constants and helper types, including the {@code xml:space} values and the set of XML whitespace characters.
 */
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
            switch (xmlSpaceValue) {
                case XML_SPACE__DEFAULT:
                    return default_;
                case XML_SPACE__PRESERVE:
                    return preserve;
                default:
                    throw new IllegalArgumentException("Unknown xml:space value: " + xmlSpaceValue);
            }
        }

        public JSON.XmlSpace toJson_XmlSpace() {
            switch (this) {
                case preserve:
                    return JSON.XmlSpace.preserve;
                case default_:
                    return JSON.XmlSpace.auto;
                default:
                    throw new IllegalArgumentException();
            }
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

    String XML_VERSION_1_0_ENCODING_UTF_8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

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
    Set<Integer> WHITE_SPACE_CHARACTERS = Java9.Set.of(0x20, 0x09, 0x0D, 0x0A);

    @Deprecated
    static String ampEncode(String raw) {
        return raw.replace("&", "&amp;");
    }

    /** XML whitespace: SPACE, TAB, CR, LF */
    static boolean isWhitespace(int codePoint) {
        switch (codePoint) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                return true;
            default:
                return false;
        }
    }

    static boolean isWhitespace(String text) {
        return Java9.String.codePoints_allMatch( text,  XML::isWhitespace);
    }

    static String normalizeWhitespace(String text) {
        // IMPROVE java is trimming slightly too many character types here
        return text.trim();
    }

    /**
     * SAX Parser is running these decodes.
     *
     * @param characterData
     * @return
     */
    static String xmlDecode(String characterData) {
        return characterData //
                .replace("&apos;", "'") //
                .replace("&quot;", "\"") //
                .replace("&lt;", "<") //
                .replace("&gt;", ">")

                // amp LAST is crucial
                .replace("&amp;", "&") //
                ;
    }

    /**
     * Encode the special chars <code>'"&<></code> as
     * <pre>
     *     &apos;, &quot;, &amp;, &lt;, &gt;
     * </pre>
     * <p>
     * <a
     * href="https://stackoverflow.com/questions/1091945/what-characters-do-i-need-to-escape-in-xml-documents/46637835#46637835">Source</a>
     *
     * <h2>Always</h2>
     * <p>
     * Escape '<' as '&lt;' unless '<' is starting a '<tag/>' or other markup. Escape '&' as '&amp;' unless '&' is
     * starting an '&entity;'. Escape control codes in XML 1.1 via Base64 or Numeric Character References.
     *
     * <h2>Attribute Values</h2>
     * attr=" 'Single quotes' are ok within double quotes." attr=' "Double quotes" are ok within single quotes.' Escape
     * " as &quot; and ' as &apos; otherwise.
     * <p>
     * In comments: no '--' allowed.
     * <p>
     * In CDATA: Escape ']]>' as ']]&gt;'.
     */
    static String xmlEncode(String characterData) {
        if (characterData == null || characterData.isEmpty()) return "";


//        // amp signs are tricky: only replace with &amp; if not declaring an entity
//        // check string and find all sub-strings of characterData which are NOT a match for P_REF
//        // (i.e. all substrings which are not an entity reference)
//        Matcher matcher = P_REF.matcher(characterData);
//        int position = 0;
//        StringBuilder result = new StringBuilder();
//        while (matcher.find()) {
//            int start = matcher.start();
//            int end = matcher.end();
//            String before = characterData.substring(position, start);
//            result.append(ampEncode(before));
//            String s = matcher.group(0);
//            result.append(s);
//            position = end;
//        }
//        String remain = characterData.substring(position);
//        result.append(ampEncode(remain));

        String result = characterData;
        String res = result //
                // we must encode '&' round-tripping crap like '&amp;nbsp;'
                .replace("&", "&amp;") //

                .replace("'", "&apos;") //
                .replace("\"", "&quot;") //
                .replace("<", "&lt;") //
                .replace(">", "&gt;");
        return res;
    }

}
