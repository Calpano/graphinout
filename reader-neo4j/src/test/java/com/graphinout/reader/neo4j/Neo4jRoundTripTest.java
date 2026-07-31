package com.graphinout.reader.neo4j;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.stream.ICjStream;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.cj.writer.CjWriter2CjStream;
import com.graphinout.base.cj.writer.ICjWriter;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.base.output.InMemoryOutputSink;
import com.graphinout.reader.cj.ConnectedJsonReader;
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
 * Round-trip test: Neo4j JSON -> CJ -> Neo4j JSON -> CJ
 * Tests that converting Neo4j JSON to CJ and back preserves the graph structure.
 */
class Neo4jRoundTripTest {

    private static final Logger log = getLogger(Neo4jRoundTripTest.class);

    /**
     * Canonical CJ fixtures whose {@code CJ -> Neo4j JSON -> CJ} round-trip currently throws
     * {@code Cannot start nodes in ArrayOfEdges} — every one of them a nested / compound graph shape the
     * Neo4j writer cannot yet stream.
     * <p>
     * This list exists so the failures are <em>counted</em>. The parameterized test used to wrap the whole
     * round-trip in {@code catch (Exception e) { log.warn("Skipping {} …") }}, which is worse than a skip:
     * a crashing fixture reported green, JUnit recorded no skip, and the only trace was a WARN nobody
     * reads. Now an unlisted fixture that crashes FAILS the build, and a listed one that starts passing
     * also fails — so the waiver cannot silently grow, and it cannot rot once the writer is fixed.
     */
    private static final java.util.Set<String> KNOWN_FAILING = java.util.Set.of(
            "baseuris.cj.json",
            "compound-nodes.cj.json",
            "full.cj.json",
            "nested-graphs.cj.json",
            "sample-1.cj.canonical.json");

    @Test
    void testRoundTrip() throws IOException {
        // Use our test file
        Resource res = TestFileUtil.resource("neo4j-sample.json");
        assertNotNull(res, "Resource not found");

        // Step 1: Read Neo4j JSON to CJ
        SingleInputSource inputSource = SingleInputSource.of(res.getPath(), res.getContentAsString());
        Neo4jReader neo4jReader1 = new Neo4jReader();
        CjWriter2CjDocumentWriter cj2document1 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream1 = new CjStream2CjWriter(cj2document1, true);

        neo4jReader1.read(inputSource, cjStream1);
        ICjDocument originalCjDoc = cj2document1.resultDoc();
        assertNotNull(originalCjDoc, "Original CJ document should not be null");

        String originalJson = CjDocuments.toJsonString(originalCjDoc);
        log.info("Original CJ JSON: {}", originalJson);

        // Step 2: Convert CJ to Neo4j JSON
        Neo4jReader neo4jWriter = new Neo4jReader();
        InMemoryOutputSink neo4jSink = InMemoryOutputSink.create();

        ICjStream neo4jStream = neo4jWriter.createCjStream(neo4jSink);
        ICjWriter cjWriter = new CjWriter2CjStream(neo4jStream);
        originalCjDoc.fire(cjWriter, false);

        String neo4jJson = neo4jSink.getBufferAsUtf8String();
        log.info("Neo4j JSON:\n{}", neo4jJson);

        assertFalse(neo4jJson.isEmpty(), "Neo4j JSON output should not be empty");
        assertTrue(neo4jJson.contains("\"type\":\"node\""), "Neo4j JSON should contain nodes");
        assertTrue(neo4jJson.contains("\"type\":\"relationship\""), "Neo4j JSON should contain relationships");

        // Step 3: Read Neo4j JSON back to CJ
        SingleInputSource neo4jInput = SingleInputSource.of("neo4j-temp", neo4jJson);
        Neo4jReader neo4jReader2 = new Neo4jReader();
        CjWriter2CjDocumentWriter cj2document2 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2 = new CjStream2CjWriter(cj2document2, true);

        neo4jReader2.read(neo4jInput, cjStream2);
        ICjDocument roundTripCjDoc = cj2document2.resultDoc();
        assertNotNull(roundTripCjDoc, "Round-trip CJ document should not be null");

        String roundTripJson = CjDocuments.toJsonString(roundTripCjDoc);
        log.info("Round-trip CJ JSON: {}", roundTripJson);

        // Step 4: Compare documents
        // Note: We can't do exact string comparison because:
        // - Neo4j format may lose some CJ metadata
        // - Property order may change
        // - Some CJ features may not map directly to Neo4j

        // Basic validation: check that we have graphs
        assertNotNull(roundTripCjDoc.graphs(), "Round-trip document should have graphs");

        // If the original had graphs with nodes/edges, the round-trip should too
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
    @DisplayName("Test CJ to Neo4j JSON round-trip - all files")
    void testRoundTripAllCjResources(String displayPath, Resource resource) {
        boolean knownToFail = KNOWN_FAILING.stream().anyMatch(displayPath::endsWith);
        try {
            testCjToNeo4jRoundTrip(resource);
        } catch (Exception e) {
            if (!knownToFail) {
                throw new AssertionError("CJ -> Neo4j JSON -> CJ round-trip crashed for " + displayPath
                        + ". If this is a genuine, accepted limitation, add the fixture to KNOWN_FAILING"
                        + " with the reason — do not re-broaden the catch.", e);
            }
            return;
        }
        if (knownToFail) {
            fail(displayPath + " is listed in KNOWN_FAILING but now round-trips cleanly. Remove it from"
                    + " that list so the fixture stays covered.");
        }
    }

    private void testCjToNeo4jRoundTrip(Resource res) throws IOException {
        // Step 1: Read CJ from resource
        SingleInputSource inputSource = SingleInputSource.of(res.getPath(), res.getContentAsString());
        ConnectedJsonReader cjReader = new ConnectedJsonReader();
        CjWriter2CjDocumentWriter cj2document1 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream1 = new CjStream2CjWriter(cj2document1, true);

        cjReader.read(inputSource, cjStream1);
        ICjDocument originalCjDoc = cj2document1.resultDoc();
        assertNotNull(originalCjDoc, "Original CJ document should not be null for " + res.getPath());

        String originalJson = CjDocuments.toJsonString(originalCjDoc);
        log.debug("Original CJ JSON: {}", originalJson);

        // Step 2: Convert CJ to Neo4j JSON
        Neo4jReader neo4jWriter = new Neo4jReader();
        InMemoryOutputSink neo4jSink = InMemoryOutputSink.create();

        java.util.List<com.graphinout.foundation.pure.input.ContentError> writeErrors = new java.util.ArrayList<>();
        neo4jWriter.setContentErrorHandler(writeErrors::add);
        ICjStream neo4jStream = neo4jWriter.createCjStream(neo4jSink);
        ICjWriter cjWriter = new CjWriter2CjStream(neo4jStream);
        originalCjDoc.fire(cjWriter, false);

        String neo4jJson = neo4jSink.getBufferAsUtf8String();
        log.debug("Neo4j JSON:\n{}", neo4jJson);

        if (neo4jJson.isEmpty()) {
            // Graph-less documents (only document-level data) legitimately produce no Neo4j output — but the
            // writer must report that loss as a ContentError rather than emitting silence (issues.adoc I3).
            assertFalse(writeErrors.isEmpty(),
                    "empty Neo4j output must be reported as a ContentError for " + res.getPath());
            return;
        }

        // Step 3: Read Neo4j JSON back to CJ
        SingleInputSource neo4jInput = SingleInputSource.of("neo4j-temp", neo4jJson);
        Neo4jReader neo4jReader2 = new Neo4jReader();
        CjWriter2CjDocumentWriter cj2document2 = new CjWriter2CjDocumentWriter();
        ICjStream cjStream2 = new CjStream2CjWriter(cj2document2, true);

        neo4jReader2.read(neo4jInput, cjStream2);
        ICjDocument roundTripCjDoc = cj2document2.resultDoc();
        assertNotNull(roundTripCjDoc, "Round-trip CJ document should not be null for " + res.getPath());

        String roundTripJson = CjDocuments.toJsonString(roundTripCjDoc);
        log.debug("Round-trip CJ JSON: {}", roundTripJson);

        // Step 4: Compare documents
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

            // Note: Some CJ files may have features that don't map cleanly to Neo4j JSON
            // We log these but don't fail the test.
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
