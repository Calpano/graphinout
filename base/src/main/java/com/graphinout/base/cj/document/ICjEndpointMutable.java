package com.graphinout.base.cj.document;

import org.jspecify.annotations.Nullable;

/**
 * Mutable variant of {@link ICjEndpoint} used while constructing an edge.
 */
public interface ICjEndpointMutable extends ICjEndpoint, ICjHasDataMutable {

    /**
     * Sets the direction of this endpoint.
     *
     * @param direction the direction (e.g., IN, OUT).
     * @return this mutable endpoint for chaining.
     */
    ICjEndpointMutable direction(CjDirection direction);

    /**
     * Sets the ID of the node this endpoint connects to.
     *
     * @param node the target node ID.
     * @return this mutable endpoint for chaining.
     */
    ICjEndpointMutable node(String node);

    /**
     * Sets the port on the node this endpoint connects to.
     *
     * @param port the target port ID, or null to clear.
     * @return this mutable endpoint for chaining.
     */
    ICjEndpointMutable port(@Nullable String port);

    /**
     * Sets the type or role of this endpoint.
     *
     * @param type the endpoint type, or null to clear.
     * @return this mutable endpoint for chaining.
     */
    ICjEndpointMutable type(@Nullable String type);

}
