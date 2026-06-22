package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.json.document.IJsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code ddot.it/this} (shorthand {@code !!this}) names the current document as subject, so its triples
 * are document-level metadata — not graph nodes/edges. See https://ddot.it/this.
 */
class DDotThisCommandTest {

    private static ICjDocument parse(String ddot) throws IOException {
        return DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
    }

    @Test
    void thisTriplesBecomeDocumentMetadataNotEdges() throws IOException {
        ICjDocument doc = parse("""
                ddot.it/this ..project.. Code Red
                ddot.it/this ..author.. Alice
                a ..knows.. b
                """);

        IJsonObject data = doc.data().jsonValue().asObject();
        assertEquals("Code Red", data.get("project").asString());
        assertEquals("Alice", data.get("author").asString());

        // the document is NOT a graph node, and the real triple is still an edge
        ICjGraph g = doc.graphs().findFirst().orElseThrow();
        assertEquals(2, g.nodes().count(), "only a, b");
        assertEquals(1, g.edges().count(), "only a knows b");
        assertTrue(g.nodes().noneMatch(n -> "ddot.it/this".equals(n.id())), "no ddot.it/this node");
    }

    @Test
    void shorthandThisAlsoBecomesDocumentMetadata() throws IOException {
        ICjDocument doc = parse("!!this ..status.. active\n");
        assertEquals("active", doc.data().jsonValue().asObject().get("status").asString());
    }

    @Test
    void repeatedThisKeyBecomesArray() throws IOException {
        // mirrors the grale provenance convention (repeated `..uses feature..` → a JSON array)
        ICjDocument doc = parse("""
                ddot.it/this ..tag.. red
                ddot.it/this ..tag.. blue
                """);
        var tags = doc.data().jsonValue().asObject().get("tag").asArray();
        assertEquals(2, tags.size());
        assertEquals("red", tags.get(0).asString());
        assertEquals("blue", tags.get(1).asString());
    }
}
