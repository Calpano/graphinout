package com.graphinout.reader.cjgef;

import com.graphinout.base.cj.analyze.CjAnalysis;
import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.analyze.CjFeature;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import com.graphinout.foundation.pure.input.ContentError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** Unit tests for the GEF reader: each GEF leniency must round-trip through the CJ model. */
class CjGefReaderTest {

    private static CjAnalysis analyze(String gef) throws IOException {
        ICjDocument doc = new CjGefReader().readToCjDocument(SingleInputSource.of("t.gef.json", gef));
        return CjAnalyzer.analyze(doc);
    }

    @Test
    void graphAtRootWithSourceTarget() throws IOException {
        // top-level nodes/edges (no graphs wrapper), numeric ids, source/target shortcut
        CjAnalysis a = analyze("""
                { "nodes": [ { "id": 1 }, { "id": 2 } ],
                  "edges": [ { "source": 1, "target": 2 } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.edgeCount()).isEqualTo(1);
        assertThat(a.features()).contains(CjFeature.DIRECTED_EDGES);
    }

    @Test
    void sourceTargetMapToInOutEndpoints() {
        // GEF spec: source → direction "in", target → direction "out" (seen from the edge), see issue #156
        String cj = Gef.toConnectedJson("""
                { "edges": [ { "source": "Alice", "target": "Bob", "type": "knows" },
                             { "sources": [ "a", "b" ], "targets": [ "c" ] } ] }
                """);
        assertThat(cj).contains("""
                [{"direction":"in","node":"Alice"},{"direction":"out","node":"Bob"}]""");
        assertThat(cj).contains("""
                [{"direction":"in","node":"a"},{"direction":"in","node":"b"},{"direction":"out","node":"c"}]""");
    }

    @Test
    void stringAndMultilangLabelShorthand() throws IOException {
        CjAnalysis a = analyze("""
                { "nodes": [
                    { "id": "a", "label": "simple label" },
                    { "id": "b", "label": [ { "language": "de", "value": "Sonne" },
                                            { "language": "en", "value": "Sun" } ] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.features()).contains(CjFeature.NODE_LABELS);
    }

    @Test
    void unknownKeysBecomeNodeData() throws IOException {
        CjAnalysis a = analyze("""
                { "nodes": [ { "id": "b", "foo": "bar", "hello": [1, 2, 3] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(1);
        assertThat(a.features()).contains(CjFeature.ATTRIBUTES_ON_NODES);
    }

    @Test
    void hyperedgeWithMixedDirections() throws IOException {
        // n-ary endpoints incl. an undirected one — exercises hyperedges + mixed directionality
        CjAnalysis a = analyze("""
                { "nodes": [ { "id": 1 }, { "id": "a" }, { "id": "d" }, { "id": "f" } ],
                  "edges": [ { "endpoints": [
                      { "direction": "in",  "node": 1 },
                      { "direction": "in",  "node": "a" },
                      { "direction": "out", "node": "d" },
                      { "direction": "undir", "node": "f" } ] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(4);
        assertThat(a.features()).contains(CjFeature.HYPEREDGES);
    }

    @Test
    void validConnectedJsonStillParses() throws IOException {
        // a strict CJ document (graphs wrapper) must pass through unchanged
        CjAnalysis a = analyze("""
                { "graphs": [ { "id": "g", "nodes": [ { "id": "n1" }, { "id": "n2" } ],
                    "edges": [ { "endpoints": [ { "direction": "out", "node": "n1" },
                                                { "direction": "in", "node": "n2" } ] } ] } ] }
                """);
        assertThat(a.nodeCount()).isEqualTo(2);
        assertThat(a.edgeCount()).isEqualTo(1);
    }

    private static List<String> warnings(String gef) throws IOException {
        List<ContentError> errors = new ArrayList<>();
        CjGefReader reader = new CjGefReader();
        reader.setContentErrorHandler(errors::add);
        reader.readToCjDocument(SingleInputSource.of("t.gef.json", gef));
        return errors.stream().map(ContentError::getMessage).toList();
    }

    /** Paths are in the GEF input: graph-at-root input has no {@code graphs[0]}. */
    @Test
    void unknownKeysAreReportedWithPath() throws IOException {
        List<String> w = warnings("""
                { "baseUri": "x",
                  "nodes": [ { "id": "a" }, { "id": "b", "foo": "bar" } ],
                  "edges": [ { "source": "a", "target": "b", "weight": 3 },
                             { "endpoints": [ { "node": "a", "role": "x" } ] } ] }
                """);
        assertThat(w).containsExactly( //
                "Unknown key 'foo' at nodes[1].foo moved to data", //
                "Unknown key 'weight' at edges[0].weight moved to data", //
                "Unknown key 'role' at edges[1].endpoints[0].role moved to data", //
                "Unknown key 'baseUri' at baseUri moved to data");
    }

    @Test
    void unknownKeyNextToNonObjectDataIsReportedAsDropped() throws IOException {
        List<String> w = warnings("""
                { "graphs": [ { "nodes": [ { "id": "a", "data": "s", "foo": 1 } ] } ] }
                """);
        assertThat(w).containsExactly("Unknown key 'foo' at graphs[0].nodes[0].foo dropped: 'data' is not an object");
    }

    @Test
    void cleanGefHasNoWarnings() throws IOException {
        assertThat(warnings("""
                { "nodes": [ { "id": 1, "label": "x", "data": { "a": 1 } } ], "edges": [ { "source": 1, "target": 1 } ] }
                """)).isEmpty();
    }
}
