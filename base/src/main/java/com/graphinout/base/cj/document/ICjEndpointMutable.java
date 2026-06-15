package com.graphinout.base.cj.document;

import org.jspecify.annotations.Nullable;

/**
 * Mutable variant of {@link ICjEndpoint} used while constructing an edge.
 */
public interface ICjEndpointMutable extends ICjEndpoint, ICjHasDataMutable {

    ICjEndpointMutable direction(CjDirection direction);

    ICjEndpointMutable node(String node);

    ICjEndpointMutable port(@Nullable String port);

    ICjEndpointMutable type(@Nullable String type);

}
