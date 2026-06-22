package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * ddot.it's suggested relation aliases are normalized to their canonical names; the untyped link
 * {@code ....} stays untyped (no type/label). See https://ddot.it/relations.
 */
class DDotRelationAliasTest {

    private static ICjGraph graph(String ddot) throws IOException {
        ICjDocument doc = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
        return doc.graphs().findFirst().orElseThrow();
    }

    @Test
    void aliasesNormalizedToCanonical() throws IOException {
        ICjGraph g = graph("""
                Alice ..is a.. Person
                Bob ..type.. Person
                Alice ..rel.. Bob
                Alice ..see also.. Carol
                """);
        List<String> types = g.edges().map(ICjEdge::type).sorted().toList();
        assertEquals(List.of("has type", "has type", "links to", "related"), types);
    }

    @Test
    void untypedLinkStaysUntyped() throws IOException {
        ICjGraph g = graph("a .... b\n");
        ICjEdge e = g.edges().findFirst().orElseThrow();
        assertNull(e.type(), "`....` must remain an untyped edge (no relation)");
    }
}
