package com.graphinout.reader.tgf;

import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.cj.element.ICjDocument;
import com.graphinout.base.cj.element.ICjEndpoint;
import com.graphinout.base.cj.element.ICjGraph;
import com.graphinout.base.cj.element.ICjHasData;
import com.graphinout.base.cj.element.ICjLabelEntry;
import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.foundation.json.value.IJsonValue;
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
            IJsonValue desc = json.resolve(CjGraphmlMapping.CjDataProperty.Description.cjPropertyKey);
            if (desc != null) {
                String s = desc.toXmlFragmentString().rawXml();
                if (s != null && !s.isEmpty()) return s;
            }
        }
        return null;
    }

    public String toTgf() {
        StringBuilder b = new StringBuilder();
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (int gi = 0; gi < graphs.size(); gi++) {
            ICjGraph g = graphs.get(gi);
            // Nodes section
            g.nodes().forEach(n -> {
                String id = n.id();
                if (id == null) return; // skip nodes without id
                b.append(id);
                String text = firstLabelOrDesc(n, n.labelEntries());
                if (text != null && !text.isEmpty()) {
                    b.append(" ").append(text);
                }
                b.append('\n');
            });
            // Separator between nodes and edges
            b.append('#').append('\n');
            // Edges section
            g.edges().forEach(e -> {
                // edge from subject to object is: subject=IN, object=OUT
                ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
                ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
                String n1 = null, n2 = null;
                if (outEp != null && inEp != null) {
                    // directed edge
                    n1 = inEp.node();
                    n2 = outEp.node();
                } else {
                    // undirected edge or hyper-edge
                    List<ICjEndpoint> eps = e.endpoints().toList();
                    if (eps.size() == 2) {
                        n1 = eps.get(0).node();
                        n2 = eps.get(1).node();
                    } else {
                        log.warn("Cannot represent hyper-edge in TGF");
                    }
                }
                if (n1 != null && n2 != null) {
                    b.append(n1).append(' ').append(n2);
                    String text = firstLabelOrDesc(e, e.labelEntries());
                    if (text != null && !text.isEmpty()) {
                        b.append(' ').append(text);
                    }
                    b.append('\n');
                }
            });
            // Separate graphs with an empty line (only if more than one graph)
            if (gi < graphs.size() - 1) {
                b.append('\n');
            }
        }
        return b.toString();
    }

}
