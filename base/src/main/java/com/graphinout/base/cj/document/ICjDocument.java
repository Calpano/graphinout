package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.JsonConstants;
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
                .putNonNull(JsonConstants.DOLLAR_SCHEMA, CjConstants.CJ_SCHEMA_LOCATION)
                .putNonNull(JsonConstants.DOLLAR_ID, CjConstants.CJ_SCHEMA_ID)
                .putMaybe(CjConstants.ROOT__BASE_URI, baseUri())
                .putMaybe(CjConstants.ROOT__CONNECTED_JSON, connectedJson(), ICjDocumentMeta::toJaJsonMap)
                .putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap)
                .build();
    }

}
