package com.graphinout.reader.mermaid;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.CjUris;
import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjHasData;
import com.graphinout.base.cj.document.ICjLabelEntry;
import com.graphinout.base.cj.document.ICjNode;
import com.graphinout.foundation.pure.json.document.IJsonValue;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/** Converts a {@link ICjDocument} to Mermaid flowchart text. */
public class MermaidOutput {

    private static final Logger log = getLogger(MermaidOutput.class);
    private final ICjDocument cjDoc;

    public MermaidOutput(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
    }

    static @Nullable String firstLabelOrDesc(ICjHasData hasData, List<ICjLabelEntry> labels) {
        if (!labels.isEmpty()) {
            String val = labels.getFirst().value();
            if (val != null && !val.isEmpty()) return val;
        }
        IJsonValue json = hasData.jsonValue();
        if (json != null && json.isObject()) {
            IJsonValue desc = json.resolve(CjDataProperty.Description.cjPropertyKey);
            if (desc != null) {
                String s = desc.toXmlFragmentString().rawXml();
                if (s != null && !s.isEmpty()) return s;
            }
        }
        return null;
    }

    public String toMermaid() {
        MermaidDoc out = new MermaidDoc();
        cjDoc2mermaidDoc(cjDoc, out);
        return out.toMermaid();
    }

    private void cjDoc2mermaidDoc(ICjDocument cjDoc, MermaidDoc out) {
        @Nullable Map<String, String> context = cjDoc.context();
        boolean hasContext = context != null && !context.isEmpty();
        Set<String> nodeIds = new LinkedHashSet<>();
        // Flatten all top-level graphs (and graphs nested *in graphs*) into one flowchart, document order.
        // Graphs nested *in nodes* become subgraph blocks (handled by node2mermaid).
        cjDoc.graphs().forEach(g -> flattenGraph(g, out, context, hasContext, nodeIds));
        // Implied nodes: referenced by an edge endpoint but never declared as a node or subgraph container.
        for (MermaidDoc.MermaidEdge e : out.edges) {
            addImplied(out, nodeIds, e.sourceId);
            addImplied(out, nodeIds, e.targetId);
        }
    }

    /** Flatten a graph's nodes and edges into {@code out}, recursing into graphs nested in graphs. */
    private void flattenGraph(ICjGraph g, MermaidDoc out, @Nullable Map<String, String> context, boolean hasContext,
                              Set<String> nodeIds) {
        g.nodes().forEach(n -> {
            MermaidDoc.MermaidNode mn = node2mermaid(n, context, hasContext);
            out.nodes.add(mn);
            nodeIds.add(mn.id);
        });
        g.edges().forEach(e -> {
            MermaidDoc.MermaidEdge me = edge2mermaid(e, context, hasContext);
            if (me != null) out.edges.add(me);
        });
        // graphs nested directly in this graph: flatten (mermaid flowchart has no graph-in-graph nesting)
        g.graphs().forEach(sub -> flattenGraph(sub, out, context, hasContext, nodeIds));
    }

    private void addImplied(MermaidDoc out, Set<String> nodeIds, String id) {
        if (nodeIds.add(id)) {
            out.nodes.add(new MermaidDoc.MermaidNode(id, null));
        }
    }

    /** Convert a CJ node to a Mermaid node, recursively turning nested graphs into subgraph blocks. */
    private MermaidDoc.MermaidNode node2mermaid(ICjNode n, @Nullable Map<String, String> context, boolean hasContext) {
        String id = hasContext ? n.uri() : n.id();
        String label = firstLabelOrDesc(n, n.labelEntries());
        MermaidDoc.MermaidNode mn = new MermaidDoc.MermaidNode(id, label);
        n.graphs().forEach(g -> mn.subgraphs.add(graph2subgraph(g, context, hasContext)));
        return mn;
    }

    private MermaidDoc.MermaidSubgraph graph2subgraph(ICjGraph g, @Nullable Map<String, String> context,
                                                      boolean hasContext) {
        String id = hasContext ? g.uri() : g.id();
        String label = firstLabelOrDesc(g, g.labelEntries());
        MermaidDoc.MermaidSubgraph sg = new MermaidDoc.MermaidSubgraph(id, label);
        g.nodes().forEach(n -> sg.nodes.add(node2mermaid(n, context, hasContext)));
        g.edges().forEach(e -> {
            MermaidDoc.MermaidEdge me = edge2mermaid(e, context, hasContext);
            if (me != null) sg.edges.add(me);
        });
        return sg;
    }

    private MermaidDoc.@Nullable MermaidEdge edge2mermaid(ICjEdge e, @Nullable Map<String, String> context,
                                                          boolean hasContext) {
        List<ICjEndpoint> eps = e.endpoints().toList();
        if (eps.size() != 2) {
            log.warn("Cannot represent hyper-edge in Mermaid flowchart");
            return null;
        }
        ICjEndpoint in = eps.stream().filter(ICjEndpoint::isSource).findFirst().orElse(null);
        ICjEndpoint out = eps.stream().filter(ICjEndpoint::isTarget).findFirst().orElse(null);
        String source, target;
        if (in != null && out != null) {
            source = in.node();
            target = out.node();
        } else {
            source = eps.get(0).node();
            target = eps.get(1).node();
        }
        if (source == null || target == null) return null;
        // Directed if any endpoint carries an explicit in/out direction; otherwise undirected.
        boolean directed = eps.stream().anyMatch(ICjEndpoint::isDirected);
        if (hasContext) {
            source = CjUris.expandId(context, source);
            target = CjUris.expandId(context, target);
        }
        String label = firstLabelOrDesc(e, e.labelEntries());
        return new MermaidDoc.MermaidEdge(source, target, label, directed);
    }
}
