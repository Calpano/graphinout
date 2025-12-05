package com.graphinout.base.cj.document;

import com.graphinout.base.cj.Cj;
import com.graphinout.base.cj.CjConstants;
import com.graphinout.base.cj.document.CjDocuments;
import com.graphinout.foundation.jajson.JaJson;
import com.graphinout.foundation.json.JsonConstants;

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

      default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap() //
                .putNonNull(JsonConstants.DOLLAR_SCHEMA, CjConstants.CJ_SCHEMA_LOCATION)
                .putNonNull(JsonConstants.DOLLAR_ID, CjConstants.CJ_SCHEMA_ID)
                .putMaybe( CjConstants.ROOT__BASE_URI, baseUri())
                .putMaybe( CjConstants.ROOT__CONNECTED_JSON, connectedJson(), ICjDocumentMeta::toJaJsonMap)
                .putMaybe( CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap)
                .build();
      }

}
