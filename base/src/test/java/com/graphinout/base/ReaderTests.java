package com.graphinout.base;

import com.graphinout.base.gio.GioReader;
import com.graphinout.base.gio.GioWriter;
import com.graphinout.base.graphml.DelegatingGraphmlWriter;
import com.graphinout.base.graphml.Graphml2XmlWriter;
import com.graphinout.base.graphml.GraphmlWriter;
import com.graphinout.base.graphml.gio.Gio2GraphmlWriter;
import com.graphinout.base.reader.ContentError;
import com.graphinout.base.validation.graphml.ValidatingGraphMlWriter;
import com.graphinout.base.writer.DelegatingGioWriter;
import com.graphinout.base.writer.ValidatingGioWriter;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.output.InMemoryOutputSink;
import com.graphinout.foundation.output.OutputSink;
import com.graphinout.foundation.xml.ValidatingXmlWriter;
import com.graphinout.foundation.xml.XmlWriter;
import com.graphinout.foundation.xml.XmlWriterImpl;
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

import static org.slf4j.LoggerFactory.getLogger;

public class ReaderTests {

    private static final Logger log = getLogger(ReaderTests.class);

    public static boolean hasReadableFileExtension(GioReader gioReader, String resourcePath) {
        return gioReader.fileFormat().fileExtensions().stream().anyMatch(resourcePath::endsWith);
    }

    public static GioWriter createWriter(OutputSink outputSink, boolean validateXml, boolean validateGraphml, boolean validateGio) throws IOException {
        XmlWriter xmlWriter = XmlWriterImpl.create(outputSink);
        if (validateXml) {
            xmlWriter = new ValidatingXmlWriter(xmlWriter);
        }

        GraphmlWriter graphmlWriter = new Graphml2XmlWriter(xmlWriter);
        if (validateGraphml) {
            graphmlWriter = new DelegatingGraphmlWriter(new ValidatingGraphMlWriter(), graphmlWriter);
        }

        GioWriter gioWriter = new Gio2GraphmlWriter(graphmlWriter);
        if (validateGio) {
            gioWriter = new DelegatingGioWriter(new ValidatingGioWriter(), gioWriter);
        }
        return gioWriter;
    }

    public static void forEachReadableResource(GioReader gioReader, Consumer<Resource> resourceConsumer) {
        TestFileProvider.getAllTestResources() //
                .filter(tr -> ReaderTests.hasReadableFileExtension(gioReader, tr.resource().getPath())) //
                .forEach(tr->resourceConsumer.accept(tr.resource()));
    }

    public static List<ContentError> readResourceToSink(GioReader gioReader, Resource resource, OutputSink outputSink) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(resource.getPath());
        log.info("Reading " + resourceUrl + " as " + gioReader.fileFormat());
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(resource.getPath(), content);
        List<ContentError> contentErrors = readTo(inputSource, gioReader, outputSink);
        return contentErrors;
    }


    /**
     * @param inputSource
     * @param gioReader
     * @param outputSink
     * @return all content errors reported
     * @throws IOException
     */
    public static List<ContentError> readTo(SingleInputSource inputSource, GioReader gioReader, OutputSink outputSink) throws IOException {
        List<ContentError> contentErrors = new ArrayList<>();
        boolean validateXml =true;
        boolean validateGraphml=true;
        boolean validateGio=true;
        GioWriter gioWriter = ReaderTests.createWriter(outputSink, validateXml, validateGraphml, validateGio);
        gioReader.errorHandler(contentErrors::add);
        gioReader.read(inputSource, gioWriter);
        return contentErrors;
    }

    /**
     * @param resource
     * @throws IOException
     */
    public static void testReadResourceToGraph(GioReader gioReader, Resource resource, List<ContentError> expectedErrors) throws IOException {
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        List<ContentError> contentErrors = readResourceToSink(gioReader, resource, outputSink);

        Assertions.assertEquals(expectedErrors.toString(), contentErrors.toString(), "expected=" + expectedErrors + " actual=" + contentErrors);
        Assertions.assertEquals(expectedErrors, contentErrors, "expected=" + expectedErrors + " actual=" + contentErrors);
    }

    public static void testWithAllResource(GioReader gioReader, Function<Resource, List<ContentError>> expectedErrorsFun) {
        forEachReadableResource(gioReader, resourcePath -> {
            try {
                testReadResourceToGraph(gioReader, resourcePath, expectedErrorsFun.apply(resourcePath));
            } catch (Throwable e) {
                log.warn("Exception during parsing", e);
            }
        });
    }


}
