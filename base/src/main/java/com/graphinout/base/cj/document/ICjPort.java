package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * Represents a port of a node or port in the CJ model, carrying an id and optional data and nested ports.
 */
public interface ICjPort extends ICjHasId, ICjHasLabel, ICjHasData, ICjElement, Comparable<ICjPort> {

    /**
     * Compare by id, label, data, children
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

    default void copyTo(ICjPortMutable targetPort) {
        ifPresentAccept(id(), targetPort::id);
        data(data -> targetPort.dataJsonValue(data.jsonValue()));
        ifPresentAccept(label(), label -> targetPort.labelMutable(label::copyTo));
        // nested ports
        ports().forEach(sourceNestedPort -> targetPort.addPort(sourceNestedPort::copyTo));
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.concat(Stream.of(data().ifNotEmpty()).filter(Objects::nonNull), ports());
    }

    Stream<ICjPort> ports();

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap() //
                .putMaybe(CjConstants.ID, id()) //
                .putMaybe(CjConstants.LABEL, label(), ICjLabel::toJaJsonMap) //
                .putMaybe(CjConstants.DATA, data(), ICjData::toJaJsonValue) //
                .putMaybe(CjConstants.PORTS, ports(), ICjPort::toJaJsonMap) //
                .build();
    }

    /**
     * A hash based on label, data, and nested ports. Excludes id.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder();
        if (label() != null) {
            sb.append("L:").append(label().structuralHash());
        }
        if (data() != null && !data().isEmpty()) {
            sb.append("|D:").append(data().hashCode());
        }
        sb.append("|P:");
        ports().forEach(p -> sb.append(p.structuralHash()).append(","));
        return Integer.toString(sb.toString().hashCode());
    }

}
