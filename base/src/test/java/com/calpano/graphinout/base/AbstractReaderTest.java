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
import com.calpano.graphinout.base.output.xml.file.ValidatingXmlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractReaderTest {

    private static final Logger log = getLogger(AbstractReaderTest.class);

    /**
     * @param inputSource
     * @param gioReader
     * @param outputSink
     * @param validateXml
     * @param validateGraphml
     * @param validateGio
     * @return all content errors reported
     * @throws IOException
     */
    public static List<ContentError> readTo(SingleInputSource inputSource, GioReader gioReader, OutputSink outputSink, //
                                            boolean validateXml, boolean validateGraphml, boolean validateGio) throws IOException {
        List<ContentError> contentErrors = new ArrayList<>();
        XmlWriter xmlWriter = new SimpleXmlWriter(outputSink);
        if (validateXml) {
            xmlWriter = new ValidatingXmlWriter( xmlWriter );
        }
        GraphmlWriter graphmlWriter = new GraphmlWriterImpl(xmlWriter);
        if (validateGraphml) {
            graphmlWriter = new ValidatingGraphMlWriter(graphmlWriter);
        }
        GioWriter gioWriter = new GioWriterImpl(graphmlWriter);
        if (validateGio) {
            // TODO        gioWriter = new ValidatingGioWriter( gioWriter );
        }
        gioReader.errorHandler(contentErrors::add);
        gioReader.read(inputSource, gioWriter);
        return contentErrors;
    }

    protected abstract boolean canRead(String resourcePath);

    protected abstract GioReader createReader();

    /**
     * We read some inputFormat X into GraphML, write GraphML (1), read that GraphML, write to GraphML again (2); compare (1) and (2)
     *
     * @param resourcePath
     * @throws IOException
     */
    void testReadResourceToGraph(String resourcePath) throws IOException {
        GioReader gioReader = createReader();
        URL resourceUrl = ClassLoader.getSystemResource(resourcePath);
        log.info("Reading " + resourceUrl + " as " + gioReader.fileFormat());
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);

        byte[] graphmlBytes;
        {
            SingleInputSource inputSource = SingleInputSource.of(resourcePath, content);
            InMemoryOutputSink outputSink = new InMemoryOutputSink();
            List<ContentError> contentErrors = readTo(inputSource, gioReader, outputSink, true, true, true);
            // TODO find a way to list expected contentErrors? static Map<resourcePath,List<ContentError>>
            Assertions.assertTrue(contentErrors.isEmpty());
            graphmlBytes = outputSink.getByteBuffer().toByteArray();
        }
        byte[] graphmlBytes2;
        {
            SingleInputSource inputSource = SingleInputSource.of(graphmlBytes);
            // TODO read with graphml reader into memory again
            // GioReader gioReader = null;
            // InMemoryOutputSink outputSink = new InMemoryOutputSink();
            // List<ContentError> contentErrors = readTo(inputSource, gioReader, outputSink, true, true, true);
            // graphmlBytes = outputSink.getByteBuffer().toByteArray();
        }
        // TODO compare byte arrays (as strings)
    }

    @Test
    void testResources() {
        getResourceFilePaths().filter(this::canRead).forEach(resourcePath -> {
            try {
                testReadResourceToGraph(resourcePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Stream<String> getResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath).filter(this::canRead);
    }

}
