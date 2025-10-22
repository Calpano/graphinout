package com.graphinout.reader.tgf;

import com.graphinout.base.cj.element.*;
import com.graphinout.base.cj.CjDirection;
import com.graphinout.base.graphml.CjGraphmlMapping;
import com.graphinout.foundation.json.value.IJsonValue;

import java.util.List;

public class TgfOutput {

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
                // Prefer OUT endpoint first, then IN (CJ sorts endpoints; using directions preserves TGF order)
                ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
                ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
                String n1 = null, n2 = null;
                if (outEp != null && inEp != null) {
                    n1 = outEp.node();
                    n2 = inEp.node();
                } else {
                    List<ICjEndpoint> eps = e.endpoints().toList();
                    if (eps.size() >= 2) {
                        n1 = eps.get(0).node();
                        n2 = eps.get(1).node();
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
