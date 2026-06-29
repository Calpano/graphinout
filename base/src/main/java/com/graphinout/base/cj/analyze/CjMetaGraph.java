package com.graphinout.base.cj.analyze;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjElementType;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.impl.CjDocumentElement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Infers a <em>meta graph</em> (a schema) from a CJ graph: it summarises which node types and edge types occur and how
 * they relate, regardless of the source format.
 * <p>
 * Every distinct <strong>node type</strong> and <strong>edge type</strong> in the source becomes a node of the meta
 * graph, typed {@value #META_TYPE_NODE} or {@value #META_TYPE_EDGE} respectively, carrying a {@value #COUNT} of how many
 * instances it had. A node type is linked to each edge type its instances touch by a {@value #EDGE_USES} edge.
 * <p>
 * Where one node type's set of used edge types is a <em>strict subset</em> of another's, the smaller (more general) type
 * is treated as a super-type: the shared {@value #EDGE_USES} edges live on the super-type only (the sub-type keeps just
 * its extras), and a {@value #EDGE_HAS_SUBTYPE} edge is added from the super-type to the sub-type. {@code has subtype}
 * edges form the transitive reduction (Hasse diagram) of the subset order, so only direct super/sub steps are emitted.
 * <p>
 * The meta graph is a whole-graph reduction (a type can't be emitted until every instance is seen), but the
 * <em>input</em> need not be held as a full document: feed an {@link Accumulator} node-by-node and edge-by-edge — it
 * keeps only node id&rarr;types, per-type counts and each edge's (type, endpoint ids), then resolves everything in
 * {@link Accumulator#build()}. {@link #metaGraph(ICjDocument)} is a convenience for callers that already hold a document.
 */
public final class CjMetaGraph {

    /** Meta node type assigned to every node representing a source node type. */
    public static final String META_TYPE_NODE = "Node";
    /** Meta node type assigned to every node representing a source edge type. */
    public static final String META_TYPE_EDGE = "Edge";
    /** Meta edge type: a node type's instances are endpoints of this edge type. */
    public static final String EDGE_USES = "uses";
    /** Meta edge type: source (super-type) generalises the target (sub-type). */
    public static final String EDGE_HAS_SUBTYPE = "has subtype";
    /** Stand-in type for nodes/edges that declare no type. */
    public static final String UNTYPED = "(untyped)";
    /** Data key holding the instance count of a type. */
    public static final String COUNT = "count";

    private CjMetaGraph() {
    }

    /** Build the meta graph from a fully-materialized document. */
    public static ICjDocument metaGraph(ICjDocument source) {
        Accumulator acc = new Accumulator();
        CjFeature.allNodes(source).forEach(n ->
                acc.addNode(n.id(), n.types().map(ICjElementType::type).collect(Collectors.toList())));
        CjFeature.allEdges(source).forEach(e ->
                acc.addEdge(e.type(), e.endpoints().map(ICjEndpoint::node).collect(Collectors.toList())));
        return acc.build();
    }

    /**
     * Streaming projection of just what a meta graph needs. Feed it the source node-by-node and edge-by-edge in any
     * order — usage is resolved in {@link #build()}, so an edge that references a not-yet-seen node id is fine. Memory
     * is the projection only (node id&rarr;types, the per-type counts, and one (type, endpoint-ids) record per edge),
     * never the labels/data/ports/nesting of a full document.
     */
    public static final class Accumulator {

        private final Map<String, Long> nodeTypeCount = new LinkedHashMap<>();
        private final Map<String, Long> edgeTypeCount = new LinkedHashMap<>();
        private final Map<String, Set<String>> nodeIdToTypes = new HashMap<>();
        private final List<EdgeUse> edgeUses = new ArrayList<>();

        public void addNode(@Nullable String id, Collection<String> types) {
            Set<String> ts = types.isEmpty() ? new LinkedHashSet<>(List.of(UNTYPED)) : new LinkedHashSet<>(types);
            ts.forEach(t -> nodeTypeCount.merge(t, 1L, Long::sum));
            if (id != null) {
                nodeIdToTypes.put(id, ts);
            }
        }

        public void addEdge(@Nullable String type, Collection<String> endpointNodeIds) {
            String et = (type == null || type.isEmpty()) ? UNTYPED : type;
            edgeTypeCount.merge(et, 1L, Long::sum);
            edgeUses.add(new EdgeUse(et, endpointNodeIds.stream().filter(Objects::nonNull).collect(Collectors.toList())));
        }

        public ICjDocument build() {
            // Resolve usage now that every node's types are known (forward references included).
            Map<String, Set<String>> uses = new LinkedHashMap<>();
            nodeTypeCount.keySet().forEach(t -> uses.put(t, new LinkedHashSet<>()));
            for (EdgeUse e : edgeUses) {
                for (String nid : e.endpoints()) {
                    Set<String> nodeTypes = nodeIdToTypes.get(nid);
                    if (nodeTypes != null) {
                        nodeTypes.forEach(nt -> uses.get(nt).add(e.type()));
                    }
                }
            }
            return buildDocument(nodeTypeCount, edgeTypeCount, uses);
        }
    }

    private record EdgeUse(String type, List<String> endpoints) {
    }

    // ----------------------------------------------------------------- inference + document construction

    private static ICjDocument buildDocument(Map<String, Long> nodeTypeCount, Map<String, Long> edgeTypeCount,
                                             Map<String, Set<String>> uses) {
        // Subtype inference over the strict-subset order of used-edge-type sets.
        List<String> nodeTypes = new ArrayList<>(uses.keySet());
        // ancestors(T) = every node type whose used-edge set is a strict subset of T's (i.e. a super-type of T)
        Map<String, Set<String>> ancestors = new HashMap<>();
        for (String t : nodeTypes) {
            Set<String> anc = new LinkedHashSet<>();
            for (String s : nodeTypes) {
                if (!s.equals(t) && isProperSubset(uses.get(s), uses.get(t))) {
                    anc.add(s);
                }
            }
            ancestors.put(t, anc);
        }
        // direct super-types = maximal ancestors (transitive reduction): an ancestor not itself an ancestor's ancestor
        Map<String, Set<String>> directSupers = new LinkedHashMap<>();
        for (String t : nodeTypes) {
            Set<String> indirect = new LinkedHashSet<>();
            for (String a : ancestors.get(t)) {
                indirect.addAll(ancestors.get(a));
            }
            Set<String> direct = new LinkedHashSet<>(ancestors.get(t));
            direct.removeAll(indirect);
            directSupers.put(t, direct);
        }

        boolean idClash = !Collections.disjoint(nodeTypeCount.keySet(), edgeTypeCount.keySet());
        UnaryOperator<String> nodeId = idClash ? t -> META_TYPE_NODE + ":" + t : UnaryOperator.identity();
        UnaryOperator<String> edgeId = idClash ? t -> META_TYPE_EDGE + ":" + t : UnaryOperator.identity();

        CjDocumentElement meta = new CjDocumentElement();
        meta.addGraph(g -> {
            nodeTypeCount.forEach((t, c) -> g.addNode(n -> {
                n.id(nodeId.apply(t));
                n.addType(ICjElementType.of(META_TYPE_NODE));
                n.data().add(COUNT, c);
            }));
            edgeTypeCount.forEach((t, c) -> g.addNode(n -> {
                n.id(edgeId.apply(t));
                n.addType(ICjElementType.of(META_TYPE_EDGE));
                n.data().add(COUNT, c);
            }));

            // 'uses' edges: only the edge types NOT already provided by a super-type ("moved up" to the super-type)
            for (String t : nodeTypes) {
                Set<String> inherited = new LinkedHashSet<>();
                ancestors.get(t).forEach(a -> inherited.addAll(uses.get(a)));
                Set<String> retained = new LinkedHashSet<>(uses.get(t));
                retained.removeAll(inherited);
                retained.forEach(e -> g.addEdge(ed -> {
                    ed.edgeType(EDGE_USES);
                    // CJ endpoints are edge-perspective: the source is the incoming endpoint, the target the outgoing one
                    ed.addEndpointIncoming(nodeId.apply(t));
                    ed.addEndpointOutgoing(edgeId.apply(e));
                }));
            }

            // 'has subtype' edges: super-type -> direct sub-type
            for (String sub : nodeTypes) {
                directSupers.get(sub).forEach(sup -> g.addEdge(ed -> {
                    ed.edgeType(EDGE_HAS_SUBTYPE);
                    ed.addEndpointIncoming(nodeId.apply(sup));
                    ed.addEndpointOutgoing(nodeId.apply(sub));
                }));
            }
        });
        return meta;
    }

    private static boolean isProperSubset(Set<String> a, Set<String> b) {
        return a.size() < b.size() && b.containsAll(a);
    }
}
