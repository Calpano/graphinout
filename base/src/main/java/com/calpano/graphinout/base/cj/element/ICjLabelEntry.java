package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface ICjLabelEntry extends ICjElement {

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
