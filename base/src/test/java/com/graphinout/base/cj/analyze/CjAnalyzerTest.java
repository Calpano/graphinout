package com.graphinout.base.cj.analyze;

import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.base.cj.document.ICjDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CjAnalyzerTest {

    // one graph: 3 nodes (a has a label + data, c is a standalone non-endpoint node), 1 directed edge a->b
    private static final String CJ = """
            {
              "$schema": "https://j-s-o-n.org/schema/cj-8.0.0.json",
              "graphs": [
                {
                  "id": "g",
                  "nodes": [
                    { "id": "a", "label": { "entries": [ { "value": "Alice" } ] }, "data": { "color": "red" } },
                    { "id": "b" },
                    { "id": "c" }
                  ],
                  "edges": [
                    { "id": "e", "endpoints": [ { "node": "a", "direction": "out" }, { "node": "b", "direction": "in" } ] }
                  ]
                }
              ]
            }
            """;

    @Test
    void countsAndDetectedFeatures() throws Exception {
        ICjDocument doc = CjDocuments.parseCjJsonString("CjAnalyzerTest", CJ);
        CjAnalysis a = CjAnalyzer.analyze(doc);

        assertEquals(1L, a.graphCount());
        assertEquals(3L, a.nodeCount());
        assertEquals(1L, a.edgeCount());

        // a standalone node (c), a directed edge, a node label and node data are present; nothing else
        assertEquals(List.of("attributes-on-nodes", "directed-edges", "node-labels", "nodes"), a.featureSlugs());

        assertTrue(a.features().contains(CjFeature.DIRECTED_EDGES));
        assertFalse(a.features().contains(CjFeature.HYPEREDGES));
        assertFalse(a.features().contains(CjFeature.UNDIRECTED_EDGES));
        assertFalse(a.features().contains(CjFeature.EDGE_LABELS));
    }
}
