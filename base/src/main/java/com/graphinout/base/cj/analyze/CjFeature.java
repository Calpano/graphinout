package com.graphinout.base.cj.analyze;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjNode;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The controlled vocabulary of graph-model features from the graph-format-registry
 * ({@code graph-features.adoc}). Each constant pairs the registry feature slug (which is also the name of the synthetic
 * CJ test file under {@code .../graph-format-features/<slug>.cj.json}) with a structural detector that answers
 * "does this CJ document exhibit the feature?".
 *
 * <p>Detectors traverse the whole document (top-level graphs plus graphs nested in graphs, nodes and edges), so they
 * stay correct regardless of where an element is nested.
 *
 * <p>Use {@link CjAnalyzer#analyze(ICjDocument)} to get the set of features present in a document together with
 * node/edge counts.
 */
public enum CjFeature {

    MULTIPLE_GRAPHS_PER_DOCUMENT("multiple-graphs-per-document", doc -> doc.graphs().count() >= 2),

    /** A node that is not an endpoint of any edge (the format can carry standalone nodes). */
    NODES("nodes", doc -> {
        Set<String> endpointNodeIds = allEdges(doc)
                .flatMap(ICjEdge::endpoints).map(ICjEndpoint::node).collect(Collectors.toSet());
        return allNodes(doc).anyMatch(n -> !endpointNodeIds.contains(n.id()));
    }),

    UNDIRECTED_EDGES("undirected-edges",
            doc -> allEdges(doc).anyMatch(e -> e.endpoints().anyMatch(ICjEndpoint::isUndirected))),

    DIRECTED_EDGES("directed-edges",
            doc -> allEdges(doc).anyMatch(e -> e.endpoints().anyMatch(ICjEndpoint::isDirected))),

    HYPEREDGES("hyperedges", doc -> allEdges(doc).anyMatch(e -> e.endpoints().count() > 2)),

    /** A single graph that contains both a directed edge and an undirected edge. */
    MIXED_DIRECTIONALITY_EDGES("mixed-directionality-edges", doc -> allGraphs(doc).anyMatch(g -> {
        boolean directed = g.edges().anyMatch(e -> e.endpoints().anyMatch(ICjEndpoint::isDirected));
        boolean undirected = g.edges().anyMatch(e -> e.endpoints().anyMatch(ICjEndpoint::isUndirected));
        return directed && undirected;
    })),

    /** An edge with a node appearing at more than one endpoint (e.g. a binary edge whose two ends are the same node). */
    SELF_LOOPS("self-loops",
            doc -> allEdges(doc).anyMatch(e -> e.endpoints().map(ICjEndpoint::node).distinct().count() < e.endpoints().count())),

    /**
     * Two distinct edges in the same graph connecting the same set of endpoint nodes (a multigraph). The signature is
     * the sorted endpoint node-id list, so it is direction-agnostic.
     */
    PARALLEL_EDGES("parallel-edges", doc -> allGraphs(doc).anyMatch(g -> {
        Set<List<String>> seen = new HashSet<>();
        return g.edges().anyMatch(e -> !seen.add(e.endpoints().map(ICjEndpoint::node).sorted().toList()));
    })),

    /** An edge endpoint that points at another edge (by id). */
    EDGES_ON_EDGES("edges-on-edges", doc -> {
        Set<String> edgeIds = allEdges(doc).map(ICjEdge::id).filter(Objects::nonNull).collect(Collectors.toSet());
        return allEdges(doc).flatMap(ICjEdge::endpoints).anyMatch(ep -> edgeIds.contains(ep.node()));
    }),

    NESTED_GRAPHS_IN_NODES("nested-graphs-in-nodes",
            doc -> allNodes(doc).anyMatch(n -> n.graphs().findAny().isPresent())),

    NESTED_GRAPHS_IN_EDGES("nested-graphs-in-edges",
            doc -> allEdges(doc).anyMatch(e -> e.graphs().findAny().isPresent())),

    NESTED_GRAPHS_IN_GRAPHS("nested-graphs-in-graphs",
            doc -> allGraphs(doc).anyMatch(g -> g.graphs().findAny().isPresent())),

    NODE_LABELS("node-labels", doc -> allNodes(doc).anyMatch(n -> hasLabel(n.label()))),

    EDGE_LABELS("edge-labels", doc -> allEdges(doc).anyMatch(e -> hasLabel(e.label()))),

    ATTRIBUTES_ON_NODES("attributes-on-nodes", doc -> allNodes(doc).anyMatch(n -> !n.data().isEmpty())),

    ATTRIBUTES_ON_EDGES("attributes-on-edges", doc -> allEdges(doc).anyMatch(e -> !e.data().isEmpty())),

    ATTRIBUTES_ON_GRAPHS("attributes-on-graphs", doc -> allGraphs(doc).anyMatch(g -> !g.data().isEmpty())),

    TYPED_EDGES("typed-edges", doc -> allEdges(doc).anyMatch(e -> {
        String t = e.type();
        return t != null && !t.isBlank();
    }));

    private final String slug;
    private final Predicate<ICjDocument> detector;

    CjFeature(String slug, Predicate<ICjDocument> detector) {
        this.slug = slug;
        this.detector = detector;
    }

    public static CjFeature bySlug(String slug) {
        for (CjFeature f : values()) {
            if (f.slug.equals(slug)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown feature slug: " + slug);
    }

    /** The registry feature slug, e.g. {@code "directed-edges"}. */
    public String slug() {
        return slug;
    }

    public boolean isPresentIn(ICjDocument doc) {
        return detector.test(doc);
    }

    // --- recursive traversal helpers ---------------------------------------------------------------------------

    /** All graphs in the document: top-level graphs plus graphs nested inside graphs, nodes and edges. */
    public static Stream<ICjGraph> allGraphs(ICjDocument doc) {
        return doc.graphs().flatMap(CjFeature::graphAndDescendants);
    }

    private static Stream<ICjGraph> graphAndDescendants(ICjGraph g) {
        return Stream.of(
                        Stream.of(g),
                        g.graphs().flatMap(CjFeature::graphAndDescendants),
                        g.nodes().flatMap(n -> n.graphs().flatMap(CjFeature::graphAndDescendants)),
                        g.edges().flatMap(e -> e.graphs().flatMap(CjFeature::graphAndDescendants)))
                .flatMap(s -> s);
    }

    public static Stream<ICjNode> allNodes(ICjDocument doc) {
        return allGraphs(doc).flatMap(ICjGraph::nodes);
    }

    public static Stream<ICjEdge> allEdges(ICjDocument doc) {
        return allGraphs(doc).flatMap(ICjGraph::edges);
    }

    private static boolean hasLabel(ICjLabel label) {
        return label != null && label.entries().findAny().isPresent();
    }
}
