package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ddot.it {@code prefix} relation declares a namespace; graphinout collects it into the CJ document
 * {@code @context} (ids are left verbatim — no expansion). See https://ddot.it/rdf.
 */
class DDotPrefixTest {

    @Test
    void prefixDeclarationBecomesContextNotEdge() throws IOException {
        ICjDocument doc = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", """
                foaf ..prefix.. http://xmlns.com/foaf/0.1/
                Alice ..knows.. Bob
                """));

        Map<String, String> ctx = doc.context();
        assertNotNull(ctx, "@context should be populated");
        assertEquals("http://xmlns.com/foaf/0.1/", ctx.get("foaf"));

        // `prefix` is a namespace declaration, not a graph edge
        ICjGraph g = doc.graphs().findFirst().orElseThrow();
        assertEquals(1, g.edges().count(), "only the knows edge");
        assertTrue(g.nodes().noneMatch(n -> "foaf".equals(n.id())), "no foaf node");
    }
}
