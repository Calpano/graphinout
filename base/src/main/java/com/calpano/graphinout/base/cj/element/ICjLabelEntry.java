package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjLabelEntry extends ICjElement {

    @Nullable
    ICjData data();

    @Nullable
    String language();

    String value();

}
