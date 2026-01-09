package com.graphinout.reader.tgf;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class TgfOutput {

    private static final Logger log = getLogger(TgfOutput.class);
    private final ICjDocument cjDoc;

    public TgfOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    private static String firstLabelOrDesc(ICjHasData hasData, List<ICjLabelEntry> labels) {
        // prefer label if present
        if (!labels.isEmpty()) {
            String val = labels.getFirst().value();
            if (val != null && !val.isEmpty()) return val;
        }
        // fallback to CJ data description
        IJsonValue json = hasData.jsonValue();
        if (json != null) {
            IJsonValue desc = json.resolve(CjDataProperty.Description.cjPropertyKey);
            if (desc != null) {
                String s = desc.toXmlFragmentString().rawXml();
                if (s != null && !s.isEmpty()) return s;
            }
        }
        return null;
    }

    public String toTgf() {
        StringBuilder nodeSection = new StringBuilder();
        StringBuilder edgeSection = new StringBuilder();

        // Collect all nodes and edges from all graphs (flattened)
        collectFromGraphs(cjDoc.graphs(), nodeSection, edgeSection);

        // Build final TGF output
        StringBuilder b = new StringBuilder();
        b.append(nodeSection);
        b.append('#').append('\n');
        b.append(edgeSection);
        return b.toString();
    }

    private void collectFromGraphs(java.util.stream.Stream<ICjGraph> graphs, StringBuilder nodeSection, StringBuilder edgeSection) {
        graphs.forEach(g -> collectFromGraph(g, nodeSection, edgeSection));
    }

    private void collectFromGraph(ICjGraph g, StringBuilder nodeSection, StringBuilder edgeSection) {
        // Collect nodes (and recursively their nested graphs)
        g.nodes().forEach(n -> {
            String id = n.id();
            if (id != null) {
                nodeSection.append(id);
                String text = firstLabelOrDesc(n, n.labelEntries());
                if (text != null && !text.isEmpty()) {
                    nodeSection.append(" ").append(text);
                }
                nodeSection.append('\n');
            }
            // Collect from nested graphs in nodes
            n.graphs().forEach(ng -> collectFromGraph(ng, nodeSection, edgeSection));
        });

        // Collect edges (and recursively their nested graphs)
        g.edges().forEach(e -> {
            ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
            ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
            String n1 = null, n2 = null;
            if (outEp != null && inEp != null) {
                n1 = inEp.node();
                n2 = outEp.node();
            } else {
                List<ICjEndpoint> eps = e.endpoints().toList();
                if (eps.size() == 2) {
                    n1 = eps.get(0).node();
                    n2 = eps.get(1).node();
                } else {
                    log.warn("Cannot represent hyper-edge in TGF");
                }
            }
            if (n1 != null && n2 != null) {
                edgeSection.append(n1).append(' ').append(n2);
                String text = firstLabelOrDesc(e, e.labelEntries());
                if (text != null && !text.isEmpty()) {
                    edgeSection.append(' ').append(text);
                }
                edgeSection.append('\n');
            }
            // Collect from nested graphs in edges
            e.graphs().forEach(ng -> collectFromGraph(ng, nodeSection, edgeSection));
        });

        // Collect from nested graphs in this graph
        g.graphs().forEach(ng -> collectFromGraph(ng, nodeSection, edgeSection));
    }

}
