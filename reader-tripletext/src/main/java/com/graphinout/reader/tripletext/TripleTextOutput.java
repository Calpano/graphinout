package com.graphinout.reader.tripletext;

import com.graphinout.base.cj.element.*;

import java.util.List;

/**
 * Renders a CjDocument into TripleText syntax.
 * Each edge becomes a line: "subject -- predicate -- object".
 * Additionally, node labels are emitted as triples: "nodeId -- label -- labelValue".
 */
public class TripleTextOutput {

    private final ICjDocument cjDoc;

    public TripleTextOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    public String toTripleText() {
        StringBuilder b = new StringBuilder();
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (int gi = 0; gi < graphs.size(); gi++) {
            ICjGraph g = graphs.get(gi);

            // First: emit node label triples (nodeId -- label -- labelValue)
            g.nodes().forEach(n -> {
                String id = n.id();
                if (id == null || id.isEmpty()) return;
                List<ICjLabelEntry> labels = n.labelEntries();
                if (!labels.isEmpty()) {
                    String val = labels.getFirst().value();
                    if (val != null && !val.isEmpty()) {
                        appendTripleLine(b, id, "label", val);
                    }
                }
            });

            // Then: emit edge triples (subject -- predicate -- object)
            g.edges().forEach(e -> {
                String predicate = null;
                List<ICjLabelEntry> labels = e.labelEntries();
                if (!labels.isEmpty()) {
                    String val = labels.getFirst().value();
                    if (val != null && !val.isEmpty()) predicate = val;
                }
                if (predicate == null) return; // cannot form a triple without predicate

                String subject = null;
                String object = null;
                try {
                    // Prefer OUT endpoint as subject and IN as object (mirrors TgfOutput)
                    ICjEndpoint outEp = e.endpoints().filter(ep -> ep.direction() == com.graphinout.base.cj.CjDirection.OUT).findFirst().orElse(null);
                    ICjEndpoint inEp = e.endpoints().filter(ep -> ep.direction() == com.graphinout.base.cj.CjDirection.IN).findFirst().orElse(null);
                    if (outEp != null && inEp != null) {
                        subject = outEp.node();
                        object = inEp.node();
                    } else {
                        List<ICjEndpoint> eps = e.endpoints().toList();
                        if (eps.size() >= 2) {
                            subject = eps.get(0).node();
                            object = eps.get(1).node();
                        }
                    }
                } catch (IllegalStateException ignore) {
                    // multiple sources/targets: skip emitting a malformed triple
                }

                if (subject != null && object != null) {
                    appendTripleLine(b, subject, predicate, object);
                }
            });

            // Separate graphs with a blank line if there are multiple graphs
            if (gi < graphs.size() - 1) {
                b.append('\n');
            }
        }
        return b.toString();
    }

    private static void appendTripleLine(StringBuilder b, String s, String p, String o) {
        b.append(s).append(" -- ").append(p).append(" -- ").append(o).append('\n');
    }
}
