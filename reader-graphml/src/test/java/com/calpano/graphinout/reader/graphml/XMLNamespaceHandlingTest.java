package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlDocument;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.slf4j.LoggerFactory.getLogger;

class XMLNamespaceHandlingTest {

    private static final Logger log = getLogger(XMLNamespaceHandlingTest.class);

    @Test
    void XMLNamespaceHandlingTest1() throws IOException {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "namespace", "XMLNamespaceHandlingTest1.xml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);

        String expected = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + //
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
                "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + GraphmlDocument.DEFAULT_GRAPHML_XSD_URL + "\"" + //
                " xmlns:foo=\"http://foo.com\">\n" + //
                "\n" + //
                "</graphml>\n"; //
        String actual = outputSink.getBufferAsUtf8String();
        assertEquals(expected.length(), actual.length());
        assertEquals(expected, actual);
    }

    @Test
    void XMLNamespaceHandlingTest2() throws IOException {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "namespace", "XMLNamespaceHandlingTest2.xml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
        OutputSink outputSink = OutputSink.createInMemory();
        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);

        String expected = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" " + //
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //
                "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns " + GraphmlDocument.DEFAULT_GRAPHML_XSD_URL + "\"" + //
                " xmlns:foo=\"http://foo.com\">\n" + //
                "\n" + //
                "</graphml>\n"; //
        assertEquals(expected, outputSink.toString());
    }

    @Test
    void XMLNamespaceHandlingTest3() throws IOException {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml", "namespace", "XMLNamespaceHandlingTest3.xml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
        OutputSink outputSink = OutputSink.createInMemory();
        GraphmlReader graphmlReader = new GraphmlReader();
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));

        RuntimeException runtimeException = assertThrowsExactly(RuntimeException.class, () -> graphmlReader.read(singleInputSource, gioWriter));
        //TODO where the error details about thw "myroot" element?
        assertEquals("Failed reading '" + inputSource.getParent().toAbsolutePath()
                + File.separatorChar + "XMLNamespaceHandlingTest3.xml'", runtimeException.getMessage());
    }
}
