package com.graphinout.base.cj.document;

import com.graphinout.base.cj.CjConstants;
import com.graphinout.foundation.pure.collections.jajson.JaJson;
import com.graphinout.foundation.pure.util.Comparables;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

import static com.graphinout.foundation.pure.functional.Nullables.ifPresentAccept;

/**
 * One endpoint of a CJ edge: references a node (and optionally a port), with a direction and optional type and data.
 */
public interface ICjEndpoint extends ICjHasData, ICjElement, Comparable<ICjEndpoint> {

    @Override
    default int compareTo(@NonNull ICjEndpoint other) {
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

    /**
     * @return the direction of this endpoint relative to the edge (e.g., IN, OUT, UNDIR).
     */
    CjDirection direction();

    /**
     * @return true if this endpoint has a specific direction (IN or OUT), false if undirected or null.
     */
    default boolean isDirected() {
        CjDirection direction = direction();
        return direction != null && direction != CjDirection.UNDIR;
    }

    /**
     * @return true if this endpoint acts as a source (IN direction).
     */
    default boolean isSource() {
        return direction() == CjDirection.IN;
    }

    /**
     * @return true if this endpoint acts as a target (OUT direction).
     */
    default boolean isTarget() {
        return direction() == CjDirection.OUT;
    }

    /**
     * @return true if this endpoint is undirected or has no explicit direction.
     */
    default boolean isUndirected() {
        CjDirection direction = direction();
        return direction == null || direction == CjDirection.UNDIR;
    }

    /**
     * @return the ID of the node this endpoint connects to.
     */
    String node();

    /**
     * @return the optional port ID on the node this endpoint connects to, or null if none.
     */
    @Nullable String port();

    /**
     * Converts this endpoint into a JaJson map.
     *
     * @return a Map representing the JSON structure of this endpoint.
     */
    default Map<String, Object> toJaJsonMap() {
        return JaJson.createMap().putMaybe(CjConstants.ENDPOINT__NODE, node()).putMaybe(CjConstants.ENDPOINT__PORT, port()).build();
    }

    /**
     * @return the optional type or role of this endpoint, or null if none.
     */
    @Nullable String type();

    /**
     * A hash based on direction, node, port, type, and data.
     */
    default String structuralHash() {
        StringBuilder sb = new StringBuilder();
        if (direction() != null) {
            sb.append("D:").append(direction());
        }
        sb.append("|N:").append(node());
        if (port() != null) {
            sb.append("|P:").append(port());
        }
        if (type() != null) {
            sb.append("|T:").append(type());
        }
        if (data() != null && !data().isEmpty()) {
            sb.append("|D:").append(data().hashCode());
        }
        return Integer.toString(sb.toString().hashCode());
    }

}
