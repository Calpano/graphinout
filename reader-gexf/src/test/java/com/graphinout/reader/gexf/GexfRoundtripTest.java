package com.graphinout.reader.gexf;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.stream.CjStream2CjWriter;
import com.graphinout.base.cj.writer.CjWriter2CjDocumentWriter;
import com.graphinout.base.input.SingleInputSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Round-trips GEXF: read -> CjDocument -> {@link GexfWriter} -> read again, asserting that nodes (with labels) and
 * directed edges survive, and that a second serialization equals the first (stable output).
 */
class GexfRoundtripTest {

    private static final String INPUT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gexf xmlns="http://www.gexf.net/1.2draft" version="1.2">
                <graph defaultedgetype="directed">
                    <nodes>
                        <node id="0" label="Hello" />
                        <node id="1" label="World" />
                    </nodes>
                    <edges>
                        <edge source="0" target="1" />
                    </edges>
                </graph>
            </gexf>
            """;

    @Test
    void roundtripGexf() throws IOException {
        ICjDocument doc1 = read(INPUT);
        String out1 = GexfWriter.toGexf(doc1);
        ICjDocument doc2 = read(out1);
        String out2 = GexfWriter.toGexf(doc2);

        assertEquals(out1, out2, "second serialization should equal the first");
        assertEquals(structure(doc1), structure(doc2), "graph structure should survive the round-trip");
        // sanity: the two nodes, their labels, and the directed edge are present
        assertEquals("nodes=[0:Hello, 1:World] edges=[0->1]", structure(doc1));
    }

    private static ICjDocument read(String content) throws IOException {
        GexfReader reader = new GexfReader();
        CjWriter2CjDocumentWriter docWriter = new CjWriter2CjDocumentWriter();
        reader.read(SingleInputSource.of("test.gexf", content), new CjStream2CjWriter(docWriter, true));
        return docWriter.resultDoc();
    }

    private static String structure(ICjDocument doc) {
        TreeSet<String> nodes = new TreeSet<>();
        doc.nodesAllIncludingImplied().forEach(n -> {
            String label = n.labelEntries().isEmpty() ? "" : ":" + n.labelEntries().getFirst().value();
            nodes.add(n.id() + label);
        });
        List<String> edges = new ArrayList<>();
        doc.edgesAll().forEach(e -> {
            List<ICjEndpoint> eps = e.endpoints().toList();
            ICjEndpoint in = eps.stream().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
            ICjEndpoint out = eps.stream().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
            if (in != null && out != null) {
                edges.add(in.node() + "->" + out.node());
            } else if (eps.size() == 2) {
                edges.add(eps.get(0).node() + "->" + eps.get(1).node());
            }
        });
        edges.sort(String::compareTo);
        return "nodes=" + nodes + " edges=" + edges;
    }

}
