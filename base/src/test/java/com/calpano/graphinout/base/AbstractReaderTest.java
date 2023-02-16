package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.ValidatingGraphMlWriter;
import com.calpano.graphinout.base.output.xml.XmlWriter;
import com.calpano.graphinout.base.output.xml.file.InMemoryOutputSink;
import com.calpano.graphinout.base.output.xml.file.SimpleXmlWriter;
import com.calpano.graphinout.base.reader.GioReader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public abstract class AbstractReaderTest {

    public static void readTo(SingleInputSource inputSource, GioReader gioReader, OutputSink outputSink, //
                              boolean validateXml, boolean validateGraphml, boolean validateGio) throws IOException {
        XmlWriter xmlWriter = new SimpleXmlWriter(outputSink);
        if (validateXml) {
            // TODO        xmlWriter = new ValidatingXmlWriter( xmlWriter );
        }
        GraphmlWriter graphmlWriter = new GraphmlWriterImpl(xmlWriter);
        if (validateGraphml) {
            graphmlWriter = new ValidatingGraphMlWriter(graphmlWriter);
        }
        GioWriter gioWriter = new GioWriterImpl(graphmlWriter);
        if (validateGio) {
            // TODO        gioWriter = new ValidatingGioWriter( gioWriter );
        }
        gioReader.read(inputSource, gioWriter);
    }

    protected abstract boolean canRead(String resourcePath);

    protected abstract GioReader createReader();

    @ParameterizedTest
    @MethodSource("getResourceFilePaths")
    void testReadToGraph(String resourcePath) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(resourcePath);
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);

        byte[] graphmlBytes;
        {
            SingleInputSource inputSource = SingleInputSource.of(resourcePath, content);
            GioReader gioReader = createReader();
            InMemoryOutputSink outputSink = new InMemoryOutputSink();
            readTo(inputSource, gioReader, outputSink, true, true, true);
            graphmlBytes = outputSink.getByteBuffer().toByteArray();
        }
        byte[] graphmlBytes2;
        {
            SingleInputSource inputSource = SingleInputSource.of(graphmlBytes);
            // TODO read with graphml reader into memory again
            // GioReader gioReader = null;
            // InMemoryOutputSink outputSink = new InMemoryOutputSink();
            // readTo(inputSource, gioReader, outputSink, true, true, true);
            // graphmlBytes = outputSink.getByteBuffer().toByteArray();
        }
        // TODO compare byte arrays (as strings)
    }

    private Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath).filter(this::canRead);
    }

}
