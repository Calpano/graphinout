package com.graphinout.reader.dot;

import com.graphinout.base.cj.factory.BaseCjOutput;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.stream.NoopCjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.writer.impl.Json2StringWriter;
import io.github.classgraph.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.slf4j.LoggerFactory.getLogger;

class DotTextReaderTest {

    public static final String EXAMPLE_DOT_PATH = "/text/dot/example.dot";
    public static final String SIMPLE_DOT = "/text/dot/synthetics/simple/simple.dot";
    public static final String SIMPLE_2_DOT = "/text/dot/synthetics/simple/simple2.dot";
    public static final String SIMPLE_3_DOT = "/text/dot/synthetics/simple/simple3.dot";
    public static final String LABEL = "label";
    public static final String NODE_A = "Node A";
    public static final String NODE_B = "Node B";
    public static final String NODE_ID_A = "A";
    public static final String NODE_ID_B = "B";
    public static final String NODE_ID_C = "C";
    public static final String NODE_ID_D = "D";
    public static final String EDGE_1 = "Edge 1";
    public static final String EDGE_2 = "Edge 2";
    public static final String EDGE_3 = "Edge 3";
    public static final String NODE_COLOR = "node-color";
    public static final String NODE_SHAPE = "node-shape";
    public static final String CIRCLE = "circle";
    public static final String COLOR_RED = "red";
    public static final String COLOR_GREEN = "green";
    private static final String NODE_LABEL = "node-label";
    private static final String EDGE_LABEL = "edge-label";
    private static final String EDGE_COLOR = "edge-color";
    private static final Logger log = getLogger(DotTextReaderTest.class);
    private final BaseCjOutput baseCjOutput = new BaseCjOutput();
    private AutoCloseable closeable;
    private DotReader underTest;
    private ICjStream cjStream;

    public static Stream<TestFileProvider.TestResource> dotResources() {
        return TestFileProvider.getAllTestResources() //
                .filter(res -> res.resource().getPath().endsWith(".dot"));
    }

    @BeforeEach
    void setUp() {
        this.underTest = new DotReader();
        this.cjStream = new NoopCjStream();
    }

    @Test
    void testExample9() throws IOException {
        TestFileProvider.TestResource res = TestFileProvider.resourceByPath("text/dot/example9--SMALL.dot");
        shouldWorkAsIntended(res.asPath(),res.resource());
    }

    /**
     * DOT fixtures tagged {@code --INVALIDdot} that {@link DotReader} currently accepts in silence — no
     * exception, no {@code Error}-level {@link com.graphinout.foundation.pure.input.ContentError}. The
     * corpus says these are malformed; the reader disagrees, and one of the two is wrong.
     *
     * <p>The list exists so the disagreement is COUNTED. This assertion used to be
     * {@code if (isInvalid(...)) return;} — a green test that asserted nothing and hid the gap entirely.
     * An unlisted invalid fixture that slips through now fails, and a listed one that starts being
     * rejected also fails, so the waiver can neither grow nor rot unnoticed.
     */
    private static final java.util.Set<String> SILENTLY_ACCEPTED_INVALID = java.util.Set.of(
            "text/dot/example4--INVALIDdot.dot",
            "text/dot/generated/edge-mid-attrs--INVALIDdot.dot",
            "text/dot/generated/edge-multi-mid-attrs--INVALIDdot.dot",
            "text/dot/generated/group-with-subgraph--INVALIDdot.dot",
            "text/dot/generated/node-attrs-before-edge--INVALIDdot.dot");

    @ParameterizedTest
    @MethodSource("dotResources")
    void shouldWorkAsIntended(String displayPath, Resource resource) throws IOException {
        // A fixture tagged --INVALIDdot must be REJECTED, not skipped: the reader either throws or reports
        // at least one Error-level ContentError. This was an early `return`, which made the case report
        // green having asserted nothing — invisible even as a skip.
        if (TestFileUtil.isInvalid(resource, "dot")) {
            java.util.List<com.graphinout.foundation.pure.input.ContentError> errors = new java.util.ArrayList<>();
            underTest.setContentErrorHandler(errors::add);
            boolean threw = false;
            try {
                underTest.read(SingleInputSource.of(displayPath, resource.getContentAsString()), new NoopCjStream());
            } catch (Exception e) {
                threw = true;
            }
            boolean rejected = threw || errors.stream()
                    .anyMatch(e -> e.level == com.graphinout.foundation.pure.input.ContentError.ErrorLevel.Error);
            boolean knownAccepted = SILENTLY_ACCEPTED_INVALID.stream().anyMatch(resource.getPath()::endsWith);
            if (!rejected && !knownAccepted) {
                throw new AssertionError("fixture is tagged --INVALIDdot but DotReader accepted it without"
                        + " throwing and without a single Error-level ContentError: " + resource.getPath());
            }
            if (rejected && knownAccepted) {
                throw new AssertionError(resource.getPath() + " is listed in SILENTLY_ACCEPTED_INVALID but"
                        + " is now properly rejected — remove it from that list.");
            }
            return;
        }

        String content = resource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter, true);
        underTest.read(singleInputSource, cjStream2CjWriter);
        String json = json2StringWriter.jsonString();
        log.info("JSON: " + json);
    }


    @Test
    // fixed #112"
    void testSubgraph() throws IOException {
        String path = "/text/dot/synthetics/simple/simple-subgraph.dot";
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        underTest.read(inputSource, cjStream);
        // no further assertions in disabled test
    }

    @Test
    void testUndirected() throws IOException {
        testRead("/text/dot/synthetics/simple/simple-undirected.dot");
    }

    private void testRead(String path) throws IOException {
        String content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
        SingleInputSource inputSource = SingleInputSource.of(path, content);
        underTest.read(inputSource, cjStream);
    }

}
