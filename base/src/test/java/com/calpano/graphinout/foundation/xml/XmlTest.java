package com.calpano.graphinout.foundation.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.slf4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.slf4j.LoggerFactory.getLogger;

public class XmlTest {

    interface MockHandler extends ContentHandler, LexicalHandler {}

    private static final Logger log = getLogger(XmlTest.class);
    private Path testResourcesPath;

    public static Stream<Arguments> graphmlFileProvider() throws Exception {
        Path testResourcesPath = Paths.get("../base/src/test/resources/xml/graphml");
        int baseLen = testResourcesPath.toString().length() + 1;
        return Files.walk(testResourcesPath).filter(Files::isRegularFile).filter(p -> {
            String pathName = p.toString().toLowerCase();
            return pathName.endsWith(".graphml") || pathName.endsWith(".graphml.xml");
        }).map(p -> Arguments.of(p.toString().substring(baseLen).replace('\\', '/'), p));
    }

    public static Stream<Arguments> xmlFileProvider() throws Exception {
        Path testResourcesPath = Paths.get("src/test/resources/xml");
        int baseLen = testResourcesPath.toString().length() + 1;
        return Files.walk(testResourcesPath).filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".xml") || p.toString().endsWith(".graphml")).map(p -> Arguments.of(p.toString().substring(baseLen).replace('\\', '/'), p));
    }

    @BeforeEach
    void setUp() {
        testResourcesPath = Paths.get("src/test/resources/xml");
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("graphmlFileProvider")
    @DisplayName("Test all GraphML files as XML files")
    void testAllGraphmlFiles(String displayPath, Path xmlFilePath) throws Exception {
        testXmlFile(xmlFilePath);
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("xmlFileProvider")
    @DisplayName("Test all XML files together")
    void testAllXmlFiles(String displayPath, Path xmlFilePath) throws Exception {
        testXmlFile(xmlFilePath);
    }

    @Test
    @DisplayName("Test cdata in XML file")
    void testCDATA() throws Exception {
        testXmlFile(testResourcesPath.resolve("cdata.xml"));
    }

    @Test
    @DisplayName("Test minimal XML file processing")
    void testMinimalXml() throws Exception {
        testXmlFile(testResourcesPath.resolve("minimal.xml"));
    }

    @Test
    @DisplayName("Test Namespaces")
    void testNamespaces() throws Exception {
        testXmlFile(testResourcesPath.resolve("namespace/XMLNamespaceHandlingTest1.xml"));
    }

    @Test
    void testTrailingWhitespace() throws ParserConfigurationException, SAXException, IOException {
        MockHandler handler = mock(MockHandler.class);
        XMLReader reader = XmlTool.createXmlReaderOn(handler);
        StringReader sr = new StringReader("<hello/>\n");
        InputSource is = new InputSource(sr);
        reader.parse(is);

        // verify SAX events
        InOrder inOrder = inOrder(handler);
        inOrder.verify(handler).startDocument();
        inOrder.verify(handler).startElement(eq(""), eq("hello"), eq("hello"), any());
        inOrder.verify(handler).endElement(eq(""), eq("hello"), eq("hello"));
        // ensure we get a '\n'
        //inOrder.verify(handler).ignorableWhitespace(eq(new char[] {'\n'}), eq(0), eq(1));
        inOrder.verify(handler).endDocument();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Test typical XML file processing")
    void testTypicalXml() throws Exception {
        testXmlFile(testResourcesPath.resolve("typical.xml"));
    }

    private void parseXmlString(String xml) throws Exception {
        SAXParser saxParser = XmlFactory.createSaxParser();
        saxParser.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), new DefaultHandler());
    }

    private void testXmlFile(Path filePath) throws Exception {
        assertTrue(Files.exists(filePath), "XML test file should exist: " + filePath);
        String inString = Files.readString(filePath);
        assertNotNull(inString);
        assertFalse(inString.trim().isEmpty());

        // Parse and write XML
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        XmlTool.parseAndWriteXml(filePath.toFile(), xmlWriter);
        String outString = xmlWriter.string();

        // Validate processed XML is not empty and is well-formed
        assertNotNull(outString);
        assertFalse(outString.trim().isEmpty());
        assertDoesNotThrow(() -> parseXmlString(outString), "Processed XML should be valid for " + filePath);
        String inNorm = XmlFormatter.normalize(inString);
        String outNorm = XmlFormatter.normalize(outString);
        String inSimple = XmlFormatter.simplifyForDebug(inNorm);
        String outSimple = XmlFormatter.simplifyForDebug(outNorm);
        assertThat(outNorm).isEqualTo(inNorm);
    }

}
