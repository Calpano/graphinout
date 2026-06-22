package com.graphinout.reader.ddot;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ddot.it's suggested relation aliases are normalized to their canonical names; the untyped link
 * {@code ....} stays untyped; and the rdf:type-like relation {@code has type} (aliases {@code is a}/{@code type})
 * sets a node type rather than an edge. See https://ddot.it/relations.
 */
class DDotRelationAliasTest {

    private static ICjGraph graph(String ddot) throws IOException {
        ICjDocument doc = DDotReader.parseDDotToCjDocument(SingleInputSource.of("in.ddot", ddot));
        return doc.graphs().findFirst().orElseThrow();
    }

    @Test
    void edgeAliasesNormalizedToCanonical() throws IOException {
        ICjGraph g = graph("""
                Alice ..rel.. Bob
                Alice ..see also.. Carol
                Dave ..tag.. urgent
                """);
        List<String> types = g.edges().map(ICjEdge::type).sorted().toList();
        assertEquals(List.of("has tag", "links to", "related"), types);
    }

    @Test
    void untypedLinkStaysUntyped() throws IOException {
        ICjGraph g = graph("a .... b\n");
        ICjEdge e = g.edges().findFirst().orElseThrow();
        assertNull(e.type(), "`....` must remain an untyped edge (no relation)");
    }

    @Test
    void typeRelationBecomesNodeTypeNotEdge() throws IOException {
        ICjGraph g = graph("""
                Alice ..is a.. Person
                Alice ..type.. Agent
                Bob ..has type.. Person
                """);
        assertEquals(0, g.edges().count(), "rdf:type-like relations are node types, not edges");

        ICjNode alice = g.nodes().filter(n -> "Alice".equals(n.id())).findFirst().orElseThrow();
        List<String> aliceTypes = alice.types().map(ICjElementType::type).sorted().toList();
        assertEquals(List.of("Agent", "Person"), aliceTypes);

        // the type object (a class) is not itself promoted to a node
        assertTrue(g.nodes().noneMatch(n -> "Person".equals(n.id())), "Person is a type, not a node");
    }
}
