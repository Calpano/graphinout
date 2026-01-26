package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a graph in the CJ model composed of nodes, edges, and optional subgraphs. Provides traversal and counts to
 * support streaming and transformations.
 */
public interface ICjGraph extends ICjGraphChunk, ICjHasGraphs, ICjCoreElement, Comparable<ICjGraph> {

    default int compareTo(@NonNull ICjGraph other) {
        return Comparables.<ICjGraph>comparing() //
                .byComparator(ICjGraphChunk::compare) //
                .byStream(ICjGraph::nodes, ICjNode::compareTo) //
                .byStream(ICjGraph::edges, ICjEdge::compareTo) //
                .byStream(ICjHasGraphs::graphs) //
                .compare(this, other);
    }

    /** Edge count in this graph, excluding subgraphs */
    default long countEdgesDirect() {
        return edges().count();
    }

    /** Node count in this graph, excluding subgraphs */
    default long countNodesDirect() {
        return nodes().count();
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat( //
                Stream.concat( //
                        Stream.of(data()), //
                        Stream.concat(nodes(), edges()) //
                ),//
                graphs());
    }

    Stream<ICjEdge> edges();

    /**
     * @return a stream excluding this, including (thisGraph)-graphs subgraphs, (thisGrapp-edge-graphs) subgraphs, and
     * (thisGraph-node-graphs) subgraphs.
     */
    default Stream<ICjGraph> graphsNestedNonRecursive() {
        return Stream.concat(graphs(), //
                Stream.concat( //
                        nodes().flatMap(ICjHasGraphs::graphs), //
                        edges().flatMap(ICjHasGraphs::graphs) //
                ));
    }

    /** @return -1 if not found */
    int indexOf(ICjGraph subGraph);

    /** @return -1 if not found */
    int indexOf(ICjNode node);

    /** @return -1 if not found */
    int indexOf(ICjEdge edge);

    Stream<ICjNode> nodes();

    default @Nullable ICjNode findNodeById(String nodeId) {
        // must use the effectiveUri of this graph
        String nodeUri = CjUris.uri(effectiveBaseUri(), nodeId);
        return document().findNodeById(nodeUri);
    }

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap() //
                .putMaybe(CjConstants.ID, id()).putMaybe(CjConstants.LABEL, label(), ICjLabel::toJaJsonMap).putMaybe(CjConstants.GRAPH__NODES, nodes(), ICjNode::toJaJsonMap).putMaybe(CjConstants.GRAPH__EDGES, edges(), ICjEdge::toJaJsonMap).putMaybe(CjConstants.DATA, data().ifNotEmpty(), ICjData::toJaJsonValue).putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap).build();
    }


}
