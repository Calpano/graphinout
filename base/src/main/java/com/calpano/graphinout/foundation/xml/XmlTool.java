package com.calpano.graphinout.foundation.xml;

import org.slf4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.StringReader;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class XmlTool {

    @SuppressWarnings("HttpUrlsUsage") public static final String PROPERTIES_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    private static final Logger log = getLogger(XmlTool.class);
    /**
     * NameStartChar  ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] |
     * [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] |
     * [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
     */
    static String _NAME_ = ":_" + "a-z" + "A-Z" + "\\u00C0-\\u00D6" + "\\u00D8-\\u00F6" + "\\u00F8-\\u02FF" + "\\u0370-\\u037D" + "\\u037F-\\u1FFF" + "\\u200C-\\u200D" + "\\u2070-\\u218F" + "\\u2C00-\\u2FEF" + "\\u3001-\\uD7FF" + "\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD" + "\\x{10000}-\\x{EFFFF}";
    static String NAMESTART = "[" + _NAME_ + "]";
    /**
     * Name   ::= NameStartChar (NameChar)*
     * <p>
     * NameChar   ::=  NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
     */
    static String NAME = "[-.0-9" + "\\u00B7" + "\\u0300-\\u036F" + "\\u203F-\\u2040" + _NAME_ + "]";
    /** EntityRef := '&' Name ';' */
    static Pattern P_ENTITYREF = Pattern.compile("&" + NAMESTART + "(" + NAME + ")*;");
    /** CharRef  ::=   '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';' */
    static Pattern P_CHARREF = Pattern.compile("&#([0-9]+|#x([0-9a-fA-F]+));");

    /** any ref */
    static Pattern P_REF = Pattern.compile(P_ENTITYREF.pattern() + "|" + P_CHARREF.pattern());

    public static <T extends ContentHandler & LexicalHandler> XMLReader createXmlReaderOn(T contentHandlerAndLexicalHandler) throws SAXException, ParserConfigurationException {
        SAXParser saxParser = XmlFactory.createSaxParser();
        XMLReader reader = saxParser.getXMLReader();
        reader.setProperty(PROPERTIES_LEXICAL_HANDLER, contentHandlerAndLexicalHandler);
        reader.setContentHandler(contentHandlerAndLexicalHandler);
        return reader;
    }

    public static void ifAttributeNotNull(Map<String, String> attributes, String attributeName, Consumer<String> consumer) {
        if (attributes != null) {
            String value = attributes.get(attributeName);
            if (value != null) {
                consumer.accept(value);
            }
        }
    }

    /**
     * Helper method to consume an attribute value if present.
     */
    public static void onAttribute(Map<String, String> attributes, String attributeName, Consumer<String> attributeValueConsumer) {
        if (attributes != null) {
            String attributeValue = attributes.get(attributeName);
            if (attributeValue != null) attributeValueConsumer.accept(attributeValue);
        }
    }

    public static void parseAndWriteXml(File xmlFile, XmlWriter xmlWriter) throws Exception {
        Sax2XmlWriter handler2XmlWriter = new Sax2XmlWriter(xmlWriter, xmlError -> log.warn("Error " + xmlError));
        XMLReader reader = XmlTool.createXmlReaderOn(handler2XmlWriter);
        reader.parse(xmlFile.getAbsolutePath());
    }

    public static void parseAndWriteXml(String xml, XmlWriter xmlWriter) throws Exception {
        Sax2XmlWriter handler2XmlWriter = new Sax2XmlWriter(xmlWriter, xmlError -> log.warn("Error " + xmlError));
        XMLReader reader = XmlTool.createXmlReaderOn(handler2XmlWriter);
        StringReader sr = new StringReader(xml);
        org.xml.sax.InputSource is = new InputSource(sr);
        reader.parse(is);
    }

    public static String xmlDecode(String characterData) {
        return characterData //
                // amp first is crucial
                .replace("&amp;", "&") //
                .replace("&apos;", "'") //
                .replace("&quot;", "\"") //
                .replace("&lt;", "<") //
                .replace("&gt;", ">");
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
    public static String xmlEncode(String characterData) {
        if (characterData == null || characterData.isEmpty()) return "";

        // amp signs are tricky: only replace with &amp; if not declaring an entity
        // check string and find all sub-strings of characterData which are NOT a match for P_REF
        // (i.e. all substrings which are not an entity reference)
        Matcher matcher = P_REF.matcher(characterData);
        int position = 0;
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String before = characterData.substring(position, start);
            result.append(ampEncode(before));
            String s = matcher.group(0);
            result.append(s);
            position = end;
        }
        String remain = characterData.substring(position);
        result.append(ampEncode(remain));

        String res = result.toString() //
                // .replace("&", "&amp;") //
                //.replace("'", "&apos;") //
                //.replace("\"", "&quot;") //
                .replace("<", "&lt;") //
                .replace(">", "&gt;");
        return res;
    }

    public static String ampEncode(String raw) {
        return raw.replace("&", "&amp;");
    }


}
