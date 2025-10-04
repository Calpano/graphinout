package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import com.calpano.graphinout.foundation.xml.XmlAssert;
import com.calpano.graphinout.foundation.xml.XmlTool;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphmlReaderXMLContentTest {

    record Result(String actual, List<ContentError> contentErrors) {}

    /**
     * String -> XML -> GraphML -> XML -> String
     */
    private static Result parseGraphmlToOutputSink(Path inputSource) throws IOException {
        // out
        Xml2StringWriter xml2stringWriter = new Xml2StringWriter();
        GraphmlWriter graphml2xmlWriter = new Graphml2XmlWriter(xml2stringWriter);
        // in
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        Xml2GraphmlWriter xml2GraphmlWriter = new Xml2GraphmlWriter(graphml2xmlWriter);
        try {
            XmlTool.parseAndWriteXml(content, xml2GraphmlWriter);
            List<ContentError> contentErrors = new ArrayList<>();
            return new Result(xml2stringWriter.resultString(), contentErrors);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * SAX parser not HTML parser It does not understand HTML tags, and content containing HTML structures that do not
     * comply with the XML standard will generate an error during initial rendering.
     */
    @Test
    @Disabled("See issue #84")
    void html_Content_Tag_test() throws IOException {
        Path inputSource = Paths.get("../base/src/test/resources/graphin/graphml/xml/HTML_Content_In_Data.xml");
        Result result = parseGraphmlToOutputSink(inputSource);
        String expected = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" +//
                "<graph>\n" + //
                "<node> <data> <html lang=\"en\">\n" + //
                "    <head>\n" + //
                "        <meta charset=\"UTF-8\">\n" + //
                "        <link rel=\"stylesheet\" href=\"css/main.css\">\n" + //
                "\n" + //
                "        <!-- Add icon library -->\n" + //
                "        <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" + //
                "        <!-- Fonts -->\n" + //
                "        </head>\n" + //
                "    <body>\n" + //
                "        <div class=\"ptext\">\n" + //
                "            <span class=\"border\">\n" + //
                "                <img src=\"./img/quote6-a.png\" alt=\"Socrates Quote\">\n" + //
                "            </span>\n" + //
                "        </div>\n" + //
                "    </body></html></data></node>\n" + //
                "</graph></graphml>\n"; //
        assertEquals(expected, result.actual);
        assertTrue(result.contentErrors.isEmpty());
    }

    @Test
    void xml_Standard_Tag_in_data_test() throws IOException {
        Path inputSource = Paths.get("../base/src/test/resources/xml/XML_Standard_Content_In_Data.xml");
        Result result = parseGraphmlToOutputSink(inputSource);
        String expected = IOUtils.toString(inputSource.toUri(), StandardCharsets.UTF_8);
        GraphmlAssert.xAssertThatIsSameGraphml(result.actual, expected,null);
        assertTrue(result.contentErrors.isEmpty());
    }

    @Test
    void xml_Standard_Tag_in_default_test() throws IOException {
        Path inputSource = Paths.get("../base/src/test/resources/xml/XML_Standard_Content_In_default.xml");
        String expected = IOUtils.toString(inputSource.toUri(), StandardCharsets.UTF_8);
        Result result = parseGraphmlToOutputSink(inputSource);
        GraphmlAssert.xAssertThatIsSameGraphml(result.actual, expected,null);
        assertTrue(result.contentErrors.isEmpty());
    }

    @Test
    void xml_Standard_Tag_in_desc_test() throws IOException {
        Path inputSource = Paths.get("../base/src/test/resources/xml/XML_Standard_Content_In_Desc.xml");
        Result result = parseGraphmlToOutputSink(inputSource);
        String expected = IOUtils.toString(inputSource.toUri(), StandardCharsets.UTF_8);
        GraphmlAssert.xAssertThatIsSameGraphml(result.actual, expected,null);
        assertTrue(result.contentErrors.isEmpty());
    }

}
