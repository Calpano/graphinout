package com.graphinout.engine;

import com.graphinout.base.cj.document.ICjDocument;
import com.graphinout.base.cj.document.ICjEdge;
import com.graphinout.base.cj.document.ICjEndpoint;
import com.graphinout.base.cj.document.ICjGraph;
import com.graphinout.base.cj.document.ICjLabel;
import com.graphinout.base.cj.document.ICjNode;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The controlled vocabulary of graph-model features from the graph-format-registry
 * ({@code graph-features.adoc}). Each constant pairs the registry feature slug (which is also the name of the synthetic
 * CJ test file under {@code json/connected-json/connected-json-7.0.0/graph-format-features/<slug>.cj.json}) with a structural detector that answers
 * "does this CJ document still exhibit the feature?".
 *
 * <p>Detectors traverse the whole document (top-level graphs plus graphs nested in graphs, nodes and edges), so they
 * stay correct after a round-trip relocates an element into a different nesting level.
 */
enum CjFeature {

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

    static CjFeature bySlug(String slug) {
        for (CjFeature f : values()) {
            if (f.slug.equals(slug)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown feature slug: " + slug);
    }

    String slug() {
        return slug;
    }

    /** The synthetic CJ resource that exercises exactly this feature. */
    String resourcePath() {
        return "json/connected-json/connected-json-7.0.0/graph-format-features/" + slug + ".cj.json";
    }

    boolean isPresentIn(ICjDocument doc) {
        return detector.test(doc);
    }

    // --- recursive traversal helpers ---------------------------------------------------------------------------

    /** All graphs in the document: top-level graphs plus graphs nested inside graphs, nodes and edges. */
    static Stream<ICjGraph> allGraphs(ICjDocument doc) {
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

    static Stream<ICjNode> allNodes(ICjDocument doc) {
        return allGraphs(doc).flatMap(ICjGraph::nodes);
    }

    static Stream<ICjEdge> allEdges(ICjDocument doc) {
        return allGraphs(doc).flatMap(ICjGraph::edges);
    }

    private static boolean hasLabel(ICjLabel label) {
        return label != null && label.entries().findAny().isPresent();
    }
}
