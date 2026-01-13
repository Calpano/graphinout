package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.JsonConstants;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;
import com.graphinout.foundation.pure.stream.PowerStreams;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Root of a CJ graph representation assembled from GIO events. It aggregates graphs and document-level metadata.
 */
public interface ICjDocument extends ICjHasGraphs, ICjDocumentChunk {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()), Stream.of(connectedJson())).filter(Objects::nonNull), graphs());
    }

    /**
     * @return All edges in the document, from all graphs and subgraphs.
     */
    default Stream<ICjEdge> edges() {
        return graphs().flatMap(ICjGraph::edges);
    }

    default @Nullable ICjNode findNode(String id) throws IllegalStateException {
        return PowerStreams.findOneOrNull(graphs().flatMap(ICjGraph::nodes).filter(n -> Objects.equals(n.id(), id)));
    }

    /**
     * @return All nodes in the document, from all graphs and subgraphs.
     */
    default Stream<ICjNode> nodes() {
        return graphs().flatMap(ICjGraph::nodes);
    }

    /**
     * @return the single graph (or null if none).
     * @throws IllegalStateException if multiple graphs are present
     */
    default @Nullable ICjGraph theGraph() throws IllegalStateException {
        List<ICjGraph> graphs = graphs().toList();
        if (graphs.isEmpty()) return null;
        if (graphs.size() == 1) return graphs.getFirst();
        throw new IllegalStateException("Multiple graphs present, use graphs() instead.");
    }

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap() //
                .putNonNull(JsonConstants.DOLLAR_SCHEMA, CjConstants.CJ_SCHEMA_LOCATION).putNonNull(JsonConstants.DOLLAR_ID, CjConstants.CJ_SCHEMA_ID).putMaybe(CjConstants.ROOT__BASE_URI, baseUri()).putMaybe(CjConstants.ROOT__CONNECTED_JSON, connectedJson(), ICjDocumentMeta::toJaJsonMap).putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap).build();
    }

    default String toJson() {
        return CjDocuments.toJsonString(this);
    }

    default String toJsonFormatted() {
        String json = toJson();
        Object jaJson = JaJson.parse(json);
        return JsonCompactFormatter.formatCompact(jaJson);
    }

}
