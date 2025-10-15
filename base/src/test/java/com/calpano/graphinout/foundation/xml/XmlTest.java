package com.calpano.graphinout.foundation.xml;

import com.calpano.graphinout.foundation.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.calpano.graphinout.foundation.TestFileProvider#xmlResources")
    @DisplayName("Test all XML files together")
    void testAllXmlFiles(String displayPath, Resource xmlResource) throws Exception {
        testXmlFile(xmlResource);
    }

    @Test
    @DisplayName("Test cdata in XML file")
    void testCDATA() throws Exception {
        testXmlFile(TestFileUtil.resource("xml/cdata.xml"));
    }

    @Test
    @DisplayName("Test minimal XML file processing")
    void testMinimalXml() throws Exception {
        testXmlFile(TestFileUtil.resource("xml/minimal.xml"));
    }

    @Test
    @DisplayName("Test Namespaces")
    void testNamespaces() throws Exception {
        testXmlFile(TestFileUtil.resource("xml/graphml/synthetic/namespace/namespace1.graphml.xml"));
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
        testXmlFile(TestFileUtil.resource("xml/typical.xml"));
    }

    private void parseXmlString(String xml) throws Exception {
        SAXParser saxParser = XmlFactory.createSaxParser();
        saxParser.parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), new DefaultHandler());
    }

    /** Read XML, write via XmlWriter to string */
    private void testXmlFile(Resource xmlResource) throws Exception {
        assertThat(xmlResource).isNotNull();

        String xmlIn = xmlResource.getContentAsString();
        assertNotNull(xmlIn);
        assertFalse(xmlIn.trim().isEmpty());

        // Parse and write XML
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        XmlTool.parseAndWriteXml(xmlResource, xmlWriter);
        String outString = xmlWriter.resultString();

        // Validate processed XML is not empty and is well-formed
        assertNotNull(outString);
        assertFalse(outString.trim().isEmpty());
        assertDoesNotThrow(() -> parseXmlString(outString), "Processed XML should be valid for " + xmlResource.getURI());
        String inNorm = XmlNormalizer.normalize(xmlIn);
        String outNorm = XmlNormalizer.normalize(outString);
        String inSimple = XmlFormatter.simplifyForDebug(inNorm);
        String outSimple = XmlFormatter.simplifyForDebug(outNorm);
        assertThat(outNorm).isEqualTo(inNorm);
    }

}
