package com.graphinout.base.cj.document;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

public interface ICjEndpoint extends ICjHasData {

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.of(data()).filter(Objects::nonNull).map(x -> x);
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

    @Nullable
    String port();

    @Nullable
    String type();

    @Nullable
    String typeNode();

    @Nullable
    String typeUri();

}
