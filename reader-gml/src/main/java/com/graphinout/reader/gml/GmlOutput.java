package com.graphinout.reader.gml;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.foundation.json.value.IJsonValue;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class GmlOutput {

    private static final Logger log = getLogger(GmlOutput.class);
    private final ICjDocument cjDoc;

    public GmlOutput(ICjDocument cjDoc) {
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

    public String toGml() {
        StringBuilder b = new StringBuilder();
        b.append("graph [\n");
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (ICjGraph g : graphs) {
            // Nodes section
            g.nodes().forEach(n -> {
                String id = n.id();
                if (id == null) return; // skip nodes without id
                b.append("  node [\n");
                b.append("    id ").append(id).append("\n");
                String text = firstLabelOrDesc(n, n.labelEntries());
                if (text != null && !text.isEmpty()) {
                    b.append("    label \"").append(text).append("\"\n");
                }
                b.append("  ]\n");
            });
            // Edges section
            g.edges().forEach(e -> {
                ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
                ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
                String n1 = null, n2 = null;
                if (outEp != null && inEp != null) {
                    // directed edge
                    n1 = outEp.node();
                    n2 = inEp.node();
                } else {
                    // undirected edge or hyper-edge
                    List<ICjEndpoint> eps = e.endpoints().toList();
                    if (eps.size() == 2) {
                        n1 = eps.get(0).node();
                        n2 = eps.get(1).node();
                    } else {
                        log.warn("Cannot represent hyper-edge in GML");
                    }
                }
                if (n1 != null && n2 != null) {
                    b.append("  edge [\n");
                    b.append("    source ").append(n1).append("\n");
                    b.append("    target ").append(n2).append("\n");
                    String text = firstLabelOrDesc(e, e.labelEntries());
                    if (text != null && !text.isEmpty()) {
                        b.append("    label \"").append(text).append("\"\n");
                    }
                    b.append("  ]\n");
                }
            });
        }
        b.append("]\n");
        return b.toString();
    }

}
