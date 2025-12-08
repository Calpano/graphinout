package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import org.jspecify.annotations.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a port of a node or port in the CJ model, carrying an id and optional data and nested ports.
 */
public interface ICjPort extends ICjHasId, ICjHasData {

    @Nullable
    ICjData data();

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.of(data()).filter(Objects::nonNull), ports());
    }

    @Nullable
    ICjLabel label();

    Stream<ICjPort> ports();

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap()
                .putMaybe(CjConstants.ID, id())
                .putMaybe(CjConstants.LABEL, label(), ICjLabel::toJaJsonMap)
                .putMaybe(CjConstants.DATA, data(), ICjData::toJaJsonValue)
                .putMaybe(CjConstants.PORTS, ports(), ICjPort::toJaJsonMap)
                .build();
    }

}
