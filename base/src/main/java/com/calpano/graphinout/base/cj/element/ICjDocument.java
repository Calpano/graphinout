package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjDocument extends ICjHasGraphs {

    @Nullable
    ICjDocumentMeta connectedJson();

    @Nullable
    ICjData data();

    @Nullable
    String baseUri();

}
