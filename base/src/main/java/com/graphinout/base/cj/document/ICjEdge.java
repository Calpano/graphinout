package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.impl.CjNodeElement;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents an edge between endpoints in the CJ model, optionally containing nested subgraphs.
 */
public interface ICjEdge extends ICjEdgeChunk, ICjHasGraphs, ICjCoreElement, Comparable<ICjEdge> {

    /**
     * Compare first by chunk properties, then by graph arrays
     */
    @Override
    default int compareTo(@NonNull ICjEdge other) {
        return Comparables.<ICjEdge>comparing() //
                .byComparator(ICjEdgeChunk::compare) //
                .byStream(ICjHasGraphs::graphs) //
                .compare(this, other);
    }

    default void copyTo(ICjEdgeMutable targetEdge) {
        ICjEdgeChunk.super.copyTo(targetEdge);
        graphs().forEach(sourceGraph -> targetEdge.addGraph(sourceGraph::copyTo));
    }


    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()), endpoints()), graphs());
    }

    /** Index of subGraph in this edge @return -1 if not found */
    int indexOf(ICjGraph subGraph);

    /**
     * Returns the resolved nodes for this edge, resolving any node IDs to actual nodes.
     */
    default Stream<@NonNull ICjNode> nodesResolved() {
        return endpoints().map(ep -> resolveNodeById(ep.node()));
    }

    @Override
    @NonNull ICjGraph parent();

    default @NonNull ICjNode resolveNodeById(String nodeId) {
        ICjNode node = parent().findNodeById(nodeId);
        if (node == null) {
            // return implied node with the graph set to the graph which defined the baseUri of the edge
            ICjNodeMutable nodeMutable = new CjNodeElement(contextGraph());
            nodeMutable.id(nodeId);
            return nodeMutable;
        }
        return node;
    }

    /**
     * A hash based on endpoints, ports, label, data, and graphs. So everything except the id.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder(ICjEdgeChunk.super.structuralHash());
        sb.append("|G:");
        graphs().forEach(g -> sb.append(g.structuralHash()).append(","));
        return Integer.toString(sb.toString().hashCode());
    }

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap().putMaybe(CjConstants.ID, id()).putMaybe(CjConstants.LABEL, label(), ICjLabel::toJaJsonMap).putMaybe(CjConstants.EDGE__ENDPOINTS, endpoints(), ICjEndpoint::toJaJsonMap).putMaybe(CjConstants.DATA, data().ifNotEmpty(), ICjData::toJaJsonValue).putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap).build();
    }

}
