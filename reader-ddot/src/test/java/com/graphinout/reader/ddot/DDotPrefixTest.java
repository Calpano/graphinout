package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void contextRoundTripsAsPrefixLinesWithoutExpansion() throws IOException {
        String ddot = """
                foaf ..prefix.. http://xmlns.com/foaf/0.1/
                foaf:Alice ..foaf:knows.. foaf:Bob
                """;
        ICjDocument cj1 = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));

        String ddot2 = new DDotOutput(cj1).toDDot();
        assertTrue(ddot2.contains("prefix"), () -> "@context must be re-emitted as a prefix line:\n" + ddot2);
        assertTrue(ddot2.contains("foaf:Alice"), () -> "CURIE id must be kept:\n" + ddot2);
        assertFalse(ddot2.contains("0.1/Alice"), () -> "ids must NOT be expanded to full IRIs:\n" + ddot2);

        ICjDocument cj2 = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in2.ddot", ddot2));
        assertEquals("http://xmlns.com/foaf/0.1/", cj2.context().get("foaf"), "@context survives the round-trip");
        ICjGraph g2 = cj2.graphs().findFirst().orElseThrow();
        assertTrue(g2.nodes().anyMatch(n -> "foaf:Alice".equals(n.id())), "CURIE node id survives");
        assertEquals("foaf:knows", g2.edges().findFirst().orElseThrow().type());
    }
}
