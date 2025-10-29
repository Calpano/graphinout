package com.graphinout.base.cj.document;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ICjLabelEntry extends ICjHasData {

    @Nullable
    ICjData data();

    @Override
    default Stream<ICjElement> directChildren() {
        return Stream.empty();
    }

    @Nullable
    String language();

    String value();

}
