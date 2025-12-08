package com.graphinout.base.xml.util;

import com.graphinout.base.xml.NamedEntities;
import com.graphinout.base.xml.factory.XmlFactory;
import com.graphinout.base.xml.sax.Sax2XmlWriter;
import com.graphinout.foundation.pure.xml.XML;
import com.graphinout.foundation.pure.xml.writer.XmlWriter;
import com.graphinout.foundation.jvm.xml.XmlFoundation;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.Nullable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.graphinout.foundation.pure.text.Texts.LF_10_N;
import static org.slf4j.LoggerFactory.getLogger;

public class XmlTool {

    /** SAX config */
    @SuppressWarnings("HttpUrlsUsage") public static final String PROPERTIES_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";

    public static <T extends ContentHandler & LexicalHandler> XMLReader createXmlReaderOn(T contentHandlerAndLexicalHandler) throws SAXException, ParserConfigurationException {
        SAXParser saxParser = XmlFactory.createSaxParser();
        XMLReader reader = saxParser.getXMLReader();
        // TODO document
        reader.setProperty(PROPERTIES_LEXICAL_HANDLER, contentHandlerAndLexicalHandler);
        reader.setContentHandler(contentHandlerAndLexicalHandler);
        // set namespace-aware
        reader.setFeature("http://xml.org/sax/features/namespaces", true);

        return reader;
    }

    /**
     * @param attributes
     * @param attributeName
     * @param consumer      gets the XML attribute value if the attribute is present and value not null
     * @retur same as consumer call
     */
    public static @Nullable String ifAttributeNotNull(Map<String, String> attributes, String attributeName, Consumer<String> consumer) {
        if (attributes != null) {
            String value = attributes.get(attributeName);
            if (value != null) {
                consumer.accept(value);
                return value;
            }
        }
        return null;
    }

    /**
     * Simulate what Preprocessing plus SAX parsing gets in memory. Normalize (1) HTML entities to amp-encoded, (2) line
     * endings to LF, (3) numerical entities to Unicode.
     *
     * <h2>XML Entities</h2>
     * A SAX parser decodes all 5 XML entities. When writing back, we encode them all.
     * <p>
     * So we normalise '{@code "}' to '{@code &quot;}'.
     *
     * <h2>Other (HTML) Named Entities</h2>
     * SAX cannot handle them, so before running the SAX parser we amp-escape the '{@code &foo;'} to
     * '{@code &amp;foo;}'.
     *
     * <h2>Line Breaks</h2>
     * XML parsers are required to normalize line endings, so CR and CRLF sequences are all turned into LF (CR=x0D,
     * LF=x0A) before presentation to the application.
     * <p>
     * If you want to retain the CR characters (why??) you should represent them as numeric character references, that
     * is {@code &#13;}.
     *
     * @param xml well-formed, but may contain HTML entities
     * @return the result of SAX parsing, then XML encoding
     */
    public static String normaliseLikeEntityPreprocessingThenSaxParsing(String xml) {
        // before SAX parsing
        String preprocessed = NamedEntities.htmlEntitiesToAmpEncoded(xml);
        // simulate SAX parsing
        String decoded = XML.xmlDecode(preprocessed);
        // normalize CR LF to LF and similar;
        String normalizedLineEndings = normalizeLineEndingsLikeSax(decoded);
        String resolvedNumericalEntities = resolveNumericalEntities(normalizedLineEndings);
        //String encoded = xmlEncode(resolvedNumericalEntities);
        //return encoded;
        return resolvedNumericalEntities;
    }

    /**
     * DO NOT resolve numerical entities first, because
     * <p>
     * TEXT #13 CR LF -> TEXT LF
     * <p>
     * but
     * <p>
     * TEXT CR CR     -> TEXT LF LF
     * <p>
     * Observed SAX2 Behavior:
     * <li>CR        parsed as LF</li>
     * <li>CR CR     parsed as LF LF</li>
     * <li>LF        parsed as LF</li>
     * <li>CR LF     parsed as LF</li>
     * <li>#13       parsed as LF</li>
     * <li>#13 CR    parsed as LF</li>
     * <li>#13 LF    parsed as LF</li>
     * <li>#13 CR LF parsed as LF</li>
     */
    public static String normalizeLineEndingsLikeSax(String in) {
        Matcher m = XmlFoundation.P_TO_LF.matcher(in);
        return m.replaceAll("" + LF_10_N);
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

    private static void parseAndWrite(InputSource saxInputSource, XmlWriter xmlWriter) throws IOException, SAXException, ParserConfigurationException {
        Sax2XmlWriter handler2XmlWriter = new Sax2XmlWriter(xmlWriter);
        XMLReader reader = XmlTool.createXmlReaderOn(handler2XmlWriter);
        reader.parse(saxInputSource);
    }

    public static void parseAndWriteXml(File xmlFile, XmlWriter xmlWriter) throws Exception {
        String xmlString = FileUtils.readFileToString(xmlFile, StandardCharsets.UTF_8);
        parseAndWriteXml(xmlFile.getAbsolutePath(), xmlString, xmlWriter);
    }

    public static void parseAndWriteXml(String inputName, String xmlString, XmlWriter xmlWriter) throws Exception {
        // preprocessing HTML named entities
        String xmlStringPreprocessed = NamedEntities.htmlEntitiesToAmpEncoded(xmlString);
        StringReader sr = new StringReader(xmlStringPreprocessed);
        InputSource inputSource = new InputSource("INPUT." + inputName);
        inputSource.setCharacterStream(sr);

        parseAndWrite(inputSource, xmlWriter);
    }

    public static void parseAndWriteXml(String xmlString, XmlWriter xmlWriter) throws Exception {
        String xmlStringPreprocessed = NamedEntities.htmlEntitiesToAmpEncoded(xmlString);
        StringReader sr = new StringReader(xmlStringPreprocessed);
        InputSource inputSource = new InputSource(sr);

        parseAndWrite(inputSource, xmlWriter);
    }

    /**
     * Replace all '{@code &123;}' by the unicode code point 123.
     *
     * @param s input
     */
    private static String resolveNumericalEntities(String s) {
        Pattern p = Pattern.compile("&#(\\d+);");
        Matcher m = p.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            int charCode = Integer.parseInt(m.group(1));
            m.appendReplacement(sb, Character.toString((char) charCode));
        }
        m.appendTail(sb);
        return sb.toString();
    }


}
