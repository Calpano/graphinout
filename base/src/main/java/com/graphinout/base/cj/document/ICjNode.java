package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.jajson.JaJson;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a node in the CJ model, which may have ports and embedded subgraphs, plus associated data/labels.
 */
public interface ICjNode extends ICjNodeChunk, ICjHasGraphs {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.concat(Stream.of(data()).filter(Objects::nonNull), ports()), graphs());
    }

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap()
                .putMaybe(CjConstants.ID, id())
                .putMaybe(CjConstants.LABEL, label(), ICjLabel::toJaJsonMap)
                .putMaybe(CjConstants.PORTS, ports(), ICjPort::toJaJsonMap)
                .putMaybe(CjConstants.DATA, data(), ICjData::toJaJsonValue)
                .putMaybe(CjConstants.GRAPHS, graphs(), ICjGraph::toJaJsonMap)
                .build();
    }

}
