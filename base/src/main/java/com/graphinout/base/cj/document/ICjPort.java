package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a port of a node or port in the CJ model, carrying an id and optional data and nested ports.
 */
public interface ICjPort extends ICjHasId, ICjHasData, ICjElement, Comparable<ICjPort> {

    /**
     * Compare by id, label, data, children
     *
     * @param other
     * @return
     */
    @Override
    default int compareTo(@NonNull ICjPort other) {
        return Comparables.<ICjPort>comparing() //
                .byKey(ICjHasId::id) //
                .byKey(ICjPort::label) //
                .byKey(ICjPort::data) //
                .byStream(ICjPort::ports) //
                .compare(this, other);
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.of(data().ifNotEmpty()).filter(Objects::nonNull), ports());
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
