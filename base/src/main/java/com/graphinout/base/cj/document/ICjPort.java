package com.graphinout.base.cj.document;

import javax.annotation.Nullable;
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


}
