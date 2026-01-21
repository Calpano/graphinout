package com.graphinout.base.cj.document;

import java.util.stream.Stream;

public interface ICjHasPorts {

    default boolean hasPorts() {
        return ports().findAny().isPresent();
    }

    Stream<ICjPort> ports();

}
