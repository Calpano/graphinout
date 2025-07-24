package com.calpano.graphinout.foundation.xml;

import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

public class XmlTool {

    @SuppressWarnings("HttpUrlsUsage") public static final String PROPERTIES_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";

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
        SAXParser saxParser = XmlFactory.createSaxParser();
        Sax2XmlWriter handler2XmlWriter = new Sax2XmlWriter(xmlWriter, null);
        XMLReader reader = saxParser.getXMLReader();
        reader.setProperty(PROPERTIES_LEXICAL_HANDLER, handler2XmlWriter);
        reader.setContentHandler(handler2XmlWriter);
        reader.parse(xmlFile.getAbsolutePath());
    }

    /**
     * Encode the special chars <code>'"&amp;<></code> as
     * <pre>
     *     &amp;apos;, &amp;quot;, &amp;amp;, &amp;lt;, &amp;gt;
     * </pre>
     */
    public static String xmlEncode(String characterData) {
        return characterData //
                .replace("&", "&amp;") //
                .replace("'", "&apos;") //
                .replace("\"", "&quot;") //
                .replace("<", "&lt;") //
                .replace(">", "&gt;");
    }

    public static String xmlDecode(String characterData) {
        return characterData //
                .replace("&amp;", "&") //
                .replace("&apos;", "'") //
                .replace("&quot;", "\"") //
                .replace("&lt;", "<") //
                .replace("&gt;", ">");
    }


}
