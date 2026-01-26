package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

public interface ICjEndpoint extends ICjHasData, ICjElement, Comparable<ICjEndpoint> {

    @Override
    default int compareTo(ICjEndpoint other) {
        return Comparables.<ICjEndpoint>comparing() //
                .byKey(ICjEndpoint::direction) //
                .byKey(ICjEndpoint::node) //
                .byKey(ICjEndpoint::port) //
                .byKey(ICjEndpoint::data)
                .compare(this, other);
    }

    default void copyTo(ICjEndpointMutable endpoint) {
        endpoint.node(node());
        endpoint.direction(direction());
        ifPresentAccept(port(), endpoint::port);
        ifPresentAccept(type(), endpoint::type);
        ifPresentAccept(data().jsonValue(), jsonValue -> endpoint.dataMutable(d -> d.setJsonValue(jsonValue)));
    }

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.of(data()).map(x -> x);
    }

    CjDirection direction();

    default boolean isDirected() {
        CjDirection direction = direction();
        return direction != null && direction != CjDirection.UNDIR;
    }

    default boolean isSource() {
        return direction() == CjDirection.IN;
    }

    default boolean isTarget() {
        return direction() == CjDirection.OUT;
    }

    default boolean isUndirected() {
        CjDirection direction = direction();
        return direction == null || direction == CjDirection.UNDIR;
    }

    String node();

    @Nullable String port();

    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap().putMaybe(CjConstants.ENDPOINT__NODE, node()).putMaybe(CjConstants.ENDPOINT__PORT, port()).build();
    }

    @Nullable String type();

}
