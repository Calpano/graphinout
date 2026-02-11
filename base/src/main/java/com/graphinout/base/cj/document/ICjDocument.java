package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.ConnectedJson;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.JsonConstants;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Root of a CJ graph representation assembled from GIO events. It aggregates graphs and document-level metadata.
 */
public interface ICjDocument extends ICjHasGraphs, ICjDocumentChunk, ICjElement {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data().ifNotEmpty()), Stream.of(connectedJson())).filter(Objects::nonNull), graphs());
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
                .putNonNull(JsonConstants.DOLLAR_SCHEMA, ConnectedJson.CJ_SCHEMA_URL) //
                .putMaybe(CjConstants.CONTEXT, context()) //
                .putMaybe(CjConstants.ROOT__CONNECTED_JSON, connectedJson(), ICjDocumentMeta::toJaJsonMap) //
                .putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap).build();
    }

    default String toJson() {
        return CjDocuments.toJsonString(this);
    }

    default String toJsonFormatted() {
        String json = toJson();
        Object jaJson = JaJson.parse(json);
        return JsonCompactFormatter.formatCompact(jaJson);
    }

    default String uri(@NonNull String queryId) {
        return CjUris.expandId(context(), queryId);
    }


    /**
     * A hash based on data, connectedJson, and graphs. Excludes context.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder();
        if (connectedJson() != null) {
            sb.append("CJ:").append(connectedJson().hashCode());
        }
        if (data() != null && !data().isEmpty()) {
            sb.append("|D:").append(data().hashCode());
        }
        sb.append("|G:");
        graphs().forEach(g -> sb.append(g.structuralHash()).append(","));
        return Integer.toString(sb.toString().hashCode());
    }

}
