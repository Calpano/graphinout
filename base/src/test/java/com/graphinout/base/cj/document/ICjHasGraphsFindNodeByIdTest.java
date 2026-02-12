package com.graphinout.base.cj.document;

import com.graphinout.base.cj.document.impl.CjDocumentElement;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link ICjHasGraphs#findNodeById(String)} with various graph/node nesting structures.
 */
class ICjHasGraphsFindNodeByIdTest {

    @Test
    void emptyDocument_returnsNull() {
        ICjDocument doc = new CjDocumentElement();
        assertThat(doc.findNodeById("n1")).isNull();
    }

    @Test
    void emptyGraph_returnsNull() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> {});
        assertThat(doc.findNodeById("n1")).isNull();
    }

    @Test
    void singleGraphSingleNode() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> g.addNode(n -> n.id("n1")));

        ICjNode found = doc.findNodeById("n1");
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo("n1");
    }

    @Test
    void singleGraphMultipleNodes() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.addNode(n -> n.id("n1"));
            g.addNode(n -> n.id("n2"));
            g.addNode(n -> n.id("n3"));
        });

        assertThat(doc.findNodeById("n1")).isNotNull();
        assertThat(doc.findNodeById("n2")).isNotNull();
        assertThat(doc.findNodeById("n3")).isNotNull();
        assertThat(doc.findNodeById("n4")).isNull();
    }

    @Test
    void multipleGraphsAtDocumentLevel() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.id("g1");
            g.addNode(n -> n.id("a"));
        });
        doc.addGraph(g -> {
            g.id("g2");
            g.addNode(n -> n.id("b"));
        });

        assertThat(doc.findNodeById("a")).isNotNull();
        assertThat(doc.findNodeById("a").id()).isEqualTo("a");
        assertThat(doc.findNodeById("b")).isNotNull();
        assertThat(doc.findNodeById("b").id()).isEqualTo("b");
        assertThat(doc.findNodeById("c")).isNull();
    }

    @Test
    void nodeInSubgraph_graphInGraph() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g1 -> {
            g1.id("g1");
            g1.addNode(n -> n.id("n1"));
            g1.addGraph(g2 -> {
                g2.id("g2");
                g2.addNode(n -> n.id("n2"));
            });
        });

        assertThat(doc.findNodeById("n1")).isNotNull();
        assertThat(doc.findNodeById("n2")).isNotNull();
        assertThat(doc.findNodeById("n2").id()).isEqualTo("n2");
    }

    @Test
    void nodeInSubgraph_graphInNode_compoundNode() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.addNode(n1 -> {
                n1.id("n1");
                n1.addGraph(sub -> sub.addNode(n2 -> n2.id("n2")));
            });
        });

        assertThat(doc.findNodeById("n1")).isNotNull();
        assertThat(doc.findNodeById("n2")).isNotNull();
        assertThat(doc.findNodeById("n2").id()).isEqualTo("n2");
    }

    @Test
    void nodeInSubgraph_graphInEdge() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.addNode(n -> n.id("n1"));
            g.addNode(n -> n.id("n2"));
            g.addEdge(e -> {
                e.id("e1");
                e.addEndpoint(ep -> ep.node("n1"));
                e.addEndpoint(ep -> ep.node("n2"));
                e.addGraph(sub -> sub.addNode(n -> n.id("n3")));
            });
        });

        assertThat(doc.findNodeById("n1")).isNotNull();
        assertThat(doc.findNodeById("n2")).isNotNull();
        assertThat(doc.findNodeById("n3")).isNotNull();
        assertThat(doc.findNodeById("n3").id()).isEqualTo("n3");
    }

    @Test
    void mixedNesting_graphInGraphAndGraphInNode() {
        // doc -> g1 -> [n1, subG -> [n2], compoundNode(n3) -> subG2 -> [n4]]
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g1 -> {
            g1.id("g1");
            g1.addNode(n -> n.id("n1"));
            g1.addGraph(subG -> {
                subG.id("subG");
                subG.addNode(n -> n.id("n2"));
            });
            g1.addNode(n3 -> {
                n3.id("n3");
                n3.addGraph(subG2 -> {
                    subG2.id("subG2");
                    subG2.addNode(n -> n.id("n4"));
                });
            });
        });

        assertThat(doc.findNodeById("n1")).isNotNull();
        assertThat(doc.findNodeById("n2")).isNotNull();
        assertThat(doc.findNodeById("n3")).isNotNull();
        assertThat(doc.findNodeById("n4")).isNotNull();
        assertThat(doc.findNodeById("missing")).isNull();
    }

    @Test
    void findNodeById_onGraphDirectly() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g1 -> {
            g1.id("g1");
            g1.addNode(n -> n.id("n1"));
            g1.addGraph(sub -> {
                sub.id("sub");
                sub.addNode(n -> n.id("n-in-sub"));
            });
        });
        doc.addGraph(g2 -> {
            g2.id("g2");
            g2.addNode(n -> n.id("n2"));
        });

        ICjGraph g1 = doc.findGraphById("g1");
        assertThat(g1).isNotNull();
        // direct nodes of g1 are found
        assertThat(g1.findNodeById("n1")).isNotNull();
        // nodes in subgraphs are also found
        assertThat(g1.findNodeById("n-in-sub")).isNotNull();
        // nodes from a different top-level graph are NOT found
        assertThat(g1.findNodeById("n2")).isNull();
    }

    @Test
    void findNodeById_onCompoundNode() {
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.addNode(n1 -> {
                n1.id("n1");
                n1.addGraph(sub -> {
                    sub.addNode(n -> n.id("inner"));
                });
            });
            g.addNode(n -> n.id("n2"));
        });

        // The compound node n1 implements ICjHasGraphs, so findNodeById should work on it
        ICjNode n1 = doc.findNodeById("n1");
        assertThat(n1).isNotNull();
        assertThat(n1.findNodeById("inner")).isNotNull();
        assertThat(n1.findNodeById("inner").id()).isEqualTo("inner");
        assertThat(n1.findNodeById("n2")).isNull();
    }

    @Test
    void multipleDocumentGraphs_withSubgraphsAndCompoundNodes() {
        // Complex scenario: two top-level graphs, each with subgraphs and compound nodes
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g1 -> {
            g1.id("g1");
            g1.addNode(n -> n.id("g1-n1"));
            g1.addGraph(sub -> {
                sub.id("g1-sub");
                sub.addNode(n -> n.id("g1-sub-n1"));
            });
        });
        doc.addGraph(g2 -> {
            g2.id("g2");
            g2.addNode(compound -> {
                compound.id("g2-compound");
                compound.addGraph(sub -> {
                    sub.id("g2-compound-sub");
                    sub.addNode(n -> n.id("g2-compound-sub-n1"));
                });
            });
        });

        assertThat(doc.findNodeById("g1-n1")).isNotNull();
        assertThat(doc.findNodeById("g1-sub-n1")).isNotNull();
        assertThat(doc.findNodeById("g2-compound")).isNotNull();
        assertThat(doc.findNodeById("g2-compound-sub-n1")).isNotNull();
        assertThat(doc.findNodeById("nonexistent")).isNull();
    }

    @Test
    void nodeInGraphInEdgeInGraph() {
        // Deeper nesting: doc -> g -> edge -> subGraph -> node
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g -> {
            g.addNode(n -> n.id("n1"));
            g.addNode(n -> n.id("n2"));
            g.addEdge(e -> {
                e.addEndpoint(ep -> ep.node("n1"));
                e.addEndpoint(ep -> ep.node("n2"));
                e.addGraph(edgeSub -> {
                    edgeSub.id("edge-sub");
                    edgeSub.addNode(n -> n.id("edge-sub-node"));
                });
            });
        });

        assertThat(doc.findNodeById("n1")).isNotNull();
        assertThat(doc.findNodeById("n2")).isNotNull();
        assertThat(doc.findNodeById("edge-sub-node")).isNotNull();
    }

    @Test
    void nodeWithSameIdInDifferentGraphs_findsFirst() {
        // When nodes have the same ID across graphs, findNodeById returns the first one encountered
        ICjDocumentMutable doc = new CjDocumentElement();
        doc.addGraph(g1 -> {
            g1.id("g1");
            g1.addNode(n -> {
                n.id("shared");
                n.addLabelWithoutLanguage("First");
            });
        });
        doc.addGraph(g2 -> {
            g2.id("g2");
            g2.addNode(n -> {
                n.id("shared");
                n.addLabelWithoutLanguage("Second");
            });
        });

        ICjNode found = doc.findNodeById("shared");
        assertThat(found).isNotNull();
        assertThat(found.id()).isEqualTo("shared");
        // Should find the first occurrence
        assertThat(found.label_().theEntry().value()).isEqualTo("First");
    }
}
