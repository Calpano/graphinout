package com.graphinout.reader.graphml;

import com.graphinout.testdata.TestFileUtil;
import com.graphinout.foundation.pure.xml.writer.Xml2StringWriter;
import com.graphinout.base.xml.util.XmlTool;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraphmlReaderXMLContentTest {


    /**
     * String -> XML -> GraphML -> XML -> String
     */
    private static String parseGraphmlToString(String resourceName) throws IOException {
        // out
        Xml2StringWriter xml2stringWriter = new Xml2StringWriter();
        IGraphmlWriter graphml2xmlWriter = new Graphml2XmlWriter(xml2stringWriter);
        // in
        String content = TestFileUtil.resource(resourceName).getContentAsString();
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2xmlWriter);
        try {
            XmlTool.parseAndWriteXml(content, xml2GraphmlWriter);
            return xml2stringWriter.resultString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * HTML embedded in a {@code <data>} element, wrapped in {@code CDATA}.
     *
     * <p>GraphML allows arbitrary XML inside {@code <data>}/{@code <default>}, and authors put HTML there —
     * but HTML with void tags ({@code <meta>}, {@code <link>}, {@code <img>}) is not well-formed XML, so it
     * can only survive as character data. Issue #84 ("HTML in GraphML") is closed as WONTFIX for exactly
     * that reason: emitting such content back as markup would produce a file no XML parser could read.
     *
     * <p>What this pins is therefore the CDATA round-trip: the payload comes back byte-for-byte, still
     * wrapped, with the key declaration, node id and {@code edgedefault} intact. That already worked — the
     * test sat {@code @Disabled} behind #84 since ~2023 while asserting an output that no conformant XML
     * writer could ever emit (raw unbalanced markup), and which additionally dropped the {@code <key>}
     * declaration, {@code id="a"}, {@code edgedefault} and the XML prolog, and pinned graphml.xsd 1.0
     * where we emit 1.1. The expectation was wrong, not the behaviour.
     */
    @Test
    void html_Content_Tag_test() throws IOException {
        String resourceName = "xml/plain-xml/HTML_Content_In_Data.xml";
        String result = parseGraphmlToString(resourceName);
        String expected = TestFileUtil.resource(resourceName).getContentAsString();
        GraphmlAssert.xAssertThatIsSameGraphml(result, expected, null);
    }

    @Test
    void xml_content_in_data() throws IOException {
        String resourceName= "xml/plain-xml/XML_Standard_Content_In_Data.xml";
        String result = parseGraphmlToString(resourceName);
        String expected = TestFileUtil.resource(resourceName).getContentAsString();
        GraphmlAssert.xAssertThatIsSameGraphml(result, expected, null);
    }

    @Test
    void xml_content_in_default() throws IOException {
        String resourceName= "xml/plain-xml/XML_Standard_Content_In_default.xml";
        String expected = TestFileUtil.resource(resourceName).getContentAsString();
        String result = parseGraphmlToString(resourceName);
        GraphmlAssert.xAssertThatIsSameGraphml(result, expected, null);
    }

    @Test
    void xml_content_in_desc() throws IOException {
        String resourceName= "xml/plain-xml/XML_Standard_Content_In_Desc.xml";
        String result = parseGraphmlToString(resourceName);
        String expected = TestFileUtil.resource(resourceName).getContentAsString();
        GraphmlAssert.xAssertThatIsSameGraphml(result, expected, null);
    }

}
