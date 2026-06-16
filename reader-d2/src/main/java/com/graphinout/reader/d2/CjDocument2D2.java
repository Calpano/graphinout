package com.graphinout.reader.d2;

import com.graphinout.base.cj.data.CjDataProperty;
import com.graphinout.base.cj.document.CjDirection;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/** Converts a {@link ICjDocument} to D2 text. */
public class CjDocument2D2 {

    private static final Logger log = getLogger(CjDocument2D2.class);
    private final ICjDocument cjDoc;
    private final @Nullable Map<String, String> context;
    private final boolean hasContext;
    /** CJ node-id (and uri) -> fully-qualified D2 id, so cross-scope edge endpoints resolve to the nested shape. */
    private final Map<String, String> qualifiedNodeIds = new LinkedHashMap<>();

    public CjDocument2D2(ICjDocument cjDoc) {
        this.cjDoc = cjDoc;
        this.context = cjDoc.context();
        this.hasContext = context != null && !context.isEmpty();
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

    /** Collect object-data scalar properties (excluding the description) as flat attribute strings. */
    static void collectAttributes(ICjHasData hasData, Map<String, String> into) {
        IJsonValue json = hasData.jsonValue();
        if (json == null || !json.isObject()) return;
        json.asObject().forEach((key, value) -> {
            if (key == null || key.equals(CjDataProperty.Description.cjPropertyKey)) return;
            if (value == null) return;
            String s;
            if (value.isString()) {
                s = value.asString();
            } else if (value.isContainer()) {
                s = value.toJsonString();
            } else {
                s = value.toJsonString();
            }
            if (s != null) into.put(key, s);
        });
    }

    public String toD2() {
        // First pass: assign each node a fully-qualified D2 id along its container path.
        cjDoc.graphs().forEach(g -> indexGraph(g, ""));
        // Second pass: build the D2 model with nesting preserved.
        D2Doc out = new D2Doc();
        cjDoc.graphs().forEach(g -> emitGraph(g, "", out.nodes, out.edges));
        return out.toD2();
    }

    /** Local D2 id for a node (its own segment, unqualified). */
    private String localId(ICjNode node) {
        return hasContext ? node.uri() : node.id();
    }

    /**
     * Qualify a node's local id under {@code prefix}, idempotently: if the local id already carries the prefix
     * (e.g. on a second writer pass over already-qualified ids) it is returned unchanged, avoiding double-prefixing.
     */
    private String qualify(String prefix, String localId) {
        if (prefix.isEmpty()) return localId;
        if (localId.equals(prefix) || localId.startsWith(prefix + ".")) return localId;
        return prefix + "." + localId;
    }

    /** Record qualified ids for all nodes reachable under {@code prefix}. */
    private void indexGraph(ICjGraph graph, String prefix) {
        graph.nodes().forEach(node -> {
            String qualified = qualify(prefix, localId(node));
            qualifiedNodeIds.putIfAbsent(node.id(), qualified);
            if (hasContext) qualifiedNodeIds.putIfAbsent(node.uri(), qualified);
            node.graphs().forEach(sub -> indexGraph(sub, qualified));
        });
        // graphs-nested-in-graphs are flattened into the same scope (D2 has no graph-in-graph container).
        graph.graphs().forEach(sub -> indexGraph(sub, prefix));
    }

    /** Emit one CJ graph's nodes and edges into the given (possibly nested) target lists. */
    private void emitGraph(ICjGraph graph, String prefix, List<D2Doc.D2Node> nodeSink, List<D2Doc.D2Edge> edgeSink) {
        graph.nodes().forEach(node -> nodeSink.add(emitNode(node, prefix)));
        graph.edges().forEach(edge -> {
            D2Doc.D2Edge d2Edge = emitEdge(edge, prefix);
            if (d2Edge != null) edgeSink.add(d2Edge);
        });
        // graphs nested directly in a graph are flattened into the same D2 scope.
        graph.graphs().forEach(sub -> emitGraph(sub, prefix, nodeSink, edgeSink));
    }

    private D2Doc.D2Node emitNode(ICjNode node, String prefix) {
        String qualified = qualify(prefix, localId(node));
        // Inside a D2 container, a child shape is declared by its LOCAL id; references from other scopes use the
        // fully-qualified dotted path (resolved via {@link #qualifiedNodeIds}).
        String d2Id = prefix.isEmpty() ? qualified : stripPrefix(qualified, prefix);
        String label = firstLabelOrDesc(node, node.labelEntries());
        D2Doc.D2Node d2Node = new D2Doc.D2Node(d2Id, label);
        collectAttributes(node, d2Node.attributes);
        // nested graphs in node -> container children
        node.graphs().forEach(sub -> emitGraph(sub, qualified, d2Node.children, d2Node.childEdges));
        return d2Node;
    }

    /** Remove the leading {@code prefix + "."} from {@code qualified} if present. */
    private static String stripPrefix(String qualified, String prefix) {
        String p = prefix + ".";
        return qualified.startsWith(p) ? qualified.substring(p.length()) : qualified;
    }

    /**
     * Resolve an edge endpoint's CJ node reference to a D2 id relative to {@code prefix}: a reference to a node in
     * the same container is emitted locally; a reference reaching out of the container uses the full dotted path.
     */
    private String resolveRef(String nodeRef, String prefix) {
        String qualified = qualifiedNodeIds.get(nodeRef);
        if (qualified == null) {
            qualified = hasContext ? CjUris.expandId(context, nodeRef) : nodeRef;
        }
        if (!prefix.isEmpty() && (qualified.equals(prefix) || qualified.startsWith(prefix + "."))) {
            return stripPrefix(qualified, prefix);
        }
        return qualified;
    }

    private D2Doc.@Nullable D2Edge emitEdge(ICjEdge edge, String prefix) {
        List<ICjEndpoint> eps = edge.endpoints().toList();
        ICjEndpoint inEp = eps.stream().filter(ep -> ep.direction() == CjDirection.IN).findFirst().orElse(null);
        ICjEndpoint outEp = eps.stream().filter(ep -> ep.direction() == CjDirection.OUT).findFirst().orElse(null);
        String source, target;
        boolean undirected;
        if (inEp != null && outEp != null) {
            source = inEp.node();
            target = outEp.node();
            undirected = false;
        } else if (eps.size() == 2) {
            source = eps.get(0).node();
            target = eps.get(1).node();
            // undirected when neither endpoint is directed
            undirected = eps.stream().noneMatch(ICjEndpoint::isDirected);
        } else {
            log.warn("Cannot represent hyper-edge in D2");
            return null;
        }
        if (source == null || target == null) return null;
        source = resolveRef(source, prefix);
        target = resolveRef(target, prefix);
        String label = firstLabelOrDesc(edge, edge.labelEntries());
        D2Doc.D2Edge d2Edge = new D2Doc.D2Edge(source, target, label, undirected);
        collectAttributes(edge, d2Edge.attributes);
        return d2Edge;
    }
}
