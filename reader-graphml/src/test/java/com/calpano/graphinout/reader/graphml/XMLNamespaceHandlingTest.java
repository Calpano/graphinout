package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.calpano.graphinout.base.graphml.xml.Graphml2XmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.xml.Xml2StringWriter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XMLNamespaceHandlingTest {

    private static String parse(Path pathIn) throws IOException {
        String contentIn = IOUtils.toString(pathIn.toUri(), StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(pathIn.toAbsolutePath().toString(), contentIn);
        GraphmlReader graphmlReader = new GraphmlReader();
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(xmlWriter));
        graphmlReader.read(singleInputSource, gioWriter);
        return xmlWriter.string();
    }

    @Test
    void XMLNamespaceHandlingTest1() throws IOException {
        Path pathIn = Path.of("../base/src/test/resources/xml/namespace/XMLNamespaceHandlingTest1.xml");
        String expected = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd">
                </graphml>""";
        String actual = parse(pathIn);
        assertEquals(expected, actual);
    }

    @Test
    void XMLNamespaceHandlingTest2() throws IOException {
        Path pathIn = Path.of("../base/src/test/resources/xml/namespace/XMLNamespaceHandlingTest2.xml");
        String expected = """
                <?xml version="1.0" encoding="UTF-8"?>
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns" xmlns:foo="http://foo.com" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.1/graphml.xsd">
                </graphml>"""; //
        String actual = parse(pathIn);
        assertEquals(expected, actual);
    }

    @Test
    void XMLNamespaceHandlingTest3() throws IOException {
        Path pathIn = Path.of("../base/src/test/resources/xml/namespace/XMLNamespaceHandlingTest3.xml");

        URI resourceUri = pathIn.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(pathIn.toAbsolutePath().toString(), content);
        Xml2StringWriter xmlWriter = new Xml2StringWriter();
        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new Gio2GraphmlWriter(new Graphml2XmlWriter(xmlWriter));
        List<ContentError> errorList = new ArrayList<>();
        graphmlReader.errorHandler(errorList::add);
        graphmlReader.read(singleInputSource, gioWriter);
        assertEquals(2, errorList.size(), "Expect contentError for <myRoot> element");
        assertEquals(ContentError.ErrorLevel.Warn, errorList.getFirst().getLevel());
        assertEquals("The Element <myroot> not acceptable tag for Graphml.", errorList.getFirst().getMessage());
    }

}
