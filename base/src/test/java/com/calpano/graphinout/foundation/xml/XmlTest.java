package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

public class XmlTest {

    private static final Logger log = getLogger(XmlTest.class);
    private Path testResourcesPath;

    static Stream<Arguments> xmlFileProvider() throws Exception {
        Path testResourcesPath = Paths.get("src/test/resources/xml");
        int baseLen = testResourcesPath.toString().length() + 1;
        return Files.walk(testResourcesPath)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".xml") || p.toString().endsWith(".graphml"))
                .map(p -> Arguments.of(p.toString().substring(baseLen).replace('\\', '/'), p));
    }

    @BeforeEach
    void setUp() {
        testResourcesPath = Paths.get("src/test/resources/xml");
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
    @DisplayName("Test typical XML file processing")
    void testTypicalXml() throws Exception {
        testXmlFile(testResourcesPath.resolve("typical.xml"));
    }

    private void parseAndWriteXml(File xmlFile, XmlWriter xmlWriter) throws Exception {
        SAXParser saxParser = XmlFactory.createSaxParser();
        Sax2XmlWriter handler2XmlWriter = new Sax2XmlWriter(xmlWriter, null);
        XMLReader reader = saxParser.getXMLReader();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler2XmlWriter);
        reader.setContentHandler(handler2XmlWriter);
        reader.parse(xmlFile.getAbsolutePath());
    }

    private void parseXmlString(String xml) throws Exception {
        SAXParser saxParser = XmlFactory.createSaxParser();
        saxParser.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), new DefaultHandler());
    }

    private void testXmlFile(Path filePath) throws Exception {
        assertTrue(Files.exists(filePath), "XML test file should exist: " + filePath);
        String originalContent = Files.readString(filePath);
        assertNotNull(originalContent);
        assertFalse(originalContent.trim().isEmpty());

        // Parse and write XML
        try (InMemoryOutputSink sink = new InMemoryOutputSink()) {
            XmlWriterImpl xmlWriter = new XmlWriterImpl(sink);
            parseAndWriteXml(filePath.toFile(), xmlWriter);

            String processedContent = sink.getBufferAsUtf8String();

            // Validate processed XML is not empty and is well-formed
            assertNotNull(processedContent);
            assertFalse(processedContent.trim().isEmpty());
            assertDoesNotThrow(() -> parseXmlString(processedContent), "Processed XML should be valid for " + filePath);
        }
    }

}
