package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import com.graphinout.testdata.TestFileUtil;
import io.github.classgraph.Resource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Per-link metadata (the {@code ,,} syntax, see https://ddot.it) must survive ddot &rarr; CJ as edge data.
 * Uses the external ddot-it corpus in graph-test-data; skips when that checkout is not configured
 * ({@code -Dgraph.test.data.dir=…}).
 */
class DDotLinkMetadataTest {

    private ICjEdge firstEdge(String resourcePath) throws IOException {
        Resource res = TestFileUtil.resource(resourcePath);
        Assumptions.assumeTrue(res != null,
                "ddot-it corpus not on classpath; set -Dgraph.test.data.dir to the graph-test-data checkout");
        ICjDocument doc = DDotReader.parseDDotToCjDocument(SingleInputSource.of(resourcePath, res.getContentAsString()));
        assertNotNull(doc);
        return doc.graphs().findFirst().orElseThrow().edges().findFirst().orElseThrow();
    }

    /** Structured properties live under {@code ddot-it:props}. */
    private static IJsonObject props(ICjEdge edge) {
        assertNotNull(edge.data().jsonValue(), "edge should carry metadata as data");
        return edge.data().jsonValue().asObject().get("ddot-it:props").asObject();
    }

    /** Free-text notes live under {@code ddot-it:text}. */
    private static String text(ICjEdge edge) {
        assertNotNull(edge.data().jsonValue(), "edge should carry metadata as data");
        return edge.data().jsonValue().asObject().get("ddot-it:text").asString();
    }

    @Test
    void inlineStructuredMetaUnderProps() throws IOException {
        // John Doe ..leads.. Project Eagle ,, ..since.. 2025
        ICjEdge edge = firstEdge("text/ddot-it/features/03-inline-meta.ddot");
        assertEquals("leads", edge.edgeType().type());
        assertEquals("2025", props(edge).get("since").asString());
    }

    @Test
    void multilineStructuredMetaUnderProps() throws IOException {
        // Dirk Hagemann ..works at.. SAP ,,  /  ..year.. 2010  /  ..fictive.. yes  /  ,,
        ICjEdge edge = firstEdge("text/ddot-it/features/04-multiline-meta.ddot");
        assertEquals("works at", edge.edgeType().type());
        IJsonObject props = props(edge);
        assertEquals("2010", props.get("year").asString());
        assertEquals("yes", props.get("fictive").asString());
    }

    @Test
    void inlineFreeTextMetaUnderText() throws IOException {
        // John Doe ..leads.. Project Eagle ,, a random note
        ICjEdge edge = firstEdge("text/ddot-it/features/13-inline-meta-text.ddot");
        assertEquals("a random note", text(edge));
    }

    @Test
    void multilineFreeTextMetaUnderText() throws IOException {
        // Dirk Hagemann ..works at.. SAP ,,  /  a random note  /  ,,
        ICjEdge edge = firstEdge("text/ddot-it/features/14-multiline-meta-text.ddot");
        assertEquals("a random note", text(edge));
    }
}
