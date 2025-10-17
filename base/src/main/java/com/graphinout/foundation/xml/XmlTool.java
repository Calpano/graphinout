package com.graphinout.foundation.xml;

import io.github.classgraph.Resource;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
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
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.graphinout.foundation.util.Texts.CR_13_R;
import static com.graphinout.foundation.util.Texts.LF_10_N;
import static org.slf4j.LoggerFactory.getLogger;

public class XmlTool {

    @SuppressWarnings("HttpUrlsUsage") public static final String PROPERTIES_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler";
    static final Pattern P_TO_LF = Pattern.compile(
            // can be a prefix
            "(&#13;)?" + //
                    // order matters
                    "(" + CR_13_R + LF_10_N + "|" + CR_13_R + "|" + LF_10_N + ")" +

                    "|" + //

                    // can be standalone
                    "(&#13;)");
    private static final Logger log = getLogger(XmlTool.class);

    @Deprecated
    public static String ampEncode(String raw) {
        return raw.replace("&", "&amp;");
    }

    /**
     * check if that is valid XML to begin with. CAUTION: File-based reading can fail in a CI/CD pipeline. Better use
     * {@link #assertCanParseAsXml(Resource)}
     */
    public static void assertCanParseAsXml(Path xmlFilePath) throws Exception {
        XmlTool.parseAndWriteXml(xmlFilePath.toFile(), Xml2AppendableWriter.createNoop());
    }

    /** check if that is valid XML to begin with */
    public static void assertCanParseAsXml(Resource xmlResource) throws Exception {
        XmlTool.parseAndWriteXml(xmlResource, Xml2AppendableWriter.createNoop());
    }

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

    public static void ifAttributeNotNull(Map<String, String> attributes, String attributeName, Consumer<String> consumer) {
        if (attributes != null) {
            String value = attributes.get(attributeName);
            if (value != null) {
                consumer.accept(value);
            }
        }
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
        String decoded = xmlDecode(preprocessed);
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
        Matcher m = P_TO_LF.matcher(in);
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
        Sax2XmlWriter handler2XmlWriter = new Sax2XmlWriter(xmlWriter, xmlError -> log.warn("Error " + xmlError));
        XMLReader reader = XmlTool.createXmlReaderOn(handler2XmlWriter);
        reader.parse(saxInputSource);
    }

    public static void parseAndWriteXml(File xmlFile, XmlWriter xmlWriter) throws Exception {
        String xmlString = FileUtils.readFileToString(xmlFile, StandardCharsets.UTF_8);
        parseAndWriteXml(xmlFile.getAbsolutePath(), xmlString, xmlWriter);
    }

    public static void parseAndWriteXml(Resource xmlResource, XmlWriter xmlWriter) throws Exception {
        String xmlString = xmlResource.getContentAsString();
        parseAndWriteXml(xmlResource.getURI().toString(), xmlString, xmlWriter);
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

    /**
     * SAX Parser is running these decodes.
     *
     * @param characterData
     * @return
     */
    public static String xmlDecode(String characterData) {
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
    public static String xmlEncode(String characterData) {
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
