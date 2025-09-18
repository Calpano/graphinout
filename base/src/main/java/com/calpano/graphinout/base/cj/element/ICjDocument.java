package com.calpano.graphinout.base.cj.element;

import javax.annotation.Nullable;

public interface ICjDocument extends ICjHasGraphs, ICjHasData {

    @Nullable
    ICjDocumentMeta connectedJson();

    @Nullable
    String baseUri();

}
