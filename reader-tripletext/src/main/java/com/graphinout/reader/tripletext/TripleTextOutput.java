package com.graphinout.reader.tripletext;

import com.graphinout.base.cj.document.CjDirection;
import com.graphinout.base.cj.document.ICjEdgeType;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.util.Nullables;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Renders a CjDocument into TripleText syntax. Each edge becomes a line: "subject -- predicate -- object".
 * Additionally, node labels are emitted as triples: "nodeId -- label -- labelValue".
 */
public class TripleTextOutput {

    private static final Logger log = getLogger(TripleTextOutput.class);
    private final ICjDocument cjDoc;

    public TripleTextOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    private static void appendTripleLine(StringBuilder b, String s, String p, boolean isDirected, String o, @Nullable String meta) {
        b.append(s);
        if (isDirected) {
            b.append(" -- ").append(p).append(" -> ").append(o);
        } else {
            b.append(" -- ").append(p).append(" -- ").append(o);
        }
        if (meta != null) {
            b.append(" .. ").append(meta);
        }
        b.append('\n');
    }

    public static void toTripleText(ICjGraph graph, StringBuilder b) {
        // First: emit node label triples (nodeId -- label -- labelValue)
        graph.nodes().forEach(n -> toTripleText(n, b));

        // Then: emit edge triples (subject -- predicate -- object)
        graph.edges().forEach(e -> toTripleText(e, b));
    }

    private static void toTripleText(ICjEdge e, StringBuilder b) {
        if (e.endpoints().count() != 2) {
            log.warn("TripleText cannot represent hyper-edges, skipping");
            return;
        }
        // edge from subject to object is: subject=IN, object=OUT
        List<ICjEndpoint> eps = e.endpoints().toList();
        ICjEndpoint s = eps.get(0);
        ICjEndpoint o = eps.get(1);
        if (s.direction() == CjDirection.OUT && o.direction() == CjDirection.IN) {
            s = eps.get(1);
            o = eps.get(0);
        }
        boolean isDirected = s.isDirected() || o.isDirected();
        String predicate = Nullables.mapOrDefault(e.edgeType(), ICjEdgeType::type, "related");
        appendTripleLine(b, s.node(), predicate, isDirected, o.node(), null);
    }

    private static void toTripleText(ICjNode n, StringBuilder b) {
        String id = n.id();
        if (id == null || id.isEmpty()) return;
        // TODO is there a better way to represent a node without a label in tripleText?
        for (ICjLabelEntry labelEntry : n.labelEntries()) {
            String val = labelEntry.value();
            String lang = labelEntry.language();
            appendTripleLine(b, id, "label", true, val, lang);
        }
    }

    public String toTripleText() {
        StringBuilder b = new StringBuilder();
        List<ICjGraph> graphs = cjDoc.graphs().toList();
        for (int gi = 0; gi < graphs.size(); gi++) {
            ICjGraph g = graphs.get(gi);
            toTripleText(g, b);

            // Separate graphs with a blank line if there are multiple graphs
            if (gi < graphs.size() - 1) {
                b.append('\n');
            }
        }
        return b.toString();
    }

}
