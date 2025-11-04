package com.graphinout.reader.gml;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjNode;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.slf4j.LoggerFactory.getLogger;

public record GmlOutput(ICjDocument cjDoc) {

    private static final Logger log = getLogger(GmlOutput.class);

    public String toGml() {
        StringBuilder b = new StringBuilder();
        b.append("graph [\n");
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (ICjGraph g : graphs) {
            // Nodes section
            g.nodes().sorted(Comparator.comparing(ICjNode::id)).forEach(cjNode -> {
                String id = cjNode.id();
                if (id == null) {
                    log.warn("Skip node without id");
                    return;
                }

                Map<String, String> gmlAttributes = new TreeMap<>();
                gmlAttributes.put("id", id);
                cjNode.labelEntries().stream().findFirst().ifPresent(label -> //
                        gmlAttributes.put("label", "\"" + label.value() + "\""));

                b.append("  node [\n");
                gmlAttributes.forEach((key, value) -> b.append("    ").append(key).append(" ").append(value).append("\n"));
                b.append("  ]\n");
            });

            // Edges section
            g.edges().forEach(e -> {
                Map<String, String> attributes = new TreeMap<>();
                ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
                ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);

                if (outEp != null && inEp != null) {
                    attributes.put("source", outEp.node());
                    attributes.put("target", inEp.node());
                } else {
                    List<ICjEndpoint> eps = e.endpoints().toList();
                    if (eps.size() == 2) {
                        attributes.put("source", eps.get(0).node());
                        attributes.put("target", eps.get(1).node());
                    } else {
                        log.warn("Cannot represent hyper-edge in GML");
                        return;
                    }
                }

                e.labelEntries().stream().findFirst().ifPresent(label -> attributes.put("label", "\"" + label.value() + "\""));

                b.append("  edge [\n");
                attributes.forEach((key, value) -> b.append("    ").append(key).append(" ").append(value).append("\n"));
                b.append("  ]\n");
            });
        }
        b.append("]\n");
        return b.toString();
    }

}
