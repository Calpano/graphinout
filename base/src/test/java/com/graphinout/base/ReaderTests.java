package com.graphinout.base;

import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.DelegatingCjStream;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.base.gio.GioReader;
import com.graphinout.base.cj.stream.ValidatingCjStream;
import com.graphinout.foundation.pure.input.ContentError;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.writer.impl.ValidatingJsonWriter;
import com.graphinout.foundation.pure.json.writer.JsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.DelegatingJsonWriter;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.base.output.OutputSink;
import com.graphinout.foundation.pure.functional.ThrowingConsumer;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        ICjStream cjStream = new CjStream2CjWriter(cj2JsonWriter, true);
        if (validateCj) {
            cjStream = new DelegatingCjStream(new ValidatingCjStream(), cjStream);
        }
        return cjStream;
    }

    public static void forEachReadableResource(GioReader gioReader, ThrowingConsumer<Resource,RuntimeException> resourceConsumer) {
        TestFileProvider.getAllTestResources() //
                .filter(tr -> ReaderTests.hasReadableFileExtension(gioReader, tr.resource().getPath())) //
                .forEach(tr -> resourceConsumer.accept(tr.resource()));
    }

    public static boolean hasReadableFileExtension(GioReader gioReader, String resourcePath) {
        return gioReader.fileFormat().fileExtensions().stream().anyMatch(resourcePath::endsWith);
    }

    public static List<ContentError> readResourceToSink(GioReader gioReader, Resource resource, OutputSink outputSink) throws IOException {
        return readResourceToSink(gioReader, resource, outputSink, true);
    }

    /** @param validateCj also check the CJ output (ids, endpoint references); off for input known to be broken */
    public static List<ContentError> readResourceToSink(GioReader gioReader, Resource resource, OutputSink outputSink, boolean validateCj) throws IOException {
        // the resource's own URL: ClassLoader.getSystemResource cannot see resources inside jars on surefire's classpath
        URL resourceUrl = resource.getURL();
        log.info("Reading " + resourceUrl + " as " + gioReader.fileFormat());
        String content = TestFileUtil.readUtf8(resourceUrl);
        SingleInputSource inputSource = SingleInputSource.of(resource.getPath(), content);
        return readTo(inputSource, gioReader, outputSink, validateCj);
    }


    /**
     * @param inputSource
     * @param gioReader   no need to replace with CJ
     * @param outputSink
     * @return all content errors reported
     * @throws IOException
     */
    public static List<ContentError> readTo(SingleInputSource inputSource, GioReader gioReader, OutputSink outputSink) throws IOException {
        return readTo(inputSource, gioReader, outputSink, true);
    }

    public static List<ContentError> readTo(SingleInputSource inputSource, GioReader gioReader, OutputSink outputSink, boolean validateCj) throws IOException {
        List<ContentError> contentErrors = new ArrayList<>();
        boolean validateJson = true;
        ICjStream cjStream = ReaderTests.createCjStream(outputSink, validateJson, validateCj);
        gioReader.setContentErrorHandler(contentErrors::add);
        gioReader.read(inputSource, cjStream);
        return contentErrors;
    }

    /**
     * @param resource
     * @throws IOException
     */
    /**
     * Reads the resource and compares the content errors of level {@link ContentError.ErrorLevel#Error} with the
     * expected ones. Warnings and infos describe how a reader leniently handled real-world input; they are logged, not
     * asserted.
     */
    public static void testReadResourceToGraph(GioReader gioReader, Resource resource, List<ContentError> expectedErrors) throws IOException {
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        // input expected to produce errors is broken by design (e.g. an edge to a missing node): the reader reports it,
        // and its CJ output cannot pass reference validation
        boolean validateCj = errorsOnly(expectedErrors).isEmpty();
        List<ContentError> contentErrors = readResourceToSink(gioReader, resource, outputSink, validateCj);
        contentErrors.stream().filter(e -> e.getLevel() != ContentError.ErrorLevel.Error)
                .forEach(e -> log.debug("{}: {}", resource.getPath(), e));
        List<ContentError> errors = errorsOnly(contentErrors);
        List<ContentError> expected = errorsOnly(expectedErrors);
        Assertions.assertEquals(expected.toString(), errors.toString(), "expected=" + expected + " actual=" + errors);
        Assertions.assertEquals(expected, errors, "expected=" + expected + " actual=" + errors);
    }

    private static List<ContentError> errorsOnly(List<ContentError> contentErrors) {
        return contentErrors.stream().filter(e -> e.getLevel() == ContentError.ErrorLevel.Error).toList();
    }

    /**
     * Reads every test resource whose file extension the reader claims and checks the reported content errors against
     * {@code expectedErrorsFun}. Files tagged {@code --INVALID<marker>} (deliberately broken input) and expected-output
     * files are skipped. Every other file must read without exception and with exactly the expected errors (warnings
     * are not compared, see {@link #testReadResourceToGraph}); all failures are collected and reported together.
     *
     * @throws AssertionError listing each failing resource
     */
    public static void testWithAllResource(GioReader gioReader, Function<Resource, List<ContentError>> expectedErrorsFun) {
        List<String> failures = new ArrayList<>();
        int[] tested = {0};
        forEachReadableResource(gioReader, resource -> {
            if (TestFileUtil.isInvalid(resource, "") || TestFileUtil.isExpected(resource)) {
                return;
            }
            tested[0]++;
            try {
                testReadResourceToGraph(gioReader, resource, expectedErrorsFun.apply(resource));
            } catch (Throwable e) {
                log.debug("Reading " + resource.getPath() + " failed", e);
                Throwable root = e;
                while (root.getCause() != null && root.getCause() != root) root = root.getCause();
                String reason = (root == e ? "" : root.getClass().getSimpleName() + ": ") + String.valueOf(root.getMessage()).lines().findFirst().orElse("");
                failures.add(resource.getPath() + ": " + (root == e ? e.getClass().getSimpleName() + ": " : "") + reason);
            }
        });
        log.info("{} read {} test resource(s), {} failed", gioReader.fileFormat().id(), tested[0], failures.size());
        if (!failures.isEmpty()) {
            throw new AssertionError(gioReader.fileFormat().id() + ": " + failures.size() + " of " + tested[0]
                    + " test resource(s) failed:\n  " + String.join("\n  ", failures));
        }
    }


}
