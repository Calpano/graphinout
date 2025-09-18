package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ICjPort extends ICjWithId {

    @Nullable
    ICjData data();

    @Nullable
    ICjLabel label();

    Stream<ICjPort> ports();

}
