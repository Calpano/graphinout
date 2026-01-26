package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.json.JsonConstants;
import com.graphinout.foundation.pure.json.formatter.JsonCompactFormatter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.nonNullOrDefault;

/**
 * Root of a CJ graph representation assembled from GIO events. It aggregates graphs and document-level metadata.
 */
public interface ICjDocument extends ICjHasGraphs, ICjDocumentChunk, ICjElement {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data().ifNotEmpty()), Stream.of(connectedJson())).filter(Objects::nonNull), graphs());
    }

    /**
     * @return All edges in the document, from all graphs and subgraphs.
     */
    default Stream<ICjEdge> edgesAll() {
        return graphsAll().flatMap(ICjGraph::edges);
    }

    default @NonNull String effectiveBaseUri() {
        return nonNullOrDefault(baseUri(), CjUris.BASE_URI_FALLBACK);
    }

    default @Nullable ICjEdge findEdgeById(@NonNull String edgeId) throws IllegalStateException {
        return edgesAll().filter(e -> e.matchesId(this, edgeId)).findFirst().orElse(null);
    }

    default @Nullable ICjGraph findGraph(String id) {
        return graphsAll().filter(g -> g.matchesId(this, id)).findFirst().orElse(null);
    }

    default @Nullable ICjNode findNodeById(@NonNull String id) throws IllegalStateException {
        return nodesAll().filter(n -> n.matchesId(this, id)).findFirst().orElse(null);
    }

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
                .putNonNull(JsonConstants.DOLLAR_SCHEMA, CjConstants.CJ_SCHEMA_URL).putNonNull(JsonConstants.DOLLAR_ID, CjConstants.CJ_SCHEMA_ID).putMaybe(CjConstants.BASE_URI, baseUri()).putMaybe(CjConstants.ROOT__CONNECTED_JSON, connectedJson(), ICjDocumentMeta::toJaJsonMap).putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap).build();
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
        return CjUris.uri(effectiveBaseUri(), queryId);
    }


}
