package com.graphinout.reader.grale;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reads a grale document into CJ and writes it back, asserting the structure and the layout-specific
 * fields survive the grale &rarr; CJ &rarr; grale round-trip.
 */
class GraleRoundTripTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private String resource(String name) throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/grale/" + name)) {
            assertNotNull(is, "resource not found: " + name);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** grale text -> CJ -> grale text, returning the re-serialised grale JSON tree. */
    private JsonNode roundTrip(String fileName) throws IOException {
        GraleReader reader = new GraleReader();
        try (SingleInputSource in = SingleInputSource.of(fileName, resource(fileName))) {
            ICjDocument doc = reader.readToCjDocument(in);
            assertNotNull(doc, "CJ document should not be null");

            InMemoryOutputSink sink = new InMemoryOutputSink();
            reader.writeCjDocument(doc, sink);
            return mapper.readTree(sink.getBufferAsUtf8String());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Test
    void simpleGraphRoundTrips() throws IOException {
        JsonNode out = roundTrip("simple.grale.json");

        assertTrue(out.path("options").path("directed").asBoolean(), "directed option preserved");
        assertEquals("LR", out.path("value").path("rankdir").asText(), "graph label preserved");

        assertEquals(2, out.get("nodes").size());
        assertEquals(1, out.get("edges").size());

        JsonNode a = out.get("nodes").get(0);
        assertEquals("a", a.get("v").asText());
        assertEquals("Start", a.path("value").path("meta").path("label").asText());
        assertEquals(60, a.path("value").path("width").asInt());

        JsonNode edge = out.get("edges").get(0);
        assertEquals("a", edge.get("v").asText());
        assertEquals("b", edge.get("w").asText());
        assertEquals("e_ab", edge.get("id").asText());
        assertEquals("next", edge.path("value").path("meta").path("label").asText());
        assertEquals(1, edge.path("value").path("weight").asInt());
    }

    @Test
    void richSampleRoundTripPreservesLayoutFields() throws IOException {
        JsonNode out = roundTrip("sample.grale.json");

        assertEquals(3, out.get("nodes").size());
        assertEquals(2, out.get("edges").size());

        // graph-level grale extras survive
        assertEquals("Z", out.path("value").path("focus").asText());
        assertTrue(out.path("value").path("markers").has("arrow"), "markers registry preserved");
        assertEquals(1, out.get("hyperedges").size(), "hyperedges preserved");
        assertTrue(out.path("diagnostics").has("warnings"), "diagnostics preserved");
        assertTrue(out.has("debug"), "debug overlays preserved");

        // per-node layout output survives
        JsonNode a = out.get("nodes").get(0);
        assertEquals("a", a.get("v").asText());
        assertEquals(60, a.path("value").path("x").asInt());
        assertEquals(60, a.path("value").path("y").asInt());

        // per-edge routed points + markers survive
        JsonNode ab = out.get("edges").get(0);
        assertEquals("arrow", ab.path("value").path("endMarker").asText());
        assertTrue(ab.path("value").path("points").isArray(), "edge points preserved");
        assertEquals(2, ab.path("value").path("points").size());
    }

    @Test
    void doubleRoundTripIsStable() throws IOException {
        // grale -> CJ -> grale, then read THAT back to CJ and write again; node/edge counts must match.
        JsonNode first = roundTrip("sample.grale.json");

        GraleReader reader = new GraleReader();
        try (SingleInputSource in = SingleInputSource.of("sample.grale.json", first.toString())) {
            ICjDocument doc = reader.readToCjDocument(in);
            InMemoryOutputSink sink = new InMemoryOutputSink();
            reader.writeCjDocument(doc, sink);
            JsonNode second = mapper.readTree(sink.getBufferAsUtf8String());

            assertEquals(first.get("nodes").size(), second.get("nodes").size());
            assertEquals(first.get("edges").size(), second.get("edges").size());
            assertEquals(first.get("hyperedges").size(), second.get("hyperedges").size());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
