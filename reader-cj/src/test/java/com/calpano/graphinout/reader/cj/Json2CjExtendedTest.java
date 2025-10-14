package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.stream.util.LoggingCjWriter;
import com.calpano.graphinout.base.cj.stream.impl.Json2CjWriter;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.json.stream.impl.JsonReaderImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.calpano.graphinout.foundation.input.SingleInputSourceOfString.inputSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Extended CJ parsing is not implemented yet")
class Json2CjExtendedTest {

    public static final String ALIAS_JSON = """
            {
              "node": [
                { "id": "n1", "name": "Node with alias" }
              ],
              "edge": [
                { "from": "n1", "to": "n1" }
              ]
            }
            """;
    public static final String ENDPOINTS_JSON = """
            {
              "nodes": [
                { "id": "n1" },
                { "id": "n2" }
              ],
              "edges": [
                {
                  "endpoints": [
                    { "direction": "in", "node": "n1" },
                    { "direction": "out", "node": "n2" }
                  ]
                }
              ]
            }
            """;
    public static final String SIMPLE_JSON = """
            {
              "nodes": [
                { "id": "n1" }
              ]
            }
            """;
    public static final String SIMPLE_JSON1 = """
            {
              "nodes": [
                { "id": "n1", "label": "Node 1" },
                { "id": "n2", "label": "Node 2" }
              ],
              "edges": [
                { "source": "n1", "target": "n2" }
              ]
            }
            """;

    @Test
    void testAliasSupport() throws IOException {
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        InputSource inputSource = inputSource("alias.json", ALIAS_JSON);
        LoggingCjWriter cjSink = new LoggingCjWriter();
        Json2CjWriter sink = new Json2CjWriter(cjSink);

        // Should process aliases (node->nodes, edge->edges, from->source, to->target, name->label) without errors
        assertDoesNotThrow(() -> {
            jsonReader.read(inputSource, sink);
        }, "Json2Cj should recognize and process property aliases without errors");
    }

    @Test
    void testCompleteExample() throws URISyntaxException, IOException {
        String resourceName = "example.connected.json5";
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(resourceName);
        assertNotNull(resourceUrl, "Resource file should exist: " + resourceName);

        Path inputPath = Paths.get(resourceUrl.toURI());
        String jsonContent = Files.readString(inputPath, StandardCharsets.UTF_8);

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        InputSource inputSource = inputSource("example.connected.json5", jsonContent);
        LoggingCjWriter cjSink = new LoggingCjWriter();
        Json2CjWriter sink = new Json2CjWriter(cjSink);

        // Process the JSON through Json2Cj - should complete without errors
        assertDoesNotThrow(() -> {
            jsonReader.read(inputSource, sink);
        }, "Json2Cj should process the example file without errors");

    }

    @Test
    void testEndpointsSupport() throws IOException {

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        InputSource inputSource = inputSource("endpoints.json", ENDPOINTS_JSON);
        LoggingCjWriter cjSink = new LoggingCjWriter();
        Json2CjWriter sink = new Json2CjWriter(cjSink);

        // Should process endpoints without errors
        assertDoesNotThrow(() -> {
            jsonReader.read(inputSource, sink);
        }, "Json2Cj should process endpoints without errors");
    }

    @Test
    void testOneNode() throws Exception {
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        try (InputSource inputSource = inputSource("one-node-test", SIMPLE_JSON)) {
            LoggingCjWriter cjSink = new LoggingCjWriter();
            Json2CjWriter sink = new Json2CjWriter(cjSink);

            // Should process simple nodes and edges without errors
            assertDoesNotThrow(() -> jsonReader.read(inputSource, sink));
        }
    }

    @Test
    void testSimpleNodeEdgeJson() throws IOException {

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        InputSource inputSource = inputSource("simple.json", SIMPLE_JSON1);
        LoggingCjWriter cjSink = new LoggingCjWriter();
        Json2CjWriter sink = new Json2CjWriter(cjSink);

        // Should process simple nodes and edges without errors
        assertDoesNotThrow(() -> {
            jsonReader.read(inputSource, sink);
        }, "Json2Cj should process simple nodes and edges without errors");

        System.out.println("=== Simple JSON CJ Events ===");
        cjSink.dump();
        System.out.println("=== End of Simple JSON CJ Events ===");
    }

}
