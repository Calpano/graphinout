package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface ICjHasGraphs {

    /**
     * @return All edges in the document, from all graphs and subgraphs.
     */
    default Stream<ICjEdge> edgesAll() {
        return graphsAll().flatMap(ICjGraph::edges);
    }

    default @Nullable ICjEdge findEdgeById(@NonNull String edgeId) throws IllegalStateException {
        return edgesAll().filter(e -> edgeId.equals(e.id())).findFirst().orElse(null);
    }

    default @Nullable ICjGraph findGraphById(String graphId) {
        if (this instanceof ICjGraph graph) {
            if (graphId.equals(graph.id())) {
                return graph;
            }
        }
        return graphsAll().filter(g -> graphId.equals(g.id())).findFirst().orElse(null);
    }


    default @Nullable ICjNode findNodeById(@NonNull String nodeId) throws IllegalStateException {
        return nodesAll().filter(n -> nodeId.equals(n.id())).findFirst().orElse(null);
    }

    Stream<ICjGraph> graphs();

    /**
     * @return All graphs in this document/graph, including this graph and their nested subgraphs, recursively. Including graphs nested in
     * nodes or edges.
     */
    default Stream<ICjGraph> graphsAll() {
        return graphs().flatMap(g -> Stream.concat(Stream.of(g), g.graphsNestedNonRecursive()));
    }

    /** Index of given graph in this element. -1 if not found */
    int indexOf(ICjGraph graph);

    /**
     * @return All nodes in the document, from all graphs and subgraphs.
     */
    default Stream<ICjNode> nodesAll() {
        return graphsAll().flatMap(ICjGraph::nodes);
    }

    /**
     * All nodes from all graphs plus implied nodes (referenced by edge endpoints but not explicitly defined).
     * Deduplicates by ID, keeping the first (explicit) occurrence.
     */
    default Stream<ICjNode> nodesAllIncludingImplied() {
        Map<String, ICjNode> seen = new LinkedHashMap<>();
        nodesAll().forEach(n -> seen.putIfAbsent(n.id(), n));
        edgesAll().flatMap(ICjEdge::nodesResolved).forEach(n -> seen.putIfAbsent(n.id(), n));
        return seen.values().stream().sorted();
    }

    /**
     * A hash based on all nested graphs.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder("G:");
        graphs().forEach(g -> sb.append(g.structuralHash()).append(","));
        return Integer.toString(sb.toString().hashCode());
    }

}
