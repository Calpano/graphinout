package com.calpano.graphinout.base;

import com.calpano.graphinout.base.gio.GioReader;
import com.calpano.graphinout.base.gio.GioWriter;
import com.calpano.graphinout.base.graphml.DelegatingGraphmlWriter;
import com.calpano.graphinout.base.graphml.GioWriterImpl;
import com.calpano.graphinout.base.graphml.GraphmlWriter;
import com.calpano.graphinout.base.graphml.GraphmlWriterImpl;
import com.calpano.graphinout.base.reader.ContentError;
import com.calpano.graphinout.base.validation.ValidatingGraphMlWriter;
import com.calpano.graphinout.base.writer.DelegatingGioWriter;
import com.calpano.graphinout.base.writer.ValidatingGioWriter;
import com.calpano.graphinout.foundation.input.SingleInputSource;
import com.calpano.graphinout.foundation.output.InMemoryOutputSink;
import com.calpano.graphinout.foundation.output.OutputSink;
import com.calpano.graphinout.foundation.xml.ValidatingXmlWriter;
import com.calpano.graphinout.foundation.xml.XmlWriter;
import com.calpano.graphinout.foundation.xml.XmlWriterImpl;
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
import java.util.function.Function;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

public class ReaderTests {

    private static final Logger log = getLogger(ReaderTests.class);

    public static boolean canRead(GioReader gioReader, String resourcePath) {
        return gioReader.fileFormat().fileExtensions().stream().anyMatch(resourcePath::endsWith);
    }

    public static GioWriter createWriter(OutputSink outputSink, boolean validateXml, boolean validateGraphml, boolean validateGio) {
        XmlWriter xmlWriter = new XmlWriterImpl(outputSink);
        if (validateXml) {
            xmlWriter = new ValidatingXmlWriter(xmlWriter);
        }

        GraphmlWriter graphmlWriter = new GraphmlWriterImpl(xmlWriter);
        if (validateGraphml) {
            graphmlWriter = new DelegatingGraphmlWriter(new ValidatingGraphMlWriter(), graphmlWriter);
        }

        GioWriter gioWriter = new GioWriterImpl(graphmlWriter);
        if (validateGio) {
            gioWriter = new DelegatingGioWriter(new ValidatingGioWriter(), gioWriter);
        }
        return gioWriter;
    }

    public static void forEachReadableResource(GioReader gioReader, Consumer<String> resourcePathConsumer) {
        getAllTestResourceFilePaths().filter(resourcePath -> ReaderTests.canRead(gioReader, resourcePath)).forEach(resourcePathConsumer);
    }

    public static Stream<String> getAllTestResourceFilePaths() {
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
        GioWriter gioWriter = ReaderTests.createWriter(outputSink, validateXml, validateGraphml, validateGio);
        gioReader.errorHandler(contentErrors::add);
        gioReader.read(inputSource, gioWriter);
        return contentErrors;
    }

    /**
     * @param resourcePath
     * @throws IOException
     */
    public static void testReadResourceToGraph(GioReader gioReader, String resourcePath, List<ContentError> expectedErrors) throws IOException {
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        List<ContentError> contentErrors = readResourceToSink(gioReader, resourcePath, outputSink, true, true, true);

        Assertions.assertEquals(expectedErrors.toString(), contentErrors.toString(), "expected=" + expectedErrors + " actual=" + contentErrors);
        Assertions.assertEquals(expectedErrors, contentErrors, "expected=" + expectedErrors + " actual=" + contentErrors);
    }

    public static void testWithAllResource(GioReader gioReader, Function<String, List<ContentError>> expectedErrorsFun) {
        forEachReadableResource(gioReader, resourcePath -> {
            try {
                testReadResourceToGraph(gioReader, resourcePath, expectedErrorsFun.apply(resourcePath));
            } catch (Throwable e) {
                log.warn("Exception during parsing", e);
            }
        });
    }


}
