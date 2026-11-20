package com.graphinout.reader.graphml;

import com.graphinout.base.cj.analyze.CjAnalyzer;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.input.SingleInputSource;
import com.graphinout.foundation.pure.input.ContentError;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * An edge or hyperedge endpoint that references an undeclared node is an Error with the element's position. GraphML
 * ids are document-wide, so an edge may come before its nodes or reference a node of a nested graph.
 */
class GraphmlDanglingEdgeTest {

    private final List<ContentError> errors = new ArrayList<>();

    private ICjDocument read(String graphml) throws IOException {
        GraphmlReader reader = new GraphmlReader();
        reader.setContentErrorHandler(errors::add);
        return reader.readToCjDocument(SingleInputSource.of("test.graphml", graphml));
    }

    private List<String> errorMessages() {
        return errors.stream().filter(e -> e.getLevel() == ContentError.ErrorLevel.Error).map(ContentError::getMessage).toList();
    }

    @Test
    void edgeToUndeclaredNodeIsAnErrorWithPosition() throws IOException {
        ICjDocument doc = read("""
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph edgedefault="directed">
                    <node id="n0"/>
                    <edge id="e1" source="n0" target="n4"/>
                  </graph>
                </graphml>
                """);
        assertThat(errorMessages()).containsExactly("Edge 'e1' references a non-existent node ID: 'n4'");
        assertThat(errors.getFirst().getLocation().line()).isEqualTo(4);
        // the edge is still read
        assertThat(CjAnalyzer.analyze(doc).edgeCount()).isEqualTo(1);
    }

    @Test
    void hyperedgeEndpointToUndeclaredNodeIsAnError() throws IOException {
        read("""
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph edgedefault="undirected">
                    <node id="a"/>
                    <hyperedge><endpoint node="a"/><endpoint node="zzz"/></hyperedge>
                  </graph>
                </graphml>
                """);
        assertThat(errorMessages()).containsExactly("Hyperedge endpoint references a non-existent node ID: 'zzz'");
    }

    @Test
    void edgeBeforeNodesAndAcrossNestedGraphsIsFine() throws IOException {
        read("""
                <graphml xmlns="http://graphml.graphdrawing.org/xmlns">
                  <graph edgedefault="directed">
                    <edge source="outer" target="inner"/>
                    <node id="outer">
                      <graph edgedefault="directed"><node id="inner"/></graph>
                    </node>
                  </graph>
                </graphml>
                """);
        assertThat(errorMessages()).isEmpty();
    }
}
