package com.calpano.graphinout.reader.graphml;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlDocument;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphmlReaderXMLContent {

    @Test
    void xML_Standard_Tag_in_data_test() throws IOException {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml","xml", "XML_Standard_Content_In_Data.xml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        GraphmlReader graphmlReader = new GraphmlReader();
        List<ContentError> contentErrors = new ArrayList<>();
        graphmlReader.errorHandler(contentErrors::add);
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);

        String expected = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" +//
                "\n" +//
                "<graph edgedefault=\"undirected\">\n" +//
                "<node><data> <b>Hello</b>World </data></node>\n" +//
                "</graph>\n" +//
                "</graphml>\n"; //
        String actual = outputSink.getBufferAsUtf8String();

        assertEquals(expected, actual);
    }

    @Test
    void xML_Standard_Tag_in_default_test() throws IOException {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml","xml", "XML_Standard_Content_In_default.xml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        GraphmlReader graphmlReader = new GraphmlReader();
        List<ContentError> contentErrors = new ArrayList<>();
        graphmlReader.errorHandler(contentErrors::add);
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);

        String expected = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n" +//
                "<key id=\"neuron_name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"><default><b>Hello</b>World</default></key>\n" + //
                "<graph edgedefault=\"undirected\">\n" + //
                "</graph>\n" + //
                "</graphml>\n"; //
        String actual = outputSink.getBufferAsUtf8String();

        assertEquals(expected, actual);
    }
    //TODO SAX parser not HTML parser
    //It does not understand HTML tags,
    // and content containing HTML structures that do not comply with
    // the XML standard will generate an error during initial rendering.
    //
    @Test
    void html_Content_Tag_test() throws IOException {
        Path inputSource = Paths.get("src", "test", "resources", "graphin", "graphml","xml", "HTML_Content_In_Data.xml");
        URI resourceUri = inputSource.toUri();
        String content = IOUtils.toString(resourceUri, StandardCharsets.UTF_8);
        SingleInputSource singleInputSource = SingleInputSource.of(inputSource.toAbsolutePath().toString(), content);
        InMemoryOutputSink outputSink = OutputSink.createInMemory();
        GraphmlReader graphmlReader = new GraphmlReader();
        List<ContentError> contentErrors = new ArrayList<>();
        graphmlReader.errorHandler(contentErrors::add);
        GioWriter gioWriter = new GioWriterImpl(new GraphmlWriterImpl(new XmlWriterImpl(outputSink)));
        graphmlReader.read(singleInputSource, gioWriter);

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
        String actual = outputSink.getBufferAsUtf8String();

        assertEquals(expected, actual);
    }
}
