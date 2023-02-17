package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.gio.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.input.SingleInputSource;
import com.calpano.graphinout.base.output.InMemoryOutputSink;
import com.calpano.graphinout.base.output.OutputSink;
import com.calpano.graphinout.base.output.ValidatingGraphMlWriter;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.reader.GioReader;
import com.calpano.graphinout.base.xml.XmlWriterImpl;
import com.calpano.graphinout.base.xml.ValidatingXmlWriter;
import com.calpano.graphinout.base.xml.XmlWriter;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class ReaderTests {

    private static final Logger log = getLogger(ReaderTests.class);

    private static boolean canRead(GioReader gioReader, String resourcePath) {
        return gioReader.fileFormat().fileExtensions().stream().anyMatch(resourcePath::endsWith);
    }

    public static void forEachReadableResource(GioReader gioReader, Consumer<String> resourcePathConsumer) {
        getAllTestResourceFilePaths().filter(resourcePath -> canRead(gioReader, resourcePath)).forEach(resourcePathConsumer);
    }

    private static Stream<String> getAllTestResourceFilePaths() {
        return new ClassGraph().scan().getAllResources().stream().map(Resource::getPath);
    }

    public static List<ContentError> readResourceToSink(GioReader gioReader, String resourcePath, OutputSink outputSink, boolean validateXml, boolean validateGraphml, boolean validateGio) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(resourcePath);
        log.info("Reading " + resourceUrl + " as " + gioReader.fileFormat());
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(resourcePath, content);
        List<ContentError> contentErrors = readTo(inputSource, gioReader, outputSink, validateXml, validateGraphml, validateGio);
        return contentErrors;
    }

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
            xmlWriter = new ValidatingXmlWriter(xmlWriter);
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

    /**
     * We read some inputFormat X into GraphML, write GraphML (1), read that GraphML, write to GraphML again (2); compare (1) and (2)
     *
     * @param resourcePath
     * @throws IOException
     */
    public static void testReadResourceToGraph(GioReader gioReader, String resourcePath) throws IOException {

        byte[] graphmlBytes1;
        {
            InMemoryOutputSink outputSink = new InMemoryOutputSink();
            List<ContentError> contentErrors = readResourceToSink(gioReader, resourcePath, outputSink, true, true, true);

            // TODO find a way to list expected contentErrors? static Map<resourcePath,List<ContentError>>
            Assertions.assertTrue(contentErrors.isEmpty());
            graphmlBytes1 = outputSink.getByteBuffer().toByteArray();
        }
        byte[] graphmlBytes2 = null;
        {
            SingleInputSource inputSource = SingleInputSource.of(graphmlBytes1);
            // TODO read with graphml reader into memory again
            // GioReader gioReader = null;
            // InMemoryOutputSink outputSink = new InMemoryOutputSink();
            // List<ContentError> contentErrors = readTo(inputSource, gioReader, outputSink, true, true, true);
            // graphmlBytes = outputSink.getByteBuffer().toByteArray();
        }
        String graphml1 = new String(graphmlBytes1, StandardCharsets.UTF_8);
        // TODO enable when ready
        //String graphml2 = new String(graphmlBytes2, StandardCharsets.UTF_8);
        // Assertions.assertEquals(graphml1, graphml2);
    }

    public static void testWithAllResource(GioReader gioReader) {
        forEachReadableResource(gioReader, resourcePath -> {
            try {
                testReadResourceToGraph(gioReader, resourcePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
