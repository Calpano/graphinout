package com.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

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
