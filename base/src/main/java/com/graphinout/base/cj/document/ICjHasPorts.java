package com.graphinout.base.cj.document;

import java.util.stream.Stream;

/**
 * Mixin for CJ elements (nodes and ports) that can contain ports.
 */
public interface ICjHasPorts {

    default boolean hasPorts() {
        return ports().findAny().isPresent();
    }

    Stream<ICjPort> ports();

}
