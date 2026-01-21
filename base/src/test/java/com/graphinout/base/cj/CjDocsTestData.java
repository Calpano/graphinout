package com.graphinout.base.cj;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjDocumentMutable;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import com.graphinout.foundation.pure.json.document.IJsonFactory;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

/**
 * Provide a set of synthetic CjDocs for testing. See <a href="https://calpano.github.io/connected-json/spec-cj.html">CJ
 * spec</a>. See also the test plan in {@code doc/test-plan-cj.adoc}.
 */
@SuppressWarnings({"CodeBlock2Expr", "HttpUrlsUsage"})
public class CjDocsTestData {

    public record TestDoc(String name, ICjDocument doc) implements Arguments {

        @Override
        public Object[] get() {
            return new Object[] { name, doc};
        }

    }

    /**
     * Test Case 1.3: A document with a `baseUri`.
     */
    public static ICjDocument documentWithBaseUri() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.baseUri("http://example.org/base/");
        return cjDoc;
    }

    // ========================================================================
    // Test Plan Section 1: Document-Level Tests
    // ========================================================================

    /**
     * Test Case 1.4: A document with custom `data`.
     */
    public static ICjDocument documentWithCustomData() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.dataMutable(data -> {
            data.add("author", "Test Engineer");
            data.add("creationDate", "2025-10-27");
        });
        return cjDoc;
    }

    /**
     * Test Case 1.2: A document with `connectedJson` metadata.
     */
    public static ICjDocument documentWithMetadata() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.connectedJson(cj -> cj.versionNumber("5.0.0").versionDate("2025-07-14").canonical(true));
        return cjDoc;
    }


    // ========================================================================
    // Test Plan Section 2: ID Management Tests
    // ========================================================================

    /**
     * Test Case 4.6: An edge where an endpoint type overrides the edge type.
     */
    public static ICjDocument edgeAndEndpointTypePrecedence() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> n.id("n1"));
            graph.addNode(n -> n.id("n2"));
            graph.addEdge(edge -> {
                edge.id("e-type");
                // TODO ensure we test all 3 edge type sources
                edge.edgeType(ICjElementType.of("related"));
                edge.addEndpoint(ep -> ep.node("n1"));
                edge.addEndpoint(ep -> ep.node("n2").type("works for"));
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 4.5: An edge that connects to a specific port on a node.
     */
    public static ICjDocument edgeReferencingPort() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> node.id("n1").addPort(port -> port.id("p1")));
            graph.addNode(node -> node.id("n2"));
            graph.addEdge(edge -> {
                edge.id("e-port");
                edge.addEndpoint(ep -> ep.node("n1").port("p1"));
                edge.addEndpoint(ep -> ep.node("n2"));
            });
        });
        return cjDoc;
    }


    // ========================================================================
    // Test Plan Section 3: Label Tests
    // ========================================================================

    /**
     * An empty graph.
     */
    public static ICjDocument emptyGraph() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
        });
        return cjDoc;
    }


    // ========================================================================
    // Test Plan Section 4: Node, Port, Edge, and Endpoint Tests
    // ========================================================================

    /**
     * Test Case 4.7: An endpoint with `type`.
     */
    public static ICjDocument endpointTypePropertyPrecedence() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> n.id("n1"));
            graph.addNode(n -> n.id("n2"));
            graph.addNode(n -> n.id("t-node"));
            // TODO add node type
            graph.addEdge(edge -> {
                edge.id("e-type-prec");
                edge.addEndpoint(ep -> {
                    ep.node("n1");
                    ep.type("a");
                });
                edge.addEndpoint(ep -> ep.node("n2"));
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 5.1: A graph nested within another graph.
     */
    public static ICjDocument graphInGraph() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(g1 -> {
            g1.id("g1");
            g1.addNode(n -> n.id("n1"));
            g1.addGraph(g2 -> {
                g2.id("g2");
                g2.addNode(n -> n.id("n2"));
                g2.addEdge(e -> {
                    e.id("e1");
                    e.addEndpoint(ep -> ep.node("n1"));
                    e.addEndpoint(ep -> ep.node("n2"));
                });
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 5.2: A graph nested within a node (a compound node).
     */
    public static ICjDocument graphInNode() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n1 -> {
                n1.id("n1");
                n1.addGraph(g -> g.addNode(n2 -> n2.id("n2")));
            });
            graph.addNode(n3 -> n3.id("n3"));
            graph.addEdge(e -> e.id("e1").addEndpoint(ep -> ep.node("n2")).addEndpoint(ep -> ep.node("n3")));
        });
        return cjDoc;
    }

    /**
     * Empty graph but with a graph label.
     */
    public static ICjDocument graphWithLabel() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> graph.addLabel("Graph Label", null));
        return cjDoc;
    }

    /**
     * Test Case 4.4: A hyper-edge with three endpoints.
     */
    public static ICjDocument hyperEdge() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> n.id("n1"));
            graph.addNode(n -> n.id("n2"));
            graph.addNode(n -> n.id("n3"));
            graph.addEdge(edge -> {
                edge.id("hyper-edge");
                edge.addEndpoint(ep -> ep.node("n1"));
                edge.addEndpoint(ep -> ep.node("n2"));
                edge.addEndpoint(ep -> ep.node("n3"));
            });
        });
        return cjDoc;
    }

    /**
     * All the different ways ids and uris can be mixed and matched: graph with or without baseUri,
     * node with id/uri/blankNodeId.
     */
    public static ICjDocument idVsUri() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.baseUri("doi:abc#");
        cjDoc.addGraph(graph -> {
            graph.id("g1-with"); // becomes https://example.com/g1-with
            graph.baseUri("https://example.com/");
            graph.addNode(node -> node.id("n1").addLabelWithoutLanguage("Node N1 in G1")); // becomes https://example.com/n1
            graph.addNode(node -> node.id("https://example.com/n2").addLabelWithoutLanguage("Node N2 in G1"));
            graph.addNode(node -> node.id("_:n3").addLabelWithoutLanguage("Node N3 in G1"));
        });
        cjDoc.addGraph(graph -> {
            graph.id("g2-without");
            graph.addNode(node -> node.id("doi:abc#n1").addLabelWithoutLanguage("Node N1 in G2")); // defaults to doi:abc-n1
            graph.addNode(node -> node.id("n2").addLabelWithoutLanguage("Node N2 in G2"));
            graph.addNode(node -> node.id("_:n4").addLabelWithoutLanguage("Node N4 in G2"));
        });
        cjDoc.addGraph(graph -> {
            graph.id("g3-edges");
            graph.addEdge(edge -> {
                edge.addEndpointIncoming("n1"); // doi:abc-n1
                edge.addEndpointOutgoing("n2"); // doi:abc-n2
                edge.addEndpointUndirected("_:n3");
            });
            graph.addEdge(edge -> {
                edge.addEndpointIncoming("https://example.com/n1"); // match
                edge.addEndpointOutgoing("https://example.com/n2"); // match
                edge.addEndpointUndirected("_:n4");
            });
            graph.addEdge(edge -> {
                edge.addEndpointIncoming("doi:abc#n1"); // doi:abc-n1
                edge.addEndpointOutgoing("doi:abc#n2"); // doi:abc-n2
                edge.addEndpointUndirected("_:n5");
            });
        });
        return cjDoc;
    }


    // ========================================================================
    // Test Plan Section 5: Graph Nesting Tests
    // ========================================================================

    /**
     * Test Case 1.1: A minimal valid document, which is an empty JSON object.
     */
    public static ICjDocument minimalDocument() {
        return new CjDocumentElement();
    }

    /**
     * One node with a label and one edge with a label between 'n1' and 'n2'.
     */
    public static ICjDocument nodeAndEdgeWithLabels() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> {
                n.id("n1");
                n.addLabelWithoutLanguage("Node N1");
            });
            graph.addNode(n -> n.id("n2"));
            graph.addEdge(e -> {
                e.addEndpoint(ep -> ep.node("n1").direction(CjDirection.IN));
                e.addEndpoint(ep -> ep.node("n2").direction(CjDirection.OUT));
                e.addLabel("edge n1->n2", "en");
            });
        });
        return cjDoc;
    }


    // ========================================================================
    // Existing Test Data (mapped to test plan where applicable)
    // ========================================================================

    /**
     * Test Case 4.2: A node with hierarchically nested ports.
     */
    public static ICjDocument nodeWithNestedPorts() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> node.id("n1").addPort(p1 -> {
                p1.id("p1");
                p1.addPort(p2 -> p2.id("p1-sub1"));
            }));
        });
        return cjDoc;
    }

    /**
     * Test Case 2.4: Two nodes with the same port ID, which is valid.
     */
    public static ICjDocument nonUniquePortIdInDifferentNodes() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addPort(port -> port.id("p1"));
            });
            graph.addNode(node -> {
                node.id("n2");
                node.addPort(port -> port.id("p1"));
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 4.3: Nodes: 'n1', 'n2'. Edges: (n1,n2).
     */
    public static ICjDocument oneEdge() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> node.id("n1"));
            graph.addNode(node -> node.id("n2"));
            graph.addEdge(edge -> {
                edge.addEndpoint(ep -> ep.node("n1").direction(CjDirection.IN));
                edge.addEndpoint(ep -> ep.node("n2").direction(CjDirection.OUT));
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 4.1: Nodes: 'n1'.
     */
    public static ICjDocument oneNode() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
            });
        });
        return cjDoc;
    }

    /**
     * Node 'n1' with a custom value in its `data` property.
     */
    public static ICjDocument oneNodeWithData() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> {
                n.id("n1");
                n.descriptionPlainText(IJsonFactory.INSTANCE, "This is node n1");
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 3.1: Nodes: 'n1'. With a label.
     */
    public static ICjDocument oneNodeWithLabel() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addLabel("Hello", null);
            });
        });
        return cjDoc;
    }

    /**
     * Test Case 3.2: Nodes: 'n1'. With two labels for different languages.
     */
    public static ICjDocument oneNodeWithLabelAndTwoLanguages() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addLabel("Hello", "en");
                node.addLabel("Hallo", "de");
            });
        });
        return cjDoc;
    }

    /**
     * Nodes: 'n1'. With a label without language.
     */
    public static ICjDocument oneNodeWithLabelWithoutLanguage() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addLabelWithoutLanguage("Hello");
            });
        });
        return cjDoc;
    }

    /**
     * Node 'n1'. One self-loop edge (n1,n1).
     */
    public static ICjDocument selfLoopEdge() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> n.id("n1"));
            graph.addEdge(e -> {
                e.addEndpoint(ep -> ep.node("n1").direction(CjDirection.IN));
                e.addEndpoint(ep -> ep.node("n1").direction(CjDirection.OUT));
            });
        });
        return cjDoc;
    }

    public static Stream<TestDoc> cjTestDocs() {
        return Stream.of(new TestDoc("documentWithBaseUri", documentWithBaseUri()),//
                new TestDoc("documentWithCustomData", documentWithCustomData()),//
                new TestDoc("documentWithMetadata", documentWithMetadata()),//
                new TestDoc("edgeAndEndpointTypePrecedence", edgeAndEndpointTypePrecedence()),//
                new TestDoc("edgeReferencingPort", edgeReferencingPort()),//
                new TestDoc("emptyGraph", emptyGraph()),//
                new TestDoc("endpointTypePropertyPrecedence", endpointTypePropertyPrecedence()),//
                new TestDoc("graphInGraph", graphInGraph()),//
                new TestDoc("graphInNode", graphInNode()),//
                new TestDoc("graphWithLabel", graphWithLabel()),//
                new TestDoc("hyperEdge", hyperEdge()),//
                new TestDoc("minimalDocument", minimalDocument()),//
                new TestDoc("nodeAndEdgeWithLabels", nodeAndEdgeWithLabels()),//
                new TestDoc("nodeWithNestedPorts", nodeWithNestedPorts()),//
                new TestDoc("nonUniquePortIdInDifferentNodes", nonUniquePortIdInDifferentNodes()),//
                new TestDoc("oneEdge", oneEdge()),//
                new TestDoc("oneNode", oneNode()),//
                new TestDoc("oneNodeWithData", oneNodeWithData()),//
                new TestDoc("oneNodeWithLabel", oneNodeWithLabel()),//
                new TestDoc("oneNodeWithLabelAndTwoLanguages", oneNodeWithLabelAndTwoLanguages()),//
                new TestDoc("oneNodeWithLabelWithoutLanguage", oneNodeWithLabelWithoutLanguage()),//
                new TestDoc("selfLoopEdge", selfLoopEdge()),//
                new TestDoc("twoEdgesSameNodes", twoEdgesSameNodes()),//
                new TestDoc("twoNodesNoEdge", twoNodesNoEdge()),//
                new TestDoc("nodeWithTypes", nodeWithTypes()),//
                new TestDoc("ids and uris", idVsUri())//
        );
    }

    /**
     * Nodes: 'n1', 'n2'. Two edges from n1 to n2.
     */
    public static ICjDocument twoEdgesSameNodes() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> n.id("n1"));
            graph.addNode(n -> n.id("n2"));
            graph.addEdge(e -> {
                e.addEndpoint(ep -> ep.node("n1").direction(CjDirection.IN));
                e.addEndpoint(ep -> ep.node("n2").direction(CjDirection.OUT));
            });
            graph.addEdge(e -> {
                e.addEndpoint(ep -> ep.node("n1").direction(CjDirection.IN));
                e.addEndpoint(ep -> ep.node("n2").direction(CjDirection.OUT));
            });
        });
        return cjDoc;
    }

    /**
     * Nodes: 'n1', 'n2'. No edges.
     */
    public static ICjDocument twoNodesNoEdge() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(n -> n.id("n1"));
            graph.addNode(n -> n.id("n2"));
        });
        return cjDoc;
    }

    /**
     * Test Case: A node with types. Node types are exactly like edge types.
     */
    public static ICjDocument nodeWithTypes() {
        ICjDocumentMutable cjDoc = new CjDocumentElement();
        cjDoc.addGraph(graph -> {
            graph.addNode(node -> {
                node.id("n1");
                node.addType(ICjElementType.of("Person"));
                node.addType(ICjElementType.of("Employee"));
            });
        });
        return cjDoc;
    }

}
