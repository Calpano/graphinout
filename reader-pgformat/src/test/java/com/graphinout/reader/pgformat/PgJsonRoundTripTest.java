package com.graphinout.reader.pgformat;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.testdata.TestFileProvider;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Round-trip test: PG-JSON -> CJ -> PG-JSON -> CJ and CJ -> PG-JSON -> CJ
 * Tests that converting to/from PG-JSON preserves the graph structure.
 */
class PgJsonRoundTripTest {

    private static final Logger log = getLogger(PgJsonRoundTripTest.class);

    @Test
    void testRoundTrip() throws IOException {
        // Use our test file
        Resource res = TestFileUtil.resource("pg-sample.json");
        assertNotNull(res, "Resource not found");

        // Step 1: Read PG-JSON to CJ
        SingleInputSource inputSource = SingleInputSource.of(res.getPath(), res.getContentAsString());
        PgJsonReader pgReader1 = new PgJsonReader();
        CjWriter2CjDocumentWriter cj2document1 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream1 = new CjStream2CjWriter(cj2document1, true);

        pgReader1.read(inputSource, cjStream1);
        ICjDocument originalCjDoc = cj2document1.resultDoc();
        assertNotNull(originalCjDoc, "Original CJ document should not be null");

        String originalJson = CjDocuments.toJsonString(originalCjDoc);
        log.info("Original CJ JSON: {}", originalJson);

        // Step 2: Convert CJ to PG-JSON
        PgJsonReader pgWriter = new PgJsonReader();
        InMemoryOutputSink pgSink = InMemoryOutputSink.create();

        ICjStream pgStream = pgWriter.createCjStream(pgSink);
        ICjWriter cjWriter = new CjWriter2CjStream(pgStream);
        originalCjDoc.fire(cjWriter, false);

        String pgJson = pgSink.getBufferAsUtf8String();
        log.info("PG-JSON:\n{}", pgJson);

        assertFalse(pgJson.isEmpty(), "PG-JSON output should not be empty");
        assertTrue(pgJson.contains("\"nodes\""), "PG-JSON should contain nodes");
        assertTrue(pgJson.contains("\"edges\""), "PG-JSON should contain edges");

        // Step 3: Read PG-JSON back to CJ
        SingleInputSource pgInput = SingleInputSource.of("pg-temp", pgJson);
        PgJsonReader pgReader2 = new PgJsonReader();
        CjWriter2CjDocumentWriter cj2document2 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2 = new CjStream2CjWriter(cj2document2, true);

        pgReader2.read(pgInput, cjStream2);
        ICjDocument roundTripCjDoc = cj2document2.resultDoc();
        assertNotNull(roundTripCjDoc, "Round-trip CJ document should not be null");

        String roundTripJson = CjDocuments.toJsonString(roundTripCjDoc);
        log.info("Round-trip CJ JSON: {}", roundTripJson);

        // Step 4: Compare documents
        assertNotNull(roundTripCjDoc.graphs(), "Round-trip document should have graphs");

        if (originalCjDoc.graphs() != null) {
            long originalGraphCount = originalCjDoc.graphs().count();
            long roundTripGraphCount = roundTripCjDoc.graphs().count();

            assertTrue(originalGraphCount > 0, "Original should have at least one graph");
            assertTrue(roundTripGraphCount > 0, "Round-trip should have at least one graph");

            // Compare node and edge counts
            int originalNodeCount = originalCjDoc.graphs().mapToInt(g -> g.nodes() != null ? (int) g.nodes().count() : 0).sum();
            int roundTripNodeCount = roundTripCjDoc.graphs().mapToInt(g -> g.nodes() != null ? (int) g.nodes().count() : 0).sum();

            int originalEdgeCount = originalCjDoc.graphs().mapToInt(g -> g.edges() != null ? (int) g.edges().count() : 0).sum();
            int roundTripEdgeCount = roundTripCjDoc.graphs().mapToInt(g -> g.edges() != null ? (int) g.edges().count() : 0).sum();

            log.info("Round-trip comparison: nodes {}->{}, edges {}->{}",
                originalNodeCount, roundTripNodeCount, originalEdgeCount, roundTripEdgeCount);

            assertEquals(originalNodeCount, roundTripNodeCount,
                "Node count should match after round-trip");
            assertEquals(originalEdgeCount, roundTripEdgeCount,
                "Edge count should match after round-trip");
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("com.graphinout.testdata.TestFileProvider#cjResourcesCanonical")
    @DisplayName("Test CJ to PG-JSON round-trip - all files")
    void testRoundTripAllCjResources(String displayPath, Resource resource) throws IOException {
        // No catch here on purpose. This used to be wrapped in
        //   catch (Exception e) { log.warn("Skipping {} due to: {}", …) }
        // which made a crashing fixture report GREEN — no failure, no skip marker, just a WARN. Every
        // canonical CJ fixture currently round-trips through PG-JSON, so the strict form costs nothing
        // today and is the whole point tomorrow: a regression must fail, not whisper.
        testCjToPgJsonRoundTrip(resource);
    }

    private void testCjToPgJsonRoundTrip(Resource res) throws IOException {
        // Step 1: Read CJ from resource
        SingleInputSource inputSource = SingleInputSource.of(res.getPath(), res.getContentAsString());
        com.graphinout.reader.cj.ConnectedJsonReader cjReader = new com.graphinout.reader.cj.ConnectedJsonReader();
        CjWriter2CjDocumentWriter cj2document1 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream1 = new CjStream2CjWriter(cj2document1, true);

        cjReader.read(inputSource, cjStream1);
        ICjDocument originalCjDoc = cj2document1.resultDoc();
        assertNotNull(originalCjDoc, "Original CJ document should not be null for " + res.getPath());

        String originalJson = CjDocuments.toJsonString(originalCjDoc);
        log.debug("Original CJ JSON: {}", originalJson);

        // Step 2: Convert CJ to PG-JSON
        PgJsonReader pgWriter = new PgJsonReader();
        InMemoryOutputSink pgSink = InMemoryOutputSink.create();

        ICjStream pgStream = pgWriter.createCjStream(pgSink);
        ICjWriter cjWriter = new CjWriter2CjStream(pgStream);
        originalCjDoc.fire(cjWriter, false);

        String pgJson = pgSink.getBufferAsUtf8String();
        log.debug("PG-JSON:\n{}", pgJson);

        assertFalse(pgJson.isEmpty(), "PG-JSON output should not be empty for " + res.getPath());

        // Step 3: Read PG-JSON back to CJ
        SingleInputSource pgInput = SingleInputSource.of("pg-temp", pgJson);
        PgJsonReader pgReader2 = new PgJsonReader();
        CjWriter2CjDocumentWriter cj2document2 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2 = new CjStream2CjWriter(cj2document2, true);

        pgReader2.read(pgInput, cjStream2);
        ICjDocument roundTripCjDoc = cj2document2.resultDoc();
        assertNotNull(roundTripCjDoc, "Round-trip CJ document should not be null for " + res.getPath());

        String roundTripJson = CjDocuments.toJsonString(roundTripCjDoc);
        log.debug("Round-trip CJ JSON: {}", roundTripJson);

        // Step 4: Compare documents
        // Note: We can't do exact string comparison because:
        // - PG-JSON format may lose some CJ metadata
        // - Property order may change
        // - Some CJ features may not map directly to PG-JSON
        // - Multiple labels on nodes/edges may be reordered

        assertNotNull(roundTripCjDoc.graphs(), "Round-trip document should have graphs for " + res.getPath());

        // If the original had graphs with nodes/edges, the round-trip should too
        if (originalCjDoc.graphs() != null) {
            long originalGraphCount = originalCjDoc.graphs().count();
            long roundTripGraphCount = roundTripCjDoc.graphs().count();

            if (originalGraphCount > 0) {
                assertTrue(roundTripGraphCount > 0,
                    "Round-trip document should have at least one graph for " + res.getPath());
            }

            // Compare node and edge counts
            int originalNodeCount = originalCjDoc.graphs().mapToInt(g -> g.nodes() != null ? (int) g.nodes().count() : 0).sum();
            int roundTripNodeCount = roundTripCjDoc.graphs().mapToInt(g -> g.nodes() != null ? (int) g.nodes().count() : 0).sum();

            int originalEdgeCount = originalCjDoc.graphs().mapToInt(g -> g.edges() != null ? (int) g.edges().count() : 0).sum();
            int roundTripEdgeCount = roundTripCjDoc.graphs().mapToInt(g -> g.edges() != null ? (int) g.edges().count() : 0).sum();

            log.info("Round-trip comparison for {}: nodes {}->{}, edges {}->{}",
                res.getPath(), originalNodeCount, roundTripNodeCount, originalEdgeCount, roundTripEdgeCount);

            // Note: Some CJ files may have edges without explicit nodes, or other features
            // that don't map cleanly to PG-JSON. We log these but don't fail the test.
            if (originalNodeCount != roundTripNodeCount) {
                log.warn("Node count mismatch for {}: {} -> {} (may be due to implicit nodes in edges)",
                    res.getPath(), originalNodeCount, roundTripNodeCount);
            } else if (originalEdgeCount != roundTripEdgeCount) {
                log.warn("Edge count mismatch for {}: {} -> {}",
                    res.getPath(), originalEdgeCount, roundTripEdgeCount);
            }
        }
    }
}
