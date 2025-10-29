package com.graphinout.reader.dot;

import com.graphinout.base.cj.factory.BaseCjOutput;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.stream.NoopCjStream;
import com.graphinout.base.cj.writer.Cj2JsonWriter;
import com.graphinout.foundation.TestFileProvider;
import com.graphinout.foundation.TestFileUtil;
import com.graphinout.foundation.input.SingleInputSource;
import com.graphinout.foundation.json.writer.impl.Json2StringWriter;
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

    @ParameterizedTest
    @MethodSource("dotResources")
    void shouldWorkAsIntended(String displayPath, Resource resource) throws IOException {
        if (TestFileUtil.isInvalid(resource, "dot")) {
            log.info("Skipping invalid file " + resource.getURI());
            return;
        }

        String content = resource.getContentAsString();
        SingleInputSource singleInputSource = SingleInputSource.of(displayPath, content);

        Json2StringWriter json2StringWriter = new Json2StringWriter();
        Cj2JsonWriter cj2JsonWriter = new Cj2JsonWriter(json2StringWriter);
        CjStream2CjWriter cjStream2CjWriter = new CjStream2CjWriter(cj2JsonWriter);
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
