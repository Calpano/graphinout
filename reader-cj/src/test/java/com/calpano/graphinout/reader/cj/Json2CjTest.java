package com.calpano.graphinout.reader.cj;

import com.calpano.graphinout.base.cj.impl.LoggingCjWriter;
import com.calpano.graphinout.foundation.input.InputSource;
import com.calpano.graphinout.foundation.json.impl.JsonReaderImpl;
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

@Disabled
class Json2CjTest {

    @Test
    void testAliasSupport() throws IOException {
        String aliasJson = """
                {
                  "node": [
                    { "id": "n1", "name": "Node with alias" }
                  ],
                  "edge": [
                    { "from": "n1", "to": "n1" }
                  ]
                }
                """;

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        InputSource inputSource = inputSource("alias.json", aliasJson);
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
        ClassLoader classLoader = Convert.class.getClassLoader();
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
        String endpointsJson = """
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

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        InputSource inputSource = inputSource("endpoints.json", endpointsJson);
        LoggingCjWriter cjSink = new LoggingCjWriter();
        Json2CjWriter sink = new Json2CjWriter(cjSink);

        // Should process endpoints without errors
        assertDoesNotThrow(() -> {
            jsonReader.read(inputSource, sink);
        }, "Json2Cj should process endpoints without errors");
    }

    @Test
    void testOneNode() throws Exception {
        String simpleJson = """
                {
                  "nodes": [
                    { "id": "n1" }
                  ]
                }
                """;
        JsonReaderImpl jsonReader = new JsonReaderImpl();
        try (InputSource inputSource = inputSource("one-node-test", simpleJson)) {
            LoggingCjWriter cjSink = new LoggingCjWriter();
            Json2CjWriter sink = new Json2CjWriter(cjSink);

            // Should process simple nodes and edges without errors
            assertDoesNotThrow(() -> jsonReader.read(inputSource, sink));
        }
    }

    @Test
    void testSimpleNodeEdgeJson() throws IOException {
        String simpleJson = """
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

        JsonReaderImpl jsonReader = new JsonReaderImpl();
        InputSource inputSource = inputSource("simple.json", simpleJson);
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
