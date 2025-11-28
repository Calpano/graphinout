package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.jajson.JaJson;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents an edge between endpoints in the CJ model, optionally containing nested subgraphs.
 */
public interface ICjEdge extends ICjEdgeChunk, ICjHasGraphs {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()).filter(Objects::nonNull), endpoints()), graphs());
    }

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap()
                .putMaybe(CjConstants.ID, id())
                .putMaybe(CjConstants.LABEL, label(), ICjLabel::toJaJsonMap)
                .putMaybe(CjConstants.EDGE__ENDPOINTS, endpoints(), ICjEndpoint::toJaJsonMap)
                .putMaybe(CjConstants.DATA, data(), ICjData::toJaJsonValue)
                .putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap)
                .build();
    }

}
