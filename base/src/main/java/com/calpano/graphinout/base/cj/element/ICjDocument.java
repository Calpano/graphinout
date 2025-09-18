package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjDocument extends  ICjWithGraphs {

    @Nullable
    ICjDocumentMeta connectedJson();

    @Nullable
    ICjData data();

    @Nullable
    String baseUri();

}
