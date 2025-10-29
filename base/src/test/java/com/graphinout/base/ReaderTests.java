package com.graphinout.base;

import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.DelegatingCjStream;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.cj.stream.ValidatingCjStream;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.input.ContentError;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.writer.impl.ValidatingJsonWriter;
import com.graphinout.foundation.json.writer.JsonWriter;
import com.graphinout.foundation.json.writer.impl.DelegatingJsonWriter;
import com.graphinout.foundation.json.writer.impl.Json2StringWriter;
import com.graphinout.foundation.output.InMemoryOutputSink;
import com.graphinout.foundation.output.OutputSink;
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

    /** validate outgoing CJ and JSON */
    public static ICjStream createCjStream(OutputSink outputSink, boolean validateJson, boolean validateCj) {

        JsonWriter jsonWriter = new Json2StringWriter(json -> {
            try {
                outputSink.write(json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        if (validateJson) {
            DelegatingJsonWriter delegatingJsonWriter = new DelegatingJsonWriter(new ValidatingJsonWriter());
            delegatingJsonWriter.addJsonWriter(jsonWriter);
            jsonWriter = delegatingJsonWriter;
        }

        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(jsonWriter);
        ICjStream cjStream = new CjStream2CjWriter(cj2JsonWriter);
        if (validateCj) {
            cjStream = new DelegatingCjStream(new ValidatingCjStream(), cjStream);
        }
        return cjStream;
    }

    public static void forEachReadableResource(GioReader gioReader, Consumer<Resource> resourceConsumer) {
        TestFileProvider.getAllTestResources() //
                .filter(tr -> ReaderTests.hasReadableFileExtension(gioReader, tr.resource().getPath())) //
                .forEach(tr -> resourceConsumer.accept(tr.resource()));
    }

    public static boolean hasReadableFileExtension(GioReader gioReader, String resourcePath) {
        return gioReader.fileFormat().fileExtensions().stream().anyMatch(resourcePath::endsWith);
    }

    public static List<ContentError> readResourceToSink(GioReader gioReader, Resource resource, OutputSink outputSink) throws IOException {
        URL resourceUrl = ClassLoader.getSystemResource(resource.getPath());
        log.info("Reading " + resourceUrl + " as " + gioReader.fileFormat());
        String content = IOUtils.toString(resourceUrl, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(resource.getPath(), content);
        return readTo(inputSource, gioReader, outputSink);
    }


    /**
     * @param inputSource
     * @param gioReader   no need to replace with CJ
     * @param outputSink
     * @return all content errors reported
     * @throws IOException
     */
    public static List<ContentError> readTo(SingleInputSource inputSource, GioReader gioReader, OutputSink outputSink) throws IOException {
        List<ContentError> contentErrors = new ArrayList<>();
        boolean validateJson = true;
        boolean validateCj = true;
        ICjStream cjStream = ReaderTests.createCjStream(outputSink, validateJson, validateCj);
        gioReader.setContentErrorHandler(contentErrors::add);
        gioReader.read(inputSource, cjStream);
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
