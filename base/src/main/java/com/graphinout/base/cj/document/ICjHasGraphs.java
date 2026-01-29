package com.graphinout.base.cj.document;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

public interface ICjHasGraphs {

    /**
     * @return All edges in the document, from all graphs and subgraphs.
     */
    default Stream<ICjEdge> edgesAll() {
        return graphsAll().flatMap(ICjGraph::edges);
    }

    @NonNull String effectiveBaseUri();

    default @Nullable ICjEdge findEdgeById(@NonNull String edgeId) throws IllegalStateException {
        String queryUri = CjUris.uri(effectiveBaseUri(), edgeId);
        return edgesAll().filter(e -> edgeId.equals(e.uri())).findFirst().orElse(null);
    }

    default @Nullable ICjGraph findGraphById(String graphId) {
        String queryUri = CjUris.uri(effectiveBaseUri(), graphId);
        if (this instanceof ICjGraph graph) {
            if (queryUri.equals(graph.uri())) {
                return graph;
            }
        }
        return graphsAll().filter(g -> queryUri.equals(g.uri())).findFirst().orElse(null);
    }

    default @Nullable ICjNode findNodeById(@NonNull String nodeId) throws IllegalStateException {
        String queryUri = CjUris.uri(effectiveBaseUri(), nodeId);
        return nodesAll().filter(n -> queryUri.equals(n.uri())).findFirst().orElse(null);
    }

    Stream<ICjGraph> graphs();

    /**
     * @return All graphs in the document, including their nested subgraphs, recursively. Including graphs nested in
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

    default Stream<ICjNode> nodesAllIncludingImplied() {
        return Stream.concat(//
                nodesAll(), //
                edgesAll().flatMap(ICjEdge::nodesResolved) //
        ).distinct().sorted();
    }

}
